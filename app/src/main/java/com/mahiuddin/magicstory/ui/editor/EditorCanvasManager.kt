package com.mahiuddin.magicstory.ui.editor

import android.content.Context
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Colorize
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material.icons.outlined.ZoomIn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mahiuddin.magicstory.domain.model.MagicStoryEditorState
import com.mahiuddin.magicstory.template.renderer.TemplateRenderer
import com.mahiuddin.magicstory.ui.components.TemplateCanvasPreview
import kotlin.math.abs

@Composable
fun EditorCanvasManager(
    state: MagicStoryEditorState,
    viewModel: EditorViewModel,
    context: Context,
    colorPickerMode: String,
    onClearSubTool: () -> Unit,
    modifier: Modifier = Modifier
) {
    var zoomScale by remember { mutableFloatStateOf(1.0f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }

    val animatedZoomScale by animateFloatAsState(
        targetValue = zoomScale,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "zoomScale"
    )
    val animatedPanOffset by animateOffsetAsState(
        targetValue = panOffset,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "panOffset"
    )

    // FIXED WORKSPACE CONTAINER WITH 2-FINGER PINCH ZOOM & PAN
    Box(
        modifier = modifier
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    val newZoom = (zoomScale * zoom).coerceIn(0.5f, 5.0f)
                    val scaleChange = newZoom / zoomScale
                    zoomScale = newZoom
                    panOffset = Offset(
                        x = (panOffset.x + pan.x) * scaleChange,
                        y = (panOffset.y + pan.y) * scaleChange
                    )
                }
            }
    ) {
        // STABLE CANVAS VIEWPORT WITH ZOOM & PAN GRAPHICS LAYER
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 80.dp)
                .graphicsLayer {
                    scaleX = animatedZoomScale
                    scaleY = animatedZoomScale
                    translationX = animatedPanOffset.x
                    translationY = animatedPanOffset.y
                },
            contentAlignment = Alignment.Center
        ) {
            TemplateCanvasPreview(
                manifest = state.template,
                editorState = state,
                modifier = Modifier.fillMaxSize(),
                isInteractive = !state.isEyedropperActive,
                zoomScale = animatedZoomScale,
                panOffset = animatedPanOffset,
                onElementSelect = { id ->
                    viewModel.selectElement(id)
                    onClearSubTool()
                },
                onElementDrag = { id, x, y -> viewModel.updateElementPosition(id, x, y) },
                onElementResize = { id, w, h -> viewModel.updateElementSize(id, w, h) },
                onElementRotate = { id, rot -> viewModel.updateElementRotation(id, rot) },
                onSnappingGuides = { viewModel.setSnappingGuideLines(it) },
                onDragEnd = { viewModel.commitGestureEnd() }
            )

            // Interactive Eyedropper Layer Overlay
            if (state.isEyedropperActive) {
                val activeSelectedId = state.selectedElementId ?: "bg"
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = { offset ->
                                    val normX = (offset.x / size.width.toFloat()).coerceIn(0f, 1f)
                                    val normY = (offset.y / size.height.toFloat()).coerceIn(0f, 1f)
                                    val sampledHex = TemplateRenderer.sampleColorAt(context, state.template, state, normX, normY)
                                    viewModel.updateEyedropperSample(sampledHex, offset.x, offset.y)
                                    awaitRelease()
                                    when (colorPickerMode) {
                                        "STROKE" -> {
                                            val curW = state.getElementStroke(activeSelectedId)?.second ?: 4f
                                            viewModel.updateElementStroke(activeSelectedId, sampledHex, curW.coerceAtLeast(2f))
                                        }
                                        "SHADOW" -> {
                                            val curEff = state.getLayerEffect(activeSelectedId)
                                            viewModel.updateDropShadow(activeSelectedId, sampledHex, curEff.dropShadowRadius.coerceAtLeast(8f), curEff.dropShadowDx, curEff.dropShadowDy)
                                        }
                                        "OUTER_GLOW" -> {
                                            val curEff = state.getLayerEffect(activeSelectedId)
                                            viewModel.updateOuterGlow(activeSelectedId, sampledHex, curEff.outerGlowRadius.coerceAtLeast(12f))
                                        }
                                        "INNER_GLOW" -> {
                                            val curEff = state.getLayerEffect(activeSelectedId)
                                            viewModel.updateInnerGlow(activeSelectedId, sampledHex, curEff.innerGlowRadius.coerceAtLeast(8f))
                                        }
                                        else -> viewModel.updateTextColor(activeSelectedId, sampledHex)
                                    }
                                    viewModel.setEyedropperActive(false)
                                }
                            )
                        }
                ) {
                    state.eyedropperTouchPos?.let { (px, py) ->
                        val sampledColor = state.eyedropperSampledColor ?: "#C91F2C"
                        Box(
                            modifier = Modifier
                                .offset { IntOffset((px - 45.dp.toPx()).toInt(), (py - 85.dp.toPx()).toInt()) }
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(Color(android.graphics.Color.parseColor(sampledColor)))
                                .border(2.dp, Color.White, CircleShape)
                                .shadow(4.dp, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(sampledColor, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }
        }



        // Floating Reset Zoom Badge (appears when zoomed or panned)
        if (abs(zoomScale - 1.0f) > 0.05f || panOffset != Offset.Zero) {
            Surface(
                onClick = {
                    zoomScale = 1.0f
                    panOffset = Offset.Zero
                },
                shape = CircleShape,
                color = Color(0xFF202020).copy(alpha = 0.85f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f)),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 12.dp, end = 16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ZoomIn,
                        contentDescription = "Reset Zoom",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${(zoomScale * 100).toInt()}% ↺",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        if (state.isExporting) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Color.White)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Exporting High-Res Image...", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
