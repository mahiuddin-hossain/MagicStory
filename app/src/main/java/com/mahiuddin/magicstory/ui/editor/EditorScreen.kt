package com.mahiuddin.magicstory.ui.editor

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.BorderColor
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Colorize
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.FontDownload
import androidx.compose.material.icons.filled.FormatAlignCenter
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material.icons.filled.FormatLineSpacing
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Gradient
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Opacity
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.mahiuddin.magicstory.domain.model.ExportFormat
import com.mahiuddin.magicstory.domain.model.ExportResolution
import com.mahiuddin.magicstory.domain.model.ExportScale
import com.mahiuddin.magicstory.domain.model.GradientConfig
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
    FONT_FAMILY,
    FONT_SIZE,
    TEXT_COLOR,
    TEXT_SPACING,
    TEXT_BG,
    STROKE,
    OPACITY,
    EFFECTS,
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
    var showColorPickerDialog by remember { mutableStateOf(false) }
    var showGradientBuilderDialog by remember { mutableStateOf(false) }
    var colorPickerTargetId by remember { mutableStateOf<String?>(null) }
    var colorPickerInputHex by remember { mutableStateOf("#C91F2C") }
    var colorPickerMode by remember { mutableStateOf("FILL") } // "FILL" or "STROKE" or "SHADOW" or "OUTER_GLOW" or "INNER_GLOW"
    var effectTab by remember { mutableStateOf("DROP_SHADOW") } // "DROP_SHADOW", "OUTER_GLOW", "INNER_GLOW"

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
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    TemplateCanvasPreview(
                        manifest = state.template,
                        editorState = state,
                        modifier = Modifier.fillMaxSize(),
                        isInteractive = !state.isEyedropperActive,
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

                    // Interactive Eyedropper Layer Overlay
                    if (state.isEyedropperActive) {
                        val activeSelectedId = state.selectedElementId ?: "bg"
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onPress = { offset ->
                                            val normX = (offset.x / size.width.toFloat()).coerceIn(0f, 1f)
                                            val normY = (offset.y / size.height.toFloat()).coerceIn(0f, 1f)
                                            val sampledHex = TemplateRenderer.sampleColorAt(context, state.template, state, normX, normY)
                                            viewModel.updateEyedropperSample(sampledHex, offset.x, offset.y)
                                            awaitRelease()
                                            when (colorPickerMode) {
                                                "STROKE" -> {
                                                    val curW = state.getElementStroke(activeSelectedId)?.second ?: 4f
                                                    viewModel.updateElementStroke(activeSelectedId, sampledHex, curW.coerceAtLeast(2f))
                                                }
                                                "SHADOW" -> {
                                                    val curEff = state.getLayerEffect(activeSelectedId)
                                                    viewModel.updateDropShadow(activeSelectedId, sampledHex, curEff.dropShadowRadius.coerceAtLeast(8f), curEff.dropShadowDx, curEff.dropShadowDy)
                                                }
                                                "OUTER_GLOW" -> {
                                                    val curEff = state.getLayerEffect(activeSelectedId)
                                                    viewModel.updateOuterGlow(activeSelectedId, sampledHex, curEff.outerGlowRadius.coerceAtLeast(12f))
                                                }
                                                "INNER_GLOW" -> {
                                                    val curEff = state.getLayerEffect(activeSelectedId)
                                                    viewModel.updateInnerGlow(activeSelectedId, sampledHex, curEff.innerGlowRadius.coerceAtLeast(8f))
                                                }
                                                else -> viewModel.updateTextColor(activeSelectedId, sampledHex)
                                            }
                                            viewModel.setEyedropperActive(false)
                                        }
                                    )
                                }
                        ) {
                            state.eyedropperTouchPos?.let { (px, py) ->
                                val sampledColor = state.eyedropperSampledColor ?: "#C91F2C"
                                Box(
                                    modifier = Modifier
                                        .offset { IntOffset((px - 45.dp.toPx()).toInt(), (py - 85.dp.toPx()).toInt()) }
                                        .size(80.dp)
                                        .clip(CircleShape)
                                        .background(Color(android.graphics.Color.parseColor(sampledColor)))
                                        .border(3.5.dp, Color.White, CircleShape)
                                        .shadow(12.dp, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(sampledColor, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            }
                        }
                    }

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
                                imageVector = if (state.isEyedropperActive) Icons.Default.Colorize else Icons.Default.TouchApp,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = when {
                                    state.isEyedropperActive -> "Touch anywhere on canvas to pick a color"
                                    state.selectedElementId != null -> "Drag to move | Pinch/Corners to resize | Top ↻ to rotate"
                                    else -> "Tap any element or background to select & edit"
                                },
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
                                    if (selectedElem !is TemplateElement.Background) {
                                        IconButton(onClick = { viewModel.duplicateElement(selectedId) }) {
                                            Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Duplicate", tint = MainText)
                                        }
                                        IconButton(onClick = { viewModel.deleteElement(selectedId) }) {
                                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = PrimaryRed)
                                        }
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
                                ActiveSubTool.FONT_FAMILY -> {
                                    val currentFont = state.getTextFontFamily(selectedId, "SANS_SERIF")
                                    val fonts = listOf(
                                        "SANS_SERIF" to Pair("Roboto Sans", FontFamily.SansSerif),
                                        "SERIF" to Pair("Editorial Serif", FontFamily.Serif),
                                        "SCRIPT" to Pair("Dancing Script", FontFamily.Cursive),
                                        "MONOSPACE" to Pair("Space Mono", FontFamily.Monospace),
                                        "CASUAL" to Pair("Casual Sans", FontFamily.Default),
                                        "SANS_SERIF_BLACK" to Pair("Impact Heavy", FontFamily.SansSerif),
                                        "SANS_SERIF_CONDENSED" to Pair("Fashion Condensed", FontFamily.SansSerif),
                                        "SANS_SERIF_LIGHT" to Pair("Montserrat Light", FontFamily.SansSerif),
                                        "SANS_SERIF_MEDIUM" to Pair("Inter Medium", FontFamily.SansSerif)
                                    )

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState())
                                            .padding(horizontal = 8.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        fonts.forEach { (key, info) ->
                                            val isSelected = currentFont.equals(key, ignoreCase = true)
                                            Surface(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .clickable { viewModel.updateFontFamily(selectedId, key) },
                                                shape = RoundedCornerShape(12.dp),
                                                color = if (isSelected) PrimaryRed.copy(alpha = 0.15f) else SecondaryBackground,
                                                border = BorderStroke(1.5.dp, if (isSelected) PrimaryRed else SubtleBorder)
                                            ) {
                                                Column(
                                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                                    horizontalAlignment = Alignment.CenterHorizontally
                                                ) {
                                                    Text(
                                                        text = "Aa MagicStory",
                                                        fontFamily = info.second,
                                                        fontSize = 15.sp,
                                                        fontWeight = if (key.contains("BLACK") || key.contains("CONDENSED")) FontWeight.Bold else FontWeight.Normal,
                                                        color = MainText
                                                    )
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text(
                                                        text = info.first,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (isSelected) PrimaryRed else SecondaryText
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
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
                                    val currentElemColor = state.getTextColor(selectedId, "#000000")
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .horizontalScroll(rememberScrollState())
                                                .padding(horizontal = 8.dp, vertical = 4.dp),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Eyedropper Button
                                            Surface(
                                                modifier = Modifier
                                                    .size(38.dp)
                                                    .clip(CircleShape)
                                                    .clickable {
                                                        colorPickerMode = "FILL"
                                                        viewModel.setEyedropperActive(true)
                                                    },
                                                shape = CircleShape,
                                                color = PrimaryRed.copy(alpha = 0.15f),
                                                border = BorderStroke(1.5.dp, PrimaryRed)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(imageVector = Icons.Default.Colorize, contentDescription = "Eyedropper", tint = PrimaryRed, modifier = Modifier.size(20.dp))
                                                }
                                            }

                                            // Custom HSV Color Picker Button (Matching Attached UI)
                                            Surface(
                                                modifier = Modifier
                                                    .size(38.dp)
                                                    .clip(CircleShape)
                                                    .clickable {
                                                        colorPickerTargetId = selectedId
                                                        colorPickerInputHex = currentElemColor
                                                        colorPickerMode = "FILL"
                                                        showColorPickerDialog = true
                                                    },
                                                shape = CircleShape,
                                                color = SecondaryBackground,
                                                border = BorderStroke(1.5.dp, PrimaryRed)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(imageVector = Icons.Default.Palette, contentDescription = "Color Picker", tint = PrimaryRed, modifier = Modifier.size(20.dp))
                                                }
                                            }

                                            // Custom Gradient Builder Trigger Button
                                            Surface(
                                                modifier = Modifier
                                                    .size(38.dp)
                                                    .clip(CircleShape)
                                                    .clickable {
                                                        colorPickerTargetId = selectedId
                                                        showGradientBuilderDialog = true
                                                    },
                                                shape = CircleShape,
                                                color = PrimaryRed,
                                                border = BorderStroke(1.5.dp, Color.White)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(imageVector = Icons.Default.Gradient, contentDescription = "Custom Gradient Builder", tint = Color.White, modifier = Modifier.size(20.dp))
                                                }
                                            }

                                            // Saved Swatches & Palette
                                            state.savedSwatches.forEach { colorHex ->
                                                Surface(
                                                    modifier = Modifier
                                                        .size(36.dp)
                                                        .clip(CircleShape)
                                                        .clickable { viewModel.updateTextColor(selectedId, colorHex) },
                                                    shape = CircleShape,
                                                    color = try { Color(android.graphics.Color.parseColor(colorHex)) } catch (_: Exception) { Color.Black },
                                                    border = BorderStroke(1.5.dp, SubtleBorder)
                                                ) {}
                                            }
                                        }
                                    }
                                }
                                ActiveSubTool.STROKE -> {
                                    val currentStroke = state.getElementStroke(selectedId) ?: Pair("#C91F2C", 0f)
                                    Column(modifier = Modifier.padding(horizontal = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Stroke Width: ${currentStroke.second.toInt()} dp",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MainText,
                                                modifier = Modifier.width(130.dp)
                                            )
                                            Slider(
                                                value = currentStroke.second,
                                                onValueChange = { viewModel.updateElementStroke(selectedId, currentStroke.first, it) },
                                                valueRange = 0f..30f,
                                                colors = SliderDefaults.colors(thumbColor = PrimaryRed, activeTrackColor = PrimaryRed),
                                                modifier = Modifier.weight(1f)
                                            )
                                        }

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .horizontalScroll(rememberScrollState()),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Eyedropper Button for Stroke
                                            Surface(
                                                modifier = Modifier
                                                    .size(34.dp)
                                                    .clip(CircleShape)
                                                    .clickable {
                                                        colorPickerMode = "STROKE"
                                                        viewModel.setEyedropperActive(true)
                                                    },
                                                shape = CircleShape,
                                                color = PrimaryRed.copy(alpha = 0.15f),
                                                border = BorderStroke(1.5.dp, PrimaryRed)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(imageVector = Icons.Default.Colorize, contentDescription = "Eyedropper Stroke", tint = PrimaryRed, modifier = Modifier.size(18.dp))
                                                }
                                            }

                                            // Custom Color Picker Button for Stroke
                                            Surface(
                                                modifier = Modifier
                                                    .size(34.dp)
                                                    .clip(CircleShape)
                                                    .clickable {
                                                        colorPickerTargetId = selectedId
                                                        colorPickerInputHex = currentStroke.first
                                                        colorPickerMode = "STROKE"
                                                        showColorPickerDialog = true
                                                    },
                                                shape = CircleShape,
                                                color = SecondaryBackground,
                                                border = BorderStroke(1.5.dp, PrimaryRed)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(imageVector = Icons.Default.Palette, contentDescription = "Stroke Color Picker", tint = PrimaryRed, modifier = Modifier.size(18.dp))
                                                }
                                            }

                                            // Clear Stroke Button
                                            Surface(
                                                modifier = Modifier
                                                    .size(34.dp)
                                                    .clip(CircleShape)
                                                    .clickable { viewModel.updateElementStroke(selectedId, null, 0f) },
                                                shape = CircleShape,
                                                color = Color.Transparent,
                                                border = BorderStroke(1.5.dp, PrimaryRed)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(imageVector = Icons.Default.Close, contentDescription = "Clear Stroke", tint = PrimaryRed, modifier = Modifier.size(18.dp))
                                                }
                                            }

                                            // Saved Swatches for Stroke
                                            state.savedSwatches.forEach { hex ->
                                                Surface(
                                                    modifier = Modifier
                                                        .size(30.dp)
                                                        .clip(CircleShape)
                                                        .clickable { viewModel.updateElementStroke(selectedId, hex, currentStroke.second.coerceAtLeast(3f)) },
                                                    shape = CircleShape,
                                                    color = try { Color(android.graphics.Color.parseColor(hex)) } catch (_: Exception) { Color.Black },
                                                    border = BorderStroke(1.dp, SubtleBorder)
                                                ) {}
                                            }
                                        }
                                    }
                                }
                                ActiveSubTool.OPACITY -> {
                                    val currentOpacity = state.getElementOpacity(selectedId)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Opacity: ${(currentOpacity * 100).toInt()}%",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = MainText,
                                            modifier = Modifier.width(110.dp)
                                        )
                                        Slider(
                                            value = currentOpacity,
                                            onValueChange = { viewModel.updateElementOpacity(selectedId, it) },
                                            valueRange = 0.0f..1.0f,
                                            colors = SliderDefaults.colors(thumbColor = PrimaryRed, activeTrackColor = PrimaryRed),
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                                ActiveSubTool.EFFECTS -> {
                                    val curEff = state.getLayerEffect(selectedId)
                                    Column(modifier = Modifier.padding(horizontal = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        // Effect Tabs (Drop Shadow / Outer Glow / Inner Glow)
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            FilterChip(
                                                selected = effectTab == "DROP_SHADOW",
                                                onClick = { effectTab = "DROP_SHADOW" },
                                                label = { Text("Drop Shadow 👥", fontSize = 11.sp) },
                                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = PrimaryRed, selectedLabelColor = Color.White),
                                                modifier = Modifier.weight(1f)
                                            )
                                            FilterChip(
                                                selected = effectTab == "OUTER_GLOW",
                                                onClick = { effectTab = "OUTER_GLOW" },
                                                label = { Text("Outer Glow ✨", fontSize = 11.sp) },
                                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = PrimaryRed, selectedLabelColor = Color.White),
                                                modifier = Modifier.weight(1f)
                                            )
                                            FilterChip(
                                                selected = effectTab == "INNER_GLOW",
                                                onClick = { effectTab = "INNER_GLOW" },
                                                label = { Text("Inner Glow 🌟", fontSize = 11.sp) },
                                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = PrimaryRed, selectedLabelColor = Color.White),
                                                modifier = Modifier.weight(1f)
                                            )
                                        }

                                        when (effectTab) {
                                            "DROP_SHADOW" -> {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text("Blur: ${curEff.dropShadowRadius.toInt()} dp", fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(90.dp))
                                                    Slider(
                                                        value = curEff.dropShadowRadius,
                                                        onValueChange = { viewModel.updateDropShadow(selectedId, curEff.dropShadowColor ?: "#000000", it, curEff.dropShadowDx, curEff.dropShadowDy) },
                                                        valueRange = 0f..40f,
                                                        colors = SliderDefaults.colors(thumbColor = PrimaryRed, activeTrackColor = PrimaryRed),
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                }
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Surface(
                                                        modifier = Modifier
                                                            .size(32.dp)
                                                            .clip(CircleShape)
                                                            .clickable {
                                                                colorPickerTargetId = selectedId
                                                                colorPickerInputHex = curEff.dropShadowColor ?: "#000000"
                                                                colorPickerMode = "SHADOW"
                                                                showColorPickerDialog = true
                                                            },
                                                        shape = CircleShape,
                                                        color = try { Color(android.graphics.Color.parseColor(curEff.dropShadowColor ?: "#000000")) } catch (_: Exception) { Color.Black },
                                                        border = BorderStroke(1.5.dp, PrimaryRed)
                                                    ) {}

                                                    FilterChip(
                                                        selected = curEff.dropShadowColor != null,
                                                        onClick = {
                                                            if (curEff.dropShadowColor != null) {
                                                                viewModel.updateDropShadow(selectedId, null, 0f, 0f, 0f)
                                                            } else {
                                                                viewModel.updateDropShadow(selectedId, "#000000", 12f, 4f, 4f)
                                                            }
                                                        },
                                                        label = { Text(if (curEff.dropShadowColor != null) "Disable Shadow" else "Enable Shadow", fontSize = 11.sp) },
                                                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = PrimaryRed, selectedLabelColor = Color.White)
                                                    )
                                                }
                                            }
                                            "OUTER_GLOW" -> {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text("Glow: ${curEff.outerGlowRadius.toInt()} dp", fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(90.dp))
                                                    Slider(
                                                        value = curEff.outerGlowRadius,
                                                        onValueChange = { viewModel.updateOuterGlow(selectedId, curEff.outerGlowColor ?: "#FF0055", it) },
                                                        valueRange = 0f..50f,
                                                        colors = SliderDefaults.colors(thumbColor = PrimaryRed, activeTrackColor = PrimaryRed),
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                }
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Surface(
                                                        modifier = Modifier
                                                            .size(32.dp)
                                                            .clip(CircleShape)
                                                            .clickable {
                                                                colorPickerTargetId = selectedId
                                                                colorPickerInputHex = curEff.outerGlowColor ?: "#FF0055"
                                                                colorPickerMode = "OUTER_GLOW"
                                                                showColorPickerDialog = true
                                                            },
                                                        shape = CircleShape,
                                                        color = try { Color(android.graphics.Color.parseColor(curEff.outerGlowColor ?: "#FF0055")) } catch (_: Exception) { Color.Red },
                                                        border = BorderStroke(1.5.dp, PrimaryRed)
                                                    ) {}

                                                    FilterChip(
                                                        selected = curEff.outerGlowColor != null,
                                                        onClick = {
                                                            if (curEff.outerGlowColor != null) {
                                                                viewModel.updateOuterGlow(selectedId, null, 0f)
                                                            } else {
                                                                viewModel.updateOuterGlow(selectedId, "#FF0055", 16f)
                                                            }
                                                        },
                                                        label = { Text(if (curEff.outerGlowColor != null) "Disable Glow" else "Enable Glow", fontSize = 11.sp) },
                                                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = PrimaryRed, selectedLabelColor = Color.White)
                                                    )
                                                }
                                            }
                                            "INNER_GLOW" -> {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text("Inner: ${curEff.innerGlowRadius.toInt()} dp", fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(90.dp))
                                                    Slider(
                                                        value = curEff.innerGlowRadius,
                                                        onValueChange = { viewModel.updateInnerGlow(selectedId, curEff.innerGlowColor ?: "#00D2FF", it) },
                                                        valueRange = 0f..30f,
                                                        colors = SliderDefaults.colors(thumbColor = PrimaryRed, activeTrackColor = PrimaryRed),
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                }
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Surface(
                                                        modifier = Modifier
                                                            .size(32.dp)
                                                            .clip(CircleShape)
                                                            .clickable {
                                                                colorPickerTargetId = selectedId
                                                                colorPickerInputHex = curEff.innerGlowColor ?: "#00D2FF"
                                                                colorPickerMode = "INNER_GLOW"
                                                                showColorPickerDialog = true
                                                            },
                                                        shape = CircleShape,
                                                        color = try { Color(android.graphics.Color.parseColor(curEff.innerGlowColor ?: "#00D2FF")) } catch (_: Exception) { Color.Cyan },
                                                        border = BorderStroke(1.5.dp, PrimaryRed)
                                                    ) {}

                                                    FilterChip(
                                                        selected = curEff.innerGlowColor != null,
                                                        onClick = {
                                                            if (curEff.innerGlowColor != null) {
                                                                viewModel.updateInnerGlow(selectedId, null, 0f)
                                                            } else {
                                                                viewModel.updateInnerGlow(selectedId, "#00D2FF", 12f)
                                                            }
                                                        },
                                                        label = { Text(if (curEff.innerGlowColor != null) "Disable Glow" else "Enable Glow", fontSize = 11.sp) },
                                                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = PrimaryRed, selectedLabelColor = Color.White)
                                                    )
                                                }
                                            }
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
                                    if (selectedElem !is TemplateElement.Background) {
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
                                                    activeSubTool = if (activeSubTool == ActiveSubTool.FONT_FAMILY) ActiveSubTool.NONE else ActiveSubTool.FONT_FAMILY
                                                }
                                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.FontDownload, contentDescription = "Font Family", tint = MainText)
                                            Text("Font", fontSize = 11.sp, color = MainText)
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
                                        Icon(imageVector = Icons.Default.Palette, contentDescription = "Fill Color", tint = MainText)
                                        Text("Fill", fontSize = 11.sp, color = MainText)
                                    }

                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable {
                                                activeSubTool = if (activeSubTool == ActiveSubTool.STROKE) ActiveSubTool.NONE else ActiveSubTool.STROKE
                                            }
                                            .padding(horizontal = 12.dp, vertical = 8.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.BorderColor, contentDescription = "Stroke Border", tint = MainText)
                                        Text("Stroke", fontSize = 11.sp, color = MainText)
                                    }

                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable {
                                                activeSubTool = if (activeSubTool == ActiveSubTool.OPACITY) ActiveSubTool.NONE else ActiveSubTool.OPACITY
                                            }
                                            .padding(horizontal = 12.dp, vertical = 8.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Opacity, contentDescription = "Opacity", tint = MainText)
                                        Text("Opacity", fontSize = 11.sp, color = MainText)
                                    }

                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable {
                                                activeSubTool = if (activeSubTool == ActiveSubTool.EFFECTS) ActiveSubTool.NONE else ActiveSubTool.EFFECTS
                                            }
                                            .padding(horizontal = 12.dp, vertical = 8.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.AutoFixHigh, contentDescription = "Layer Effects", tint = PrimaryRed)
                                        Text("Effects", fontSize = 11.sp, color = PrimaryRed, fontWeight = FontWeight.Bold)
                                    }

                                    if (selectedElem !is TemplateElement.Background) {
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

                                    if (selectedElem !is TemplateElement.Background) {
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
                        var draggingIndex by remember { mutableStateOf<Int?>(null) }
                        var draggingItemOffsetY by remember { mutableFloatStateOf(0f) }
                        val density = LocalDensity.current
                        val rowHeightPx = with(density) { 68.dp.toPx() }

                        val sortedLayers = state.template.elements
                            .filter { !state.isElementDeleted(it.id) }
                            .sortedByDescending { state.getElementZIndex(it.id, it.zIndex) }

                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            itemsIndexed(
                                items = sortedLayers,
                                key = { _, item -> item.id }
                            ) { index, element ->
                                val isDragging = draggingIndex == index
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
                                    isDragging = isDragging,
                                    offsetY = if (isDragging) draggingItemOffsetY else 0f,
                                    manifest = state.template,
                                    state = state,
                                    context = context,
                                    onSelect = { viewModel.selectElement(element.id) },
                                    onDragStart = {
                                        draggingIndex = index
                                        draggingItemOffsetY = 0f
                                    },
                                    onDrag = { dragAmountY ->
                                        draggingItemOffsetY += dragAmountY
                                        val moveThreshold = rowHeightPx * 0.5f

                                        if (draggingItemOffsetY > moveThreshold && index < sortedLayers.size - 1) {
                                            viewModel.reorderLayerByOffset(element.id, 1)
                                            draggingIndex = index + 1
                                            draggingItemOffsetY -= rowHeightPx
                                        } else if (draggingItemOffsetY < -moveThreshold && index > 0) {
                                            viewModel.reorderLayerByOffset(element.id, -1)
                                            draggingIndex = index - 1
                                            draggingItemOffsetY += rowHeightPx
                                        }
                                    },
                                    onDragEnd = {
                                        draggingIndex = null
                                        draggingItemOffsetY = 0f
                                    },
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

    // Photorealistic HSV 2D Saturation-Value Color Picker Dialog (Matching Attached UI)
    if (showColorPickerDialog && colorPickerTargetId != null) {
        val targetId = colorPickerTargetId!!
        AdvancedColorPickerDialog(
            initialColorHex = colorPickerInputHex,
            onDismiss = { showColorPickerDialog = false },
            onEyedropperClick = { viewModel.setEyedropperActive(true) },
            onSaveSwatch = { hex -> viewModel.addSavedSwatch(hex) },
            onApplyColor = { hex ->
                when (colorPickerMode) {
                    "STROKE" -> {
                        val curW = state.getElementStroke(targetId)?.second ?: 4f
                        viewModel.updateElementStroke(targetId, hex, curW.coerceAtLeast(2f))
                    }
                    "SHADOW" -> {
                        val curEff = state.getLayerEffect(targetId)
                        viewModel.updateDropShadow(targetId, hex, curEff.dropShadowRadius.coerceAtLeast(8f), curEff.dropShadowDx, curEff.dropShadowDy)
                    }
                    "OUTER_GLOW" -> {
                        val curEff = state.getLayerEffect(targetId)
                        viewModel.updateOuterGlow(targetId, hex, curEff.outerGlowRadius.coerceAtLeast(12f))
                    }
                    "INNER_GLOW" -> {
                        val curEff = state.getLayerEffect(targetId)
                        viewModel.updateInnerGlow(targetId, hex, curEff.innerGlowRadius.coerceAtLeast(8f))
                    }
                    else -> viewModel.updateTextColor(targetId, hex)
                }
            }
        )
    }

    // Custom Gradient Builder Dialog (With Photoshop Style Linear, Radial, Sweep, Reverse & Angle)
    if (showGradientBuilderDialog && colorPickerTargetId != null) {
        val targetId = colorPickerTargetId!!
        val selectedElem = state.template.elements.find { it.id == targetId }
        val currentGrad = state.getElementGradient(targetId) ?: if (selectedElem is TemplateElement.Background) state.bgGradientConfig else null

        CustomGradientPickerDialog(
            initialConfig = currentGrad,
            onDismiss = { showGradientBuilderDialog = false },
            onApply = { config ->
                viewModel.updateElementGradient(targetId, config)
                showGradientBuilderDialog = false
            },
            onEyedropperClick = { viewModel.setEyedropperActive(true) },
            onSaveSwatch = { hex -> viewModel.addSavedSwatch(hex) }
        )
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
private fun AdvancedColorPickerDialog(
    initialColorHex: String,
    onDismiss: () -> Unit,
    onEyedropperClick: () -> Unit,
    onSaveSwatch: (String) -> Unit,
    onApplyColor: (String) -> Unit
) {
    var hsv by remember {
        val colorInt = try { android.graphics.Color.parseColor(initialColorHex) } catch (_: Exception) { android.graphics.Color.parseColor("#C91F2C") }
        val array = FloatArray(3)
        android.graphics.Color.colorToHSV(colorInt, array)
        mutableStateOf(array)
    }

    var alpha by remember {
        val colorInt = try { android.graphics.Color.parseColor(initialColorHex) } catch (_: Exception) { android.graphics.Color.parseColor("#C91F2C") }
        mutableFloatStateOf(android.graphics.Color.alpha(colorInt) / 255f)
    }

    val currentArgb = remember(hsv, alpha) {
        android.graphics.Color.HSVToColor((alpha * 255).toInt(), hsv)
    }

    val currentHex = remember(currentArgb) {
        String.format("#%06X", 0xFFFFFF and currentArgb)
    }

    var hexInputText by remember(currentHex) { mutableStateOf(currentHex) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Color Picker", fontWeight = FontWeight.Bold, color = MainText, fontSize = 18.sp)

                Surface(
                    modifier = Modifier.clip(RoundedCornerShape(8.dp)),
                    color = SecondaryBackground,
                    border = BorderStroke(1.dp, SubtleBorder)
                ) {
                    Text(
                        text = "Hex",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MainText,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. Photorealistic 2D Saturation-Value (SV) Box
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, SubtleBorder, RoundedCornerShape(12.dp))
                ) {
                    val boxW = constraints.maxWidth.toFloat()
                    val boxH = constraints.maxHeight.toFloat()

                    val pureColor = Color(android.graphics.Color.HSVToColor(floatArrayOf(hsv[0], 1f, 1f)))

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(Color.White, pureColor)
                                )
                            )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, Color.Black)
                                    )
                                )
                                .pointerInput(Unit) {
                                    detectDragGestures(
                                        onDragStart = { offset ->
                                            val sat = (offset.x / boxW).coerceIn(0f, 1f)
                                            val valB = (1f - offset.y / boxH).coerceIn(0f, 1f)
                                            hsv = floatArrayOf(hsv[0], sat, valB)
                                        },
                                        onDrag = { change, _ ->
                                            change.consume()
                                            val sat = (change.position.x / boxW).coerceIn(0f, 1f)
                                            val valB = (1f - change.position.y / boxH).coerceIn(0f, 1f)
                                            hsv = floatArrayOf(hsv[0], sat, valB)
                                        }
                                    )
                                }
                        ) {
                            // Circular Selector Thumb ◯
                            val thumbX = hsv[1] * boxW
                            val thumbY = (1f - hsv[2]) * boxH

                            Box(
                                modifier = Modifier
                                    .offset { IntOffset((thumbX - 12.dp.toPx()).toInt(), (thumbY - 12.dp.toPx()).toInt()) }
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .border(2.5.dp, Color.White, CircleShape)
                                    .shadow(4.dp, CircleShape)
                            )
                        }
                    }
                }

                // 2. 1D Rainbow Hue Slider Bar
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(26.dp)
                        .clip(RoundedCornerShape(13.dp))
                        .border(1.dp, SubtleBorder, RoundedCornerShape(13.dp))
                ) {
                    val hueBarW = constraints.maxWidth.toFloat()
                    val rainbowColors = listOf(
                        Color.Red, Color.Yellow, Color.Green, Color.Cyan, Color.Blue, Color.Magenta, Color.Red
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(brush = Brush.horizontalGradient(rainbowColors))
                            .pointerInput(Unit) {
                                detectDragGestures(
                                    onDragStart = { offset ->
                                        val newHue = (offset.x / hueBarW * 360f).coerceIn(0f, 360f)
                                        hsv = floatArrayOf(newHue, hsv[1], hsv[2])
                                    },
                                    onDrag = { change, _ ->
                                        change.consume()
                                        val newHue = (change.position.x / hueBarW * 360f).coerceIn(0f, 360f)
                                        hsv = floatArrayOf(newHue, hsv[1], hsv[2])
                                    }
                                )
                            }
                    ) {
                        val thumbHueX = (hsv[0] / 360f) * hueBarW
                        Box(
                            modifier = Modifier
                                .offset { IntOffset((thumbHueX - 11.dp.toPx()).toInt(), 2.dp.toPx().toInt()) }
                                .size(22.dp)
                                .clip(CircleShape)
                                .border(2.dp, Color.White, CircleShape)
                                .shadow(3.dp, CircleShape)
                        )
                    }
                }

                // 3. 1D Alpha Opacity Bar (Checkerboard Background)
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(26.dp)
                        .clip(RoundedCornerShape(13.dp))
                        .border(1.dp, SubtleBorder, RoundedCornerShape(13.dp))
                ) {
                    val alphaBarW = constraints.maxWidth.toFloat()
                    val solidColor = Color(android.graphics.Color.HSVToColor(hsv))

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(solidColor.copy(alpha = 0f), solidColor)
                                )
                            )
                            .pointerInput(Unit) {
                                detectDragGestures(
                                    onDragStart = { offset ->
                                        alpha = (offset.x / alphaBarW).coerceIn(0f, 1f)
                                    },
                                    onDrag = { change, _ ->
                                        change.consume()
                                        alpha = (change.position.x / alphaBarW).coerceIn(0f, 1f)
                                    }
                                )
                            }
                    ) {
                        val thumbAlphaX = alpha * alphaBarW
                        Box(
                            modifier = Modifier
                                .offset { IntOffset((thumbAlphaX - 11.dp.toPx()).toInt(), 2.dp.toPx().toInt()) }
                                .size(22.dp)
                                .clip(CircleShape)
                                .border(2.dp, Color.White, CircleShape)
                                .shadow(3.dp, CircleShape)
                        )
                    }
                }

                // 4. Live Color Hex Input + Eyedropper + Add Swatch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp)),
                        color = SecondaryBackground,
                        border = BorderStroke(1.dp, SubtleBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(currentArgb))
                                    .border(1.dp, SubtleBorder, RoundedCornerShape(4.dp))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedTextField(
                                value = hexInputText,
                                onValueChange = {
                                    hexInputText = it
                                    try {
                                        val parsed = android.graphics.Color.parseColor(it)
                                        val arr = FloatArray(3)
                                        android.graphics.Color.colorToHSV(parsed, arr)
                                        hsv = arr
                                    } catch (_: Exception) {}
                                },
                                modifier = Modifier.weight(1f),
                                textStyle = TextStyle(fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MainText),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.Transparent, unfocusedBorderColor = Color.Transparent)
                            )
                        }
                    }

                    // Eyedropper Button
                    Surface(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                onDismiss()
                                onEyedropperClick()
                            },
                        shape = RoundedCornerShape(10.dp),
                        color = SecondaryBackground,
                        border = BorderStroke(1.dp, SubtleBorder)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(imageVector = Icons.Default.Colorize, contentDescription = "Eyedropper", tint = MainText, modifier = Modifier.size(20.dp))
                        }
                    }

                    // Add Swatch Button
                    Surface(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onSaveSwatch(currentHex) },
                        shape = RoundedCornerShape(10.dp),
                        color = SecondaryBackground,
                        border = BorderStroke(1.dp, SubtleBorder)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Swatch", tint = PrimaryRed, modifier = Modifier.size(22.dp))
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onApplyColor(currentHex)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed)
            ) {
                Text("Apply Color")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MainText)
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
private fun CustomGradientPickerDialog(
    initialConfig: GradientConfig?,
    onDismiss: () -> Unit,
    onApply: (GradientConfig) -> Unit,
    onEyedropperClick: () -> Unit,
    onSaveSwatch: (String) -> Unit
) {
    var colors by remember { mutableStateOf(initialConfig?.colors ?: listOf("#FF512F", "#DD2476")) }
    var angle by remember { mutableFloatStateOf(initialConfig?.angle ?: 45f) }
    var type by remember { mutableStateOf(initialConfig?.type ?: "LINEAR") }
    var isReversed by remember { mutableStateOf(initialConfig?.isReversed ?: false) }
    var tileMode by remember { mutableStateOf(initialConfig?.tileMode ?: "CLAMP") }

    var selectedStopIndex by remember { mutableIntStateOf(0) }
    var hexInput by remember { mutableStateOf(colors.getOrElse(selectedStopIndex) { "#C91F2C" }) }
    var showStopColorPicker by remember { mutableStateOf(false) }

    if (showStopColorPicker) {
        AdvancedColorPickerDialog(
            initialColorHex = colors.getOrElse(selectedStopIndex) { "#C91F2C" },
            onDismiss = { showStopColorPicker = false },
            onEyedropperClick = {
                showStopColorPicker = false
                onDismiss()
                onEyedropperClick()
            },
            onSaveSwatch = onSaveSwatch,
            onApplyColor = { pickedHex ->
                val updated = colors.toMutableList()
                if (selectedStopIndex in updated.indices) {
                    updated[selectedStopIndex] = pickedHex
                    colors = updated
                    hexInput = pickedHex
                }
                showStopColorPicker = false
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Palette, contentDescription = null, tint = PrimaryRed)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Custom Gradient Builder", fontWeight = FontWeight.Bold, color = MainText)
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Live Gradient Preview Box
                val displayColors = if (isReversed) colors.reversed() else colors
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            brush = when (type.uppercase()) {
                                "RADIAL" -> Brush.radialGradient(
                                    colors = displayColors.map { try { Color(android.graphics.Color.parseColor(it)) } catch (_: Exception) { Color.Red } }
                                )
                                "SWEEP" -> Brush.sweepGradient(
                                    colors = displayColors.map { try { Color(android.graphics.Color.parseColor(it)) } catch (_: Exception) { Color.Red } }
                                )
                                else -> Brush.linearGradient(
                                    colors = displayColors.map { try { Color(android.graphics.Color.parseColor(it)) } catch (_: Exception) { Color.Red } }
                                )
                            }
                        )
                        .border(1.5.dp, SubtleBorder, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$type | ${angle.toInt()}°",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.45f), CircleShape)
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                    )
                }

                // Gradient Type Buttons (Linear / Radial / Sweep)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("LINEAR", "RADIAL", "SWEEP").forEach { t ->
                        FilterChip(
                            selected = type == t,
                            onClick = { type = t },
                            label = { Text(t, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = PrimaryRed, selectedLabelColor = Color.White),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Reverse & Tile Mode Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = isReversed,
                        onClick = { isReversed = !isReversed },
                        label = { Text("⇄ Reverse", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = PrimaryRed, selectedLabelColor = Color.White)
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("CLAMP", "MIRROR", "REPEAT").forEach { tm ->
                            FilterChip(
                                selected = tileMode == tm,
                                onClick = { tileMode = tm },
                                label = { Text(tm, fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = PrimaryRed, selectedLabelColor = Color.White)
                            )
                        }
                    }
                }

                // Color Stops Rail Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Color Stops (${colors.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MainText)

                    Row {
                        IconButton(
                            onClick = {
                                if (colors.size < 5) {
                                    colors = colors + "#3B82F6"
                                    selectedStopIndex = colors.size - 1
                                    hexInput = "#3B82F6"
                                }
                            },
                            enabled = colors.size < 5
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Stop", tint = PrimaryRed)
                        }

                        IconButton(
                            onClick = {
                                if (colors.size > 2) {
                                    colors = colors.filterIndexed { idx, _ -> idx != selectedStopIndex }
                                    selectedStopIndex = selectedStopIndex.coerceAtMost(colors.size - 1)
                                    hexInput = colors[selectedStopIndex]
                                }
                            },
                            enabled = colors.size > 2
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Stop", tint = PrimaryRed)
                        }
                    }
                }

                // Color Stop Thumbs
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    colors.forEachIndexed { idx, hex ->
                        val isSelected = idx == selectedStopIndex
                        Box(
                            modifier = Modifier
                                .size(if (isSelected) 42.dp else 34.dp)
                                .clip(CircleShape)
                                .background(try { Color(android.graphics.Color.parseColor(hex)) } catch (_: Exception) { Color.Red })
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) PrimaryRed else SubtleBorder,
                                    shape = CircleShape
                                )
                                .clickable {
                                    selectedStopIndex = idx
                                    hexInput = hex
                                    showStopColorPicker = true
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("${idx + 1}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }

                // Active Stop Hex Color & Open Custom Color Picker
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { showStopColorPicker = true },
                        shape = RoundedCornerShape(10.dp),
                        color = try { Color(android.graphics.Color.parseColor(colors.getOrElse(selectedStopIndex) { "#C91F2C" })) } catch (_: Exception) { Color.Red },
                        border = BorderStroke(1.5.dp, PrimaryRed)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(imageVector = Icons.Default.Palette, contentDescription = "Pick Stop Color", tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }

                    OutlinedTextField(
                        value = hexInput,
                        onValueChange = {
                            hexInput = it
                            try {
                                Color(android.graphics.Color.parseColor(it))
                                colors = colors.toMutableList().apply { set(selectedStopIndex, it) }
                            } catch (_: Exception) {}
                        },
                        label = { Text("Stop ${selectedStopIndex + 1} Hex Color") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryRed, cursorColor = PrimaryRed)
                    )
                }

                // Angle Slider
                if (type == "LINEAR") {
                    Text("Gradient Angle (${angle.toInt()}°)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MainText)
                    Slider(
                        value = angle,
                        onValueChange = { angle = it },
                        valueRange = 0f..360f,
                        colors = SliderDefaults.colors(thumbColor = PrimaryRed, activeTrackColor = PrimaryRed)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(0f to "0° (H)", 45f to "45° (Diag)", 90f to "90° (V)", 135f to "135°", 180f to "180°", 270f to "270°").forEach { (a, label) ->
                            FilterChip(
                                selected = angle == a,
                                onClick = { angle = a },
                                label = { Text(label, fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = PrimaryRed, selectedLabelColor = Color.White)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onApply(
                        GradientConfig(
                            colors = colors,
                            angle = angle,
                            type = type,
                            isReversed = isReversed,
                            tileMode = tileMode
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed)
            ) {
                Text("Apply Gradient")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MainText)
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun LayerRowItem(
    element: TemplateElement,
    index: Int,
    totalLayers: Int,
    isSelected: Boolean,
    isLocked: Boolean,
    isHidden: Boolean,
    isDragging: Boolean,
    offsetY: Float,
    manifest: TemplateManifest,
    state: MagicStoryEditorState,
    context: Context,
    onSelect: () -> Unit,
    onDragStart: () -> Unit,
    onDrag: (Float) -> Unit,
    onDragEnd: () -> Unit,
    onToggleLock: () -> Unit,
    onToggleHide: () -> Unit,
    onDelete: () -> Unit
) {
    val elevationAnim by animateDpAsState(if (isDragging) 12.dp else 0.dp, label = "elevation")
    val scaleAnim by animateFloatAsState(if (isDragging) 1.04f else 1.0f, label = "scale")

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .zIndex(if (isDragging) 100f else 0f)
            .graphicsLayer {
                translationY = offsetY
                scaleX = scaleAnim
                scaleY = scaleAnim
            }
            .shadow(elevation = elevationAnim, shape = RoundedCornerShape(12.dp), ambientColor = PrimaryRed, spotColor = PrimaryRed)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onSelect),
        shape = RoundedCornerShape(12.dp),
        color = when {
            isDragging -> PrimaryRed.copy(alpha = 0.22f)
            isSelected -> PrimaryRed.copy(alpha = 0.12f)
            else -> SecondaryBackground
        },
        border = BorderStroke(if (isDragging) 2.dp else 1.5.dp, if (isDragging || isSelected) PrimaryRed else SubtleBorder)
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
                            onDragStart = { onDragStart() },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                onDrag(dragAmount.y)
                            },
                            onDragEnd = { onDragEnd() },
                            onDragCancel = { onDragEnd() }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.DragHandle,
                    contentDescription = "Drag to reorder layer",
                    tint = if (isDragging) PrimaryRed else SecondaryText,
                    modifier = Modifier.size(24.dp)
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

                if (element !is TemplateElement.Background) {
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
        is TemplateElement.Background -> "Background Layer"
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
