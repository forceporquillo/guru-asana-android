package dev.forcecodes.guruasana.ui.poses.camera

import android.os.Bundle
import android.view.View
import androidx.camera.core.*
import androidx.window.WindowManager
import dagger.hilt.android.AndroidEntryPoint
import dev.forcecodes.guruasana.R
import dev.forcecodes.guruasana.databinding.FragmentPoseTrackingBinding
import dev.forcecodes.guruasana.logger.Logger
import dev.forcecodes.guruasana.ui.poses.DetectPosesViewModel.CameraViewState
import dev.forcecodes.guruasana.ui.poses.LumaListener
import dev.forcecodes.guruasana.utils.binding.viewBinding
import java.nio.ByteBuffer
import java.util.ArrayDeque
import java.util.concurrent.Executors
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

@AndroidEntryPoint
class CapturePoseFragment : CameraPoseProviderFragment(R.layout.fragment_pose_tracking) {

    private val binding by viewBinding(FragmentPoseTrackingBinding::bind)

    private lateinit var windowManager: WindowManager

    companion object {
        private const val RATIO_4_3_VALUE = 4.0 / 3.0
        private const val RATIO_16_9_VALUE = 16.0 / 9.0
    }

    private fun aspectRatio(width: Int, height: Int): Int {
        val previewRatio = max(width, height).toDouble() / min(width, height)
        if (abs(previewRatio - RATIO_4_3_VALUE) <= abs(previewRatio - RATIO_16_9_VALUE)) {
            return AspectRatio.RATIO_4_3
        }
        return AspectRatio.RATIO_16_9
    }

    override fun onBindCameraUseCase(cameraViewState: CameraViewState): Array<UseCase> {
        if (cameraViewState !is CameraViewState.Photo) return emptyArray()

        val metrics = windowManager.getCurrentWindowMetrics().bounds
        Logger.d("Screen metrics: ${metrics.width()} x ${metrics.height()}")
        val screenAspectRatio = aspectRatio(metrics.width(), metrics.height())

        val rotation = binding.previewView.display.rotation

        val analysis = ImageAnalysis.Builder()
            .setTargetAspectRatio(screenAspectRatio)
            .setTargetRotation(rotation)
            .build()

        analysis.setAnalyzer(
            Executors.newSingleThreadExecutor(),
            LuminosityAnalyzer { luma, fps ->
                Logger.d("Luma $luma FPS $fps")
            }
        )

        val imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .setTargetAspectRatio(screenAspectRatio)
            .setTargetRotation(rotation)
            .build()

        // set also to viewModel
        viewModel.imageCapture = imageCapture

        val preview = Preview.Builder()
            .setTargetAspectRatio(screenAspectRatio)
            .setTargetRotation(rotation)
            .build()

        preview.setSurfaceProvider(
            Executors.newSingleThreadExecutor(),
            binding.previewView.surfaceProvider
        )

        return arrayOf(analysis, preview, imageCapture)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        windowManager = WindowManager(requireContext())
    }

    private inner class LuminosityAnalyzer(listener: LumaListener? = null) :
        ImageAnalysis.Analyzer {
        private val frameRateWindow = 8
        private val frameTimestamps = ArrayDeque<Long>(5)
        private val listeners = ArrayList<LumaListener>().apply { listener?.let { add(it) } }
        private var lastAnalyzedTimestamp = 0L
        var framesPerSecond: Double = -1.0
            private set

        /**
         * Helper extension function used to extract a byte array from an image plane buffer
         */
        private fun ByteBuffer.toByteArray(): ByteArray {
            rewind()    // Rewind the buffer to zero
            val data = ByteArray(remaining())
            get(data)   // Copy the buffer into a byte array
            return data // Return the byte array
        }

        /**
         * Analyzes an image to produce a result.
         *
         * <p>The caller is responsible for ensuring this analysis method can be executed quickly
         * enough to prevent stalls in the image acquisition pipeline. Otherwise, newly available
         * images will not be acquired and analyzed.
         *
         * <p>The image passed to this method becomes invalid after this method returns. The caller
         * should not store external references to this image, as these references will become
         * invalid.
         *
         * @param image image being analyzed VERY IMPORTANT: Analyzer method implementation must
         * call image.close() on received images when finished using them. Otherwise, new images
         * may not be received or the camera may stall, depending on back pressure setting.
         *
         */
        override fun analyze(image: ImageProxy) {
            // If there are no listeners attached, we don't need to perform analysis
            if (listeners.isEmpty()) {
                image.close()
                return
            }

            // Keep track of frames analyzed
            val currentTime = System.currentTimeMillis()
            frameTimestamps.push(currentTime)

            // Compute the FPS using a moving average
            while (frameTimestamps.size >= frameRateWindow) frameTimestamps.removeLast()
            val timestampFirst = frameTimestamps.peekFirst() ?: currentTime
            val timestampLast = frameTimestamps.peekLast() ?: currentTime
            framesPerSecond =
                1.0 / ((timestampFirst - timestampLast) / frameTimestamps.size.coerceAtLeast(1)
                    .toDouble()) * 1000.0

            // Analysis could take an arbitrarily long amount of time
            // Since we are running in a different thread, it won't stall other use cases

            lastAnalyzedTimestamp = frameTimestamps.first

            // Since format in ImageAnalysis is YUV, image.planes[0] contains the luminance plane
            val buffer = image.planes[0].buffer

            // Extract image data from callback object
            val data = buffer.toByteArray()

            // Convert the data into an array of pixel values ranging 0-255
            val pixels = data.map { it.toInt() and 0xFF }

            // Compute average luminance for the image
            val luma = pixels.average()

            // Call all listeners with new value
            listeners.forEach { it(luma, framesPerSecond.toInt()) }

            image.close()
        }
    }

}