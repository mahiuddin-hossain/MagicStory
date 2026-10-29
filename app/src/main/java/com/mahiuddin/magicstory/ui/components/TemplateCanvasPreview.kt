package com.mahiuddin.magicstory.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.mahiuddin.magicstory.domain.model.MagicStoryEditorState
import com.mahiuddin.magicstory.domain.model.TemplateElement
import com.mahiuddin.magicstory.domain.model.TemplateManifest
import com.mahiuddin.magicstory.template.renderer.TemplateRenderer
import com.mahiuddin.magicstory.ui.theme.SoftRedShadow

@Composable
fun TemplateCanvasPreview(
    manifest: TemplateManifest,
    editorState: MagicStoryEditorState,
    modifier: Modifier = Modifier,
    isInteractive: Boolean = false,
    onElementSelect: (String?) -> Unit = {},
    onElementDrag: (String, Float, Float) -> Unit = { _, _, _ -> },
    onDragEnd: () -> Unit = {}
) {
    val context = LocalContext.current
    val bitmapCache = remember { mutableMapOf<String, Bitmap>() }
    val aspectRatio = manifest.numericAspectRatio

    var draggedElementId by remember { mutableStateOf<String?>(null) }

    val interactiveModifier = if (isInteractive) {
        Modifier
            .pointerInput(manifest, editorState) {
                detectTapGestures { offset ->
                    val canvasWidth = size.width.toFloat()
                    val canvasHeight = size.height.toFloat()
                    if (canvasWidth > 0 && canvasHeight > 0) {
                        val normX = offset.x / canvasWidth
                        val normY = offset.y / canvasHeight
                        val touchedId = findTouchedElement(manifest, editorState, normX, normY)
                        onElementSelect(touchedId)
                    }
                }
            }
            .pointerInput(manifest, editorState) {
                detectDragGestures(
                    onDragStart = { startOffset ->
                        val canvasWidth = size.width.toFloat()
                        val canvasHeight = size.height.toFloat()
                        if (canvasWidth > 0 && canvasHeight > 0) {
                            val normX = startOffset.x / canvasWidth
                            val normY = startOffset.y / canvasHeight
                            draggedElementId = findTouchedElement(manifest, editorState, normX, normY)
                            onElementSelect(draggedElementId)
                        }
                    },
                    onDragEnd = {
                        draggedElementId = null
                        onDragEnd()
                    },
                    onDragCancel = {
                        draggedElementId = null
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val targetId = draggedElementId ?: return@detectDragGestures
                        val canvasWidth = size.width.toFloat()
                        val canvasHeight = size.height.toFloat()
                        if (canvasWidth > 0 && canvasHeight > 0) {
                            val currentX = getElementCurrentX(manifest, editorState, targetId)
                            val currentY = getElementCurrentY(manifest, editorState, targetId)

                            val deltaX = dragAmount.x / canvasWidth
                            val deltaY = dragAmount.y / canvasHeight

                            val newX = currentX + deltaX
                            val newY = currentY + deltaY

                            onElementDrag(targetId, newX, newY)
                        }
                    }
                )
            }
    } else Modifier

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .aspectRatio(aspectRatio)
                .shadow(elevation = 12.dp, shape = RoundedCornerShape(16.dp), ambientColor = SoftRedShadow, spotColor = SoftRedShadow)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .then(interactiveModifier)
            ) {
                val canvasWidth = size.width
                val canvasHeight = size.height

                drawContext.canvas.nativeCanvas.let { nativeCanvas ->
                    TemplateRenderer.renderToCanvas(
                        canvas = nativeCanvas,
                        width = canvasWidth,
                        height = canvasHeight,
                        manifest = manifest,
                        editorState = editorState,
                        context = context,
                        bitmapCache = bitmapCache
                    )
                }
            }
        }
    }
}

private fun findTouchedElement(
    manifest: TemplateManifest,
    editorState: MagicStoryEditorState,
    normX: Float,
    normY: Float
): String? {
    val sortedElements = manifest.elements.filter { it.visible }.sortedByDescending { it.zIndex }
    for (element in sortedElements) {
        val (x, y, w, h) = when (element) {
            is TemplateElement.Text -> {
                val ex = editorState.getElementX(element.id, element.x)
                val ey = editorState.getElementY(element.id, element.y)
                listOf(ex, ey, element.width, element.height)
            }
            is TemplateElement.Shape -> {
                val ex = editorState.getElementX(element.id, element.x)
                val ey = editorState.getElementY(element.id, element.y)
                listOf(ex, ey, element.width, element.height)
            }
            is TemplateElement.PhotoSlot -> {
                val ex = editorState.getElementX(element.id, element.x)
                val ey = editorState.getElementY(element.id, element.y)
                listOf(ex, ey, element.width, element.height)
            }
            is TemplateElement.PhotoCard -> {
                val ex = editorState.getElementX(element.id, element.x)
                val ey = editorState.getElementY(element.id, element.y)
                listOf(ex, ey, element.width, element.height)
            }
            is TemplateElement.PillCta -> {
                val ex = editorState.getElementX(element.id, element.x)
                val ey = editorState.getElementY(element.id, element.y)
                listOf(ex, ey, element.width, element.height)
            }
            else -> continue
        }

        if (normX in x..(x + w) && normY in y..(y + h)) {
            return element.id
        }
    }
    return null
}

private fun getElementCurrentX(
    manifest: TemplateManifest,
    editorState: MagicStoryEditorState,
    elementId: String
): Float {
    val elem = manifest.elements.find { it.id == elementId } ?: return 0f
    val defaultX = when (elem) {
        is TemplateElement.Text -> elem.x
        is TemplateElement.Shape -> elem.x
        is TemplateElement.PhotoSlot -> elem.x
        is TemplateElement.PhotoCard -> elem.x
        is TemplateElement.PillCta -> elem.x
        else -> 0f
    }
    return editorState.getElementX(elementId, defaultX)
}

private fun getElementCurrentY(
    manifest: TemplateManifest,
    editorState: MagicStoryEditorState,
    elementId: String
): Float {
    val elem = manifest.elements.find { it.id == elementId } ?: return 0f
    val defaultY = when (elem) {
        is TemplateElement.Text -> elem.y
        is TemplateElement.Shape -> elem.y
        is TemplateElement.PhotoSlot -> elem.y
        is TemplateElement.PhotoCard -> elem.y
        is TemplateElement.PillCta -> elem.y
        else -> 0f
    }
    return editorState.getElementY(elementId, defaultY)
}
