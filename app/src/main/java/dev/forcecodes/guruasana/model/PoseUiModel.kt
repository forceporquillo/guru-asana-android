package dev.forcecodes.guruasana.model

import androidx.annotation.DrawableRes
import androidx.recyclerview.widget.DiffUtil
import androidx.room.Entity
import androidx.room.PrimaryKey

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
    val sinkritName: String,
    val drawableId: Int,
    val cardColor: Int,
    val level: Int
) {
    companion object {
        val COMPARATOR = object: DiffUtil.ItemCallback<Poses>() {
            override fun areItemsTheSame(oldItem: Poses, newItem: Poses): Boolean {
                return oldItem.title == newItem.title
            }

            override fun areContentsTheSame(oldItem: Poses, newItem: Poses): Boolean {
                return oldItem == newItem
            }
        }
    }
}

@Entity
data class CustomizePose(
    @PrimaryKey
    val poseName: String,
    val sanskritName: String?,
    val description: String?,
    val accuracyRate: String?,
    val drawableId: Int?,
    val level: Int?,
    val isAddedByUser: Boolean = false,
    var isChecked: Boolean? = false
) {
    companion object {
        val COMPARATOR = object: DiffUtil.ItemCallback<CustomizePose>() {
            override fun areItemsTheSame(
                oldItem: CustomizePose,
                newItem: CustomizePose
            ): Boolean {
                return oldItem.poseName == newItem.poseName
            }

            override fun areContentsTheSame(
                oldItem: CustomizePose,
                newItem: CustomizePose
            ): Boolean {
                return oldItem == newItem
            }

        }
    }
}
