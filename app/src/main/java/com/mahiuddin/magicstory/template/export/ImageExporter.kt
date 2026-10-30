package com.mahiuddin.magicstory.template.export

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import com.mahiuddin.magicstory.domain.model.MagicStoryEditorState
import com.mahiuddin.magicstory.domain.model.TemplateManifest
import com.mahiuddin.magicstory.template.renderer.TemplateRenderer
import com.mahiuddin.magicstory.util.MediaStoreUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ImageExporter {

    suspend fun exportImage(
        context: Context,
        manifest: TemplateManifest,
        editorState: MagicStoryEditorState
    ): Result<Uri> = withContext(Dispatchers.IO) {
        runCatching {
            val resolution = editorState.exportResolution
            val scaleMultiplier = editorState.exportScale.multiplier
            val exportFormat = editorState.exportFormat

            val targetWidth = (resolution.width * scaleMultiplier).toInt()
            val targetHeight = (resolution.height * scaleMultiplier).toInt()

            val bitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            TemplateRenderer.renderToCanvas(
                canvas = canvas,
                width = targetWidth.toFloat(),
                height = targetHeight.toFloat(),
                manifest = manifest,
                editorState = editorState,
                context = context
            )

            val saveResult = MediaStoreUtils.saveExportToGallery(
                context = context,
                bitmap = bitmap,
                filenamePrefix = "MagicStory_${manifest.id}",
                format = exportFormat,
                quality = 95
            )

            bitmap.recycle()

            saveResult.getOrThrow()
        }
    }
}
