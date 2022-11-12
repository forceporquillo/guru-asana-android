package dev.forcecodes.guruasana.ui.poses

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.forcecodes.guruasana.YogaPosesFactory
import dev.forcecodes.guruasana.di.IoDispatcher
import dev.forcecodes.guruasana.domain.BaseFlowUseCase
import dev.forcecodes.guruasana.domain.SubjectInteractor
import dev.forcecodes.guruasana.domain.UseCaseParams
import dev.forcecodes.guruasana.model.PoseUiModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

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

fun <T> List<T>.toFlow(): Flow<List<T>> = flowOf(this)

@Singleton
class LoadYogaCategoriesUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher ioDispatcher: CoroutineDispatcher
) : BaseFlowUseCase<LoadYogaCategoriesUseCase.Params, List<String>>(ioDispatcher) {

    private val regex = Regex("(Yoga|Poses)")

    object Params : UseCaseParams.Params()

    override fun execute(params: Params): Flow<List<String>> {
        return YogaPosesFactory.singleton(context)
            .flatMap { poses ->
                poses.yogaCategories?.map { it.name } ?: emptyList()
            }
            .distinct()
            .map { name ->
                name?.replace(regex, "") ?: "".trimStart()
            }.toFlow()
    }

    override fun mapExceptionToResult(throwable: Throwable) = emptyList<String>()
}

@Singleton
class SelectedPosesInteractor @Inject constructor(
    @ApplicationContext private val context: Context
) : SubjectInteractor<Int, List<PoseUiModel>>() {

    override fun createObservable(params: Int): Flow<List<PoseUiModel>> {
        return YogaPosesFactory.singleton(context)
            .filter { poses ->
                poses.yogaCategories?.map { category ->
                    category.id ?: -1
                }?.contains(params) ?: false
            }.map { poses ->
                PoseUiModel(
                    title = poses.englishName ?: "",
                    description = poses.sanskritName ?: "",
                    thumbnail = null,
                    thumbnailUri = poses.imgUrl,
                    multiPoses = true
                )
            }.toFlow()
    }
}