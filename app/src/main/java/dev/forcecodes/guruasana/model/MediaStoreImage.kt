package dev.forcecodes.guruasana.model

import android.net.Uri
import androidx.recyclerview.widget.DiffUtil
import java.util.Date

/**
 * Simple data class to hold information about an image included in the device's MediaStore.
 */
data class MediaStoreImage(
    val id: Long,
    val displayName: String,
    val dateAdded: Date,
    val contentUri: Uri
)