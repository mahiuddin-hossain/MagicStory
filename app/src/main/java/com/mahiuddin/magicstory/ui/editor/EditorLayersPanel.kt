package com.mahiuddin.magicstory.ui.editor

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.DragHandle
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mahiuddin.magicstory.domain.model.MagicStoryEditorState
import com.mahiuddin.magicstory.ui.theme.PrimaryRed

@Composable
fun EditorLayersPanel(
    editorState: MagicStoryEditorState,
    viewModel: EditorViewModel,
    onDismiss: () -> Unit
) {
    val elements = editorState.template.elements.sortedByDescending { editorState.getElementZIndex(it.id, it.zIndex) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("PixelLab Layers Stack 🥞", fontWeight = FontWeight.Bold, color = Color(0xFF202020)) },
        text = {
            LazyColumn(
                modifier = Modifier.height(300.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(elements) { elem ->
                    val isSelected = editorState.selectedElementId == elem.id
                    val isLocked = editorState.isElementLocked(elem.id)
                    val isHidden = editorState.isElementHidden(elem.id)
                    val label = getElementLabel(elem, elem.id)

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { viewModel.selectElement(elem.id) },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) PrimaryRed.copy(alpha = 0.12f) else Color(0xFFF9F9F9),
                        border = BorderStroke(1.dp, if (isSelected) PrimaryRed else Color(0xFFE5E5E5))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(imageVector = Icons.Outlined.DragHandle, contentDescription = null, tint = Color(0xFF999999), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF202020), maxLines = 1)
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { viewModel.toggleElementLock(elem.id) }, modifier = Modifier.size(28.dp)) {
                                    Icon(imageVector = if (isLocked) Icons.Outlined.Lock else Icons.Outlined.LockOpen, contentDescription = "Lock", tint = if (isLocked) PrimaryRed else Color(0xFF777777), modifier = Modifier.size(18.dp))
                                }
                                IconButton(onClick = { viewModel.toggleElementHide(elem.id) }, modifier = Modifier.size(28.dp)) {
                                    Icon(imageVector = if (isHidden) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility, contentDescription = "Visibility", tint = if (isHidden) Color(0xFFBBBBBB) else Color(0xFF777777), modifier = Modifier.size(18.dp))
                                }
                                IconButton(onClick = { viewModel.moveElementZOrder(elem.id, ZOrderAction.MOVE_UP) }, modifier = Modifier.size(28.dp)) {
                                    Icon(imageVector = Icons.Outlined.ArrowUpward, contentDescription = "Move Up", tint = Color(0xFF333333), modifier = Modifier.size(18.dp))
                                }
                                IconButton(onClick = { viewModel.moveElementZOrder(elem.id, ZOrderAction.MOVE_DOWN) }, modifier = Modifier.size(28.dp)) {
                                    Icon(imageVector = Icons.Outlined.ArrowDownward, contentDescription = "Move Down", tint = Color(0xFF333333), modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed), shape = RoundedCornerShape(8.dp)) {
                Text("Close", fontWeight = FontWeight.Bold)
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(16.dp)
    )
}
