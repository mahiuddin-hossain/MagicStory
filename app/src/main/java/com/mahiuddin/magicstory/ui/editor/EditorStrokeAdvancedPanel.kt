package com.mahiuddin.magicstory.ui.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Colorize
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mahiuddin.magicstory.domain.model.MagicStoryEditorState
import com.mahiuddin.magicstory.domain.model.TemplateElement
import com.mahiuddin.magicstory.ui.components.MinimalSlider
import com.mahiuddin.magicstory.ui.theme.PrimaryRed

@Composable
fun StrokeAdvancedPanel(
    shape: TemplateElement.Shape,
    editorState: MagicStoryEditorState,
    onUpdateStrokeStyle: (String, String, String, Float) -> Unit,
    onUpdateStrokeWidth: (Float) -> Unit,
    onOpenColorPicker: () -> Unit,
    onOpenEyedropper: () -> Unit
) {
    val currentStroke = editorState.getElementStroke(shape.id) ?: Pair(shape.borderColor ?: "#C91F2C", shape.borderWidth)
    var style by remember(shape) { mutableStateOf(shape.strokeStyle) }
    var cap by remember(shape) { mutableStateOf(shape.strokeCap) }
    var join by remember(shape) { mutableStateOf(shape.strokeJoin) }
    var opacity by remember(shape) { mutableFloatStateOf(shape.strokeOpacity) }

    Column(modifier = Modifier.padding(horizontal = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        MinimalSlider(
            value = currentStroke.second,
            onValueChange = { onUpdateStrokeWidth(it) },
            valueRange = 0f..40f,
            label = "Stroke Width",
            valueFormatter = { "${it.toInt()} dp" }
        )

        Text("Stroke Pattern", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF202020))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("SOLID" to "Solid ━", "DASHED" to "Dashed ╍", "DOTTED" to "Dotted ┈").forEach { (k, l) ->
                FilterChip(
                    selected = style == k,
                    onClick = {
                        style = k
                        onUpdateStrokeStyle(style, cap, join, opacity)
                    },
                    label = { Text(l, fontSize = 10.sp) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = PrimaryRed, selectedLabelColor = Color.White)
                )
            }
        }

        Text("Line Cap", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF202020))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("BUTT" to "Butt", "ROUND" to "Round", "SQUARE" to "Square").forEach { (k, l) ->
                FilterChip(
                    selected = cap == k,
                    onClick = {
                        cap = k
                        onUpdateStrokeStyle(style, cap, join, opacity)
                    },
                    label = { Text(l, fontSize = 10.sp) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = PrimaryRed, selectedLabelColor = Color.White)
                )
            }
        }

        Text("Line Join", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF202020))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("MITER" to "Miter", "ROUND" to "Round", "BEVEL" to "Bevel").forEach { (k, l) ->
                FilterChip(
                    selected = join == k,
                    onClick = {
                        join = k
                        onUpdateStrokeStyle(style, cap, join, opacity)
                    },
                    label = { Text(l, fontSize = 10.sp) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = PrimaryRed, selectedLabelColor = Color.White)
                )
            }
        }

        MinimalSlider(
            value = opacity,
            onValueChange = {
                opacity = it
                onUpdateStrokeStyle(style, cap, join, opacity)
            },
            valueRange = 0f..1f,
            label = "Stroke Opacity",
            valueFormatter = { "${(it * 100).toInt()}%" }
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Button(
                onClick = onOpenColorPicker,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(imageVector = Icons.Outlined.Palette, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Pick Stroke Color", fontSize = 11.sp)
            }
            IconButton(onClick = onOpenEyedropper) {
                Icon(imageVector = Icons.Outlined.Colorize, contentDescription = "Eyedropper", tint = PrimaryRed, modifier = Modifier.size(18.dp))
            }
        }
    }
}
