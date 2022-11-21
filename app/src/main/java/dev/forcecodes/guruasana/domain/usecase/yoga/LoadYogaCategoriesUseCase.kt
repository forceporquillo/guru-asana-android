package dev.forcecodes.guruasana.domain.usecase.yoga

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.forcecodes.guruasana.ui.LightningYogaPosesFactory
import dev.forcecodes.guruasana.di.IoDispatcher
import dev.forcecodes.guruasana.domain.BaseFlowUseCase
import dev.forcecodes.guruasana.domain.UseCaseParams
import dev.forcecodes.guruasana.utils.extensions.toFlow
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoadYogaCategoriesUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher ioDispatcher: CoroutineDispatcher
) : BaseFlowUseCase<LoadYogaCategoriesUseCase.Params, List<String>>(ioDispatcher) {

    private val regex = Regex("(Yoga|Poses)")

    object Params : UseCaseParams.Params()

    override fun execute(params: Params): Flow<List<String>> {
        return LightningYogaPosesFactory.singleton(context)
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