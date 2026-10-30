package com.mahiuddin.magicstory.util

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.mahiuddin.magicstory.domain.model.ExportFormat
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

object MediaStoreUtils {

    fun saveExportToGallery(
        context: Context,
        bitmap: Bitmap,
        filenamePrefix: String = "MagicStory",
        format: ExportFormat = ExportFormat.JPEG,
        quality: Int = 95
    ): Result<Uri> {
        return runCatching {
            val extension = format.extension
            val mimeType = format.mimeType
            val filename = "${filenamePrefix}_${System.currentTimeMillis()}.$extension"
            val resolver = context.contentResolver

            val targetUri: Uri?

            if (format == ExportFormat.PDF) {
                // PDF Export
                val pdfDocument = PdfDocument()
                val pageInfo = PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, 1).create()
                val page = pdfDocument.startPage(pageInfo)
                val pdfCanvas = page.canvas
                pdfCanvas.drawBitmap(bitmap, 0f, 0f, null)
                pdfDocument.finishPage(page)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                        put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                        put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_DOCUMENTS}/MagicStory")
                        put(MediaStore.MediaColumns.IS_PENDING, 1)
                    }
                    val collection = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                    targetUri = resolver.insert(collection, contentValues)
                        ?: throw IllegalStateException("Failed to create PDF entry.")

                    resolver.openOutputStream(targetUri)?.use { out ->
                        pdfDocument.writeTo(out)
                    }

                    contentValues.clear()
                    contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    resolver.update(targetUri, contentValues, null, null)
                } else {
                    val docsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                    val magicStoryDir = File(docsDir, "MagicStory").apply { if (!exists()) mkdirs() }
                    val pdfFile = File(magicStoryDir, filename)

                    FileOutputStream(pdfFile).use { out ->
                        pdfDocument.writeTo(out)
                    }
                    targetUri = Uri.fromFile(pdfFile)
                }
                pdfDocument.close()
            } else {
                // JPEG or PNG Export
                val compressFormat = if (format == ExportFormat.PNG) Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                        put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                        put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/MagicStory")
                        put(MediaStore.MediaColumns.IS_PENDING, 1)
                    }

                    val collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                    targetUri = resolver.insert(collection, contentValues)
                        ?: throw IllegalStateException("Failed to create MediaStore entry.")

                    resolver.openOutputStream(targetUri)?.use { out: OutputStream ->
                        bitmap.compress(compressFormat, quality, out)
                    }

                    contentValues.clear()
                    contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    resolver.update(targetUri, contentValues, null, null)
                } else {
                    val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    val magicStoryDir = File(picturesDir, "MagicStory").apply { if (!exists()) mkdirs() }
                    val imageFile = File(magicStoryDir, filename)

                    FileOutputStream(imageFile).use { out ->
                        bitmap.compress(compressFormat, quality, out)
                    }

                    val contentValues = ContentValues().apply {
                        put(MediaStore.Images.Media.DATA, imageFile.absolutePath)
                        put(MediaStore.Images.Media.MIME_TYPE, mimeType)
                    }
                    targetUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                        ?: Uri.fromFile(imageFile)
                }
            }

            targetUri
        }
    }
}
