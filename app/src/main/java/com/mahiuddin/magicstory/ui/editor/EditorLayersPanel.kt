package com.mahiuddin.magicstory.ui.editor

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DragHandle
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
import com.mahiuddin.magicstory.domain.model.TemplateElement
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
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Layers Stack 🥞", fontWeight = FontWeight.Bold, color = Color(0xFF202020), fontSize = 16.sp)
                Text("${elements.size} Layers", fontSize = 12.sp, color = Color(0xFF777777), fontWeight = FontWeight.Medium)
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.height(380.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(elements) { elem ->
                    val isSelected = editorState.selectedElementId == elem.id
                    val isLocked = editorState.isElementLocked(elem.id)
                    val isHidden = editorState.isElementHidden(elem.id)
                    val label = getElementLabel(elem, elem.id)

                    // Element thumbnail icon
                    val thumbnailIcon = when (elem) {
                        is TemplateElement.Text -> Icons.Outlined.TextFields
                        is TemplateElement.PhotoSlot, is TemplateElement.PhotoCard -> Icons.Outlined.Image
                        is TemplateElement.Shape -> Icons.Outlined.Category
                        else -> Icons.Outlined.Category
                    }

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { viewModel.selectElement(elem.id) },
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) PrimaryRed.copy(alpha = 0.12f) else Color(0xFFF9F9F9),
                        border = BorderStroke(1.dp, if (isSelected) PrimaryRed else Color(0xFFE0E0E0))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Left: Drag handle & Selection Checkbox
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.DragHandle,
                                    contentDescription = "Reorder",
                                    tint = Color(0xFF999999),
                                    modifier = Modifier.size(16.dp)
                                )
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = { viewModel.selectElement(elem.id) },
                                    colors = CheckboxDefaults.colors(checkedColor = PrimaryRed),
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            // Center: Thumbnail preview & Label
                            Surface(
                                modifier = Modifier
                                    .size(36.dp, 30.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = Color.White,
                                border = BorderStroke(1.dp, Color(0xFFCCCCCC))
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = thumbnailIcon,
                                        contentDescription = null,
                                        tint = Color(0xFF404040),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF202020),
                                maxLines = 1,
                                modifier = Modifier.weight(1f)
                            )

                            // Right: PixelLab 2x2 Action Grid (Lock, Edit, Visibility, Delete)
                            Column(
                                verticalArrangement = Arrangement.spacedBy(2.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Lock button
                                    Surface(
                                        onClick = { viewModel.toggleElementLock(elem.id) },
                                        modifier = Modifier.size(26.dp),
                                        shape = RoundedCornerShape(4.dp),
                                        color = if (isLocked) PrimaryRed.copy(alpha = 0.2f) else Color(0xFFEEEEEE)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = if (isLocked) Icons.Outlined.Lock else Icons.Outlined.LockOpen,
                                                contentDescription = "Lock",
                                                tint = if (isLocked) PrimaryRed else Color(0xFF666666),
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }

                                    // Edit button
                                    Surface(
                                        onClick = { viewModel.selectElement(elem.id) },
                                        modifier = Modifier.size(26.dp),
                                        shape = RoundedCornerShape(4.dp),
                                        color = Color(0xFFEEEEEE)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Outlined.Edit,
                                                contentDescription = "Edit",
                                                tint = Color(0xFF666666),
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Visibility button
                                    Surface(
                                        onClick = { viewModel.toggleElementHide(elem.id) },
                                        modifier = Modifier.size(26.dp),
                                        shape = RoundedCornerShape(4.dp),
                                        color = if (isHidden) Color(0xFFDDDDDD) else Color(0xFFEEEEEE)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = if (isHidden) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                                contentDescription = "Visibility",
                                                tint = if (isHidden) Color(0xFF888888) else Color(0xFF666666),
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }

                                    // Delete button
                                    Surface(
                                        onClick = { viewModel.deleteElement(elem.id) },
                                        modifier = Modifier.size(26.dp),
                                        shape = RoundedCornerShape(4.dp),
                                        color = Color(0xFFFFEBEE)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Outlined.Delete,
                                                contentDescription = "Delete",
                                                tint = PrimaryRed,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
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
