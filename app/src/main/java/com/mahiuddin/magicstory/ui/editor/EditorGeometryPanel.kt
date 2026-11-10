package com.mahiuddin.magicstory.ui.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mahiuddin.magicstory.domain.model.TemplateElement
import com.mahiuddin.magicstory.ui.components.MinimalSlider

@Composable
fun GeometryPanel(
    shape: TemplateElement.Shape,
    onUpdateGeometry: (Int, Int, Float) -> Unit
) {
    var sides by remember(shape) { mutableStateOf(shape.polygonSides.toFloat()) }
    var points by remember(shape) { mutableStateOf(shape.starPoints.toFloat()) }
    var ratio by remember(shape) { mutableFloatStateOf(shape.starInnerRadiusRatio) }

    Column(modifier = Modifier.padding(horizontal = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        val shapeType = shape.shapeType.uppercase()
        if (shapeType == "POLYGON" || shapeType == "HEXAGON") {
            MinimalSlider(
                value = sides,
                onValueChange = {
                    sides = it
                    onUpdateGeometry(it.toInt(), points.toInt(), ratio)
                },
                valueRange = 3f..16f,
                label = "Polygon Sides",
                valueFormatter = { "${it.toInt()}" }
            )
        }
        if (shapeType == "STAR") {
            MinimalSlider(
                value = points,
                onValueChange = {
                    points = it
                    onUpdateGeometry(sides.toInt(), it.toInt(), ratio)
                },
                valueRange = 3f..16f,
                label = "Star Points",
                valueFormatter = { "${it.toInt()}" }
            )
            MinimalSlider(
                value = ratio,
                onValueChange = {
                    ratio = it
                    onUpdateGeometry(sides.toInt(), points.toInt(), it)
                },
                valueRange = 0.1f..0.9f,
                label = "Inner Radius Ratio",
                valueFormatter = { "${(it * 100).toInt()}%" }
            )
        }
    }
}
