package com.mahiuddin.magicstory.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.ChangeHistory
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.outlined.CropDin
import androidx.compose.material.icons.outlined.Diamond
import androidx.compose.material.icons.outlined.Polyline
import androidx.compose.material.icons.outlined.RoundedCorner
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun EditorVectorShapeManager(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onShapeSelected: (String) -> Unit
) {
    val shapes = listOf(
        Triple("RECTANGLE", "Rectangle", Icons.Outlined.CropDin),
        Triple("ROUNDED_RECTANGLE", "Rounded Rect", Icons.Outlined.RoundedCorner),
        Triple("ELLIPSE", "Circle / Ellipse", Icons.Outlined.Circle),
        Triple("TRIANGLE", "Triangle", Icons.Outlined.ChangeHistory),
        Triple("DIAMOND", "Diamond", Icons.Outlined.Diamond),
        Triple("PENTAGON", "Pentagon", Icons.Outlined.Category),
        Triple("HEXAGON", "Hexagon", Icons.Outlined.Category),
        Triple("OCTAGON", "Octagon", Icons.Outlined.Category),
        Triple("STAR", "Star", Icons.Outlined.Star),
        Triple("POLYGON", "Custom Polygon", Icons.Outlined.Polyline)
    )

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = Modifier.background(Color.White)
    ) {
        shapes.forEach { (key, label, icon) ->
            DropdownMenuItem(
                text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            tint = Color(0xFF202020),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = label,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = Color(0xFF202020)
                        )
                    }
                },
                onClick = {
                    onShapeSelected(key)
                    onDismiss()
                }
            )
        }
    }
}
