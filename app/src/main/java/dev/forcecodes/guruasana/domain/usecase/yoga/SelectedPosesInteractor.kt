package dev.forcecodes.guruasana.domain.usecase.yoga

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.forcecodes.guruasana.ui.LightningYogaPosesFactory
import dev.forcecodes.guruasana.domain.SubjectInteractor
import dev.forcecodes.guruasana.model.PoseUiModel
import dev.forcecodes.guruasana.utils.extensions.toFlow
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SelectedPosesInteractor @Inject constructor(
    @ApplicationContext private val context: Context
) : SubjectInteractor<Int, List<PoseUiModel>>() {

    override fun createObservable(params: Int): Flow<List<PoseUiModel>> {
        return LightningYogaPosesFactory.singleton(context)
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