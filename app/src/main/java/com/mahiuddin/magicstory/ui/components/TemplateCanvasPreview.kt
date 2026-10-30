package com.mahiuddin.magicstory.ui.components

import android.graphics.Bitmap
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
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
import com.mahiuddin.magicstory.domain.model.GuideLine
import com.mahiuddin.magicstory.domain.model.MagicStoryEditorState
import com.mahiuddin.magicstory.domain.model.TemplateElement
import com.mahiuddin.magicstory.domain.model.TemplateManifest
import com.mahiuddin.magicstory.template.renderer.TemplateRenderer
import com.mahiuddin.magicstory.ui.theme.SoftRedShadow
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

enum class TouchHandle {
    NONE,
    BODY,
    TOP_LEFT,
    TOP_RIGHT,
    BOTTOM_LEFT,
    BOTTOM_RIGHT,
    LEFT,
    RIGHT,
    TOP,
    BOTTOM,
    ROTATION
}

enum class GestureMode {
    IDLE,
    MOVING,
    RESIZING,
    ROTATING
}

@Composable
fun TemplateCanvasPreview(
    manifest: TemplateManifest,
    editorState: MagicStoryEditorState,
    modifier: Modifier = Modifier,
    isInteractive: Boolean = false,
    onElementSelect: (String?) -> Unit = {},
    onElementDrag: (String, Float, Float) -> Unit = { _, _, _ -> },
    onElementResize: (String, Float, Float) -> Unit = { _, _, _ -> },
    onElementRotate: (String, Float) -> Unit = { _, _ -> },
    onSnappingGuides: (List<GuideLine>) -> Unit = {},
    onDragEnd: () -> Unit = {}
) {
    val context = LocalContext.current
    val bitmapCache = remember { mutableMapOf<String, Bitmap>() }
    val activeAspectRatio = editorState.exportResolution.width.toFloat() / editorState.exportResolution.height.toFloat()
    val animatedAspectRatio by animateFloatAsState(
        targetValue = activeAspectRatio,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "canvasAspectRatio"
    )

    val currentManifest by rememberUpdatedState(manifest)
    val currentState by rememberUpdatedState(editorState)
    val currentOnSelect by rememberUpdatedState(onElementSelect)
    val currentOnDrag by rememberUpdatedState(onElementDrag)
    val currentOnResize by rememberUpdatedState(onElementResize)
    val currentOnRotate by rememberUpdatedState(onElementRotate)
    val currentOnSnappingGuides by rememberUpdatedState(onSnappingGuides)
    val currentOnDragEnd by rememberUpdatedState(onDragEnd)

    val interactiveModifier = if (isInteractive) {
        Modifier
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val canvasW = size.width.toFloat()
                    val canvasH = size.height.toFloat()
                    if (canvasW > 0 && canvasH > 0) {
                        val normX = offset.x / canvasW
                        val normY = offset.y / canvasH
                        val (hitId, _) = findHitElementAndHandle(currentManifest, currentState, normX, normY, canvasW, canvasH)
                        currentOnSelect(hitId)
                    }
                }
            }
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        val canvasW = size.width.toFloat()
                        val canvasH = size.height.toFloat()

                        if (canvasW <= 0 || canvasH <= 0) continue

                        val startNormX = down.position.x / canvasW
                        val startNormY = down.position.y / canvasH

                        val (targetId, handle) = findHitElementAndHandle(currentManifest, currentState, startNormX, startNormY, canvasW, canvasH)

                        if (targetId == null) {
                            currentOnSelect(null)
                            continue
                        }

                        currentOnSelect(targetId)

                        val mode = when (handle) {
                            TouchHandle.ROTATION -> GestureMode.ROTATING
                            TouchHandle.BODY -> GestureMode.MOVING
                            TouchHandle.NONE -> GestureMode.IDLE
                            else -> GestureMode.RESIZING
                        }

                        if (mode == GestureMode.IDLE) continue

                        val initialElemX = getElementX(currentManifest, currentState, targetId)
                        val initialElemY = getElementY(currentManifest, currentState, targetId)
                        val initialElemW = getElementW(currentManifest, currentState, targetId)
                        val initialElemH = getElementH(currentManifest, currentState, targetId)

                        var lastTouchPx = down.position.x
                        var lastTouchPy = down.position.y

                        var currentX = initialElemX
                        var currentY = initialElemY
                        var currentW = initialElemW
                        var currentH = initialElemH

                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull() ?: break

                            if (!change.pressed) {
                                currentOnSnappingGuides(emptyList())
                                currentOnDragEnd()
                                break
                            }

                            val touchPx = change.position.x
                            val touchPy = change.position.y

                            val dxPx = touchPx - lastTouchPx
                            val dyPx = touchPy - lastTouchPy

                            lastTouchPx = touchPx
                            lastTouchPy = touchPy

                            when (mode) {
                                GestureMode.MOVING -> {
                                    val dxNorm = dxPx / canvasW
                                    val dyNorm = dyPx / canvasH
                                    var newX = currentX + dxNorm
                                    var newY = currentY + dyNorm

                                    val centerX = newX + currentW / 2f
                                    val centerY = newY + currentH / 2f

                                    val guides = mutableListOf<GuideLine>()

                                    // Canvas Center Snapping (X Center)
                                    if (abs(centerX - 0.5f) < 0.018f) {
                                        newX = 0.5f - currentW / 2f
                                        guides.add(GuideLine(isVertical = true, positionNorm = 0.5f, colorHex = "#C91F2C"))
                                    }

                                    // Canvas Center Snapping (Y Center)
                                    if (abs(centerY - 0.5f) < 0.018f) {
                                        newY = 0.5f - currentH / 2f
                                        guides.add(GuideLine(isVertical = false, positionNorm = 0.5f, colorHex = "#C91F2C"))
                                    }

                                    currentX = newX
                                    currentY = newY
                                    currentOnSnappingGuides(guides)
                                    currentOnDrag(targetId, currentX, currentY)
                                    change.consume()
                                }
                                GestureMode.ROTATING -> {
                                    val elemCenterX = (currentX + currentW / 2f) * canvasW
                                    val elemCenterY = (currentY + currentH / 2f) * canvasH
                                    val angleRad = atan2((touchPy - elemCenterY).toDouble(), (touchPx - elemCenterX).toDouble())
                                    var angleDeg = Math.toDegrees(angleRad).toFloat() + 90f
                                    if (angleDeg < 0) angleDeg += 360f

                                    // Angle Snapping for 0, 45, 90, 135, 180, 225, 270, 315, 360
                                    val snapAngles = floatArrayOf(0f, 45f, 90f, 135f, 180f, 225f, 270f, 315f, 360f)
                                    for (snap in snapAngles) {
                                        if (abs(angleDeg - snap) <= 3.5f) {
                                            angleDeg = if (snap == 360f) 0f else snap
                                            break
                                        }
                                    }

                                    currentOnRotate(targetId, angleDeg)
                                    change.consume()
                                }
                                GestureMode.RESIZING -> {
                                    val dwNorm = dxPx / canvasW
                                    val dhNorm = dyPx / canvasH

                                    when (handle) {
                                        TouchHandle.BOTTOM_RIGHT -> {
                                            val scale = ((currentW + dwNorm) / initialElemW).coerceAtLeast(0.05f / initialElemW)
                                            currentW = initialElemW * scale
                                            currentH = initialElemH * scale
                                        }
                                        TouchHandle.TOP_LEFT -> {
                                            val fixedRight = currentX + currentW
                                            val fixedBottom = currentY + currentH
                                            val scale = ((currentW - dwNorm) / initialElemW).coerceAtLeast(0.05f / initialElemW)
                                            val newW = initialElemW * scale
                                            val newH = initialElemH * scale
                                            currentX = fixedRight - newW
                                            currentY = fixedBottom - newH
                                            currentW = newW
                                            currentH = newH
                                            currentOnDrag(targetId, currentX, currentY)
                                        }
                                        TouchHandle.TOP_RIGHT -> {
                                            val fixedBottom = currentY + currentH
                                            val scale = ((currentW + dwNorm) / initialElemW).coerceAtLeast(0.05f / initialElemW)
                                            val newW = initialElemW * scale
                                            val newH = initialElemH * scale
                                            currentY = fixedBottom - newH
                                            currentW = newW
                                            currentH = newH
                                            currentOnDrag(targetId, currentX, currentY)
                                        }
                                        TouchHandle.BOTTOM_LEFT -> {
                                            val fixedRight = currentX + currentW
                                            val scale = ((currentW - dwNorm) / initialElemW).coerceAtLeast(0.05f / initialElemW)
                                            val newW = initialElemW * scale
                                            val newH = initialElemH * scale
                                            currentX = fixedRight - newW
                                            currentW = newW
                                            currentH = newH
                                            currentOnDrag(targetId, currentX, currentY)
                                        }
                                        TouchHandle.RIGHT -> {
                                            currentW = (currentW + dwNorm).coerceAtLeast(0.05f)
                                        }
                                        TouchHandle.LEFT -> {
                                            val newW = (currentW - dwNorm).coerceAtLeast(0.05f)
                                            currentX += (currentW - newW)
                                            currentW = newW
                                            currentOnDrag(targetId, currentX, currentY)
                                        }
                                        TouchHandle.BOTTOM -> {
                                            currentH = (currentH + dhNorm).coerceAtLeast(0.05f)
                                        }
                                        TouchHandle.TOP -> {
                                            val newH = (currentH - dhNorm).coerceAtLeast(0.05f)
                                            currentY += (currentH - newH)
                                            currentH = newH
                                            currentOnDrag(targetId, currentX, currentY)
                                        }
                                        else -> {}
                                    }
                                    currentOnResize(targetId, currentW, currentH)
                                    change.consume()
                                }
                                else -> {}
                            }
                        }
                    }
                }
            }
    } else Modifier

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .aspectRatio(animatedAspectRatio)
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

private fun findHitElementAndHandle(
    manifest: TemplateManifest,
    editorState: MagicStoryEditorState,
    normX: Float,
    normY: Float,
    canvasW: Float,
    canvasH: Float
): Pair<String?, TouchHandle> {
    val sortedElements = manifest.elements.filter { it.visible && !editorState.isElementDeleted(it.id) }.sortedByDescending { it.zIndex }

    val scaleFactor = canvasW / manifest.referenceWidth.toFloat()
    val touchPxX = normX * canvasW
    val touchPxY = normY * canvasH

    editorState.selectedElementId?.let { selectedId ->
        val elem = sortedElements.find { it.id == selectedId }
        if (elem != null) {
            val handle = checkElementHandleHit(elem, manifest, editorState, touchPxX, touchPxY, canvasW, canvasH, scaleFactor)
            if (handle != TouchHandle.NONE) {
                return Pair(selectedId, handle)
            }
        }
    }

    for (element in sortedElements) {
        val handle = checkElementHandleHit(element, manifest, editorState, touchPxX, touchPxY, canvasW, canvasH, scaleFactor)
        if (handle != TouchHandle.NONE) {
            return Pair(element.id, handle)
        }
    }

    return Pair(null, TouchHandle.NONE)
}

private fun checkElementHandleHit(
    element: TemplateElement,
    manifest: TemplateManifest,
    editorState: MagicStoryEditorState,
    touchPxX: Float,
    touchPxY: Float,
    canvasW: Float,
    canvasH: Float,
    scaleFactor: Float
): TouchHandle {
    if (element is TemplateElement.Background) {
        if (touchPxX in 0f..canvasW && touchPxY in 0f..canvasH) {
            return TouchHandle.BODY
        }
        return TouchHandle.NONE
    }

    val refW = manifest.referenceWidth.toFloat()
    val refH = manifest.referenceHeight.toFloat()

    val elemX = editorState.getElementX(element.id, getElementDefaultX(element)) * canvasW
    val elemY = editorState.getElementY(element.id, getElementDefaultY(element)) * canvasH
    val elemW = editorState.getElementW(element.id, getElementDefaultW(element)) * refW * scaleFactor
    val elemH = editorState.getElementH(element.id, getElementDefaultH(element)) * refH * scaleFactor
    val rotation = editorState.getElementRotation(element.id, getElementDefaultRotation(element))

    val cx = elemX + elemW / 2f
    val cy = elemY + elemH / 2f

    val rad = Math.toRadians(-rotation.toDouble())
    val dx = touchPxX - cx
    val dy = touchPxY - cy

    val lx = (dx * cos(rad) - dy * sin(rad)).toFloat()
    val ly = (dx * sin(rad) + dy * cos(rad)).toFloat()

    val w2 = elemW / 2f
    val h2 = elemH / 2f

    val hitRadius = 24f * scaleFactor

    val stemTopY = -h2 - 28f * scaleFactor
    if (distSq(lx, ly, 0f, stemTopY) <= (hitRadius * 1.5f) * (hitRadius * 1.5f)) {
        return TouchHandle.ROTATION
    }

    if (distSq(lx, ly, -w2, -h2) <= hitRadius * hitRadius) return TouchHandle.TOP_LEFT
    if (distSq(lx, ly, w2, -h2) <= hitRadius * hitRadius) return TouchHandle.TOP_RIGHT
    if (distSq(lx, ly, -w2, h2) <= hitRadius * hitRadius) return TouchHandle.BOTTOM_LEFT
    if (distSq(lx, ly, w2, h2) <= hitRadius * hitRadius) return TouchHandle.BOTTOM_RIGHT

    if (distSq(lx, ly, -w2, 0f) <= hitRadius * hitRadius) return TouchHandle.LEFT
    if (distSq(lx, ly, w2, 0f) <= hitRadius * hitRadius) return TouchHandle.RIGHT
    if (distSq(lx, ly, 0f, -h2) <= hitRadius * hitRadius) return TouchHandle.TOP
    if (distSq(lx, ly, 0f, h2) <= hitRadius * hitRadius) return TouchHandle.BOTTOM

    if (lx in -w2..w2 && ly in -h2..h2) {
        return TouchHandle.BODY
    }

    return TouchHandle.NONE
}

private fun distSq(x1: Float, y1: Float, x2: Float, y2: Float): Float {
    val dx = x1 - x2
    val dy = y1 - y2
    return dx * dx + dy * dy
}

private fun getElementDefaultX(element: TemplateElement): Float = when (element) {
    is TemplateElement.Text -> element.x
    is TemplateElement.Shape -> element.x
    is TemplateElement.PhotoSlot -> element.x
    is TemplateElement.PhotoCard -> element.x
    is TemplateElement.PillCta -> element.x
    else -> 0f
}

private fun getElementDefaultY(element: TemplateElement): Float = when (element) {
    is TemplateElement.Text -> element.y
    is TemplateElement.Shape -> element.y
    is TemplateElement.PhotoSlot -> element.y
    is TemplateElement.PhotoCard -> element.y
    is TemplateElement.PillCta -> element.y
    else -> 0f
}

private fun getElementDefaultW(element: TemplateElement): Float = when (element) {
    is TemplateElement.Text -> element.width
    is TemplateElement.Shape -> element.width
    is TemplateElement.PhotoSlot -> element.width
    is TemplateElement.PhotoCard -> element.width
    is TemplateElement.PillCta -> element.width
    else -> 0.1f
}

private fun getElementDefaultH(element: TemplateElement): Float = when (element) {
    is TemplateElement.Text -> element.height
    is TemplateElement.Shape -> element.height
    is TemplateElement.PhotoSlot -> element.height
    is TemplateElement.PhotoCard -> element.height
    is TemplateElement.PillCta -> element.height
    else -> 0.1f
}

private fun getElementDefaultRotation(element: TemplateElement): Float = when (element) {
    is TemplateElement.Text -> element.rotation
    is TemplateElement.Shape -> element.rotation
    is TemplateElement.PhotoSlot -> element.rotation
    is TemplateElement.PhotoCard -> element.rotation
    is TemplateElement.PillCta -> element.rotation
    else -> 0f
}

private fun getElementX(manifest: TemplateManifest, editorState: MagicStoryEditorState, elementId: String): Float {
    val elem = manifest.elements.find { it.id == elementId } ?: return 0f
    return editorState.getElementX(elementId, getElementDefaultX(elem))
}

private fun getElementY(manifest: TemplateManifest, editorState: MagicStoryEditorState, elementId: String): Float {
    val elem = manifest.elements.find { it.id == elementId } ?: return 0f
    return editorState.getElementY(elementId, getElementDefaultY(elem))
}

private fun getElementW(manifest: TemplateManifest, editorState: MagicStoryEditorState, elementId: String): Float {
    val elem = manifest.elements.find { it.id == elementId } ?: return 0.1f
    return editorState.getElementW(elementId, getElementDefaultW(elem))
}

private fun getElementH(manifest: TemplateManifest, editorState: MagicStoryEditorState, elementId: String): Float {
    val elem = manifest.elements.find { it.id == elementId } ?: return 0.1f
    return editorState.getElementH(elementId, getElementDefaultH(elem))
}

private fun getElementRotation(manifest: TemplateManifest, editorState: MagicStoryEditorState, elementId: String): Float {
    val elem = manifest.elements.find { it.id == elementId } ?: return 0f
    return editorState.getElementRotation(elementId, getElementDefaultRotation(elem))
}
