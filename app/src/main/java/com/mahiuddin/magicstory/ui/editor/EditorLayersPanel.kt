package com.mahiuddin.magicstory.ui.editor

import android.graphics.Paint
import android.graphics.RectF
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DragHandle
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.mahiuddin.magicstory.domain.model.MagicStoryEditorState
import com.mahiuddin.magicstory.domain.model.TemplateElement
import com.mahiuddin.magicstory.template.renderer.VectorShapePathFactory
import com.mahiuddin.magicstory.ui.theme.PrimaryRed
import com.mahiuddin.magicstory.util.BitmapUtils

@Composable
fun CheckerboardBox(
    modifier: Modifier = Modifier,
    squareSizeDp: Float = 5f,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val squarePx = squareSizeDp.dp.toPx()
            val cols = (size.width / squarePx).toInt() + 1
            val rows = (size.height / squarePx).toInt() + 1

            for (r in 0 until rows) {
                for (c in 0 until cols) {
                    val isEven = (r + c) % 2 == 0
                    drawRect(
                        color = if (isEven) Color(0xFFFFFFFF) else Color(0xFFE2E2E2),
                        topLeft = Offset(c * squarePx, r * squarePx),
                        size = Size(squarePx, squarePx)
                    )
                }
            }
        }
        content()
    }
}

@Composable
fun EditorLayersPanel(
    visible: Boolean,
    editorState: MagicStoryEditorState,
    viewModel: EditorViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val elements = editorState.template.elements.sortedByDescending { editorState.getElementZIndex(it.id, it.zIndex) }

    Box(modifier = Modifier.fillMaxSize()) {
        // Scrim backdrop overlay
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.35f))
                    .clickable { onDismiss() }
            )
        }

        // Slide-in left side drawer panel (PixelLab style)
        AnimatedVisibility(
            visible = visible,
            enter = slideInHorizontally(initialOffsetX = { -it }) + fadeIn(),
            exit = slideOutHorizontally(targetOffsetX = { -it }) + fadeOut(),
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            Surface(
                modifier = Modifier
                    .width(260.dp)
                    .fillMaxHeight(),
                color = Color.White,
                shadowElevation = 12.dp,
                border = BorderStroke(1.dp, Color(0xFFE0E0E0))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 6.dp, vertical = 10.dp)
                ) {
                    // Header Bar: Title, Layer Count, Close
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 4.dp, end = 4.dp, bottom = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Layers", modifier = Modifier.padding(top = 15.dp), fontWeight = FontWeight.Bold, color = Color(0xFF202020), fontSize = 12.sp)
                            Text("${elements.size} Items", fontSize = 8.sp, color = Color(0xFF777777), fontWeight = FontWeight.Medium)
                        }
                        IconButton(onClick = onDismiss, modifier = Modifier.size(26.dp)) {
                            Icon(imageVector = Icons.Outlined.Close, contentDescription = "Close", tint = Color(0xFF777777), modifier = Modifier.size(16.dp))
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Color(0xFFEAEAEA), thickness = 1.dp)

                    // LazyColumn for PixelLab-style transparent layer cards with Drag-to-Reorder gesture
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(elements, key = { it.id }) { elem ->
                            var offsetY by remember { mutableFloatStateOf(0f) }
                            var isDragging by remember { mutableStateOf(false) }

                            val animatedOffsetY by animateFloatAsState(
                                targetValue = if (isDragging) offsetY else 0f,
                                animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                                label = "layerOffsetY"
                            )

                            val isSelected = editorState.selectedElementId == elem.id
                            val isLocked = editorState.isElementLocked(elem.id)
                            val isHidden = editorState.isElementHidden(elem.id)

                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .zIndex(if (isDragging) 10f else 0f)
                                    .graphicsLayer {
                                        translationY = animatedOffsetY
                                        scaleX = if (isDragging) 1.03f else 1.0f
                                        scaleY = if (isDragging) 1.03f else 1.0f
                                        shadowElevation = if (isDragging) 12.dp.toPx() else 0f
                                    }
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { viewModel.selectElement(elem.id) },
                                shape = RoundedCornerShape(6.dp),
                                color = when {
                                    isDragging -> Color(0xFFBBDEFB)
                                    isSelected -> Color(0xFFE3F2FD)
                                    else -> Color(0xFFFBFBFB)
                                },
                                border = BorderStroke(
                                    width = if (isDragging || isSelected) 1.5.dp else 1.dp,
                                    color = if (isDragging || isSelected) Color(0xFF1976D2) else Color(0xFFE0E0E0)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Left: Drag handle (:::) with vertical drag-to-reorder gesture
                                    Box(
                                        modifier = Modifier
                                            .width(26.dp)
                                            .fillMaxHeight()
                                            .pointerInput(elem.id) {
                                                detectDragGestures(
                                                    onDragStart = {
                                                        isDragging = true
                                                        offsetY = 0f
                                                        viewModel.selectElement(elem.id)
                                                    },
                                                    onDragEnd = {
                                                        isDragging = false
                                                        offsetY = 0f
                                                    },
                                                    onDragCancel = {
                                                        isDragging = false
                                                        offsetY = 0f
                                                    }
                                                ) { change, dragAmount ->
                                                    change.consume()
                                                    offsetY += dragAmount.y
                                                    val threshold = 50.dp.toPx()
                                                    if (offsetY < -threshold) {
                                                        viewModel.reorderLayerByOffset(elem.id, -1)
                                                        offsetY = 0f
                                                    } else if (offsetY > threshold) {
                                                        viewModel.reorderLayerByOffset(elem.id, 1)
                                                        offsetY = 0f
                                                    }
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.DragHandle,
                                            contentDescription = "Reorder Drag Handle",
                                            tint = if (isDragging) PrimaryRed else Color(0xFF888888),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    // Center: Authentic PNG Transparent Checkerboard Content Preview Box
                                    CheckerboardBox(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                            .padding(vertical = 3.dp, horizontal = 2.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .border(1.dp, Color(0xFFCCCCCC), RoundedCornerShape(4.dp))
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(4.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            when (elem) {
                                                is TemplateElement.Text -> {
                                                    val textVal = editorState.getTextValue(elem.id, elem.defaultText)
                                                    val fontFamStr = editorState.getTextFontFamily(elem.id, elem.fontFamily).uppercase()
                                                    val fontFam = when {
                                                        fontFamStr.contains("SERIF") -> FontFamily.Serif
                                                        fontFamStr.contains("SCRIPT") -> FontFamily.Cursive
                                                        fontFamStr.contains("MONO") -> FontFamily.Monospace
                                                        else -> FontFamily.SansSerif
                                                    }
                                                    val fontColorHex = editorState.getTextColor(elem.id, elem.color)
                                                    val fontColor = try {
                                                        Color(android.graphics.Color.parseColor(fontColorHex))
                                                    } catch (_: Exception) { Color(0xFF202020) }

                                                    Text(
                                                        text = textVal,
                                                        fontFamily = fontFam,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = fontColor,
                                                        style = TextStyle(
                                                            shadow = Shadow(
                                                                color = Color.Black.copy(alpha = 0.75f),
                                                                offset = Offset(1f, 1.5f),
                                                                blurRadius = 3f
                                                            )
                                                        ),
                                                        maxLines = 2,
                                                        textAlign = TextAlign.Center
                                                    )
                                                }
                                                is TemplateElement.Shape -> {
                                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                                        val rect = RectF(0f, 0f, size.width, size.height)
                                                        val path = VectorShapePathFactory.createPath(elem, rect, 1.0f)
                                                        val colorHex = editorState.getElementStroke(elem.id)?.first ?: elem.color
                                                        val shapePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                                                            color = try { android.graphics.Color.parseColor(colorHex) } catch (_: Exception) { android.graphics.Color.RED }
                                                            style = Paint.Style.FILL
                                                        }
                                                        drawContext.canvas.nativeCanvas.drawPath(path, shapePaint)

                                                        val stroke = editorState.getElementStroke(elem.id)
                                                        if (stroke != null && stroke.second > 0f) {
                                                            val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                                                                color = try { android.graphics.Color.parseColor(stroke.first) } catch (_: Exception) { android.graphics.Color.BLACK }
                                                                style = Paint.Style.STROKE
                                                                strokeWidth = (stroke.second * 0.3f).coerceAtLeast(1.5f)
                                                            }
                                                            drawContext.canvas.nativeCanvas.drawPath(path, strokePaint)
                                                        }
                                                    }
                                                }
                                                is TemplateElement.PhotoSlot, is TemplateElement.PhotoCard -> {
                                                    val photoUriStr = when (elem) {
                                                        is TemplateElement.PhotoSlot -> editorState.getPhotoUri(elem.id, elem.defaultAsset)
                                                        is TemplateElement.PhotoCard -> editorState.getPhotoUri(elem.photoSlotId, elem.defaultAsset)
                                                        else -> null
                                                    }
                                                    val photoBitmap = remember(photoUriStr) {
                                                        if (!photoUriStr.isNullOrBlank()) {
                                                            BitmapUtils.loadBitmap(context, photoUriStr, 120, 120)
                                                        } else null
                                                    }

                                                    if (photoBitmap != null) {
                                                        Image(
                                                            bitmap = photoBitmap.asImageBitmap(),
                                                            contentDescription = "Photo Layer Preview",
                                                            contentScale = ContentScale.Crop,
                                                            modifier = Modifier
                                                                .fillMaxSize()
                                                                .clip(RoundedCornerShape(3.dp))
                                                        )
                                                    } else {
                                                        Icon(
                                                            imageVector = Icons.Outlined.Image,
                                                            contentDescription = "Photo Preview",
                                                            tint = Color(0xFF444444),
                                                            modifier = Modifier.size(20.dp)
                                                        )
                                                    }
                                                }
                                                is TemplateElement.Background -> {
                                                    val bgHex = editorState.getTextColor("bg", elem.color)
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxSize()
                                                            .background(try { Color(android.graphics.Color.parseColor(bgHex)) } catch (_: Exception) { Color.White })
                                                    )
                                                }
                                                else -> {
                                                    Icon(
                                                        imageVector = Icons.Outlined.Category,
                                                        contentDescription = null,
                                                        tint = Color(0xFF666666),
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(4.dp))

                                    // Right: PixelLab 2x2 Action Grid (Lock, Edit, Visibility, Delete)
                                    Column(
                                        modifier = Modifier
                                            .width(54.dp)
                                            .fillMaxHeight()
                                            .padding(vertical = 3.dp),
                                        verticalArrangement = Arrangement.spacedBy(2.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Row(
                                            modifier = Modifier.weight(1f),
                                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Lock button
                                            Surface(
                                                onClick = { viewModel.toggleElementLock(elem.id) },
                                                modifier = Modifier.size(24.dp),
                                                shape = RoundedCornerShape(3.dp),
                                                color = if (isLocked) PrimaryRed.copy(alpha = 0.2f) else Color(0xFFF0F0F0)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(
                                                        imageVector = if (isLocked) Icons.Outlined.Lock else Icons.Outlined.LockOpen,
                                                        contentDescription = "Lock",
                                                        tint = if (isLocked) PrimaryRed else Color(0xFF666666),
                                                        modifier = Modifier.size(12.dp)
                                                    )
                                                }
                                            }

                                            // Edit button
                                            Surface(
                                                onClick = { viewModel.selectElement(elem.id) },
                                                modifier = Modifier.size(24.dp),
                                                shape = RoundedCornerShape(3.dp),
                                                color = Color(0xFFF0F0F0)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(
                                                        imageVector = Icons.Outlined.Edit,
                                                        contentDescription = "Edit",
                                                        tint = Color(0xFF666666),
                                                        modifier = Modifier.size(12.dp)
                                                    )
                                                }
                                            }
                                        }

                                        Row(
                                            modifier = Modifier.weight(1f),
                                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Visibility button
                                            Surface(
                                                onClick = { viewModel.toggleElementHide(elem.id) },
                                                modifier = Modifier.size(24.dp),
                                                shape = RoundedCornerShape(3.dp),
                                                color = if (isHidden) Color(0xFFDDDDDD) else Color(0xFFF0F0F0)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(
                                                        imageVector = if (isHidden) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                                        contentDescription = "Visibility",
                                                        tint = if (isHidden) Color(0xFF888888) else Color(0xFF666666),
                                                        modifier = Modifier.size(12.dp)
                                                    )
                                                }
                                            }

                                            // Delete button
                                            Surface(
                                                onClick = { viewModel.deleteElement(elem.id) },
                                                modifier = Modifier.size(24.dp),
                                                shape = RoundedCornerShape(3.dp),
                                                color = Color(0xFFFFEBEE)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(
                                                        imageVector = Icons.Outlined.Delete,
                                                        contentDescription = "Delete",
                                                        tint = PrimaryRed,
                                                        modifier = Modifier.size(12.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
