package dev.forcecodes.guruasana.model

import androidx.recyclerview.widget.DiffUtil
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class PostProcessMetrics(
    @PrimaryKey
    val timestamp: Long = System.currentTimeMillis(),
    val tag: String?,
    val poseName: String?,
    val category: String?,
    val confidence: String?
) {
    companion object {
        val COMPARATOR = object: DiffUtil.ItemCallback<PostProcessMetrics>() {
            override fun areItemsTheSame(
                oldItem: PostProcessMetrics,
                newItem: PostProcessMetrics
            ): Boolean {
                return oldItem.poseName == newItem.poseName
            }

            override fun areContentsTheSame(
                oldItem: PostProcessMetrics,
                newItem: PostProcessMetrics
            ): Boolean {
                return oldItem == newItem
            }
        }
    }
}