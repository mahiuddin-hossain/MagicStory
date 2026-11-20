package com.mahiuddin.magicstory.ui.editor

import android.graphics.Color as AndroidColor
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
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Colorize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mahiuddin.magicstory.ui.theme.PrimaryRed

@Composable
fun UniversalColorPickerDialog(
    initialColorHex: String,
    savedSwatches: List<String>,
    onColorSelected: (String) -> Unit,
    onAddSavedSwatch: (String) -> Unit,
    onDismiss: () -> Unit,
    onOpenEyedropper: () -> Unit
) {
    val parsedHsv = remember(initialColorHex) {
        val hsv = FloatArray(3)
        try {
            val colorInt = AndroidColor.parseColor(initialColorHex)
            AndroidColor.colorToHSV(colorInt, hsv)
            val alpha = (AndroidColor.alpha(colorInt) / 255f)
            Pair(hsv, alpha)
        } catch (_: Exception) {
            Pair(floatArrayOf(0f, 1f, 1f), 1f)
        }
    }

    var hue by remember { mutableFloatStateOf(parsedHsv.first[0]) }
    var saturation by remember { mutableFloatStateOf(parsedHsv.first[1]) }
    var value by remember { mutableFloatStateOf(parsedHsv.first[2]) }
    var alpha by remember { mutableFloatStateOf(parsedHsv.second) }

    var selectedMode by remember { mutableStateOf("RGB") }
    var showModeDropdown by remember { mutableStateOf(false) }

    val currentColor = remember(hue, saturation, value, alpha) {
        val colorInt = AndroidColor.HSVToColor((alpha * 255).toInt(), floatArrayOf(hue, saturation, value))
        Color(colorInt)
    }

    val currentColorHex = remember(hue, saturation, value, alpha) {
        val colorInt = AndroidColor.HSVToColor((alpha * 255).toInt(), floatArrayOf(hue, saturation, value))
        if (alpha < 0.99f) {
            "#%08X".format(colorInt)
        } else {
            "#%06X".format(0xFFFFFF and colorInt)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Color Picker 🎨", fontWeight = FontWeight.Bold, color = Color(0xFF202020), fontSize = 16.sp)
                IconButton(onClick = onDismiss, modifier = Modifier.size(26.dp)) {
                    Icon(imageVector = Icons.Outlined.Close, contentDescription = "Close", tint = Color(0xFF777777), modifier = Modifier.size(18.dp))
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // 1. 2D Saturation & Value Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp))
                        .pointerInput(hue) {
                            detectDragGestures { change, _ ->
                                change.consume()
                                val w = size.width.toFloat()
                                val h = size.height.toFloat()
                                if (w > 0 && h > 0) {
                                    saturation = (change.position.x / w).coerceIn(0f, 1f)
                                    value = 1f - (change.position.y / h).coerceIn(0f, 1f)
                                }
                            }
                        }
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val baseColor = Color.hsv(hue, 1f, 1f)

                        // Base Color
                        drawRect(color = baseColor)

                        // Saturation Gradient (White to Transparent)
                        drawRect(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color.White, Color.Transparent)
                            )
                        )

                        // Value/Brightness Gradient (Transparent to Black)
                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black)
                            )
                        )

                        // Selection Ring Handle
                        val posX = saturation * size.width
                        val posY = (1f - value) * size.height

                        drawCircle(Color.White, radius = 10.dp.toPx(), center = Offset(posX, posY))
                        drawCircle(currentColor, radius = 7.dp.toPx(), center = Offset(posX, posY))
                        drawCircle(Color.White, radius = 5.dp.toPx(), center = Offset(posX, posY), style = Stroke(width = 1.5.dp.toPx()))
                    }
                }

                // 2. Rainbow Hue Slider + Eyedropper Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = {
                            onOpenEyedropper()
                            onDismiss()
                        },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Colorize,
                            contentDescription = "Eyedropper",
                            tint = PrimaryRed,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(14.dp)
                            .clip(CircleShape)
                            .pointerInput(Unit) {
                                detectDragGestures { change, _ ->
                                    change.consume()
                                    val w = size.width.toFloat()
                                    if (w > 0) {
                                        hue = (change.position.x / w * 360f).coerceIn(0f, 360f)
                                    }
                                }
                            }
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val rainbowColors = listOf(
                                Color.Red, Color.Yellow, Color.Green, Color.Cyan, Color.Blue, Color.Magenta, Color.Red
                            )
                            drawRoundRect(
                                brush = Brush.horizontalGradient(colors = rainbowColors),
                                cornerRadius = CornerRadius(7.dp.toPx(), 7.dp.toPx())
                            )

                            val thumbX = (hue / 360f) * size.width
                            drawCircle(Color.White, radius = 8.dp.toPx(), center = Offset(thumbX, size.height / 2f))
                            drawCircle(Color.hsv(hue, 1f, 1f), radius = 5.dp.toPx(), center = Offset(thumbX, size.height / 2f))
                        }
                    }
                }

                // 3. Alpha / Opacity Transparency Slider
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(14.dp)
                        .clip(CircleShape)
                        .pointerInput(Unit) {
                            detectDragGestures { change, _ ->
                                change.consume()
                                val w = size.width.toFloat()
                                if (w > 0) {
                                    alpha = (change.position.x / w).coerceIn(0f, 1f)
                                }
                            }
                        }
                ) {
                    CheckerboardBox(modifier = Modifier.fillMaxSize(), squareSizeDp = 4f) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val baseOpaque = Color.hsv(hue, saturation, value, 1f)
                            drawRoundRect(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(Color.Transparent, baseOpaque)
                                ),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(7.dp.toPx(), 7.dp.toPx())
                            )

                            val thumbX = alpha * size.width
                            drawCircle(Color.White, radius = 8.dp.toPx(), center = Offset(thumbX, size.height / 2f))
                            drawCircle(currentColor, radius = 5.dp.toPx(), center = Offset(thumbX, size.height / 2f))
                        }
                    }
                }

                // 4. Color Mode & Numeric Input Row (RGB / HEX)
                val colorInt = AndroidColor.HSVToColor((alpha * 255).toInt(), floatArrayOf(hue, saturation, value))
                val r = AndroidColor.red(colorInt)
                val g = AndroidColor.green(colorInt)
                val b = AndroidColor.blue(colorInt)
                val aPct = (alpha * 100).toInt()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Mode Dropdown
                    Box {
                        Surface(
                            onClick = { showModeDropdown = true },
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF0F0F0),
                            border = BorderStroke(1.dp, Color(0xFFE0E0E0))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(selectedMode, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF202020))
                                Text(" ▾", fontSize = 10.sp, color = Color(0xFF777777))
                            }
                        }

                        DropdownMenu(
                            expanded = showModeDropdown,
                            onDismissRequest = { showModeDropdown = false },
                            modifier = Modifier.background(Color.White)
                        ) {
                            DropdownMenuItem(text = { Text("RGB", fontWeight = FontWeight.Bold) }, onClick = { selectedMode = "RGB"; showModeDropdown = false })
                            DropdownMenuItem(text = { Text("HEX", fontWeight = FontWeight.Bold) }, onClick = { selectedMode = "HEX"; showModeDropdown = false })
                        }
                    }

                    // Numeric / Hex Inputs
                    if (selectedMode == "RGB") {
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
                                .padding(horizontal = 6.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("$r", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF202020))
                            Text("$g", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF202020))
                            Text("$b", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF202020))
                            Text("$aPct%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryRed)
                        }
                    } else {
                        OutlinedTextField(
                            value = currentColorHex,
                            onValueChange = {},
                            readOnly = true,
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryRed),
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                        )
                    }
                }

                // 5. Saved Colors Row ("Saved Colors" + Button)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Saved Colors", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF202020))
                        IconButton(
                            onClick = { onAddSavedSwatch(currentColorHex) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(imageVector = Icons.Outlined.Add, contentDescription = "Add Swatch", tint = PrimaryRed, modifier = Modifier.size(18.dp))
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Current active color preview swatch
                        Surface(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape),
                            shape = CircleShape,
                            color = currentColor,
                            border = BorderStroke(2.dp, PrimaryRed)
                        ) {}

                        val swatches = savedSwatches.ifEmpty {
                            listOf("#C91F2C", "#A91521", "#E53945", "#000000", "#FFFFFF", "#1E3A8A", "#065F46", "#D97706", "#8235DC", "#3657FF", "#FF0055", "#00D2FF")
                        }

                        swatches.forEach { hex ->
                            Surface(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .clickable {
                                        try {
                                            val cInt = AndroidColor.parseColor(hex)
                                            val hsv = FloatArray(3)
                                            AndroidColor.colorToHSV(cInt, hsv)
                                            hue = hsv[0]
                                            saturation = hsv[1]
                                            value = hsv[2]
                                            alpha = AndroidColor.alpha(cInt) / 255f
                                        } catch (_: Exception) {}
                                    },
                                shape = CircleShape,
                                color = try { Color(AndroidColor.parseColor(hex)) } catch (_: Exception) { Color.Black },
                                border = BorderStroke(1.dp, Color(0xFFD0D0D0))
                            ) {}
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onColorSelected(currentColorHex)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Select Color", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF777777))
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(16.dp)
    )
}
