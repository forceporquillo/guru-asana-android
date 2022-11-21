package dev.forcecodes.guruasana.domain.usecase.gallery

import android.content.ContentUris
import android.content.Context
import android.database.ContentObserver
import android.os.Build
import android.provider.MediaStore
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.forcecodes.guruasana.di.IoDispatcher
import dev.forcecodes.guruasana.domain.BaseFlowUseCase
import dev.forcecodes.guruasana.domain.UseCaseParams
import dev.forcecodes.guruasana.model.MediaStoreImage
import dev.forcecodes.guruasana.utils.extensions.tryOffer
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.Date
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaStoreImageUseCase @Inject constructor(
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    @ApplicationContext private val context: Context
) : BaseFlowUseCase<MediaStoreImageUseCase.Params, List<MediaStoreImage>>(ioDispatcher) {

    object Params : UseCaseParams.Params()

    override fun execute(params: Params): Flow<List<MediaStoreImage>> {
        return context.register()
    }

    private fun Context.register(): Flow<List<MediaStoreImage>> {
        return callbackFlow {
            val observer = object : ContentObserver(null) {
                override fun onChange(selfChange: Boolean) {
                    super.onChange(selfChange)
                    tryOffer(loadImages(context))
                }
            }
            contentResolver.registerContentObserver(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                true, observer
            )
            awaitClose {
                contentResolver.unregisterContentObserver(observer)
            }
        }
    }

    override fun mapExceptionToResult(throwable: Throwable): List<MediaStoreImage> {
        return emptyList()
    }

}

fun loadImages(context: Context): MutableList<MediaStoreImage> {
    val images = mutableListOf<MediaStoreImage>()

    val projection = arrayOf(
        MediaStore.Images.Media._ID,
        MediaStore.Images.Media.DISPLAY_NAME,
        MediaStore.Images.Media.DATE_ADDED
    )

    val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

    val selection =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.ImageColumns.RELATIVE_PATH + " like ? "
        } else {
            null
        }

      val selectionArgs = arrayOf("%Guru Asana%")

    context.contentResolver.query(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        projection,
        selection,
        selectionArgs,
        sortOrder
    )?.use { cursor ->

        val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
        val dateModifiedColumn =
            cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
        val displayNameColumn =
            cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)

        while (cursor.moveToNext()) {

            // Here we'll use the column indexes that we found above.
            val id = cursor.getLong(idColumn)
            val dateModified =
                Date(TimeUnit.SECONDS.toMillis(cursor.getLong(dateModifiedColumn)))
            val displayName = cursor.getString(displayNameColumn)

            val contentUri = ContentUris.withAppendedId(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                id
            )

            val image = MediaStoreImage(id, displayName, dateModified, contentUri)
            images += image

            // For debugging, we'll output the image objects we create to logcat.
            //Logger.v("Added image: $image")
        }
        cursor.close()
    }

    return images
}
