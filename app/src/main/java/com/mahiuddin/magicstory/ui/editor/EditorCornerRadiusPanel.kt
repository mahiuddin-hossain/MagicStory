package com.mahiuddin.magicstory.ui.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import com.mahiuddin.magicstory.domain.model.TemplateElement
import com.mahiuddin.magicstory.ui.components.MinimalSlider
import com.mahiuddin.magicstory.ui.theme.PrimaryRed

@Composable
fun CornerRadiusPanel(
    shape: TemplateElement.Shape,
    onUpdateSingleRadius: (Float) -> Unit,
    onUpdateIndividualRadii: (Float, Float, Float, Float, Boolean) -> Unit
) {
    var isLinked by remember(shape) { mutableStateOf(shape.isCornerLinked) }
    var singleRadius by remember(shape) { mutableFloatStateOf(if (shape.cornerRadius > 0f) shape.cornerRadius else 16f) }
    var tl by remember(shape) { mutableFloatStateOf(shape.topLeftRadius) }
    var tr by remember(shape) { mutableFloatStateOf(shape.topRightRadius) }
    var br by remember(shape) { mutableFloatStateOf(shape.bottomRightRadius) }
    var bl by remember(shape) { mutableFloatStateOf(shape.bottomLeftRadius) }

    Column(modifier = Modifier.padding(horizontal = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Corner Radius Mode", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF202020))
            Row(verticalAlignment = Alignment.CenterVertically) {
                FilterChip(
                    selected = isLinked,
                    onClick = {
                        isLinked = true
                        onUpdateSingleRadius(singleRadius)
                    },
                    label = { Text("Uniform 🔗", fontSize = 10.sp) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = PrimaryRed, selectedLabelColor = Color.White)
                )
                Spacer(modifier = Modifier.width(6.dp))
                FilterChip(
                    selected = !isLinked,
                    onClick = {
                        isLinked = false
                        onUpdateIndividualRadii(tl, tr, br, bl, false)
                    },
                    label = { Text("Individual 🔓", fontSize = 10.sp) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = PrimaryRed, selectedLabelColor = Color.White)
                )
            }
        }

        if (isLinked) {
            MinimalSlider(
                value = singleRadius,
                onValueChange = {
                    singleRadius = it
                    onUpdateSingleRadius(it)
                },
                valueRange = 0f..80f,
                label = "All Corners",
                valueFormatter = { "${it.toInt()} dp" }
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                MinimalSlider(
                    value = tl,
                    onValueChange = {
                        tl = it
                        onUpdateIndividualRadii(tl, tr, br, bl, false)
                    },
                    valueRange = 0f..80f,
                    label = "Top-Left",
                    valueFormatter = { "${it.toInt()} dp" }
                )
                MinimalSlider(
                    value = tr,
                    onValueChange = {
                        tr = it
                        onUpdateIndividualRadii(tl, tr, br, bl, false)
                    },
                    valueRange = 0f..80f,
                    label = "Top-Right",
                    valueFormatter = { "${it.toInt()} dp" }
                )
                MinimalSlider(
                    value = br,
                    onValueChange = {
                        br = it
                        onUpdateIndividualRadii(tl, tr, br, bl, false)
                    },
                    valueRange = 0f..80f,
                    label = "Bottom-Right",
                    valueFormatter = { "${it.toInt()} dp" }
                )
                MinimalSlider(
                    value = bl,
                    onValueChange = {
                        bl = it
                        onUpdateIndividualRadii(tl, tr, br, bl, false)
                    },
                    valueRange = 0f..80f,
                    label = "Bottom-Left",
                    valueFormatter = { "${it.toInt()} dp" }
                )
            }
        }
    }
}
