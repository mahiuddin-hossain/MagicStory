package com.mahiuddin.magicstory.util

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

object MediaStoreUtils {

    fun saveBitmapToGallery(
        context: Context,
        bitmap: Bitmap,
        filenamePrefix: String = "MagicStory",
        quality: Int = 95
    ): Result<Uri> {
        return runCatching {
            val filename = "${filenamePrefix}_${System.currentTimeMillis()}.jpg"
            val resolver = context.contentResolver

            val imageUri: Uri?
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/MagicStory")
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }

                val collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                imageUri = resolver.insert(collection, contentValues)
                    ?: throw IllegalStateException("Failed to create MediaStore entry.")

                resolver.openOutputStream(imageUri)?.use { out: OutputStream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
                }

                contentValues.clear()
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(imageUri, contentValues, null, null)
            } else {
                val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val magicStoryDir = File(picturesDir, "MagicStory").apply { if (!exists()) mkdirs() }
                val imageFile = File(magicStoryDir, filename)

                FileOutputStream(imageFile).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
                }

                val contentValues = ContentValues().apply {
                    put(MediaStore.Images.Media.DATA, imageFile.absolutePath)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                }
                imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                    ?: Uri.fromFile(imageFile)
            }

            imageUri
        }
    }
}
