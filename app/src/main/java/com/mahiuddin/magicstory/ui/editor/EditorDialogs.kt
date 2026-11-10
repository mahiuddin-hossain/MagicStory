package com.mahiuddin.magicstory.ui.editor

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Icon
import androidx.compose.ui.unit.sp
import com.mahiuddin.magicstory.domain.model.ExportFormat
import com.mahiuddin.magicstory.domain.model.ExportScale
import com.mahiuddin.magicstory.domain.model.GradientConfig
import com.mahiuddin.magicstory.domain.model.MagicStoryEditorState
import com.mahiuddin.magicstory.domain.model.TemplateElement
import com.mahiuddin.magicstory.ui.components.MinimalSlider
import com.mahiuddin.magicstory.ui.theme.PrimaryRed

fun getElementLabel(element: TemplateElement?, elementId: String): String {
    return when (element) {
        is TemplateElement.Text -> "Text Element"
        is TemplateElement.PhotoSlot -> "Photo Slot"
        is TemplateElement.PhotoCard -> {
            when (elementId) {
                element.photoSlotId -> "Photo Slot"
                element.captionId -> "Photo Caption"
                else -> "Photo Card"
            }
        }
        is TemplateElement.PillCta -> {
            when (elementId) {
                element.leftTextId -> "CTA Left Text"
                element.buttonTextId -> "CTA Button Text"
                else -> "Pill CTA"
            }
        }
        is TemplateElement.Shape -> {
            val name = element.shapeType.lowercase()
                .split("_")
                .joinToString(" ") { word -> word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() } }
            "$name Shape"
        }
        is TemplateElement.Background -> "Canvas Background"
        null -> if (elementId == "bg") "Canvas Background" else "Element ($elementId)"
    }
}

@Composable
fun TextEditDialog(
    initialText: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf(initialText) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Text Content", fontWeight = FontWeight.Bold, color = Color(0xFF202020)) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 5,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryRed, cursorColor = PrimaryRed)
            )
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(text) },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Apply", fontWeight = FontWeight.Bold)
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

@Composable
fun TransformPrecisionDialog(
    selectedId: String,
    editorState: MagicStoryEditorState,
    viewModel: EditorViewModel,
    onDismiss: () -> Unit
) {
    val selectedElem = editorState.template.elements.find { it.id == selectedId }
    val curX = editorState.getElementX(selectedId, (selectedElem as? TemplateElement.Shape)?.x ?: 0.1f)
    val curY = editorState.getElementY(selectedId, (selectedElem as? TemplateElement.Shape)?.y ?: 0.1f)
    val curW = editorState.getElementW(selectedId, (selectedElem as? TemplateElement.Shape)?.width ?: 0.3f)
    val curH = editorState.getElementH(selectedId, (selectedElem as? TemplateElement.Shape)?.height ?: 0.3f)
    val curRot = editorState.getElementRotation(selectedId, (selectedElem as? TemplateElement.Shape)?.rotation ?: 0f)

    var posX by remember { mutableFloatStateOf(curX) }
    var posY by remember { mutableFloatStateOf(curY) }
    var width by remember { mutableFloatStateOf(curW) }
    var height by remember { mutableFloatStateOf(curH) }
    var rot by remember { mutableFloatStateOf(curRot) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Transform & Precision Bounding Box", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF202020)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                MinimalSlider(value = posX, onValueChange = { posX = it; viewModel.updateElementPosition(selectedId, posX, posY) }, valueRange = 0f..1f, label = "Position X", valueFormatter = { "%.2f".format(it) })
                MinimalSlider(value = posY, onValueChange = { posY = it; viewModel.updateElementPosition(selectedId, posX, posY) }, valueRange = 0f..1f, label = "Position Y", valueFormatter = { "%.2f".format(it) })
                MinimalSlider(value = width, onValueChange = { width = it; viewModel.updateElementSize(selectedId, width, height) }, valueRange = 0.05f..1f, label = "Width", valueFormatter = { "%.2f".format(it) })
                MinimalSlider(value = height, onValueChange = { height = it; viewModel.updateElementSize(selectedId, width, height) }, valueRange = 0.05f..1f, label = "Height", valueFormatter = { "%.2f".format(it) })
                MinimalSlider(value = rot, onValueChange = { rot = it; viewModel.updateElementRotation(selectedId, rot) }, valueRange = 0f..360f, label = "Rotation Angle", valueFormatter = { "${it.toInt()}°" })
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed), shape = RoundedCornerShape(8.dp)) {
                Text("Done", fontWeight = FontWeight.Bold)
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun ExportOptionsDialog(
    editorState: MagicStoryEditorState,
    viewModel: EditorViewModel,
    onDismiss: () -> Unit,
    onExportConfirmed: () -> Unit
) {
    var format by remember { mutableStateOf(editorState.exportFormat) }
    var scale by remember { mutableStateOf(editorState.exportScale) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Export High-Res Image 💾", fontWeight = FontWeight.Bold, color = Color(0xFF202020)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Export Format", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF202020))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ExportFormat.entries.forEach { fmt ->
                        FilterChip(
                            selected = format == fmt,
                            onClick = {
                                format = fmt
                                viewModel.setExportFormat(fmt)
                            },
                            label = { Text(fmt.name) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = PrimaryRed, selectedLabelColor = Color.White)
                        )
                    }
                }

                Text("Resolution Scaling Multiplier", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF202020))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    ExportScale.entries.forEach { sc ->
                        FilterChip(
                            selected = scale == sc,
                            onClick = {
                                scale = sc
                                viewModel.setExportScale(sc)
                            },
                            label = { Text(sc.label) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = PrimaryRed, selectedLabelColor = Color.White),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onExportConfirmed, colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed), shape = RoundedCornerShape(8.dp)) {
                Text("Export High-Res", fontWeight = FontWeight.Bold)
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

@Composable
fun AdvancedColorPickerDialog(
    initialColorHex: String,
    onColorSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    onOpenEyedropper: () -> Unit
) {
    var hexInput by remember { mutableStateOf(initialColorHex) }
    val presetColors = listOf("#C91F2C", "#A91521", "#E53945", "#000000", "#FFFFFF", "#1E3A8A", "#065F46", "#D97706", "#8235DC", "#3657FF", "#FF0055", "#00D2FF")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Color Palette & Hex Selector 🎨", fontWeight = FontWeight.Bold, color = Color(0xFF202020)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = hexInput,
                    onValueChange = { hexInput = it },
                    label = { Text("Hex Color Code (#RRGGBB)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryRed, cursorColor = PrimaryRed),
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Quick Palette Swatches", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF202020))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presetColors.forEach { colorHex ->
                        Surface(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .clickable { hexInput = colorHex },
                            shape = CircleShape,
                            color = try { Color(android.graphics.Color.parseColor(colorHex)) } catch (_: Exception) { Color.Black },
                            border = BorderStroke(1.5.dp, Color(0xFFD0D0D0))
                        ) {}
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onColorSelected(hexInput) },
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

@Composable
fun CustomGradientPickerDialog(
    initialGradient: GradientConfig?,
    onGradientSelected: (GradientConfig) -> Unit,
    onDismiss: () -> Unit
) {
    var c1 by remember { mutableStateOf(initialGradient?.colors?.getOrNull(0) ?: "#C91F2C") }
    var c2 by remember { mutableStateOf(initialGradient?.colors?.getOrNull(1) ?: "#000000") }
    var angle by remember { mutableFloatStateOf(initialGradient?.angle ?: 45f) }
    var type by remember { mutableStateOf(initialGradient?.type ?: "LINEAR") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Custom Gradient Builder 🌈", fontWeight = FontWeight.Bold, color = Color(0xFF202020)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = type == "LINEAR", onClick = { type = "LINEAR" }, label = { Text("Linear") }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = PrimaryRed, selectedLabelColor = Color.White))
                    FilterChip(selected = type == "RADIAL", onClick = { type = "RADIAL" }, label = { Text("Radial") }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = PrimaryRed, selectedLabelColor = Color.White))
                }

                OutlinedTextField(value = c1, onValueChange = { c1 = it }, label = { Text("Start Color Hex") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = c2, onValueChange = { c2 = it }, label = { Text("End Color Hex") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                MinimalSlider(value = angle, onValueChange = { angle = it }, valueRange = 0f..360f, label = "Direction Angle", valueFormatter = { "${it.toInt()}°" })
            }
        },
        confirmButton = {
            Button(
                onClick = { onGradientSelected(GradientConfig(colors = listOf(c1, c2), angle = angle, type = type)) },
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

@Composable
fun BoxScope.BottomPanelOverlay(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        modifier = modifier
    ) {
        content()
    }
}

@Composable
fun EditorActionIconBtn(
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.size(36.dp),
        shape = RoundedCornerShape(8.dp),
        color = if (selected) PrimaryRed.copy(alpha = 0.15f) else Color(0xFFF5F5F5),
        border = BorderStroke(1.dp, if (selected) PrimaryRed else Color(0xFFE0E0E0))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = if (selected) PrimaryRed else Color(0xFF404040),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
