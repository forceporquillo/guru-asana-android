package dev.forcecodes.guruasana.ui.poses

import android.annotation.SuppressLint
import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.forcecodes.guruasana.domain.usecase.gallery.MediaStoreImageUseCase
import dev.forcecodes.guruasana.model.MediaStoreImage
import dev.forcecodes.guruasana.poseprocessor.classification.PoseClassifierProcessor
import dev.forcecodes.guruasana.ui.ReactiveStateViewModel
import dev.forcecodes.guruasana.ui.UiEvent
import dev.forcecodes.guruasana.ui.UiState
import dev.forcecodes.guruasana.ui.poses.DetectPosesViewModel.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
@SuppressLint("StaticFieldLeak")
class DetectPosesViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    contentResolveInteractor: MediaStoreImageUseCase
) : ReactiveStateViewModel<CameraViewState, CameraUiEvent>
    (CameraViewState.Photo(CameraSelector.LENS_FACING_BACK)) {

    val imageGallery: StateFlow<List<MediaStoreImage>> =
        contentResolveInteractor.invoke(MediaStoreImageUseCase.Params)
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val cameraProvider: ProcessCameraProvider by lazy {
        ProcessCameraProvider.getInstance(context).get()
    }

    val currentViewState: CameraViewState
        get() = state.value

    var shutterTimer: Int = 0
    var currentLensFacing: Int = CameraSelector.LENS_FACING_BACK
    var imageCapture: ImageCapture? = null

    var poseClassificationLevel: PoseClassifierProcessor.PoseDifficultyLevel? = null

    var categoryLevel: Int = -1
        set(value) {
            val level = when (value) {
                0 -> PoseClassifierProcessor.PoseDifficultyLevel.BEGINNER
                1 -> PoseClassifierProcessor.PoseDifficultyLevel.INTERMEDIATE
                2 -> PoseClassifierProcessor.PoseDifficultyLevel.ADVANCED
                else -> PoseClassifierProcessor.PoseDifficultyLevel.ALL
            }
            poseClassificationLevel = level
            field = value
        }

    private var runClassification: Boolean = false

    sealed class CameraViewState : UiState {
        data class Live(
            val lensFacing: Int = CameraSelector.LENS_FACING_BACK,
            val runClassification: Boolean
        ) : CameraViewState()

        data class Photo(
            val lensFacing: Int = CameraSelector.LENS_FACING_BACK
        ) : CameraViewState()
    }

    sealed class CameraUiEvent : UiEvent {
        object OnTogglePhoto : CameraUiEvent()
        object OnToggleLive : CameraUiEvent()
        object OnClickLive : CameraUiEvent()
        data class OnToggleLens(
            val lensFacing: Int,
            val isLive: Boolean
        ) : CameraUiEvent()
    }

    override fun stateReducer(oldState: CameraViewState, event: CameraUiEvent) {
        val state = when (event) {
            is CameraUiEvent.OnTogglePhoto -> {
                CameraViewState.Photo(currentLensFacing)
            }
            is CameraUiEvent.OnToggleLive -> {
                CameraViewState.Live(currentLensFacing, false)
            }
            is CameraUiEvent.OnToggleLens -> {
                currentLensFacing = if (currentLensFacing == event.lensFacing) {
                    if (event.lensFacing == CameraSelector.LENS_FACING_FRONT) {
                        CameraSelector.LENS_FACING_BACK
                    } else {
                        CameraSelector.LENS_FACING_FRONT
                    }
                } else event.lensFacing
                // side effect
                if (event.isLive) {
                    val oldClassificationState = if (oldState is CameraViewState.Live) {
                        oldState.runClassification
                    } else false
                    CameraViewState.Live(currentLensFacing, oldClassificationState)
                } else {
                    CameraViewState.Photo(currentLensFacing)
                }
            }
            is CameraUiEvent.OnClickLive -> {
                runClassification = !runClassification
                CameraViewState.Live(currentLensFacing, runClassification)
            }
        }
        setState(state)
    }

    init {
        // initial load
        notifyGalleryInteractor()
    }

    fun notifyGalleryInteractor() {
//        viewModelScope {
//            contentResolveInteractor
//                .invoke(ContentResolveInteractor.Params)
//                .collect {
//                    _imagesGallery.value = it
//                }
//        }
    }

    fun resetClassificationState() {
        runClassification = false
    }

}