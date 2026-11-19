package com.mahiuddin.magicstory.ui.editor

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.RotateLeft
import androidx.compose.material.icons.automirrored.outlined.RotateRight
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material.icons.outlined.SwapVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.mahiuddin.magicstory.ui.theme.PrimaryRed
import com.mahiuddin.magicstory.util.BitmapUtils
import java.io.File
import java.io.FileOutputStream
import kotlin.math.abs

data class AspectPreset(
    val label: String,
    val ratio: Float? // width / height, or null for free
)

private enum class CropHandle {
    NONE,
    BODY,
    TOP_LEFT,
    TOP_RIGHT,
    BOTTOM_LEFT,
    BOTTOM_RIGHT,
    LEFT,
    RIGHT,
    TOP,
    BOTTOM
}

@Composable
fun EditorImageCropDialog(
    imageUri: Uri,
    onConfirm: (Uri) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var sourceBitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(imageUri) {
        sourceBitmap = BitmapUtils.loadBitmap(context, imageUri.toString(), 1920, 1920)
    }

    if (sourceBitmap == null) return

    var rotationDegrees by remember { mutableFloatStateOf(0f) }
    var flipX by remember { mutableStateOf(false) }
    var flipY by remember { mutableStateOf(false) }
    var isCircleMask by remember { mutableStateOf(false) }
    var isRatioLocked by remember { mutableStateOf(false) }
    var selectedPreset by remember { mutableStateOf<AspectPreset?>(null) }

    // Crop box coordinates normalized (0f..1f relative to transformed image)
    var cropNormLeft by remember { mutableFloatStateOf(0.05f) }
    var cropNormTop by remember { mutableFloatStateOf(0.05f) }
    var cropNormRight by remember { mutableFloatStateOf(0.95f) }
    var cropNormBottom by remember { mutableFloatStateOf(0.95f) }

    val presets = listOf(
        AspectPreset("1:1", 1.0f),
        AspectPreset("16:9", 16f / 9f),
        AspectPreset("4:3", 4f / 3f),
        AspectPreset("3:2", 3f / 2f),
        AspectPreset("9:16", 9f / 16f),
        AspectPreset("3:4", 3f / 4f),
        AspectPreset("2:3", 2f / 3f)
    )

    val bmp = sourceBitmap!!
    val transformedBitmap = remember(bmp, rotationDegrees, flipX, flipY) {
        applyTransformsToBitmap(bmp, rotationDegrees, flipX, flipY)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF121212))
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Top Action Bar: Title, Cancel, Confirm
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "Cancel",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Text(
                        text = "Crop & Edit Image ✂️",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 16.sp
                    )

                    IconButton(
                        onClick = {
                            val croppedUri = processAndSaveCroppedBitmap(
                                context = context,
                                bitmap = transformedBitmap,
                                cropLeft = cropNormLeft,
                                cropTop = cropNormTop,
                                cropRight = cropNormRight,
                                cropBottom = cropNormBottom,
                                isCircle = isCircleMask
                            )
                            croppedUri?.let(onConfirm)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Check,
                            contentDescription = "Confirm",
                            tint = PrimaryRed,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                // Center Image Viewport with Interactive 3x3 Crop Box & Corner Handles
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    ImageCropCanvasViewport(
                        bitmap = transformedBitmap,
                        cropLeft = cropNormLeft,
                        cropTop = cropNormTop,
                        cropRight = cropNormRight,
                        cropBottom = cropNormBottom,
                        isCircleMask = isCircleMask,
                        onCropChange = { l, t, r, b ->
                            cropNormLeft = l
                            cropNormTop = t
                            cropNormRight = r
                            cropNormBottom = b
                        }
                    )
                }

                // Aspect Ratio Presets Row (Matching PixelLab Screenshot)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    presets.forEach { preset ->
                        val isSelected = selectedPreset == preset
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable {
                                    selectedPreset = preset
                                    val targetRatio = preset.ratio ?: 1.0f
                                    val bw = transformedBitmap.width.toFloat()
                                    val bh = transformedBitmap.height.toFloat()
                                    val imgRatio = if (bh > 0) bw / bh else 1.0f

                                    val normRatio = targetRatio / imgRatio
                                    val (normW, normH) = if (normRatio <= 1.0f) {
                                        Pair((0.85f * normRatio).coerceIn(0.15f, 0.95f), 0.85f)
                                    } else {
                                        Pair(0.85f, (0.85f / normRatio).coerceIn(0.15f, 0.95f))
                                    }

                                    cropNormLeft = ((1f - normW) / 2f).coerceIn(0f, 1f - normW)
                                    cropNormRight = cropNormLeft + normW
                                    cropNormTop = ((1f - normH) / 2f).coerceIn(0f, 1f - normH)
                                    cropNormBottom = cropNormTop + normH
                                },
                            shape = RoundedCornerShape(6.dp),
                            color = if (isSelected) PrimaryRed else Color(0xFF262626),
                            border = BorderStroke(1.dp, if (isSelected) PrimaryRed else Color(0xFF404040))
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp, 20.dp)
                                        .border(1.dp, if (isSelected) Color.White else Color(0xFFCCCCCC), RoundedCornerShape(2.dp))
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = preset.label,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                // Editing Action Buttons Row (Rotate, Flip, Circle Mask, Aspect Lock)
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFF1E1E1E),
                    border = BorderStroke(1.dp, Color(0xFF2C2C2C))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Rotate Left
                        IconButton(onClick = { rotationDegrees = (rotationDegrees - 90f) % 360f }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.RotateLeft,
                                contentDescription = "Rotate Left",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        // Rotate Right
                        IconButton(onClick = { rotationDegrees = (rotationDegrees + 90f) % 360f }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.RotateRight,
                                contentDescription = "Rotate Right",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        // Circle Mask Toggle
                        IconButton(onClick = { isCircleMask = !isCircleMask }) {
                            Icon(
                                imageVector = Icons.Outlined.AccountCircle,
                                contentDescription = "Circle Crop",
                                tint = if (isCircleMask) PrimaryRed else Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        // Ratio Lock Toggle
                        IconButton(onClick = { isRatioLocked = !isRatioLocked }) {
                            Icon(
                                imageVector = if (isRatioLocked) Icons.Outlined.Lock else Icons.Outlined.LockOpen,
                                contentDescription = "Lock Ratio",
                                tint = if (isRatioLocked) PrimaryRed else Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        // Flip Horizontal
                        IconButton(onClick = { flipX = !flipX }) {
                            Icon(
                                imageVector = Icons.Outlined.SwapHoriz,
                                contentDescription = "Flip Horizontal",
                                tint = if (flipX) PrimaryRed else Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        // Flip Vertical
                        IconButton(onClick = { flipY = !flipY }) {
                            Icon(
                                imageVector = Icons.Outlined.SwapVert,
                                contentDescription = "Flip Vertical",
                                tint = if (flipY) PrimaryRed else Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ImageCropCanvasViewport(
    bitmap: Bitmap,
    cropLeft: Float,
    cropTop: Float,
    cropRight: Float,
    cropBottom: Float,
    isCircleMask: Boolean,
    onCropChange: (Float, Float, Float, Float) -> Unit
) {
    var activeHandle by remember { mutableStateOf(CropHandle.NONE) }

    val currentLeft by rememberUpdatedState(cropLeft)
    val currentTop by rememberUpdatedState(cropTop)
    val currentRight by rememberUpdatedState(cropRight)
    val currentBottom by rememberUpdatedState(cropBottom)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(bitmap, isCircleMask) {
                detectDragGestures(
                    onDragStart = { offset ->
                        val w = size.width.toFloat()
                        val h = size.height.toFloat()
                        if (w <= 0 || h <= 0) return@detectDragGestures

                        val bw = bitmap.width.toFloat()
                        val bh = bitmap.height.toFloat()
                        val scale = minOf(w / bw, h / bh)
                        val drawW = bw * scale
                        val drawH = bh * scale
                        val offsetX = (w - drawW) / 2f
                        val offsetY = (h - drawH) / 2f

                        val pxL = offsetX + currentLeft * drawW
                        val pxT = offsetY + currentTop * drawH
                        val pxR = offsetX + currentRight * drawW
                        val pxB = offsetY + currentBottom * drawH

                        val touchX = offset.x
                        val touchY = offset.y
                        val cornerHitRadius = 44.dp.toPx()
                        val edgeHitRadius = 28.dp.toPx()

                        activeHandle = when {
                            // 1. Corners
                            abs(touchX - pxL) < cornerHitRadius && abs(touchY - pxT) < cornerHitRadius -> CropHandle.TOP_LEFT
                            abs(touchX - pxR) < cornerHitRadius && abs(touchY - pxT) < cornerHitRadius -> CropHandle.TOP_RIGHT
                            abs(touchX - pxL) < cornerHitRadius && abs(touchY - pxB) < cornerHitRadius -> CropHandle.BOTTOM_LEFT
                            abs(touchX - pxR) < cornerHitRadius && abs(touchY - pxB) < cornerHitRadius -> CropHandle.BOTTOM_RIGHT

                            // 2. Edges
                            abs(touchX - pxL) < edgeHitRadius && touchY in pxT..pxB -> CropHandle.LEFT
                            abs(touchX - pxR) < edgeHitRadius && touchY in pxT..pxB -> CropHandle.RIGHT
                            abs(touchY - pxT) < edgeHitRadius && touchX in pxL..pxR -> CropHandle.TOP
                            abs(touchY - pxB) < edgeHitRadius && touchX in pxL..pxR -> CropHandle.BOTTOM

                            // 3. Body Pan
                            touchX in pxL..pxR && touchY in pxT..pxB -> CropHandle.BODY
                            else -> CropHandle.NONE
                        }
                    },
                    onDragEnd = { activeHandle = CropHandle.NONE },
                    onDragCancel = { activeHandle = CropHandle.NONE }
                ) { change, dragAmount ->
                    change.consume()
                    val w = size.width.toFloat()
                    val h = size.height.toFloat()
                    if (w <= 0 || h <= 0) return@detectDragGestures

                    val bw = bitmap.width.toFloat()
                    val bh = bitmap.height.toFloat()
                    val scale = minOf(w / bw, h / bh)
                    val drawW = bw * scale
                    val drawH = bh * scale

                    if (drawW <= 0 || drawH <= 0) return@detectDragGestures

                    val dxNorm = dragAmount.x / drawW
                    val dyNorm = dragAmount.y / drawH

                    var newL = currentLeft
                    var newT = currentTop
                    var newR = currentRight
                    var newB = currentBottom

                    when (activeHandle) {
                        CropHandle.LEFT -> {
                            newL = (currentLeft + dxNorm).coerceIn(0f, currentRight - 0.05f)
                            if (isCircleMask) {
                                val sizeVal = currentRight - newL
                                newB = (currentTop + sizeVal).coerceIn(0f, 1f)
                            }
                        }
                        CropHandle.RIGHT -> {
                            newR = (currentRight + dxNorm).coerceIn(currentLeft + 0.05f, 1f)
                            if (isCircleMask) {
                                val sizeVal = newR - currentLeft
                                newB = (currentTop + sizeVal).coerceIn(0f, 1f)
                            }
                        }
                        CropHandle.TOP -> {
                            newT = (currentTop + dyNorm).coerceIn(0f, currentBottom - 0.05f)
                            if (isCircleMask) {
                                val sizeVal = currentBottom - newT
                                newR = (currentLeft + sizeVal).coerceIn(0f, 1f)
                            }
                        }
                        CropHandle.BOTTOM -> {
                            newB = (currentBottom + dyNorm).coerceIn(currentTop + 0.05f, 1f)
                            if (isCircleMask) {
                                val sizeVal = newB - currentTop
                                newR = (currentLeft + sizeVal).coerceIn(0f, 1f)
                            }
                        }
                        CropHandle.TOP_LEFT -> {
                            newL = (currentLeft + dxNorm).coerceIn(0f, currentRight - 0.05f)
                            newT = (currentTop + dyNorm).coerceIn(0f, currentBottom - 0.05f)
                            if (isCircleMask) {
                                val sizeVal = maxOf(newR - newL, newB - newT)
                                newL = newR - sizeVal
                                newT = newB - sizeVal
                            }
                        }
                        CropHandle.TOP_RIGHT -> {
                            newR = (currentRight + dxNorm).coerceIn(currentLeft + 0.05f, 1f)
                            newT = (currentTop + dyNorm).coerceIn(0f, currentBottom - 0.05f)
                            if (isCircleMask) {
                                val sizeVal = maxOf(newR - newL, newB - newT)
                                newR = newL + sizeVal
                                newT = newB - sizeVal
                            }
                        }
                        CropHandle.BOTTOM_LEFT -> {
                            newL = (currentLeft + dxNorm).coerceIn(0f, currentRight - 0.05f)
                            newB = (currentBottom + dyNorm).coerceIn(currentTop + 0.05f, 1f)
                            if (isCircleMask) {
                                val sizeVal = maxOf(newR - newL, newB - newT)
                                newL = newR - sizeVal
                                newB = newT + sizeVal
                            }
                        }
                        CropHandle.BOTTOM_RIGHT -> {
                            newR = (currentRight + dxNorm).coerceIn(currentLeft + 0.05f, 1f)
                            newB = (currentBottom + dyNorm).coerceIn(currentTop + 0.05f, 1f)
                            if (isCircleMask) {
                                val sizeVal = maxOf(newR - newL, newB - newT)
                                newR = newL + sizeVal
                                newB = newT + sizeVal
                            }
                        }
                        CropHandle.BODY -> {
                            val cropW = currentRight - currentLeft
                            val cropH = currentBottom - currentTop

                            newL = (currentLeft + dxNorm).coerceIn(0f, 1f - cropW)
                            newR = newL + cropW
                            newT = (currentTop + dyNorm).coerceIn(0f, 1f - cropH)
                            newB = newT + cropH
                        }
                        else -> {}
                    }

                    onCropChange(newL, newT, newR, newB)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasW = size.width
            val canvasH = size.height
            if (canvasW <= 0 || canvasH <= 0) return@Canvas

            val bw = bitmap.width.toFloat()
            val bh = bitmap.height.toFloat()
            val scale = minOf(canvasW / bw, canvasH / bh)
            val drawW = bw * scale
            val drawH = bh * scale
            val offsetX = (canvasW - drawW) / 2f
            val offsetY = (canvasH - drawH) / 2f

            // Draw Transformed Bitmap
            drawImage(
                image = bitmap.asImageBitmap(),
                dstOffset = IntOffset(offsetX.toInt(), offsetY.toInt()),
                dstSize = IntSize(drawW.toInt(), drawH.toInt())
            )

            // Crop Box Coordinates
            val cropPxL = offsetX + cropLeft * drawW
            val cropPxT = offsetY + cropTop * drawH
            val cropPxR = offsetX + cropRight * drawW
            val cropPxB = offsetY + cropBottom * drawH
            val cropPxW = cropPxR - cropPxL
            val cropPxH = cropPxB - cropPxT

            // Dimmed Scrim outside Crop Box
            val scrimColor = Color.Black.copy(alpha = 0.6f)
            drawRect(color = scrimColor, topLeft = Offset(0f, 0f), size = Size(canvasW, cropPxT))
            drawRect(color = scrimColor, topLeft = Offset(0f, cropPxB), size = Size(canvasW, canvasH - cropPxB))
            drawRect(color = scrimColor, topLeft = Offset(0f, cropPxT), size = Size(cropPxL, cropPxH))
            drawRect(color = scrimColor, topLeft = Offset(cropPxR, cropPxT), size = Size(canvasW - cropPxR, cropPxH))

            // 3x3 Rule-of-Thirds Grid inside Crop Box
            val gridColor = Color.White.copy(alpha = 0.55f)
            val strokeW = 1.dp.toPx()

            // Vertical lines
            drawLine(gridColor, Offset(cropPxL + cropPxW / 3f, cropPxT), Offset(cropPxL + cropPxW / 3f, cropPxB), strokeWidth = strokeW)
            drawLine(gridColor, Offset(cropPxL + (2f * cropPxW) / 3f, cropPxT), Offset(cropPxL + (2f * cropPxW) / 3f, cropPxB), strokeWidth = strokeW)

            // Horizontal lines
            drawLine(gridColor, Offset(cropPxL, cropPxT + cropPxH / 3f), Offset(cropPxR, cropPxT + cropPxH / 3f), strokeWidth = strokeW)
            drawLine(gridColor, Offset(cropPxL, cropPxT + (2f * cropPxH) / 3f), Offset(cropPxR, cropPxT + (2f * cropPxH) / 3f), strokeWidth = strokeW)

            // Draw Crop Boundary & Solid White Corner Handles
            if (isCircleMask) {
                drawCircle(
                    color = Color.White,
                    radius = minOf(cropPxW, cropPxH) / 2f,
                    center = Offset(cropPxL + cropPxW / 2f, cropPxT + cropPxH / 2f),
                    style = Stroke(width = 2.dp.toPx())
                )
            } else {
                drawRect(
                    color = Color.White,
                    topLeft = Offset(cropPxL, cropPxT),
                    size = Size(cropPxW, cropPxH),
                    style = Stroke(width = 2.dp.toPx())
                )

                // Corner Handles
                val handleRadius = 7.dp.toPx()
                drawCircle(Color.White, handleRadius, Offset(cropPxL, cropPxT))
                drawCircle(Color.White, handleRadius, Offset(cropPxR, cropPxT))
                drawCircle(Color.White, handleRadius, Offset(cropPxL, cropPxB))
                drawCircle(Color.White, handleRadius, Offset(cropPxR, cropPxB))
            }
        }
    }
}

private fun applyTransformsToBitmap(bitmap: Bitmap, rotation: Float, flipX: Boolean, flipY: Boolean): Bitmap {
    if (rotation == 0f && !flipX && !flipY) return bitmap

    val matrix = Matrix().apply {
        if (rotation != 0f) postRotate(rotation)
        if (flipX || flipY) postScale(if (flipX) -1f else 1f, if (flipY) -1f else 1f)
    }

    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}

private fun processAndSaveCroppedBitmap(
    context: Context,
    bitmap: Bitmap,
    cropLeft: Float,
    cropTop: Float,
    cropRight: Float,
    cropBottom: Float,
    isCircle: Boolean
): Uri? {
    return try {
        val bw = bitmap.width
        val bh = bitmap.height

        val x = (cropLeft * bw).toInt().coerceIn(0, bw - 1)
        val y = (cropTop * bh).toInt().coerceIn(0, bh - 1)
        val w = ((cropRight - cropLeft) * bw).toInt().coerceIn(1, bw - x)
        val h = ((cropBottom - cropTop) * bh).toInt().coerceIn(1, bh - y)

        var cropped = Bitmap.createBitmap(bitmap, x, y, w, h)

        if (isCircle) {
            val size = minOf(w, h)
            val circleBmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(circleBmp)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)

            // Ensure background is 100% transparent PNG with SRC_IN mask
            canvas.drawColor(android.graphics.Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
            canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)

            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
            val srcX = (w - size) / 2
            val srcY = (h - size) / 2
            val srcRect = Rect(srcX, srcY, srcX + size, srcY + size)
            val destRect = Rect(0, 0, size, size)
            canvas.drawBitmap(cropped, srcRect, destRect, paint)

            cropped = circleBmp
        }

        val cacheFile = File(context.cachePathOrDir(), "cropped_image_${System.currentTimeMillis()}.png")
        FileOutputStream(cacheFile).use { out ->
            cropped.compress(Bitmap.CompressFormat.PNG, 100, out)
        }

        Uri.fromFile(cacheFile)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

private fun Context.cachePathOrDir(): File {
    return externalCacheDir ?: cacheDir
}
