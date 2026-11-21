package com.mahiuddin.magicstory.ui.editor

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Redo
import androidx.compose.material.icons.automirrored.outlined.Undo
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.AspectRatio
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.mahiuddin.magicstory.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mahiuddin.magicstory.domain.model.ExportResolution
import com.mahiuddin.magicstory.ui.theme.PrimaryRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorTopAppBar(
    title: String,
    onBackPress: () -> Unit,
    onOpenExportDialog: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF202020),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackPress) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFF202020),
                    modifier = Modifier.size(20.dp)
                )
            }
        },
        actions = {
            // Export / Save
            IconButton(onClick = onOpenExportDialog) {
                Icon(
                    imageVector = Icons.Outlined.Save,
                    contentDescription = "Save Image",
                    tint = PrimaryRed,
                    modifier = Modifier.size(20.dp)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
    )
}

@Composable
fun EditorSecondaryQuickToolbar(
    canUndo: Boolean,
    canRedo: Boolean,
    hasCopiedItem: Boolean = false,
    onPaste: () -> Unit = {},
    showShapeMenu: Boolean,
    onOpenShapeLibrary: () -> Unit,
    onDismissShapeMenu: () -> Unit,
    onShapeSelected: (String) -> Unit,
    showTextMenu: Boolean,
    onOpenTextMenu: () -> Unit,
    onDismissTextMenu: () -> Unit,
    onAddHorizontalTextClicked: () -> Unit,
    onAddVerticalTextClicked: () -> Unit,
    onInsertImage: () -> Unit,
    onSetExportResolution: (ExportResolution) -> Unit,
    onOpenLayersDrawer: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit
) {
    var showAspectMenu by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(38.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFEAEAEA))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Insert Vector Shape Library Button with Popup Dropdown
            Box {
                IconButton(onClick = onOpenShapeLibrary, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Outlined.Category,
                        contentDescription = "Insert Shape",
                        tint = Color(0xFF202020),
                        modifier = Modifier.size(18.dp)
                    )
                }
                EditorVectorShapeManager(
                    expanded = showShapeMenu,
                    onDismiss = onDismissShapeMenu,
                    onShapeSelected = onShapeSelected
                )
            }

            // 2. Insert Image Button
            IconButton(onClick = onInsertImage, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = Icons.Outlined.AddPhotoAlternate,
                    contentDescription = "Insert Image",
                    tint = Color(0xFF202020),
                    modifier = Modifier.size(18.dp)
                )
            }

            // 3. Add Text Button with Popup Dropdown (Horizontal / Vertical)
            Box {
                IconButton(onClick = onOpenTextMenu, modifier = Modifier.size(32.dp)) {
                    Icon(
                        painter = painterResource(id = R.drawable.add_text_icon),
                        contentDescription = "Add Text",
                        tint = Color(0xFF202020),
                        modifier = Modifier.size(18.dp)
                    )
                }
                EditorAddTextDropdownMenu(
                    expanded = showTextMenu,
                    onDismiss = onDismissTextMenu,
                    onAddHorizontalTextClicked = onAddHorizontalTextClicked,
                    onAddVerticalTextClicked = onAddVerticalTextClicked
                )
            }

            // Group Separator
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .width(1.dp)
                    .height(14.dp)
                    .background(Color(0xFFE5E5E5))
            )

            // 4. Canvas Size Aspect Ratio Selector
            Box {
                IconButton(onClick = { showAspectMenu = true }, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Outlined.AspectRatio,
                        contentDescription = "Canvas Size",
                        tint = Color(0xFF202020),
                        modifier = Modifier.size(18.dp)
                    )
                }
                DropdownMenu(
                    expanded = showAspectMenu,
                    onDismissRequest = { showAspectMenu = false },
                    modifier = Modifier.background(Color.White)
                ) {
                    ExportResolution.entries.forEach { res ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(text = res.label, fontWeight = FontWeight.Bold, color = Color(0xFF202020))
                                    Text(text = "${res.width}x${res.height} px", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF777777))
                                }
                            },
                            onClick = {
                                onSetExportResolution(res)
                                showAspectMenu = false
                            }
                        )
                    }
                }
            }

            // 5. Layers Drawer Button
            IconButton(onClick = onOpenLayersDrawer, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = Icons.Outlined.Layers,
                    contentDescription = "Layers Drawer",
                    tint = Color(0xFF202020),
                    modifier = Modifier.size(18.dp)
                )
            }

            // 6. Dynamic Paste Button (Visible ONLY when an element has been copied!)
            if (hasCopiedItem) {
                IconButton(onClick = onPaste, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Outlined.ContentPaste,
                        contentDescription = "Paste Element",
                        tint = PrimaryRed,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Group Separator
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .width(1.dp)
                    .height(14.dp)
                    .background(Color(0xFFE5E5E5))
            )

            // 7. Undo / Backward
            IconButton(onClick = onUndo, enabled = canUndo, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Undo,
                    contentDescription = "Undo",
                    tint = if (canUndo) Color(0xFF202020) else Color(0xFFBDBDBD),
                    modifier = Modifier.size(18.dp)
                )
            }

            // 8. Redo / Forward
            IconButton(onClick = onRedo, enabled = canRedo, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Redo,
                    contentDescription = "Redo",
                    tint = if (canRedo) Color(0xFF202020) else Color(0xFFBDBDBD),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
