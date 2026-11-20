package com.mahiuddin.magicstory.ui.editor

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mahiuddin.magicstory.domain.model.GradientConfig
import com.mahiuddin.magicstory.ui.components.MinimalSlider
import com.mahiuddin.magicstory.ui.theme.PrimaryRed

@Composable
fun UniversalGradientPickerDialog(
    initialGradient: GradientConfig?,
    savedSwatches: List<String>,
    onGradientSelected: (GradientConfig) -> Unit,
    onAddSavedSwatch: (String) -> Unit,
    onDismiss: () -> Unit,
    onOpenEyedropper: () -> Unit
) {
    var colors by remember {
        mutableStateOf(
            initialGradient?.colors?.ifEmpty { listOf("#C91F2C", "#000000") }
                ?: listOf("#C91F2C", "#000000")
        )
    }
    var angle by remember { mutableFloatStateOf(initialGradient?.angle ?: 45f) }
    var type by remember { mutableStateOf(initialGradient?.type ?: "LINEAR") }

    var editingColorIndex by remember { mutableStateOf<Int?>(null) }
    var editingColorHex by remember { mutableStateOf("#C91F2C") }

    val presetGradients = listOf(
        GradientConfig(listOf("#C91F2C", "#1A0305"), 45f, "LINEAR"),
        GradientConfig(listOf("#FF416C", "#FF4B2B"), 45f, "LINEAR"),
        GradientConfig(listOf("#00F2FE", "#4FACFE"), 45f, "LINEAR"),
        GradientConfig(listOf("#7F00FF", "#E100FF"), 45f, "LINEAR"),
        GradientConfig(listOf("#11998E", "#38EF7D"), 45f, "LINEAR"),
        GradientConfig(listOf("#0575E6", "#00F6FF"), 45f, "LINEAR"),
        GradientConfig(listOf("#232526", "#414345"), 45f, "LINEAR"),
        GradientConfig(listOf("#F857A6", "#FF5858"), 45f, "LINEAR")
    )

    val composeColors = remember(colors) {
        colors.map { hex ->
            try { Color(AndroidColor.parseColor(hex)) } catch (_: Exception) { Color.Black }
        }
    }

    if (editingColorIndex != null) {
        UniversalColorPickerDialog(
            initialColorHex = editingColorHex,
            savedSwatches = savedSwatches,
            onColorSelected = { selectedHex ->
                val idx = editingColorIndex
                if (idx != null && idx in colors.indices) {
                    val updated = colors.toMutableList()
                    updated[idx] = selectedHex
                    colors = updated
                }
                editingColorIndex = null
            },
            onAddSavedSwatch = onAddSavedSwatch,
            onDismiss = { editingColorIndex = null },
            onOpenEyedropper = onOpenEyedropper
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Universal Gradient Builder 🌈", fontWeight = FontWeight.Bold, color = Color(0xFF202020), fontSize = 16.sp)
                IconButton(onClick = onDismiss, modifier = Modifier.size(26.dp)) {
                    Icon(imageVector = Icons.Outlined.Close, contentDescription = "Close", tint = Color(0xFF777777), modifier = Modifier.size(18.dp))
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // 1. Live Interactive Gradient Preview Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp))
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val brush = when (type.uppercase()) {
                            "RADIAL" -> Brush.radialGradient(
                                colors = composeColors,
                                center = Offset(size.width / 2f, size.height / 2f),
                                radius = minOf(size.width, size.height) / 1.5f
                            )
                            "SWEEP" -> Brush.sweepGradient(
                                colors = composeColors,
                                center = Offset(size.width / 2f, size.height / 2f)
                            )
                            else -> {
                                val rad = Math.toRadians(angle.toDouble())
                                val dx = (Math.cos(rad) * size.width / 2f).toFloat()
                                val dy = (Math.sin(rad) * size.height / 2f).toFloat()
                                val center = Offset(size.width / 2f, size.height / 2f)
                                Brush.linearGradient(
                                    colors = composeColors,
                                    start = center - Offset(dx, dy),
                                    end = center + Offset(dx, dy)
                                )
                            }
                        }
                        drawRect(brush = brush)
                    }
                }

                // 2. Type Selector Tabs & Reverse Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterChip(
                            selected = type == "LINEAR",
                            onClick = { type = "LINEAR" },
                            label = { Text("Linear 📐", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = PrimaryRed, selectedLabelColor = Color.White)
                        )
                        FilterChip(
                            selected = type == "RADIAL",
                            onClick = { type = "RADIAL" },
                            label = { Text("Radial ⭕", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = PrimaryRed, selectedLabelColor = Color.White)
                        )
                        FilterChip(
                            selected = type == "SWEEP",
                            onClick = { type = "SWEEP" },
                            label = { Text("Sweep 🌀", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = PrimaryRed, selectedLabelColor = Color.White)
                        )
                    }

                    IconButton(
                        onClick = { colors = colors.reversed() },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(imageVector = Icons.Outlined.SwapHoriz, contentDescription = "Reverse Colors", tint = PrimaryRed, modifier = Modifier.size(20.dp))
                    }
                }

                // Angle Slider for Linear & Sweep
                if (type != "RADIAL") {
                    MinimalSlider(
                        value = angle,
                        onValueChange = { angle = it },
                        valueRange = 0f..360f,
                        label = "Angle",
                        valueFormatter = { "${it.toInt()}°" }
                    )
                }

                // 3. Gradient Color Stops List with 2D Universal Color Picker
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Color Stops (${colors.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF202020))

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(
                                onClick = {
                                    if (colors.size > 2) {
                                        colors = colors.dropLast(1)
                                    }
                                },
                                enabled = colors.size > 2,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(imageVector = Icons.Outlined.Remove, contentDescription = "Remove Stop", tint = if (colors.size > 2) Color.Red else Color.Gray, modifier = Modifier.size(16.dp))
                            }

                            IconButton(
                                onClick = {
                                    if (colors.size < 6) {
                                        colors = colors + colors.last()
                                    }
                                },
                                enabled = colors.size < 6,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(imageVector = Icons.Outlined.Add, contentDescription = "Add Stop", tint = PrimaryRed, modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        colors.forEachIndexed { index, hex ->
                            Surface(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .clickable {
                                        editingColorHex = hex
                                        editingColorIndex = index
                                    },
                                shape = CircleShape,
                                color = try { Color(AndroidColor.parseColor(hex)) } catch (_: Exception) { Color.Black },
                                border = BorderStroke(2.dp, Color(0xFF202020))
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("${index + 1}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }
                }

                // 4. Gradient Presets Row
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Quick Gradient Presets", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF202020))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        presetGradients.forEach { preset ->
                            val presetColors = preset.colors.map { h -> try { Color(AndroidColor.parseColor(h)) } catch (_: Exception) { Color.Black } }
                            Surface(
                                modifier = Modifier
                                    .size(44.dp, 28.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable {
                                        colors = preset.colors
                                        type = preset.type
                                        angle = preset.angle
                                    },
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, Color(0xFFD0D0D0))
                            ) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    drawRect(brush = Brush.horizontalGradient(colors = presetColors))
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onGradientSelected(
                        GradientConfig(
                            colors = colors,
                            angle = angle,
                            type = type
                        )
                    )
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Apply Gradient", fontWeight = FontWeight.Bold)
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
