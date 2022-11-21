package dev.forcecodes.guruasana.ui.poses.camera

import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.UseCase
import androidx.core.view.isVisible
import dagger.hilt.android.AndroidEntryPoint
import dev.forcecodes.guruasana.R
import dev.forcecodes.guruasana.databinding.FragmentLivePoseFeedBinding
import dev.forcecodes.guruasana.logger.Logger
import dev.forcecodes.guruasana.poseprocessor.ConfidencePoseGraphicCallback
import dev.forcecodes.guruasana.poseprocessor.InferenceInfoGraphicCallback
import dev.forcecodes.guruasana.poseprocessor.PoseDetectorProcessor
import dev.forcecodes.guruasana.poseprocessor.VisionImageProcessor
import dev.forcecodes.guruasana.poseprocessor.camera.GraphicOverlay
import dev.forcecodes.guruasana.poseprocessor.classification.PoseClassifierProcessor
import dev.forcecodes.guruasana.poseprocessor.utils.PreferenceUtils
import dev.forcecodes.guruasana.ui.poses.DetectPosesViewModel.CameraViewState
import dev.forcecodes.guruasana.utils.binding.viewBinding
import java.util.Locale
import java.util.concurrent.Executors

internal val String.capitalizeWords
    get() = this.lowercase(Locale.getDefault()).split(" ").joinToString(" ")
    { it.capitalize(Locale.getDefault()) }

@AndroidEntryPoint
class LivePoseFeedFragment : CameraPoseProviderFragment(R.layout.fragment_live_pose_feed),
    InferenceInfoGraphicCallback, ConfidencePoseGraphicCallback {

    private val binding by viewBinding(FragmentLivePoseFeedBinding::bind)

    private var visionImageProcessor: VisionImageProcessor? = null
    private var needUpdateGraphicOverlayImageSourceInfo = false

    override fun onInferenceInfoChangedListener(
        frameLatency: Long,
        detectorLatency: Long,
        framesPerSecond: Int
    ) {
        synchronizeWithUiThread(binding) {
            frameLatencyTv.text = getString(R.string.frame_latency_ms, frameLatency.toString())
            detectorLatencyTv.text =
                getString(R.string.detector_latency_ms, detectorLatency.toString())
            framesTv.text = getString(R.string.fps_ms, framesPerSecond.toString())
        }
    }

    override fun onGetPoseClassificationChangeListener(result: List<PoseClassifierProcessor.PoseResult>) {
        synchronizeWithUiThread(binding) {
            for (poseResult in result) {
                val pose = if (poseResult.poseName == null) {
                    getString(R.string.recognizing_default_pose)
                } else {
                    val confidence = poseResult.confidence?.times(100)?.toInt()
                    "${poseResult.poseName} - $confidence% Confidence"
                }
                val formattedPose = pose.replace(REGEX, "")
                    .replace("_", " ")
                binding.poseClassName.text = formattedPose.capitalizeWords.trimStart()
            }
        }
    }

    private fun ensureImageProcessorIsInitialized(classify: Boolean) {
        // stop existing process
        visionImageProcessor?.stop()
        visionImageProcessor = null

        val detectorOptions = PreferenceUtils.getPoseDetectorOptionsForLivePreview(requireContext())
        visionImageProcessor = PoseDetectorProcessor(
            requireContext(),
            detectorOptions,
            viewModel.poseClassificationLevel,
            true,
            classify,
            this,
            this
        )
    }

    private fun hideViews(isRunning: Boolean) {
        binding.apply {
            poseClassName.isVisible = isRunning
            frameLatencyTv.isVisible = isRunning
            detectorLatencyTv.isVisible = isRunning
            framesTv.isVisible = isRunning
        }
    }

    override fun onBindCameraUseCase(cameraViewState: CameraViewState): Array<UseCase> {
        if (cameraViewState !is CameraViewState.Live) return emptyArray()

        needUpdateGraphicOverlayImageSourceInfo = true

        ensureImageProcessorIsInitialized(cameraViewState.runClassification)
        hideViews(cameraViewState.runClassification)

        val analysisUseCase = ImageAnalysis.Builder()
            .build()

        val liveDetectionAnalyzer =
            PoseLiveDetectionAnalyzer(cameraViewState.lensFacing, binding.graphicOverlay)

        analysisUseCase.setAnalyzer(Executors.newSingleThreadExecutor(), liveDetectionAnalyzer)

        return arrayOf(analysisUseCase)
    }

    inner class PoseLiveDetectionAnalyzer(
        private val lensFacing: Int,
        private val graphicOverlay: GraphicOverlay
    ) : ImageAnalysis.Analyzer {

        override fun analyze(imageProxy: ImageProxy) {
            if (needUpdateGraphicOverlayImageSourceInfo) {
                val isImageFlipped = lensFacing == CameraSelector.LENS_FACING_FRONT
                val rotationDegrees = imageProxy.imageInfo.rotationDegrees
                if (rotationDegrees == 0 || rotationDegrees == 180) {
                    graphicOverlay.setImageSourceInfo(
                        imageProxy.width, imageProxy.height, isImageFlipped
                    )
                } else {
                    graphicOverlay.setImageSourceInfo(
                        imageProxy.height, imageProxy.width, isImageFlipped
                    )
                }
                Logger.d("Image Flipped $isImageFlipped")
                needUpdateGraphicOverlayImageSourceInfo = false
            }
            visionImageProcessor?.processImageProxy(
                imageProxy, graphicOverlay
            )
        }
    }

    override fun onStop() {
        super.onStop()
        visionImageProcessor?.stop()
    }

    companion object {
        val REGEX = Regex("(beginner|intermediate|advanced)")
    }
}