package dev.forcecodes.guruasana.model

import androidx.annotation.DrawableRes

data class PoseUiModel(
    val title: String,
    val description: String,
    @DrawableRes
    val thumbnail: Int?,
    val thumbnailUri: String? = null,
    val multiPoses: Boolean
)

data class Poses(
    val title: String,
    val synName: String,
    val drawableId: Int,
    val cardColor: Int
)
