package com.mahiuddin.magicstory.ui.editor

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mahiuddin.magicstory.domain.model.ExportResolution
import com.mahiuddin.magicstory.domain.model.MagicStoryEditorState
import com.mahiuddin.magicstory.domain.model.TemplateElement
import com.mahiuddin.magicstory.ui.components.ExportSuccessDialog
import com.mahiuddin.magicstory.ui.components.PhotoPickerButton
import com.mahiuddin.magicstory.ui.components.TemplateCanvasPreview
import com.mahiuddin.magicstory.ui.components.TextEditField
import com.mahiuddin.magicstory.ui.theme.MainBackground
import com.mahiuddin.magicstory.ui.theme.MainText
import com.mahiuddin.magicstory.ui.theme.PrimaryRed
import com.mahiuddin.magicstory.ui.theme.SecondaryBackground
import com.mahiuddin.magicstory.ui.theme.SecondaryText
import com.mahiuddin.magicstory.ui.theme.SubtleBorder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    templateId: String,
    viewModel: EditorViewModel,
    onBackPress: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val editorState by viewModel.state.collectAsState()

    LaunchedEffect(templateId) {
        viewModel.loadTemplate(templateId)
    }

    var selectedTab by remember { mutableIntStateOf(0) }
    var showResetDialog by remember { mutableStateOf(false) }

    val state = editorState

    if (state == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = PrimaryRed)
        }
        return
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = SecondaryBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = state.template.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MainText
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackPress) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MainText)
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.undo() },
                        enabled = state.canUndo
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Undo,
                            contentDescription = "Undo",
                            tint = if (state.canUndo) MainText else SecondaryText.copy(alpha = 0.4f)
                        )
                    }
                    IconButton(
                        onClick = { viewModel.redo() },
                        enabled = state.canRedo
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Redo,
                            contentDescription = "Redo",
                            tint = if (state.canRedo) MainText else SecondaryText.copy(alpha = 0.4f)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Button(
                        onClick = { viewModel.exportImage(context) },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Export", fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MainBackground)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main Interactive Canvas Preview (Supports Tap & Drag)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                TemplateCanvasPreview(
                    manifest = state.template,
                    editorState = state,
                    modifier = Modifier.fillMaxSize(),
                    isInteractive = true,
                    onElementSelect = { viewModel.selectElement(it) },
                    onElementDrag = { id, x, y -> viewModel.updateElementPosition(id, x, y) },
                    onDragEnd = { viewModel.commitDragEnd() }
                )

                // Drag instruction badge overlay
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .clip(CircleShape)
                        .background(MainText.copy(alpha = 0.75f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.TouchApp,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (state.selectedElementId != null) "Drag to move element" else "Tap or drag any element to move",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                if (state.isExporting) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = Color.White)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Exporting High-Res Design...", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Bottom Controls Panel
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(MainBackground)
                    .padding(bottom = 12.dp)
            ) {
                val tabs = listOf("Replace Photo", "Edit Text", "Format", "Reset")

                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MainBackground,
                    contentColor = PrimaryRed,
                    indicator = { tabPositions ->
                        if (selectedTab < tabPositions.size) {
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                color = PrimaryRed
                            )
                        }
                    }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = {
                                if (index == 3) {
                                    showResetDialog = true
                                } else {
                                    selectedTab = index
                                }
                            },
                            text = {
                                Text(
                                    text = title,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selectedTab == index) PrimaryRed else SecondaryText,
                                    fontSize = 13.sp
                                )
                            }
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(230.dp)
                        .padding(16.dp)
                ) {
                    when (selectedTab) {
                        0 -> ReplacePhotoTab(state = state, viewModel = viewModel)
                        1 -> EditTextTab(state = state, viewModel = viewModel)
                        2 -> ExportFormatTab(state = state, viewModel = viewModel)
                    }
                }
            }
        }
    }

    // Export Success Dialog
    state.exportResultUri?.let { uriString ->
        ExportSuccessDialog(
            imageUriString = uriString,
            onDismiss = { viewModel.clearExportResult() }
        )
    }

    // Reset Confirmation Dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset to Original Design?", fontWeight = FontWeight.Bold) },
            text = { Text("This will restore the template's default positions, photos, and text edits.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetToDefaults()
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed)
                ) {
                    Text("Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel", color = MainText)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
private fun ReplacePhotoTab(
    state: MagicStoryEditorState,
    viewModel: EditorViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val photoElements = state.template.elements.filter {
            it is TemplateElement.PhotoSlot || it is TemplateElement.PhotoCard
        }

        if (photoElements.isEmpty()) {
            Text("This template has no photo replacement slots.", color = SecondaryText)
        } else {
            photoElements.forEach { element ->
                val (slotId, label) = when (element) {
                    is TemplateElement.PhotoSlot -> element.id to (element.label ?: "Photo Frame")
                    is TemplateElement.PhotoCard -> element.photoSlotId to "Central Photo Card Image"
                    else -> "" to "Photo Slot"
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SecondaryBackground)
                        .padding(12.dp)
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MainText
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    PhotoPickerButton(
                        label = "Choose Image from Gallery",
                        onPhotoSelected = { uri ->
                            viewModel.updatePhoto(slotId, uri)
                        }
                    )
                }
            }
        }
    }
}

data class TextEditItem(
    val id: String,
    val label: String,
    val defaultText: String,
    val defaultFontSize: Float
)

@Composable
private fun EditTextTab(
    state: MagicStoryEditorState,
    viewModel: EditorViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        val textEditableFields = mutableListOf<TextEditItem>()

        state.template.elements.forEach { element ->
            when (element) {
                is TemplateElement.Text -> {
                    if (element.editable) {
                        textEditableFields.add(
                            TextEditItem(
                                element.id,
                                element.label ?: "Text Element (${element.id})",
                                element.defaultText,
                                element.fontSize
                            )
                        )
                    }
                }
                is TemplateElement.PhotoCard -> {
                    textEditableFields.add(
                        TextEditItem(
                            element.captionId,
                            "Card Script Caption",
                            element.captionDefaultText,
                            element.captionFontSize
                        )
                    )
                }
                is TemplateElement.PillCta -> {
                    textEditableFields.add(
                        TextEditItem(
                            element.leftTextId,
                            "CTA Left Label",
                            element.leftDefaultText,
                            element.leftFontSize
                        )
                    )
                    textEditableFields.add(
                        TextEditItem(
                            element.buttonTextId,
                            "CTA Button Label",
                            element.buttonDefaultText,
                            element.buttonFontSize
                        )
                    )
                }
                else -> {}
            }
        }

        if (textEditableFields.isEmpty()) {
            Text("No editable text fields found.", color = SecondaryText)
        } else {
            textEditableFields.forEach { (id, label, defaultText, defaultFontSize) ->
                val currentValue = state.getTextValue(id, defaultText)
                val currentFontSize = state.getFontSize(id, defaultFontSize)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SecondaryBackground)
                        .padding(12.dp)
                ) {
                    TextEditField(
                        label = label,
                        value = currentValue,
                        onValueChange = { newValue ->
                            viewModel.updateText(id, newValue)
                        }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Text Size: ${currentFontSize.toInt()} sp",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MainText
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedIconButton(
                                onClick = {
                                    val newSize = (currentFontSize - 2f).coerceAtLeast(8f)
                                    viewModel.updateFontSize(id, newSize)
                                },
                                modifier = Modifier.size(36.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Remove, contentDescription = "Decrease text size", tint = MainText)
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            OutlinedIconButton(
                                onClick = {
                                    val newSize = (currentFontSize + 2f).coerceAtMost(160f)
                                    viewModel.updateFontSize(id, newSize)
                                },
                                modifier = Modifier.size(36.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = "Increase text size", tint = MainText)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExportFormatTab(
    state: MagicStoryEditorState,
    viewModel: EditorViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Target Output Resolution",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MainText
        )

        ExportResolution.entries.forEach { res ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (state.exportResolution == res) SecondaryBackground else Color.Transparent)
                    .clickable { viewModel.setExportResolution(res) }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = state.exportResolution == res,
                    onClick = { viewModel.setExportResolution(res) },
                    colors = RadioButtonDefaults.colors(selectedColor = PrimaryRed)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = res.label,
                        fontWeight = FontWeight.Bold,
                        color = MainText
                    )
                    Text(
                        text = "${res.width} x ${res.height} px",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SecondaryText
                    )
                }
            }
        }
    }
}
