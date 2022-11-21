package dev.forcecodes.guruasana.ui.poses.camera

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver
import androidx.annotation.LayoutRes
import androidx.camera.core.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.viewbinding.ViewBinding
import dev.forcecodes.guruasana.logger.Logger
import dev.forcecodes.guruasana.poseprocessor.VisionImageProcessor
import dev.forcecodes.guruasana.ui.poses.DetectPosesViewModel
import dev.forcecodes.guruasana.utils.extensions.launchWithViewLifecycle

abstract class CameraPoseProviderFragment(@LayoutRes layoutId: Int) : Fragment(layoutId) {

    protected val viewModel by activityViewModels<DetectPosesViewModel>()
    private val handler = Handler(Looper.getMainLooper())
    private var camera: Camera? = null
    private var viewIsDestroy = false

    protected fun <T : ViewBinding> synchronizeWithUiThread(binding: T, block: T.() -> Unit) {
        handler.post {
            try {
                if (viewIsDestroy.not()) {
                    block.invoke(binding)
                }
            } catch (e: IllegalStateException) {
                Logger.e(e)
            }
        }
    }

    private inline fun View.afterMeasured(crossinline block: (View) -> Unit) {
        if (measuredWidth > 0 && measuredHeight > 0) {
            block(this)
        } else {
            viewTreeObserver.addOnGlobalLayoutListener(object :
                ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    if (measuredWidth > 0 && measuredHeight > 0) {
                        viewTreeObserver.removeOnGlobalLayoutListener(this)
                        block(this@afterMeasured)
                    }
                }
            })
        }
    }

    protected open fun onAttachPoseDetectorProcessor(): VisionImageProcessor? {
        return null
    }

    abstract fun onBindCameraUseCase(cameraViewState: DetectPosesViewModel.CameraViewState): Array<UseCase>

    private fun createCamerasSelector(
        @CameraSelector.LensFacing lensFacing: Int
    ): CameraSelector {
        return CameraSelector.Builder()
            .requireLensFacing(lensFacing)
            .build()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.post {
            launchWithViewLifecycle {
                viewModel.state.collect { cameraViewState ->
                    initializeCameraUse(cameraViewState)
                    attachLensFocus(view)
                }
            }
        }
    }

    protected fun initializeCameraUse(cameraViewState: DetectPosesViewModel.CameraViewState) {
        viewModel.cameraProvider.unbindAll()

        val lensFacingState = when (cameraViewState) {
            is DetectPosesViewModel.CameraViewState.Live -> cameraViewState.lensFacing
            is DetectPosesViewModel.CameraViewState.Photo -> cameraViewState.lensFacing
        }
        Logger.d("LensFacingState: $lensFacingState")

        val cameraSelector = createCamerasSelector(lensFacingState)
        val useCases = onBindCameraUseCase(cameraViewState)

        camera = viewModel.cameraProvider.bindToLifecycle(viewLifecycleOwner, cameraSelector, *useCases)

        // invert lens state
        viewModel.currentLensFacing = lensFacingState
    }

    @SuppressLint("ClickableViewAccessibility")
    protected fun attachLensFocus(view: View) {
        val onTouchListener = View.OnTouchListener { _, event ->
            return@OnTouchListener when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    true
                }
                MotionEvent.ACTION_UP -> {
                    val factory: MeteringPointFactory = SurfaceOrientedMeteringPointFactory(
                        view.width.toFloat(), view.height.toFloat()
                    )
                    val autoFocusPoint = factory.createPoint(event.x, event.y)
                    try {
                        camera?.cameraControl?.startFocusAndMetering(
                            FocusMeteringAction.Builder(
                                autoFocusPoint, FocusMeteringAction.FLAG_AF
                            ).apply {
                                // focus only when the user tap the preview
                                disableAutoCancel()
                            }.build()
                        )
                    } catch (e: CameraInfoUnavailableException) {
                        Logger.d("cannot access camera", e)
                    }
                    true
                }
                else -> false // Unhandled event.
            }
        }
        view.afterMeasured {
            it.setOnTouchListener(onTouchListener)
        }
    }

    override fun onStop() {
        super.onStop()
        viewIsDestroy = true
    }
}