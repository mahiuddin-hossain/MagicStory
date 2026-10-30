package com.mahiuddin.magicstory.ui.editor

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.automirrored.filled.FormatAlignLeft
import androidx.compose.material.icons.automirrored.filled.FormatAlignRight
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.FormatAlignCenter
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material.icons.filled.FormatLineSpacing
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Texture
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mahiuddin.magicstory.domain.model.ExportFormat
import com.mahiuddin.magicstory.domain.model.ExportResolution
import com.mahiuddin.magicstory.domain.model.ExportScale
import com.mahiuddin.magicstory.domain.model.MagicStoryEditorState
import com.mahiuddin.magicstory.domain.model.TemplateElement
import com.mahiuddin.magicstory.domain.model.TemplateManifest
import com.mahiuddin.magicstory.template.renderer.TemplateRenderer
import com.mahiuddin.magicstory.ui.components.ExportSuccessDialog
import com.mahiuddin.magicstory.ui.components.TemplateCanvasPreview
import com.mahiuddin.magicstory.ui.theme.MainBackground
import com.mahiuddin.magicstory.ui.theme.MainText
import com.mahiuddin.magicstory.ui.theme.PrimaryRed
import com.mahiuddin.magicstory.ui.theme.SecondaryBackground
import com.mahiuddin.magicstory.ui.theme.SecondaryText
import com.mahiuddin.magicstory.ui.theme.SubtleBorder

enum class ActiveSubTool {
    NONE,
    FONT_SIZE,
    TEXT_COLOR,
    TEXT_SPACING,
    TEXT_BG,
    BG_TEXTURE,
    ROTATE
}

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

    var showAspectMenu by remember { mutableStateOf(false) }
    var showLayersDrawer by remember { mutableStateOf(false) }
    var showExportOptionsDialog by remember { mutableStateOf(false) }
    var activeSubTool by remember { mutableStateOf(ActiveSubTool.NONE) }
    var textEditDialogVisible by remember { mutableStateOf(false) }

    val state = editorState

    if (state == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = PrimaryRed)
        }
        return
    }

    var targetPhotoSlotId by remember { mutableStateOf<String?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            targetPhotoSlotId?.let { slotId ->
                viewModel.updatePhoto(slotId, it)
            }
        }
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
                        color = MainText,
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackPress) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MainText)
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { showAspectMenu = true }) {
                            Icon(imageVector = Icons.Default.AspectRatio, contentDescription = "Canvas Size", tint = PrimaryRed)
                        }
                        DropdownMenu(
                            expanded = showAspectMenu,
                            onDismissRequest = { showAspectMenu = false },
                            modifier = Modifier.background(MainBackground)
                        ) {
                            ExportResolution.entries.forEach { res ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(text = res.label, fontWeight = FontWeight.Bold, color = MainText)
                                            Text(text = "${res.width}x${res.height} px", style = MaterialTheme.typography.bodyMedium, color = SecondaryText)
                                        }
                                    },
                                    onClick = {
                                        viewModel.setExportResolution(res)
                                        showAspectMenu = false
                                    }
                                )
                            }
                        }
                    }

                    IconButton(onClick = { showLayersDrawer = true }) {
                        Icon(imageVector = Icons.Default.Layers, contentDescription = "Layers Drawer", tint = MainText)
                    }

                    IconButton(onClick = { viewModel.undo() }, enabled = state.canUndo) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.Undo, contentDescription = "Undo", tint = if (state.canUndo) MainText else SecondaryText.copy(alpha = 0.4f))
                    }
                    IconButton(onClick = { viewModel.redo() }, enabled = state.canRedo) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.Redo, contentDescription = "Redo", tint = if (state.canRedo) MainText else SecondaryText.copy(alpha = 0.4f))
                    }
                    IconButton(onClick = { showExportOptionsDialog = true }) {
                        Icon(imageVector = Icons.Default.Download, contentDescription = "Export Image", tint = PrimaryRed)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MainBackground)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Main Canvas Workspace
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clickable {
                            viewModel.selectElement(null)
                            activeSubTool = ActiveSubTool.NONE
                        }
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    TemplateCanvasPreview(
                        manifest = state.template,
                        editorState = state,
                        modifier = Modifier.fillMaxSize(),
                        isInteractive = true,
                        onElementSelect = { id ->
                            viewModel.selectElement(id)
                            activeSubTool = ActiveSubTool.NONE
                        },
                        onElementDrag = { id, x, y -> viewModel.updateElementPosition(id, x, y) },
                        onElementResize = { id, w, h -> viewModel.updateElementSize(id, w, h) },
                        onElementRotate = { id, rot -> viewModel.updateElementRotation(id, rot) },
                        onSnappingGuides = { viewModel.setSnappingGuideLines(it) },
                        onDragEnd = { viewModel.commitGestureEnd() }
                    )

                    // Contextual Canvas Top Hint
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
                                text = if (state.selectedElementId != null) "Drag to move | Pinch/Corners to resize | Top ↻ to rotate" else "Tap any element to select & edit",
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
                                Text("Exporting High-Res Image...", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Canva-Style Dynamic Contextual Bottom Panel
                AnimatedVisibility(
                    visible = state.selectedElementId != null,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                ) {
                    state.selectedElementId?.let { selectedId ->
                        val selectedElem = state.template.elements.find {
                            it.id == selectedId || (it is TemplateElement.PhotoCard && (it.photoSlotId == selectedId || it.captionId == selectedId)) || (it is TemplateElement.PillCta && (it.leftTextId == selectedId || it.buttonTextId == selectedId))
                        }

                        val isLocked = state.isElementLocked(selectedId)

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                                .background(MainBackground)
                                .padding(top = 10.dp, bottom = 16.dp, start = 12.dp, end = 12.dp)
                        ) {
                            // Header Bar: Element Label, Lock Toggle, Duplicate, Delete
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val label = getElementLabel(selectedElem, selectedId)
                                Text(
                                    text = if (isLocked) "$label 🔒" else label,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryRed
                                )

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = { viewModel.toggleElementLock(selectedId) }) {
                                        Icon(
                                            imageVector = if (isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                                            contentDescription = "Lock Element",
                                            tint = if (isLocked) PrimaryRed else MainText
                                        )
                                    }
                                    IconButton(onClick = { viewModel.duplicateElement(selectedId) }) {
                                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Duplicate", tint = MainText)
                                    }
                                    IconButton(onClick = { viewModel.deleteElement(selectedId) }) {
                                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = PrimaryRed)
                                    }
                                    IconButton(onClick = {
                                        viewModel.selectElement(null)
                                        activeSubTool = ActiveSubTool.NONE
                                    }) {
                                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = SecondaryText)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Sub-Tool Expansion Panel
                            when (activeSubTool) {
                                ActiveSubTool.FONT_SIZE -> {
                                    val currentSize = state.getFontSize(selectedId, 24f)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        OutlinedIconButton(
                                            onClick = {
                                                val newSize = (currentSize - 4f).coerceAtLeast(0f)
                                                viewModel.updateFontSize(selectedId, newSize)
                                                viewModel.commitGestureEnd()
                                            },
                                            modifier = Modifier.size(36.dp),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.Remove, contentDescription = "Decrease text size", tint = MainText)
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Text(
                                            text = "${currentSize.toInt()} sp",
                                            fontWeight = FontWeight.Bold,
                                            color = MainText,
                                            modifier = Modifier.width(64.dp),
                                            fontSize = 13.sp
                                        )

                                        Spacer(modifier = Modifier.width(4.dp))

                                        Slider(
                                            value = currentSize.coerceIn(0f, 500f),
                                            onValueChange = { viewModel.updateFontSize(selectedId, it) },
                                            onValueChangeFinished = { viewModel.commitGestureEnd() },
                                            valueRange = 0f..500f,
                                            colors = SliderDefaults.colors(
                                                thumbColor = PrimaryRed,
                                                activeTrackColor = PrimaryRed,
                                                inactiveTrackColor = SubtleBorder
                                            ),
                                            modifier = Modifier.weight(1f)
                                        )

                                        Spacer(modifier = Modifier.width(4.dp))

                                        OutlinedIconButton(
                                            onClick = {
                                                val newSize = (currentSize + 4f).coerceAtMost(500f)
                                                viewModel.updateFontSize(selectedId, newSize)
                                                viewModel.commitGestureEnd()
                                            },
                                            modifier = Modifier.size(36.dp),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.Add, contentDescription = "Increase text size", tint = MainText)
                                        }
                                    }
                                }
                                ActiveSubTool.TEXT_COLOR -> {
                                    val colorPalette = listOf(
                                        "#000000", "#FFFFFF", "#C91F2C", "#A91521", "#E53945",
                                        "#211D1E", "#777174", "#FFF7F7", "#1E3A8A", "#065F46", "#D97706"
                                    )
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState())
                                            .padding(horizontal = 8.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        colorPalette.forEach { colorHex ->
                                            Surface(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(CircleShape)
                                                    .clickable { viewModel.updateTextColor(selectedId, colorHex) },
                                                shape = CircleShape,
                                                color = Color(android.graphics.Color.parseColor(colorHex)),
                                                border = BorderStroke(1.5.dp, SubtleBorder)
                                            ) {}
                                        }
                                    }
                                }
                                ActiveSubTool.TEXT_SPACING -> {
                                    val currentLetterSpacing = state.getLetterSpacing(selectedId, 0f)
                                    val currentLineSpacing = state.getLineSpacing(selectedId, 1.0f)
                                    Column(modifier = Modifier.padding(horizontal = 8.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("Letter: ${(currentLetterSpacing * 100).toInt()}", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(80.dp))
                                            Slider(
                                                value = currentLetterSpacing,
                                                onValueChange = { viewModel.updateTextLetterSpacing(selectedId, it) },
                                                onValueChangeFinished = { viewModel.commitGestureEnd() },
                                                valueRange = 0.0f..0.4f,
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("Line: ${"%.1f".format(currentLineSpacing)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(80.dp))
                                            Slider(
                                                value = currentLineSpacing,
                                                onValueChange = { viewModel.updateTextLineSpacing(selectedId, it) },
                                                onValueChangeFinished = { viewModel.commitGestureEnd() },
                                                valueRange = 0.8f..2.0f,
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                                }
                                ActiveSubTool.TEXT_BG -> {
                                    val bgColors = listOf("#C91F2C", "#A91521", "#211D1E", "#000000", "#FFF7F7", "#D9E0D8", "#EAE4DC")
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState())
                                            .padding(horizontal = 8.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Surface(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .clickable { viewModel.updateTextBgColor(selectedId, null) },
                                            shape = CircleShape,
                                            color = Color.Transparent,
                                            border = BorderStroke(1.5.dp, PrimaryRed)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(imageVector = Icons.Default.Close, contentDescription = "Clear", tint = PrimaryRed, modifier = Modifier.size(18.dp))
                                            }
                                        }

                                        bgColors.forEach { colorHex ->
                                            Surface(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(CircleShape)
                                                    .clickable { viewModel.updateTextBgColor(selectedId, colorHex) },
                                                shape = CircleShape,
                                                color = Color(android.graphics.Color.parseColor(colorHex)),
                                                border = BorderStroke(1.5.dp, SubtleBorder)
                                            ) {}
                                        }
                                    }
                                }
                                ActiveSubTool.BG_TEXTURE -> {
                                    val textures = listOf("NONE", "PAPER", "NOISE", "CANVAS")
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 8.dp, vertical = 4.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        textures.forEach { tex ->
                                            FilterChip(
                                                selected = state.bgTextureType == tex,
                                                onClick = { viewModel.updateBgTexture(tex) },
                                                label = { Text(tex) },
                                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = PrimaryRed, selectedLabelColor = Color.White)
                                            )
                                        }
                                    }
                                }
                                ActiveSubTool.ROTATE -> {
                                    val currentRot = state.getElementRotation(selectedId, 0f)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${currentRot.toInt()}°",
                                            fontWeight = FontWeight.Bold,
                                            color = MainText,
                                            modifier = Modifier.width(60.dp)
                                        )
                                        Slider(
                                            value = currentRot,
                                            onValueChange = { viewModel.updateElementRotation(selectedId, it) },
                                            onValueChangeFinished = { viewModel.commitGestureEnd() },
                                            valueRange = 0f..360f,
                                            colors = SliderDefaults.colors(
                                                thumbColor = PrimaryRed,
                                                activeTrackColor = PrimaryRed,
                                                inactiveTrackColor = SubtleBorder
                                            ),
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                                else -> {}
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Type-Specific Icon Action Row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                                    .padding(horizontal = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isImageElement(selectedElem, selectedId)) {
                                    val slotId = getPhotoSlotId(selectedElem, selectedId)
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(PrimaryRed.copy(alpha = 0.1f))
                                            .clickable {
                                                targetPhotoSlotId = slotId
                                                photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                            }
                                            .padding(horizontal = 14.dp, vertical = 8.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Image, contentDescription = "Replace Image", tint = PrimaryRed)
                                        Text("Replace", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PrimaryRed)
                                    }

                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable { viewModel.toggleElementFlipX(selectedId) }
                                            .padding(horizontal = 12.dp, vertical = 8.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Flip, contentDescription = "Flip H", tint = MainText)
                                        Text("Flip H", fontSize = 11.sp, color = MainText)
                                    }

                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable { viewModel.toggleElementFlipY(selectedId) }
                                            .padding(horizontal = 12.dp, vertical = 8.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.SwapVert, contentDescription = "Flip V", tint = MainText)
                                        Text("Flip V", fontSize = 11.sp, color = MainText)
                                    }

                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable {
                                                activeSubTool = if (activeSubTool == ActiveSubTool.ROTATE) ActiveSubTool.NONE else ActiveSubTool.ROTATE
                                            }
                                            .padding(horizontal = 12.dp, vertical = 8.dp)
                                    ) {
                                        Icon(imageVector = Icons.AutoMirrored.Filled.RotateRight, contentDescription = "Rotate", tint = MainText)
                                        Text("Rotate", fontSize = 11.sp, color = MainText)
                                    }
                                } else {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable { textEditDialogVisible = true }
                                            .padding(horizontal = 12.dp, vertical = 8.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Text", tint = MainText)
                                        Text("Edit", fontSize = 11.sp, color = MainText)
                                    }

                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable {
                                                activeSubTool = if (activeSubTool == ActiveSubTool.FONT_SIZE) ActiveSubTool.NONE else ActiveSubTool.FONT_SIZE
                                            }
                                            .padding(horizontal = 12.dp, vertical = 8.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.FormatSize, contentDescription = "Font Size", tint = MainText)
                                        Text("Size", fontSize = 11.sp, color = MainText)
                                    }

                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable {
                                                activeSubTool = if (activeSubTool == ActiveSubTool.TEXT_COLOR) ActiveSubTool.NONE else ActiveSubTool.TEXT_COLOR
                                            }
                                            .padding(horizontal = 12.dp, vertical = 8.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Palette, contentDescription = "Color", tint = MainText)
                                        Text("Color", fontSize = 11.sp, color = MainText)
                                    }

                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable {
                                                activeSubTool = if (activeSubTool == ActiveSubTool.TEXT_SPACING) ActiveSubTool.NONE else ActiveSubTool.TEXT_SPACING
                                            }
                                            .padding(horizontal = 12.dp, vertical = 8.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.FormatLineSpacing, contentDescription = "Spacing", tint = MainText)
                                        Text("Spacing", fontSize = 11.sp, color = MainText)
                                    }

                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable {
                                                activeSubTool = if (activeSubTool == ActiveSubTool.TEXT_BG) ActiveSubTool.NONE else ActiveSubTool.TEXT_BG
                                            }
                                            .padding(horizontal = 12.dp, vertical = 8.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.FormatColorFill, contentDescription = "Bg Pill", tint = MainText)
                                        Text("Bg Pill", fontSize = 11.sp, color = MainText)
                                    }

                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable {
                                                viewModel.toggleTextCase(selectedId)
                                            }
                                            .padding(horizontal = 12.dp, vertical = 8.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.TextFields, contentDescription = "Case", tint = MainText)
                                        Text("Case", fontSize = 11.sp, color = MainText)
                                    }

                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable {
                                                val currentWeight = state.getTextFontWeight(selectedId, "NORMAL")
                                                val nextWeight = if (currentWeight.uppercase() == "BOLD") "NORMAL" else "BOLD"
                                                viewModel.updateFontWeight(selectedId, nextWeight)
                                            }
                                            .padding(horizontal = 12.dp, vertical = 8.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.FormatBold, contentDescription = "Bold", tint = MainText)
                                        Text("Bold", fontSize = 11.sp, color = MainText)
                                    }

                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable {
                                                val currentAlign = state.getTextAlignment(selectedId, "CENTER")
                                                val nextAlign = when (currentAlign.uppercase()) {
                                                    "LEFT" -> "CENTER"
                                                    "CENTER" -> "RIGHT"
                                                    else -> "LEFT"
                                                }
                                                viewModel.updateTextAlignment(selectedId, nextAlign)
                                            }
                                            .padding(horizontal = 12.dp, vertical = 8.dp)
                                    ) {
                                        Icon(
                                            imageVector = when (state.getTextAlignment(selectedId, "CENTER").uppercase()) {
                                                "LEFT" -> Icons.AutoMirrored.Filled.FormatAlignLeft
                                                "RIGHT" -> Icons.AutoMirrored.Filled.FormatAlignRight
                                                else -> Icons.Default.FormatAlignCenter
                                            },
                                            contentDescription = "Align",
                                            tint = MainText
                                        )
                                        Text("Align", fontSize = 11.sp, color = MainText)
                                    }

                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable {
                                                activeSubTool = if (activeSubTool == ActiveSubTool.BG_TEXTURE) ActiveSubTool.NONE else ActiveSubTool.BG_TEXTURE
                                            }
                                            .padding(horizontal = 12.dp, vertical = 8.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Texture, contentDescription = "Texture", tint = MainText)
                                        Text("Texture", fontSize = 11.sp, color = MainText)
                                    }

                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable {
                                                activeSubTool = if (activeSubTool == ActiveSubTool.ROTATE) ActiveSubTool.NONE else ActiveSubTool.ROTATE
                                            }
                                            .padding(horizontal = 12.dp, vertical = 8.dp)
                                    ) {
                                        Icon(imageVector = Icons.AutoMirrored.Filled.RotateRight, contentDescription = "Rotate", tint = MainText)
                                        Text("Rotate", fontSize = 11.sp, color = MainText)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // PixelLab / Photoshop-Style Right Navigation Drawer for Layer Stack View & Drag Reordering
            AnimatedVisibility(
                visible = showLayersDrawer,
                enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
                exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut(),
                modifier = Modifier.fillMaxSize()
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Dark Backdrop
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.45f))
                            .clickable { showLayersDrawer = false }
                    )

                    // Right Drawer Content Sheet (PixelLab Style)
                    Column(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .fillMaxHeight()
                            .width(320.dp)
                            .background(MainBackground)
                            .padding(top = 16.dp, bottom = 16.dp, start = 12.dp, end = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Layers, contentDescription = null, tint = PrimaryRed, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "PixelLab Layer Stack",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MainText
                                )
                            }

                            IconButton(onClick = { showLayersDrawer = false }) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = SecondaryText)
                            }
                        }

                        Text(
                            text = "Drag ::: handle up/down to reorder layers. Top layer renders on top.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SecondaryText,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        // Scrollable List of Layers with Transparent Checkerboard Thumbnails & Drag Handles
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            val sortedLayers = state.template.elements
                                .filter { !state.isElementDeleted(it.id) }
                                .sortedByDescending { state.getElementZIndex(it.id, it.zIndex) }

                            sortedLayers.forEachIndexed { index, element ->
                                val isLocked = state.isElementLocked(element.id)
                                val isHidden = state.isElementHidden(element.id)
                                val isSelected = state.selectedElementId == element.id

                                LayerRowItem(
                                    element = element,
                                    index = index,
                                    totalLayers = sortedLayers.size,
                                    isSelected = isSelected,
                                    isLocked = isLocked,
                                    isHidden = isHidden,
                                    manifest = state.template,
                                    state = state,
                                    context = context,
                                    onSelect = { viewModel.selectElement(element.id) },
                                    onDragReorder = { delta -> viewModel.reorderLayerByOffset(element.id, delta) },
                                    onToggleLock = { viewModel.toggleElementLock(element.id) },
                                    onToggleHide = { viewModel.toggleElementHide(element.id) },
                                    onDelete = { viewModel.deleteElement(element.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // High-Res & Export Format Dialog
    if (showExportOptionsDialog) {
        AlertDialog(
            onDismissRequest = { showExportOptionsDialog = false },
            title = { Text("Export Settings", fontWeight = FontWeight.Bold, color = MainText) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("Export Format", fontWeight = FontWeight.Bold, color = MainText, fontSize = 14.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ExportFormat.entries.forEach { fmt ->
                            FilterChip(
                                selected = state.exportFormat == fmt,
                                onClick = { viewModel.setExportFormat(fmt) },
                                label = { Text(fmt.name) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = PrimaryRed, selectedLabelColor = Color.White)
                            )
                        }
                    }

                    Text("Resolution Scaling", fontWeight = FontWeight.Bold, color = MainText, fontSize = 14.sp)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        ExportScale.entries.forEach { scale ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (state.exportScale == scale) SecondaryBackground else Color.Transparent)
                                    .clickable { viewModel.setExportScale(scale) }
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = state.exportScale == scale,
                                    onClick = { viewModel.setExportScale(scale) },
                                    colors = RadioButtonDefaults.colors(selectedColor = PrimaryRed)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = scale.label, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MainText)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showExportOptionsDialog = false
                        viewModel.exportImage(context)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed)
                ) {
                    Text("Save & Export")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportOptionsDialog = false }) {
                    Text("Cancel", color = MainText)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Inline Text Editing Dialog
    if (textEditDialogVisible && state.selectedElementId != null) {
        val selectedId = state.selectedElementId!!
        val defaultText = getElementDefaultText(state.template, selectedId)
        var textInput by remember(selectedId) { mutableStateOf(state.getTextValue(selectedId, defaultText)) }

        AlertDialog(
            onDismissRequest = { textEditDialogVisible = false },
            title = { Text("Edit Text", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = textInput,
                    onValueChange = {
                        textInput = it
                        viewModel.updateText(selectedId, it)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryRed,
                        cursorColor = PrimaryRed
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = { textEditDialogVisible = false },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed)
                ) {
                    Text("Done")
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Export Success Dialog
    state.exportResultUri?.let { uriString ->
        ExportSuccessDialog(
            imageUriString = uriString,
            onDismiss = { viewModel.clearExportResult() }
        )
    }
}

@Composable
private fun LayerRowItem(
    element: TemplateElement,
    index: Int,
    totalLayers: Int,
    isSelected: Boolean,
    isLocked: Boolean,
    isHidden: Boolean,
    manifest: TemplateManifest,
    state: MagicStoryEditorState,
    context: Context,
    onSelect: () -> Unit,
    onDragReorder: (Int) -> Unit,
    onToggleLock: () -> Unit,
    onToggleHide: () -> Unit,
    onDelete: () -> Unit
) {
    val density = LocalDensity.current
    var accumulativeY by remember { mutableFloatStateOf(0f) }
    val rowHeightPx = with(density) { 64.dp.toPx() }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onSelect),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) PrimaryRed.copy(alpha = 0.12f) else SecondaryBackground,
        border = BorderStroke(1.5.dp, if (isSelected) PrimaryRed else SubtleBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // PixelLab 6-Dot Drag Handle (::: Drag to Reorder)
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .pointerInput(element.id) {
                        detectDragGestures(
                            onDragStart = { accumulativeY = 0f },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                accumulativeY += dragAmount.y
                                val steps = (accumulativeY / rowHeightPx).toInt()
                                if (steps != 0) {
                                    onDragReorder(steps)
                                    accumulativeY -= steps * rowHeightPx
                                }
                            },
                            onDragEnd = { accumulativeY = 0f },
                            onDragCancel = { accumulativeY = 0f }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.DragHandle,
                    contentDescription = "Drag to reorder layer",
                    tint = SecondaryText,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Transparent Checkerboard Live Thumbnail Box
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, SubtleBorder, RoundedCornerShape(8.dp))
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    drawContext.canvas.nativeCanvas.let { nativeCanvas ->
                        TemplateRenderer.renderElementThumbnail(
                            canvas = nativeCanvas,
                            width = w,
                            height = h,
                            element = element,
                            manifest = manifest,
                            editorState = state,
                            context = context
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Element Label & Layer Number
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = getElementLabel(element, element.id),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MainText,
                    maxLines = 1
                )
                Text(
                    text = "Layer ${totalLayers - index}",
                    fontSize = 10.sp,
                    color = SecondaryText
                )
            }

            // PixelLab Matrix Actions: Lock, Hide, Delete
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onToggleLock,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = if (isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                        contentDescription = "Lock",
                        tint = if (isLocked) PrimaryRed else SecondaryText,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = onToggleHide,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = if (isHidden) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = "Hide",
                        tint = if (isHidden) PrimaryRed else SecondaryText,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = PrimaryRed,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

private fun isImageElement(element: TemplateElement?, selectedId: String): Boolean {
    if (element == null) return false
    return element is TemplateElement.PhotoSlot || (element is TemplateElement.PhotoCard && (selectedId == element.id || selectedId == element.photoSlotId))
}

private fun getPhotoSlotId(element: TemplateElement?, selectedId: String): String {
    return when (element) {
        is TemplateElement.PhotoSlot -> element.id
        is TemplateElement.PhotoCard -> element.photoSlotId
        else -> selectedId
    }
}

private fun getElementLabel(element: TemplateElement?, selectedId: String): String {
    return when (element) {
        is TemplateElement.Text -> element.label ?: "Text Element"
        is TemplateElement.PhotoSlot -> element.label ?: "Image Frame"
        is TemplateElement.PhotoCard -> if (selectedId == element.captionId) "Card Caption" else "Photo Card Element"
        is TemplateElement.Shape -> "Shape Element"
        is TemplateElement.PillCta -> "CTA Element"
        else -> "Selected Element"
    }
}

private fun getElementDefaultText(template: TemplateManifest, elementId: String): String {
    template.elements.forEach { element ->
        when (element) {
            is TemplateElement.Text -> if (element.id == elementId) return element.defaultText
            is TemplateElement.PhotoCard -> if (element.captionId == elementId) return element.captionDefaultText
            is TemplateElement.PillCta -> {
                if (element.leftTextId == elementId) return element.leftDefaultText
                if (element.buttonTextId == elementId) return element.buttonDefaultText
            }
            else -> {}
        }
    }
    return ""
}
