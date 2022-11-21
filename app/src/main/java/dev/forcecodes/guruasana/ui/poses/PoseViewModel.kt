package dev.forcecodes.guruasana.ui.poses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.forcecodes.guruasana.domain.usecase.yoga.LoadYogaCategoriesUseCase
import dev.forcecodes.guruasana.domain.usecase.yoga.SelectedPosesInteractor
import dev.forcecodes.guruasana.model.PoseUiModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class PoseViewModel @Inject constructor(
    loadYogaCategoriesUseCase: LoadYogaCategoriesUseCase,
    private val selectedPosesInteractor: SelectedPosesInteractor
) : ViewModel() {

    val categories: StateFlow<List<String>> = loadYogaCategoriesUseCase
        .invoke(LoadYogaCategoriesUseCase.Params)
        .observedUiState()

    val poses: StateFlow<List<PoseUiModel>> = selectedPosesInteractor.flow
        .observedUiState()

    fun onSelectPoseCategory(category: Int) {
        selectedPosesInteractor.invoke(category)
    }

    private fun <T> Flow<List<T>>.observedUiState(): StateFlow<List<T>> {
        return stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    }

    init {
        // fetch
        onSelectPoseCategory(INITIAL_CATEGORY)
    }

    companion object {
        private const val INITIAL_CATEGORY = 1
    }
}