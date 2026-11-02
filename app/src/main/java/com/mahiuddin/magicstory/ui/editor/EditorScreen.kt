package com.mahiuddin.magicstory.ui.editor

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Redo
import androidx.compose.material.icons.automirrored.outlined.RotateRight
import androidx.compose.material.icons.automirrored.outlined.Undo
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.AspectRatio
import androidx.compose.material.icons.outlined.BorderColor
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Colorize
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.DragHandle
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FormatColorFill
import androidx.compose.material.icons.outlined.FormatLineSpacing
import androidx.compose.material.icons.outlined.FormatSize
import androidx.compose.material.icons.outlined.Gradient
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material.icons.outlined.Opacity
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.RoundedCorner
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material.icons.outlined.SwapVert
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.material.icons.outlined.Texture
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material.icons.outlined.Transform
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mahiuddin.magicstory.domain.model.ExportFormat
import com.mahiuddin.magicstory.domain.model.ExportResolution
import com.mahiuddin.magicstory.domain.model.ExportScale
import com.mahiuddin.magicstory.domain.model.GradientConfig
import com.mahiuddin.magicstory.domain.model.MagicStoryEditorState
import com.mahiuddin.magicstory.domain.model.TemplateElement
import com.mahiuddin.magicstory.template.renderer.TemplateRenderer
import com.mahiuddin.magicstory.ui.components.ExportSuccessDialog
import com.mahiuddin.magicstory.ui.components.MinimalSlider
import com.mahiuddin.magicstory.ui.components.TemplateCanvasPreview
import com.mahiuddin.magicstory.ui.theme.PrimaryRed

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
    ROTATE,
    SHAPE_TYPE,
    CORNER_RADIUS,
    SHAPE_SELECTION,
    STROKE_ADVANCED,
    GEOMETRY,
    TRANSFORM_PRECISION
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
    var showShapeLibraryDialog by remember { mutableStateOf(false) }
    var showTransformPrecisionDialog by remember { mutableStateOf(false) }
    var showLayersDrawer by remember { mutableStateOf(false) }
    var showExportOptionsDialog by remember { mutableStateOf(false) }
    var showColorPickerDialog by remember { mutableStateOf(false) }
    var showGradientBuilderDialog by remember { mutableStateOf(false) }
    var colorPickerTargetId by remember { mutableStateOf<String?>(null) }
    var colorPickerInputHex by remember { mutableStateOf("#C91F2C") }
    var colorPickerMode by remember { mutableStateOf("FILL") }
    var effectTab by remember { mutableStateOf("DROP_SHADOW") }

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

    val insertImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.addImageElement(it)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color(0xFFFAFAFA),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = state.template.title,
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        // Canvas Aspect Ratio Selector
                        Box {
                            IconButton(onClick = { showAspectMenu = true }) {
                                Icon(
                                    imageVector = Icons.Outlined.AspectRatio,
                                    contentDescription = "Canvas Size",
                                    tint = Color(0xFF333333),
                                    modifier = Modifier.size(20.dp)
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
                                            viewModel.setExportResolution(res)
                                            showAspectMenu = false
                                        }
                                    )
                                }
                            }
                        }

                        // Undo
                        IconButton(onClick = { viewModel.undo() }, enabled = state.canUndo) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.Undo,
                                contentDescription = "Undo",
                                tint = if (state.canUndo) Color(0xFF333333) else Color(0xFFBDBDBD),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Redo
                        IconButton(onClick = { viewModel.redo() }, enabled = state.canRedo) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.Redo,
                                contentDescription = "Redo",
                                tint = if (state.canRedo) Color(0xFF333333) else Color(0xFFBDBDBD),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .width(1.dp)
                                .height(18.dp)
                                .background(Color(0xFFE0E0E0))
                        )

                        // Export
                        IconButton(onClick = { showExportOptionsDialog = true }) {
                            Icon(
                                imageVector = Icons.Outlined.Download,
                                contentDescription = "Export Image",
                                tint = PrimaryRed,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Secondary Quick Toolbar Bar
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFFEAEAEA))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 1. Insert Vector Shape Library Button
                        IconButton(onClick = { showShapeLibraryDialog = true }) {
                            Icon(
                                imageVector = Icons.Outlined.Category,
                                contentDescription = "Insert Shape",
                                tint = Color(0xFF202020),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // 2. Insert Image Button
                        IconButton(
                            onClick = {
                                insertImageLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.AddPhotoAlternate,
                                contentDescription = "Insert Image",
                                tint = Color(0xFF202020),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Group Separator
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .width(1.dp)
                                .height(16.dp)
                                .background(Color(0xFFE5E5E5))
                        )

                        // 3. Canvas Size Aspect Ratio Selector
                        Box {
                            IconButton(onClick = { showAspectMenu = true }) {
                                Icon(
                                    imageVector = Icons.Outlined.AspectRatio,
                                    contentDescription = "Canvas Size",
                                    tint = Color(0xFF202020),
                                    modifier = Modifier.size(20.dp)
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
                                            viewModel.setExportResolution(res)
                                            showAspectMenu = false
                                        }
                                    )
                                }
                            }
                        }

                        // 4. Layers Drawer Button
                        IconButton(onClick = { showLayersDrawer = true }) {
                            Icon(
                                imageVector = Icons.Outlined.Layers,
                                contentDescription = "Layers Drawer",
                                tint = Color(0xFF202020),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Group Separator
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .width(1.dp)
                                .height(16.dp)
                                .background(Color(0xFFE5E5E5))
                        )

                        // 5. Undo / Backward
                        IconButton(onClick = { viewModel.undo() }, enabled = state.canUndo) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.Undo,
                                contentDescription = "Undo",
                                tint = if (state.canUndo) Color(0xFF202020) else Color(0xFFBDBDBD),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // 6. Redo / Forward
                        IconButton(onClick = { viewModel.redo() }, enabled = state.canRedo) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.Redo,
                                contentDescription = "Redo",
                                tint = if (state.canRedo) Color(0xFF202020) else Color(0xFFBDBDBD),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // FIXED WORKSPACE CONTAINER
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    // STABLE CANVAS VIEWPORT (Fixed dimensions independent from bottom panel overlay)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 80.dp),
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
                                            .border(2.dp, Color.White, CircleShape)
                                            .shadow(4.dp, CircleShape),
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
                                .background(Color(0xFF202020).copy(alpha = 0.75f))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (state.isEyedropperActive) Icons.Outlined.Colorize else Icons.Outlined.TouchApp,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = when {
                                        state.isEyedropperActive -> "Touch anywhere on canvas to pick a color"
                                        state.selectedElementId != null -> "Drag to move | Corners to resize | Top stem to rotate"
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

                    // FLOATING CONTEXTUAL BOTTOM PANEL OVERLAY
                    BottomPanelOverlay(
                        visible = state.selectedElementId != null,
                        modifier = Modifier.align(Alignment.BottomCenter)
                    ) {
                        state.selectedElementId?.let { selectedId ->
                            val selectedElem = state.template.elements.find {
                                it.id == selectedId || (it is TemplateElement.PhotoCard && (it.photoSlotId == selectedId || it.captionId == selectedId)) || (it is TemplateElement.PillCta && (it.leftTextId == selectedId || it.buttonTextId == selectedId))
                            }

                            val isLocked = state.isElementLocked(selectedId)

                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .shadow(elevation = 4.dp, shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                                color = Color.White,
                                border = BorderStroke(1.dp, Color(0xFFEAEAEA))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 10.dp, bottom = 16.dp, start = 12.dp, end = 12.dp)
                                ) {
                                    // Header Bar: Element Label, Lock Toggle, Duplicate, Delete
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 8.dp, vertical = 2.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val label = getElementLabel(selectedElem, selectedId)
                                        Text(
                                            text = if (isLocked) "$label 🔒" else label,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF202020),
                                            fontSize = 13.sp
                                        )

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            IconButton(onClick = { viewModel.toggleElementLock(selectedId) }) {
                                                Icon(
                                                    imageVector = if (isLocked) Icons.Outlined.Lock else Icons.Outlined.LockOpen,
                                                    contentDescription = "Lock Element",
                                                    tint = if (isLocked) PrimaryRed else Color(0xFF333333),
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                            if (selectedElem !is TemplateElement.Background) {
                                                IconButton(onClick = { viewModel.duplicateElement(selectedId) }) {
                                                    Icon(imageVector = Icons.Outlined.ContentCopy, contentDescription = "Duplicate", tint = Color(0xFF333333), modifier = Modifier.size(20.dp))
                                                }
                                                IconButton(onClick = { viewModel.deleteElement(selectedId) }) {
                                                    Icon(imageVector = Icons.Outlined.Delete, contentDescription = "Delete", tint = PrimaryRed, modifier = Modifier.size(20.dp))
                                                }
                                            }
                                            IconButton(onClick = {
                                                viewModel.selectElement(null)
                                                activeSubTool = ActiveSubTool.NONE
                                            }) {
                                                Icon(imageVector = Icons.Outlined.Close, contentDescription = "Close", tint = Color(0xFF777777), modifier = Modifier.size(20.dp))
                                            }
                                        }
                                    }

                                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Color(0xFFEAEAEA), thickness = 1.dp)

                                    Spacer(modifier = Modifier.height(6.dp))

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
                                                        color = if (isSelected) PrimaryRed.copy(alpha = 0.15f) else Color.White,
                                                        border = BorderStroke(1.5.dp, if (isSelected) PrimaryRed else Color(0xFFEAEAEA))
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
                                                                color = Color(0xFF202020)
                                                            )
                                                            Spacer(modifier = Modifier.height(2.dp))
                                                            Text(
                                                                text = info.first,
                                                                fontSize = 10.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = if (isSelected) PrimaryRed else Color(0xFF777777)
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        ActiveSubTool.FONT_SIZE -> {
                                            val currentSize = state.getFontSize(selectedId, 24f)
                                            MinimalSlider(
                                                value = currentSize,
                                                onValueChange = { viewModel.updateFontSize(selectedId, it) },
                                                onValueChangeFinished = { viewModel.commitGestureEnd() },
                                                valueRange = 0f..500f,
                                                label = "Size",
                                                valueFormatter = { "${it.toInt()} sp" },
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
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
                                                    Surface(
                                                        modifier = Modifier
                                                            .size(38.dp)
                                                            .clip(CircleShape)
                                                            .clickable {
                                                                colorPickerMode = "FILL"
                                                                viewModel.setEyedropperActive(true)
                                                            },
                                                        shape = CircleShape,
                                                        color = Color.White,
                                                        border = BorderStroke(1.dp, Color(0xFFE0E0E0))
                                                    ) {
                                                        Box(contentAlignment = Alignment.Center) {
                                                            Icon(imageVector = Icons.Outlined.Colorize, contentDescription = "Eyedropper", tint = PrimaryRed, modifier = Modifier.size(18.dp))
                                                        }
                                                    }

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
                                                        color = Color.White,
                                                        border = BorderStroke(1.dp, Color(0xFFE0E0E0))
                                                    ) {
                                                        Box(contentAlignment = Alignment.Center) {
                                                            Icon(imageVector = Icons.Outlined.Palette, contentDescription = "Color Picker", tint = Color(0xFF333333), modifier = Modifier.size(18.dp))
                                                        }
                                                    }

                                                    Surface(
                                                        modifier = Modifier
                                                            .size(38.dp)
                                                            .clip(CircleShape)
                                                            .clickable {
                                                                colorPickerTargetId = selectedId
                                                                showGradientBuilderDialog = true
                                                            },
                                                        shape = CircleShape,
                                                        color = Color.White,
                                                        border = BorderStroke(1.dp, Color(0xFFE0E0E0))
                                                    ) {
                                                        Box(contentAlignment = Alignment.Center) {
                                                            Icon(imageVector = Icons.Outlined.Gradient, contentDescription = "Custom Gradient Builder", tint = Color(0xFF333333), modifier = Modifier.size(18.dp))
                                                        }
                                                    }

                                                    state.savedSwatches.forEach { colorHex ->
                                                        Surface(
                                                            modifier = Modifier
                                                                .size(36.dp)
                                                                .clip(CircleShape)
                                                                .clickable { viewModel.updateTextColor(selectedId, colorHex) },
                                                            shape = CircleShape,
                                                            color = try { Color(android.graphics.Color.parseColor(colorHex)) } catch (_: Exception) { Color.Black },
                                                            border = BorderStroke(1.5.dp, Color(0xFFEAEAEA))
                                                        ) {}
                                                    }
                                                }
                                            }
                                        }
                                        ActiveSubTool.STROKE -> {
                                            val currentStroke = state.getElementStroke(selectedId) ?: Pair("#C91F2C", 0f)
                                            Column(modifier = Modifier.padding(horizontal = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                MinimalSlider(
                                                    value = currentStroke.second,
                                                    onValueChange = { viewModel.updateElementStroke(selectedId, currentStroke.first, it) },
                                                    valueRange = 0f..30f,
                                                    label = "Stroke Width",
                                                    valueFormatter = { "${it.toInt()} dp" }
                                                )

                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .horizontalScroll(rememberScrollState()),
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Surface(
                                                        modifier = Modifier
                                                            .size(34.dp)
                                                            .clip(CircleShape)
                                                            .clickable {
                                                                colorPickerMode = "STROKE"
                                                                viewModel.setEyedropperActive(true)
                                                            },
                                                        shape = CircleShape,
                                                        color = Color.White,
                                                        border = BorderStroke(1.dp, Color(0xFFE0E0E0))
                                                    ) {
                                                        Box(contentAlignment = Alignment.Center) {
                                                            Icon(imageVector = Icons.Outlined.Colorize, contentDescription = "Eyedropper Stroke", tint = PrimaryRed, modifier = Modifier.size(18.dp))
                                                        }
                                                    }

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
                                                        color = Color.White,
                                                        border = BorderStroke(1.dp, Color(0xFFE0E0E0))
                                                    ) {
                                                        Box(contentAlignment = Alignment.Center) {
                                                            Icon(imageVector = Icons.Outlined.Palette, contentDescription = "Stroke Color Picker", tint = Color(0xFF333333), modifier = Modifier.size(18.dp))
                                                        }
                                                    }

                                                    Surface(
                                                        modifier = Modifier
                                                            .size(34.dp)
                                                            .clip(CircleShape)
                                                            .clickable { viewModel.updateElementStroke(selectedId, null, 0f) },
                                                        shape = CircleShape,
                                                        color = Color.White,
                                                        border = BorderStroke(1.dp, Color(0xFFE0E0E0))
                                                    ) {
                                                        Box(contentAlignment = Alignment.Center) {
                                                            Icon(imageVector = Icons.Outlined.Close, contentDescription = "Clear Stroke", tint = Color(0xFF777777), modifier = Modifier.size(18.dp))
                                                        }
                                                    }

                                                    state.savedSwatches.forEach { hex ->
                                                        Surface(
                                                            modifier = Modifier
                                                                .size(30.dp)
                                                                .clip(CircleShape)
                                                                .clickable { viewModel.updateElementStroke(selectedId, hex, currentStroke.second.coerceAtLeast(3f)) },
                                                            shape = CircleShape,
                                                            color = try { Color(android.graphics.Color.parseColor(hex)) } catch (_: Exception) { Color.Black },
                                                            border = BorderStroke(1.dp, Color(0xFFEAEAEA))
                                                        ) {}
                                                    }
                                                }
                                            }
                                        }
                                        ActiveSubTool.OPACITY -> {
                                            val currentOpacity = state.getElementOpacity(selectedId)
                                            MinimalSlider(
                                                value = currentOpacity,
                                                onValueChange = { viewModel.updateElementOpacity(selectedId, it) },
                                                valueRange = 0f..1f,
                                                label = "Opacity",
                                                valueFormatter = { "${(it * 100).toInt()}%" },
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                        ActiveSubTool.EFFECTS -> {
                                            val curEff = state.getLayerEffect(selectedId)
                                            Column(modifier = Modifier.padding(horizontal = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    FilterChip(
                                                        selected = effectTab == "DROP_SHADOW",
                                                        onClick = { effectTab = "DROP_SHADOW" },
                                                        label = { Text("Drop Shadow 👥", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = PrimaryRed, selectedLabelColor = Color.White),
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                    FilterChip(
                                                        selected = effectTab == "OUTER_GLOW",
                                                        onClick = { effectTab = "OUTER_GLOW" },
                                                        label = { Text("Outer Glow ✨", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = PrimaryRed, selectedLabelColor = Color.White),
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                    FilterChip(
                                                        selected = effectTab == "INNER_GLOW",
                                                        onClick = { effectTab = "INNER_GLOW" },
                                                        label = { Text("Inner Glow 🌟", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = PrimaryRed, selectedLabelColor = Color.White),
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                }

                                                when (effectTab) {
                                                    "DROP_SHADOW" -> {
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

                                                            Surface(
                                                                modifier = Modifier
                                                                    .size(32.dp)
                                                                    .clip(CircleShape)
                                                                    .clickable {
                                                                        colorPickerMode = "SHADOW"
                                                                        viewModel.setEyedropperActive(true)
                                                                    },
                                                                shape = CircleShape,
                                                                color = Color.White,
                                                                border = BorderStroke(1.dp, Color(0xFFE0E0E0))
                                                            ) {
                                                                Box(contentAlignment = Alignment.Center) {
                                                                    Icon(imageVector = Icons.Outlined.Colorize, contentDescription = "Eyedropper Shadow", tint = PrimaryRed, modifier = Modifier.size(18.dp))
                                                                }
                                                            }

                                                            FilterChip(
                                                                selected = curEff.dropShadowColor != null,
                                                                onClick = {
                                                                    if (curEff.dropShadowColor != null) {
                                                                        viewModel.updateDropShadow(selectedId, null, 0f, 45f, 0f, 0f, 0f)
                                                                    } else {
                                                                        viewModel.updateDropShadow(selectedId, "#000000", 12f, 135f, 8f, 2f, 0.6f)
                                                                    }
                                                                },
                                                                label = { Text(if (curEff.dropShadowColor != null) "Disable Shadow" else "Enable Shadow", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = PrimaryRed, selectedLabelColor = Color.White)
                                                            )
                                                        }

                                                        if (curEff.dropShadowColor != null) {
                                                            MinimalSlider(
                                                                value = curEff.dropShadowOpacity,
                                                                onValueChange = { viewModel.updateDropShadow(selectedId, curEff.dropShadowColor, curEff.dropShadowRadius, curEff.dropShadowAngle, curEff.dropShadowDistance, curEff.dropShadowSpread, it) },
                                                                valueRange = 0f..1f,
                                                                label = "Opacity",
                                                                valueFormatter = { "${(it * 100).toInt()}%" }
                                                            )
                                                            MinimalSlider(
                                                                value = curEff.dropShadowRadius,
                                                                onValueChange = { viewModel.updateDropShadow(selectedId, curEff.dropShadowColor, it, curEff.dropShadowAngle, curEff.dropShadowDistance, curEff.dropShadowSpread, curEff.dropShadowOpacity) },
                                                                valueRange = 0f..60f,
                                                                label = "Blur",
                                                                valueFormatter = { "${it.toInt()} dp" }
                                                            )
                                                            MinimalSlider(
                                                                value = curEff.dropShadowDistance,
                                                                onValueChange = { viewModel.updateDropShadow(selectedId, curEff.dropShadowColor, curEff.dropShadowRadius, curEff.dropShadowAngle, it, curEff.dropShadowSpread, curEff.dropShadowOpacity) },
                                                                valueRange = 0f..100f,
                                                                label = "Distance",
                                                                valueFormatter = { "${it.toInt()} dp" }
                                                            )
                                                            Column {
                                                                MinimalSlider(
                                                                    value = curEff.dropShadowAngle,
                                                                    onValueChange = { viewModel.updateDropShadow(selectedId, curEff.dropShadowColor, curEff.dropShadowRadius, it, curEff.dropShadowDistance, curEff.dropShadowSpread, curEff.dropShadowOpacity) },
                                                                    valueRange = 0f..360f,
                                                                    label = "Angle",
                                                                    valueFormatter = { "${it.toInt()}°" }
                                                                )

                                                                Row(
                                                                    modifier = Modifier
                                                                        .fillMaxWidth()
                                                                        .horizontalScroll(rememberScrollState()),
                                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                                ) {
                                                                    listOf(45f to "45° (Diag)", 90f to "90° (V)", 135f to "135° (PS)", 180f to "180°", 270f to "270°", 315f to "315°").forEach { (ang, label) ->
                                                                        FilterChip(
                                                                            selected = curEff.dropShadowAngle == ang,
                                                                            onClick = { viewModel.updateDropShadow(selectedId, curEff.dropShadowColor, curEff.dropShadowRadius, ang, curEff.dropShadowDistance, curEff.dropShadowSpread, curEff.dropShadowOpacity) },
                                                                            label = { Text(label, fontSize = 10.sp) },
                                                                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = PrimaryRed, selectedLabelColor = Color.White)
                                                                        )
                                                                    }
                                                                }
                                                            }
                                                            MinimalSlider(
                                                                value = curEff.dropShadowSpread,
                                                                onValueChange = { viewModel.updateDropShadow(selectedId, curEff.dropShadowColor, curEff.dropShadowRadius, curEff.dropShadowAngle, curEff.dropShadowDistance, it, curEff.dropShadowOpacity) },
                                                                valueRange = 0f..30f,
                                                                label = "Spread",
                                                                valueFormatter = { "${it.toInt()} dp" }
                                                            )
                                                        }
                                                    }
                                                    "OUTER_GLOW" -> {
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

                                                            Surface(
                                                                modifier = Modifier
                                                                    .size(32.dp)
                                                                    .clip(CircleShape)
                                                                    .clickable {
                                                                        colorPickerMode = "OUTER_GLOW"
                                                                        viewModel.setEyedropperActive(true)
                                                                    },
                                                                shape = CircleShape,
                                                                color = Color.White,
                                                                border = BorderStroke(1.dp, Color(0xFFE0E0E0))
                                                            ) {
                                                                Box(contentAlignment = Alignment.Center) {
                                                                    Icon(imageVector = Icons.Outlined.Colorize, contentDescription = "Eyedropper Glow", tint = PrimaryRed, modifier = Modifier.size(18.dp))
                                                                }
                                                            }

                                                            FilterChip(
                                                                selected = curEff.outerGlowColor != null,
                                                                onClick = {
                                                                    if (curEff.outerGlowColor != null) {
                                                                        viewModel.updateOuterGlow(selectedId, null, 0f, 0f, 0f)
                                                                    } else {
                                                                        viewModel.updateOuterGlow(selectedId, "#FF0055", 16f, 4f, 0.8f)
                                                                    }
                                                                },
                                                                label = { Text(if (curEff.outerGlowColor != null) "Disable Glow" else "Enable Glow", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = PrimaryRed, selectedLabelColor = Color.White)
                                                            )
                                                        }

                                                        if (curEff.outerGlowColor != null) {
                                                            MinimalSlider(
                                                                value = curEff.outerGlowOpacity,
                                                                onValueChange = { viewModel.updateOuterGlow(selectedId, curEff.outerGlowColor, curEff.outerGlowRadius, curEff.outerGlowSpread, it) },
                                                                valueRange = 0f..1f,
                                                                label = "Opacity",
                                                                valueFormatter = { "${(it * 100).toInt()}%" }
                                                            )
                                                            MinimalSlider(
                                                                value = curEff.outerGlowRadius,
                                                                onValueChange = { viewModel.updateOuterGlow(selectedId, curEff.outerGlowColor, it, curEff.outerGlowSpread, curEff.outerGlowOpacity) },
                                                                valueRange = 0f..80f,
                                                                label = "Radius",
                                                                valueFormatter = { "${it.toInt()} dp" }
                                                            )
                                                            MinimalSlider(
                                                                value = curEff.outerGlowSpread,
                                                                onValueChange = { viewModel.updateOuterGlow(selectedId, curEff.outerGlowColor, curEff.outerGlowRadius, it, curEff.outerGlowOpacity) },
                                                                valueRange = 0f..40f,
                                                                label = "Spread",
                                                                valueFormatter = { "${it.toInt()} dp" }
                                                            )
                                                        }
                                                    }
                                                    "INNER_GLOW" -> {
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

                                                            Surface(
                                                                modifier = Modifier
                                                                    .size(32.dp)
                                                                    .clip(CircleShape)
                                                                    .clickable {
                                                                        colorPickerMode = "INNER_GLOW"
                                                                        viewModel.setEyedropperActive(true)
                                                                    },
                                                                shape = CircleShape,
                                                                color = Color.White,
                                                                border = BorderStroke(1.dp, Color(0xFFE0E0E0))
                                                            ) {
                                                                Box(contentAlignment = Alignment.Center) {
                                                                    Icon(imageVector = Icons.Outlined.Colorize, contentDescription = "Eyedropper Inner Glow", tint = PrimaryRed, modifier = Modifier.size(18.dp))
                                                                }
                                                            }

                                                            FilterChip(
                                                                selected = curEff.innerGlowColor != null,
                                                                onClick = {
                                                                    if (curEff.innerGlowColor != null) {
                                                                        viewModel.updateInnerGlow(selectedId, null, 0f, 0f, 0f)
                                                                    } else {
                                                                        viewModel.updateInnerGlow(selectedId, "#00D2FF", 12f, 2f, 0.7f)
                                                                    }
                                                                },
                                                                label = { Text(if (curEff.innerGlowColor != null) "Disable Glow" else "Enable Glow", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = PrimaryRed, selectedLabelColor = Color.White)
                                                            )
                                                        }

                                                        if (curEff.innerGlowColor != null) {
                                                            MinimalSlider(
                                                                value = curEff.innerGlowOpacity,
                                                                onValueChange = { viewModel.updateInnerGlow(selectedId, curEff.innerGlowColor, curEff.innerGlowRadius, curEff.innerGlowChoke, it) },
                                                                valueRange = 0f..1f,
                                                                label = "Opacity",
                                                                valueFormatter = { "${(it * 100).toInt()}%" }
                                                            )
                                                            MinimalSlider(
                                                                value = curEff.innerGlowRadius,
                                                                onValueChange = { viewModel.updateInnerGlow(selectedId, curEff.innerGlowColor, it, curEff.innerGlowChoke, curEff.innerGlowOpacity) },
                                                                valueRange = 0f..50f,
                                                                label = "Radius",
                                                                valueFormatter = { "${it.toInt()} dp" }
                                                            )
                                                            MinimalSlider(
                                                                value = curEff.innerGlowChoke,
                                                                onValueChange = { viewModel.updateInnerGlow(selectedId, curEff.innerGlowColor, curEff.innerGlowRadius, it, curEff.innerGlowOpacity) },
                                                                valueRange = 0f..30f,
                                                                label = "Choke",
                                                                valueFormatter = { "${it.toInt()} dp" }
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        ActiveSubTool.TEXT_SPACING -> {
                                            val currentLetterSpacing = state.getLetterSpacing(selectedId, 0f)
                                            val currentLineSpacing = state.getLineSpacing(selectedId, 1.0f)
                                            Column(modifier = Modifier.padding(horizontal = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                MinimalSlider(
                                                    value = currentLetterSpacing,
                                                    onValueChange = { viewModel.updateTextLetterSpacing(selectedId, it) },
                                                    onValueChangeFinished = { viewModel.commitGestureEnd() },
                                                    valueRange = 0.0f..0.4f,
                                                    label = "Letter",
                                                    valueFormatter = { "${(it * 100).toInt()}" }
                                                )
                                                MinimalSlider(
                                                    value = currentLineSpacing,
                                                    onValueChange = { viewModel.updateTextLineSpacing(selectedId, it) },
                                                    onValueChangeFinished = { viewModel.commitGestureEnd() },
                                                    valueRange = 0.8f..2.0f,
                                                    label = "Line",
                                                    valueFormatter = { "%.1f".format(it) }
                                                )
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
                                                    color = Color.White,
                                                    border = BorderStroke(1.dp, Color(0xFFE0E0E0))
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Icon(imageVector = Icons.Outlined.Close, contentDescription = "Clear", tint = Color(0xFF777777), modifier = Modifier.size(18.dp))
                                                    }
                                                }

                                                bgColors.forEach { colorHex ->
                                                    Surface(
                                                        modifier = Modifier
                                                            .size(36.dp)
                                                            .clip(CircleShape)
                                                            .clickable { viewModel.updateTextBgColor(selectedId, colorHex) },
                                                        shape = CircleShape,
                                                        color = try { Color(android.graphics.Color.parseColor(colorHex)) } catch (_: Exception) { Color.Black },
                                                        border = BorderStroke(1.5.dp, Color(0xFFEAEAEA))
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
                                        ActiveSubTool.SHAPE_TYPE -> {
                                            val currentShape = (selectedElem as? TemplateElement.Shape)?.shapeType ?: "RECTANGLE"
                                            val shapeTypes = listOf(
                                                "RECTANGLE" to "⬛ Rect",
                                                "ROUNDED_RECTANGLE" to "🔲 Rounded",
                                                "TRIANGLE" to "🔺 Triangle",
                                                "ELLIPSE" to "🔴 Circle",
                                                "HEXAGON" to "⬢ Hexagon"
                                            )
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .horizontalScroll(rememberScrollState())
                                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                shapeTypes.forEach { (typeKey, label) ->
                                                    FilterChip(
                                                        selected = currentShape.equals(typeKey, ignoreCase = true),
                                                        onClick = { viewModel.updateShapeType(selectedId, typeKey) },
                                                        label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = PrimaryRed, selectedLabelColor = Color.White)
                                                    )
                                                }
                                            }
                                        }
                                        ActiveSubTool.CORNER_RADIUS -> {
                                            (selectedElem as? TemplateElement.Shape)?.let { shape ->
                                                CornerRadiusPanel(
                                                    shape = shape,
                                                    onUpdateSingleRadius = { viewModel.updateShapeCornerRadius(selectedId, it) },
                                                    onUpdateIndividualRadii = { tl, tr, br, bl, isLinked ->
                                                        viewModel.updateShapeCornerRadii(selectedId, tl, tr, br, bl, isLinked)
                                                    }
                                                )
                                            }
                                        }
                                        ActiveSubTool.GEOMETRY -> {
                                            (selectedElem as? TemplateElement.Shape)?.let { shape ->
                                                GeometryPanel(
                                                    shape = shape,
                                                    onUpdateGeometry = { sides, points, innerRatio ->
                                                        viewModel.updateShapeGeometry(selectedId, sides, points, innerRatio)
                                                    }
                                                )
                                            }
                                        }
                                        ActiveSubTool.STROKE_ADVANCED -> {
                                            (selectedElem as? TemplateElement.Shape)?.let { shape ->
                                                StrokeAdvancedPanel(
                                                    shape = shape,
                                                    editorState = state,
                                                    onUpdateStrokeStyle = { style, cap, join, opacity ->
                                                        viewModel.updateShapeStrokeStyle(selectedId, style, cap, join, opacity)
                                                    },
                                                    onUpdateStrokeWidth = { w ->
                                                        val strokeColor = state.getElementStroke(selectedId)?.first ?: shape.borderColor ?: "#C91F2C"
                                                        viewModel.updateElementStroke(selectedId, strokeColor, w)
                                                    },
                                                    onOpenColorPicker = {
                                                        colorPickerTargetId = selectedId
                                                        colorPickerInputHex = state.getElementStroke(selectedId)?.first ?: shape.borderColor ?: "#C91F2C"
                                                        colorPickerMode = "STROKE"
                                                        showColorPickerDialog = true
                                                    },
                                                    onOpenEyedropper = {
                                                        colorPickerMode = "STROKE"
                                                        viewModel.setEyedropperActive(true)
                                                    }
                                                )
                                            }
                                        }
                                        ActiveSubTool.ROTATE -> {
                                            val currentRot = state.getElementRotation(selectedId, 0f)
                                            MinimalSlider(
                                                value = currentRot,
                                                onValueChange = { viewModel.updateElementRotation(selectedId, it) },
                                                onValueChangeFinished = { viewModel.commitGestureEnd() },
                                                valueRange = 0f..360f,
                                                label = "Angle",
                                                valueFormatter = { "${it.toInt()}°" },
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                        else -> {}
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    // Contextual Action Buttons Row
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState())
                                            .padding(horizontal = 4.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Edit Text Content (if text element)
                                        if (selectedElem is TemplateElement.Text || (selectedElem is TemplateElement.PhotoCard && selectedId == selectedElem.captionId)) {
                                            FilterChip(
                                                selected = textEditDialogVisible,
                                                onClick = { textEditDialogVisible = true },
                                                label = { Text("Edit Text", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                                leadingIcon = { Icon(imageVector = Icons.Outlined.Edit, contentDescription = null, modifier = Modifier.size(18.dp)) },
                                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = PrimaryRed, selectedLabelColor = Color.White)
                                            )
                                            FilterChip(
                                                selected = activeSubTool == ActiveSubTool.FONT_FAMILY,
                                                onClick = { activeSubTool = if (activeSubTool == ActiveSubTool.FONT_FAMILY) ActiveSubTool.NONE else ActiveSubTool.FONT_FAMILY },
                                                label = { Text("Font Family", fontSize = 11.sp) },
                                                leadingIcon = { Icon(imageVector = Icons.Outlined.TextFields, contentDescription = null, modifier = Modifier.size(18.dp)) }
                                            )
                                            FilterChip(
                                                selected = activeSubTool == ActiveSubTool.FONT_SIZE,
                                                onClick = { activeSubTool = if (activeSubTool == ActiveSubTool.FONT_SIZE) ActiveSubTool.NONE else ActiveSubTool.FONT_SIZE },
                                                label = { Text("Font Size", fontSize = 11.sp) },
                                                leadingIcon = { Icon(imageVector = Icons.Outlined.FormatSize, contentDescription = null, modifier = Modifier.size(18.dp)) }
                                            )
                                            FilterChip(
                                                selected = activeSubTool == ActiveSubTool.TEXT_COLOR,
                                                onClick = { activeSubTool = if (activeSubTool == ActiveSubTool.TEXT_COLOR) ActiveSubTool.NONE else ActiveSubTool.TEXT_COLOR },
                                                label = { Text("Color & Fill", fontSize = 11.sp) },
                                                leadingIcon = { Icon(imageVector = Icons.Outlined.FormatColorFill, contentDescription = null, modifier = Modifier.size(18.dp)) }
                                            )
                                            FilterChip(
                                                selected = activeSubTool == ActiveSubTool.TEXT_SPACING,
                                                onClick = { activeSubTool = if (activeSubTool == ActiveSubTool.TEXT_SPACING) ActiveSubTool.NONE else ActiveSubTool.TEXT_SPACING },
                                                label = { Text("Spacing", fontSize = 11.sp) },
                                                leadingIcon = { Icon(imageVector = Icons.Outlined.FormatLineSpacing, contentDescription = null, modifier = Modifier.size(18.dp)) }
                                            )
                                            FilterChip(
                                                selected = activeSubTool == ActiveSubTool.TEXT_BG,
                                                onClick = { activeSubTool = if (activeSubTool == ActiveSubTool.TEXT_BG) ActiveSubTool.NONE else ActiveSubTool.TEXT_BG },
                                                label = { Text("Text Highlight", fontSize = 11.sp) }
                                            )
                                        }

                                        // Replace Photo (if Photo Slot / Card)
                                        if (selectedElem is TemplateElement.PhotoSlot || (selectedElem is TemplateElement.PhotoCard && selectedId == selectedElem.photoSlotId)) {
                                            FilterChip(
                                                selected = false,
                                                onClick = {
                                                    targetPhotoSlotId = selectedId
                                                    photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                                },
                                                label = { Text("Replace Photo", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                                leadingIcon = { Icon(imageVector = Icons.Outlined.Image, contentDescription = null, modifier = Modifier.size(18.dp)) },
                                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = PrimaryRed, selectedLabelColor = Color.White)
                                            )
                                        }

                                        // Shape Controls
                                        if (selectedElem is TemplateElement.Shape) {
                                            FilterChip(
                                                selected = activeSubTool == ActiveSubTool.TEXT_COLOR,
                                                onClick = { activeSubTool = if (activeSubTool == ActiveSubTool.TEXT_COLOR) ActiveSubTool.NONE else ActiveSubTool.TEXT_COLOR },
                                                label = { Text("Fill Color", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                                leadingIcon = { Icon(imageVector = Icons.Outlined.Palette, contentDescription = null, modifier = Modifier.size(18.dp)) }
                                            )
                                            FilterChip(
                                                selected = activeSubTool == ActiveSubTool.SHAPE_TYPE,
                                                onClick = { activeSubTool = if (activeSubTool == ActiveSubTool.SHAPE_TYPE) ActiveSubTool.NONE else ActiveSubTool.SHAPE_TYPE },
                                                label = { Text("Shape Type", fontSize = 11.sp) },
                                                leadingIcon = { Icon(imageVector = Icons.Outlined.Category, contentDescription = null, modifier = Modifier.size(18.dp)) }
                                            )

                                            val shapeTypeStr = selectedElem.shapeType.uppercase()
                                            if (shapeTypeStr == "RECTANGLE" || shapeTypeStr == "ROUNDED_RECTANGLE") {
                                                FilterChip(
                                                    selected = activeSubTool == ActiveSubTool.CORNER_RADIUS,
                                                    onClick = { activeSubTool = if (activeSubTool == ActiveSubTool.CORNER_RADIUS) ActiveSubTool.NONE else ActiveSubTool.CORNER_RADIUS },
                                                    label = { Text("Corners", fontSize = 11.sp) },
                                                    leadingIcon = { Icon(imageVector = Icons.Outlined.RoundedCorner, contentDescription = null, modifier = Modifier.size(18.dp)) }
                                                )
                                            }
                                            if (shapeTypeStr == "POLYGON" || shapeTypeStr == "STAR") {
                                                FilterChip(
                                                    selected = activeSubTool == ActiveSubTool.GEOMETRY,
                                                    onClick = { activeSubTool = if (activeSubTool == ActiveSubTool.GEOMETRY) ActiveSubTool.NONE else ActiveSubTool.GEOMETRY },
                                                    label = { Text("Geometry", fontSize = 11.sp) },
                                                    leadingIcon = { Icon(imageVector = Icons.Outlined.Star, contentDescription = null, modifier = Modifier.size(18.dp)) }
                                                )
                                            }

                                            FilterChip(
                                                selected = activeSubTool == ActiveSubTool.STROKE_ADVANCED,
                                                onClick = { activeSubTool = if (activeSubTool == ActiveSubTool.STROKE_ADVANCED) ActiveSubTool.NONE else ActiveSubTool.STROKE_ADVANCED },
                                                label = { Text("Stroke", fontSize = 11.sp) },
                                                leadingIcon = { Icon(imageVector = Icons.Outlined.BorderColor, contentDescription = null, modifier = Modifier.size(18.dp)) }
                                            )
                                            FilterChip(
                                                selected = false,
                                                onClick = { viewModel.swapShapeFillAndStroke(selectedId) },
                                                label = { Text("Swap Fill/Stroke ⇄", fontSize = 10.sp) }
                                            )
                                        }

                                        // Background Controls
                                        if (selectedElem is TemplateElement.Background || selectedId == "bg") {
                                            FilterChip(
                                                selected = activeSubTool == ActiveSubTool.TEXT_COLOR,
                                                onClick = { activeSubTool = if (activeSubTool == ActiveSubTool.TEXT_COLOR) ActiveSubTool.NONE else ActiveSubTool.TEXT_COLOR },
                                                label = { Text("Canvas Color", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                                leadingIcon = { Icon(imageVector = Icons.Outlined.Palette, contentDescription = null, modifier = Modifier.size(18.dp)) }
                                            )
                                            FilterChip(
                                                selected = activeSubTool == ActiveSubTool.BG_TEXTURE,
                                                onClick = { activeSubTool = if (activeSubTool == ActiveSubTool.BG_TEXTURE) ActiveSubTool.NONE else ActiveSubTool.BG_TEXTURE },
                                                label = { Text("Texture", fontSize = 11.sp) },
                                                leadingIcon = { Icon(imageVector = Icons.Outlined.Texture, contentDescription = null, modifier = Modifier.size(18.dp)) }
                                            )
                                        }

                                        // Common Controls: Opacity, Effects, Rotate, Flip, Transform Precision
                                        if (selectedElem !is TemplateElement.Background) {
                                            FilterChip(
                                                selected = activeSubTool == ActiveSubTool.OPACITY,
                                                onClick = { activeSubTool = if (activeSubTool == ActiveSubTool.OPACITY) ActiveSubTool.NONE else ActiveSubTool.OPACITY },
                                                label = { Text("Opacity", fontSize = 11.sp) },
                                                leadingIcon = { Icon(imageVector = Icons.Outlined.Opacity, contentDescription = null, modifier = Modifier.size(18.dp)) }
                                            )
                                            FilterChip(
                                                selected = activeSubTool == ActiveSubTool.EFFECTS,
                                                onClick = { activeSubTool = if (activeSubTool == ActiveSubTool.EFFECTS) ActiveSubTool.NONE else ActiveSubTool.EFFECTS },
                                                label = { Text("Effects", fontSize = 11.sp) },
                                                leadingIcon = { Icon(imageVector = Icons.Outlined.Tune, contentDescription = null, modifier = Modifier.size(18.dp)) }
                                            )
                                            FilterChip(
                                                selected = activeSubTool == ActiveSubTool.ROTATE,
                                                onClick = { activeSubTool = if (activeSubTool == ActiveSubTool.ROTATE) ActiveSubTool.NONE else ActiveSubTool.ROTATE },
                                                label = { Text("Rotate", fontSize = 11.sp) },
                                                leadingIcon = { Icon(imageVector = Icons.AutoMirrored.Outlined.RotateRight, contentDescription = null, modifier = Modifier.size(18.dp)) }
                                            )
                                            FilterChip(
                                                selected = state.getFlipX(selectedId),
                                                onClick = { viewModel.toggleElementFlipX(selectedId) },
                                                label = { Text("Flip H", fontSize = 11.sp) },
                                                leadingIcon = { Icon(imageVector = Icons.Outlined.SwapHoriz, contentDescription = null, modifier = Modifier.size(18.dp)) }
                                            )
                                            FilterChip(
                                                selected = state.getFlipY(selectedId),
                                                onClick = { viewModel.toggleElementFlipY(selectedId) },
                                                label = { Text("Flip V", fontSize = 11.sp) },
                                                leadingIcon = { Icon(imageVector = Icons.Outlined.SwapVert, contentDescription = null, modifier = Modifier.size(18.dp)) }
                                            )
                                            FilterChip(
                                                selected = showTransformPrecisionDialog,
                                                onClick = { showTransformPrecisionDialog = true },
                                                label = { Text("Precision", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                                leadingIcon = { Icon(imageVector = Icons.Outlined.Transform, contentDescription = null, modifier = Modifier.size(18.dp)) }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // DIALOGS & OVERLAYS

    // 1. Inline Text Edit Dialog
    if (textEditDialogVisible && state.selectedElementId != null) {
        val currentText = state.getTextValue(state.selectedElementId!!, "")
        TextEditDialog(
            initialText = currentText,
            onConfirm = { newText ->
                viewModel.updateText(state.selectedElementId!!, newText)
                textEditDialogVisible = false
            },
            onDismiss = { textEditDialogVisible = false }
        )
    }

    // 2. Vector Shape Library Selection Dialog
    if (showShapeLibraryDialog) {
        ShapeSelectionDialog(
            onDismiss = { showShapeLibraryDialog = false },
            onShapeSelected = { shapeKey ->
                viewModel.addShapeElement(shapeKey)
                showShapeLibraryDialog = false
            }
        )
    }

    // 3. Illustrator-Style Transform Precision Dialog
    if (showTransformPrecisionDialog && state.selectedElementId != null) {
        TransformPrecisionDialog(
            selectedId = state.selectedElementId!!,
            editorState = state,
            viewModel = viewModel,
            onDismiss = { showTransformPrecisionDialog = false }
        )
    }

    // 4. PixelLab-Style Layer Stack Drawer
    if (showLayersDrawer) {
        LayersDrawer(
            editorState = state,
            viewModel = viewModel,
            onDismiss = { showLayersDrawer = false }
        )
    }

    // 5. High-Res Image Export Dialog
    if (showExportOptionsDialog) {
        ExportOptionsDialog(
            editorState = state,
            viewModel = viewModel,
            onDismiss = { showExportOptionsDialog = false },
            onExportConfirmed = {
                viewModel.exportImage(context)
                showExportOptionsDialog = false
            }
        )
    }

    // 6. Advanced 2D HSV Color Picker Dialog
    if (showColorPickerDialog && colorPickerTargetId != null) {
        val targetId = colorPickerTargetId!!
        AdvancedColorPickerDialog(
            initialColorHex = colorPickerInputHex,
            onColorSelected = { hex ->
                viewModel.addSavedSwatch(hex)
                when (colorPickerMode) {
                    "STROKE" -> {
                        val curW = state.getElementStroke(targetId)?.second ?: 4f
                        viewModel.updateElementStroke(targetId, hex, curW.coerceAtLeast(2f))
                    }
                    "SHADOW" -> {
                        val curEff = state.getLayerEffect(targetId)
                        viewModel.updateDropShadow(targetId, hex, curEff.dropShadowRadius.coerceAtLeast(8f), curEff.dropShadowAngle, curEff.dropShadowDistance, curEff.dropShadowSpread, curEff.dropShadowOpacity)
                    }
                    "OUTER_GLOW" -> {
                        val curEff = state.getLayerEffect(targetId)
                        viewModel.updateOuterGlow(targetId, hex, curEff.outerGlowRadius.coerceAtLeast(12f), curEff.outerGlowSpread, curEff.outerGlowOpacity)
                    }
                    "INNER_GLOW" -> {
                        val curEff = state.getLayerEffect(targetId)
                        viewModel.updateInnerGlow(targetId, hex, curEff.innerGlowRadius.coerceAtLeast(8f), curEff.innerGlowChoke, curEff.innerGlowOpacity)
                    }
                    else -> viewModel.updateTextColor(targetId, hex)
                }
                showColorPickerDialog = false
            },
            onDismiss = { showColorPickerDialog = false },
            onOpenEyedropper = {
                showColorPickerDialog = false
                viewModel.setEyedropperActive(true)
            }
        )
    }

    // 7. Custom Gradient Builder Dialog
    if (showGradientBuilderDialog && colorPickerTargetId != null) {
        val targetId = colorPickerTargetId!!
        val curGradient = state.getElementGradient(targetId)
        CustomGradientPickerDialog(
            initialGradient = curGradient,
            onGradientSelected = { grad ->
                viewModel.updateElementGradient(targetId, grad)
                showGradientBuilderDialog = false
            },
            onDismiss = { showGradientBuilderDialog = false }
        )
    }

    // 8. Export Success Notification Dialog
    state.exportResultUri?.let { uriStr ->
        ExportSuccessDialog(
            imageUriString = uriStr,
            onDismiss = { viewModel.clearExportResult() }
        )
    }
}

// HELPER PANELS & COMPOSABLES

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

@Composable
fun StrokeAdvancedPanel(
    shape: TemplateElement.Shape,
    editorState: MagicStoryEditorState,
    onUpdateStrokeStyle: (String, String, String, Float) -> Unit,
    onUpdateStrokeWidth: (Float) -> Unit,
    onOpenColorPicker: () -> Unit,
    onOpenEyedropper: () -> Unit
) {
    val currentStroke = editorState.getElementStroke(shape.id) ?: Pair(shape.borderColor ?: "#C91F2C", shape.borderWidth)
    var style by remember(shape) { mutableStateOf(shape.strokeStyle) }
    var cap by remember(shape) { mutableStateOf(shape.strokeCap) }
    var join by remember(shape) { mutableStateOf(shape.strokeJoin) }
    var opacity by remember(shape) { mutableFloatStateOf(shape.strokeOpacity) }

    Column(modifier = Modifier.padding(horizontal = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        MinimalSlider(
            value = currentStroke.second,
            onValueChange = { onUpdateStrokeWidth(it) },
            valueRange = 0f..40f,
            label = "Stroke Width",
            valueFormatter = { "${it.toInt()} dp" }
        )

        Text("Stroke Pattern", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF202020))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("SOLID" to "Solid ━", "DASHED" to "Dashed ╍", "DOTTED" to "Dotted ┈").forEach { (k, l) ->
                FilterChip(
                    selected = style == k,
                    onClick = {
                        style = k
                        onUpdateStrokeStyle(style, cap, join, opacity)
                    },
                    label = { Text(l, fontSize = 10.sp) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = PrimaryRed, selectedLabelColor = Color.White)
                )
            }
        }

        Text("Line Cap", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF202020))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("BUTT" to "Butt", "ROUND" to "Round", "SQUARE" to "Square").forEach { (k, l) ->
                FilterChip(
                    selected = cap == k,
                    onClick = {
                        cap = k
                        onUpdateStrokeStyle(style, cap, join, opacity)
                    },
                    label = { Text(l, fontSize = 10.sp) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = PrimaryRed, selectedLabelColor = Color.White)
                )
            }
        }

        Text("Line Join", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF202020))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("MITER" to "Miter", "ROUND" to "Round", "BEVEL" to "Bevel").forEach { (k, l) ->
                FilterChip(
                    selected = join == k,
                    onClick = {
                        join = k
                        onUpdateStrokeStyle(style, cap, join, opacity)
                    },
                    label = { Text(l, fontSize = 10.sp) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = PrimaryRed, selectedLabelColor = Color.White)
                )
            }
        }

        MinimalSlider(
            value = opacity,
            onValueChange = {
                opacity = it
                onUpdateStrokeStyle(style, cap, join, opacity)
            },
            valueRange = 0f..1f,
            label = "Stroke Opacity",
            valueFormatter = { "${(it * 100).toInt()}%" }
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Button(
                onClick = onOpenColorPicker,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(imageVector = Icons.Outlined.Palette, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Pick Stroke Color", fontSize = 11.sp)
            }
            IconButton(onClick = onOpenEyedropper) {
                Icon(imageVector = Icons.Outlined.Colorize, contentDescription = "Eyedropper", tint = PrimaryRed, modifier = Modifier.size(18.dp))
            }
        }
    }
}

fun getElementLabel(element: TemplateElement?, elementId: String): String {
    return when (element) {
        is TemplateElement.Text -> "Text Element"
        is TemplateElement.PhotoSlot -> "Photo Slot"
        is TemplateElement.PhotoCard -> {
            when (elementId) {
                element.photoSlotId -> "Photo Slot"
                element.captionId -> "Photo Caption"
                else -> "Photo Card"
            }
        }
        is TemplateElement.PillCta -> {
            when (elementId) {
                element.leftTextId -> "CTA Left Text"
                element.buttonTextId -> "CTA Button Text"
                else -> "Pill CTA"
            }
        }
        is TemplateElement.Shape -> {
            val name = element.shapeType.lowercase()
                .split("_")
                .joinToString(" ") { word -> word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() } }
            "$name Shape"
        }
        is TemplateElement.Background -> "Canvas Background"
        null -> if (elementId == "bg") "Canvas Background" else "Element ($elementId)"
    }
}

// DIALOGS

@Composable
fun TextEditDialog(
    initialText: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf(initialText) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Text Content", fontWeight = FontWeight.Bold, color = Color(0xFF202020)) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 5,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryRed, cursorColor = PrimaryRed)
            )
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(text) },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Apply", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF777777))
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun ShapeSelectionDialog(
    onDismiss: () -> Unit,
    onShapeSelected: (String) -> Unit
) {
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

@Composable
fun TransformPrecisionDialog(
    selectedId: String,
    editorState: MagicStoryEditorState,
    viewModel: EditorViewModel,
    onDismiss: () -> Unit
) {
    val selectedElem = editorState.template.elements.find { it.id == selectedId }
    val curX = editorState.getElementX(selectedId, (selectedElem as? TemplateElement.Shape)?.x ?: 0.1f)
    val curY = editorState.getElementY(selectedId, (selectedElem as? TemplateElement.Shape)?.y ?: 0.1f)
    val curW = editorState.getElementW(selectedId, (selectedElem as? TemplateElement.Shape)?.width ?: 0.3f)
    val curH = editorState.getElementH(selectedId, (selectedElem as? TemplateElement.Shape)?.height ?: 0.3f)
    val curRot = editorState.getElementRotation(selectedId, (selectedElem as? TemplateElement.Shape)?.rotation ?: 0f)

    var posX by remember { mutableFloatStateOf(curX) }
    var posY by remember { mutableFloatStateOf(curY) }
    var width by remember { mutableFloatStateOf(curW) }
    var height by remember { mutableFloatStateOf(curH) }
    var rot by remember { mutableFloatStateOf(curRot) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Transform & Precision Bounding Box", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF202020)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                MinimalSlider(value = posX, onValueChange = { posX = it; viewModel.updateElementPosition(selectedId, posX, posY) }, valueRange = 0f..1f, label = "Position X", valueFormatter = { "%.2f".format(it) })
                MinimalSlider(value = posY, onValueChange = { posY = it; viewModel.updateElementPosition(selectedId, posX, posY) }, valueRange = 0f..1f, label = "Position Y", valueFormatter = { "%.2f".format(it) })
                MinimalSlider(value = width, onValueChange = { width = it; viewModel.updateElementSize(selectedId, width, height) }, valueRange = 0.05f..1f, label = "Width", valueFormatter = { "%.2f".format(it) })
                MinimalSlider(value = height, onValueChange = { height = it; viewModel.updateElementSize(selectedId, width, height) }, valueRange = 0.05f..1f, label = "Height", valueFormatter = { "%.2f".format(it) })
                MinimalSlider(value = rot, onValueChange = { rot = it; viewModel.updateElementRotation(selectedId, rot) }, valueRange = 0f..360f, label = "Rotation Angle", valueFormatter = { "${it.toInt()}°" })
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed), shape = RoundedCornerShape(8.dp)) {
                Text("Done", fontWeight = FontWeight.Bold)
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun LayersDrawer(
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

@Composable
fun ExportOptionsDialog(
    editorState: MagicStoryEditorState,
    viewModel: EditorViewModel,
    onDismiss: () -> Unit,
    onExportConfirmed: () -> Unit
) {
    var format by remember { mutableStateOf(editorState.exportFormat) }
    var scale by remember { mutableStateOf(editorState.exportScale) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Export High-Res Image 💾", fontWeight = FontWeight.Bold, color = Color(0xFF202020)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Export Format", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF202020))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ExportFormat.entries.forEach { fmt ->
                        FilterChip(
                            selected = format == fmt,
                            onClick = {
                                format = fmt
                                viewModel.setExportFormat(fmt)
                            },
                            label = { Text(fmt.name) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = PrimaryRed, selectedLabelColor = Color.White)
                        )
                    }
                }

                Text("Resolution Scaling Multiplier", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF202020))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    ExportScale.entries.forEach { sc ->
                        FilterChip(
                            selected = scale == sc,
                            onClick = {
                                scale = sc
                                viewModel.setExportScale(sc)
                            },
                            label = { Text(sc.label) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = PrimaryRed, selectedLabelColor = Color.White),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onExportConfirmed, colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed), shape = RoundedCornerShape(8.dp)) {
                Text("Export High-Res", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF777777))
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun AdvancedColorPickerDialog(
    initialColorHex: String,
    onColorSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    onOpenEyedropper: () -> Unit
) {
    var hexInput by remember { mutableStateOf(initialColorHex) }
    val presetColors = listOf("#C91F2C", "#A91521", "#E53945", "#000000", "#FFFFFF", "#1E3A8A", "#065F46", "#D97706", "#8235DC", "#3657FF", "#FF0055", "#00D2FF")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Color Palette & Hex Selector 🎨", fontWeight = FontWeight.Bold, color = Color(0xFF202020)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = hexInput,
                    onValueChange = { hexInput = it },
                    label = { Text("Hex Color Code (#RRGGBB)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryRed, cursorColor = PrimaryRed),
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Quick Palette Swatches", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF202020))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presetColors.forEach { colorHex ->
                        Surface(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .clickable { hexInput = colorHex },
                            shape = CircleShape,
                            color = try { Color(android.graphics.Color.parseColor(colorHex)) } catch (_: Exception) { Color.Black },
                            border = BorderStroke(1.5.dp, Color(0xFFD0D0D0))
                        ) {}
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onColorSelected(hexInput) },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Select Color", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF777777))
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun CustomGradientPickerDialog(
    initialGradient: GradientConfig?,
    onGradientSelected: (GradientConfig) -> Unit,
    onDismiss: () -> Unit
) {
    var c1 by remember { mutableStateOf(initialGradient?.colors?.getOrNull(0) ?: "#C91F2C") }
    var c2 by remember { mutableStateOf(initialGradient?.colors?.getOrNull(1) ?: "#000000") }
    var angle by remember { mutableFloatStateOf(initialGradient?.angle ?: 45f) }
    var type by remember { mutableStateOf(initialGradient?.type ?: "LINEAR") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Custom Gradient Builder 🌈", fontWeight = FontWeight.Bold, color = Color(0xFF202020)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = type == "LINEAR", onClick = { type = "LINEAR" }, label = { Text("Linear") }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = PrimaryRed, selectedLabelColor = Color.White))
                    FilterChip(selected = type == "RADIAL", onClick = { type = "RADIAL" }, label = { Text("Radial") }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = PrimaryRed, selectedLabelColor = Color.White))
                }

                OutlinedTextField(value = c1, onValueChange = { c1 = it }, label = { Text("Start Color Hex") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = c2, onValueChange = { c2 = it }, label = { Text("End Color Hex") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                MinimalSlider(value = angle, onValueChange = { angle = it }, valueRange = 0f..360f, label = "Direction Angle", valueFormatter = { "${it.toInt()}°" })
            }
        },
        confirmButton = {
            Button(
                onClick = { onGradientSelected(GradientConfig(colors = listOf(c1, c2), angle = angle, type = type)) },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Apply Gradient", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF777777))
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun BoxScope.BottomPanelOverlay(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        modifier = modifier
    ) {
        content()
    }
}
