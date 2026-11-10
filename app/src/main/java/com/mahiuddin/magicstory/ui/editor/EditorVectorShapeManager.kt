package com.mahiuddin.magicstory.ui.editor

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun EditorVectorShapeManager(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onShapeSelected: (String) -> Unit
) {
    if (!showDialog) return

    val shapes = listOf(
        "RECTANGLE" to "⬛ Rectangle",
        "ROUNDED_RECTANGLE" to "🔲 Rounded Rect",
        "ELLIPSE" to "🔴 Circle / Ellipse",
        "TRIANGLE" to "🔺 Triangle",
        "DIAMOND" to "🔷 Diamond",
        "PENTAGON" to "⬟ Pentagon",
        "HEXAGON" to "⬢ Hexagon",
        "OCTAGON" to "🛑 Octagon",
        "STAR" to "⭐ Star",
        "POLYGON" to "⬡ Custom Polygon"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Insert Vector Shape", fontWeight = FontWeight.Bold, color = Color(0xFF202020)) },
        text = {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.height(280.dp)
            ) {
                items(shapes) { (key, label) ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onShapeSelected(key) },
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFF5F5F5),
                        border = BorderStroke(1.dp, Color(0xFFE0E0E0))
                    ) {
                        Box(modifier = Modifier.padding(12.dp), contentAlignment = Alignment.Center) {
                            Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF202020))
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF777777))
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(16.dp)
    )
}
