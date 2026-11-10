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
import androidx.compose.material.icons.outlined.Title
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

    var showShapeMenu by remember { mutableStateOf(false) }
    var showTransformPrecisionDialog by remember { mutableStateOf(false) }
    var showLayersDrawer by remember { mutableStateOf(false) }
    var showExportOptionsDialog by remember { mutableStateOf(false) }
    var showColorPickerDialog by remember { mutableStateOf(false) }
    var showGradientBuilderDialog by remember { mutableStateOf(false) }
    var colorPickerTargetId by remember { mutableStateOf<String?>(null) }
    var colorPickerInputHex by remember { mutableStateOf("#C91F2C") }
    var colorPickerMode by remember { mutableStateOf("FILL") }

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
            EditorTopAppBar(
                title = state.template.title,
                onBackPress = onBackPress,
                onOpenExportDialog = { showExportOptionsDialog = true }
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
                EditorSecondaryQuickToolbar(
                    canUndo = state.canUndo,
                    canRedo = state.canRedo,
                    showShapeMenu = showShapeMenu,
                    onOpenShapeLibrary = { showShapeMenu = true },
                    onDismissShapeMenu = { showShapeMenu = false },
                    onShapeSelected = { shapeKey ->
                        viewModel.addShapeElement(shapeKey)
                        showShapeMenu = false
                    },
                    onInsertImage = { insertImageLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                    onSetExportResolution = { viewModel.setExportResolution(it) },
                    onOpenLayersDrawer = { showLayersDrawer = true },
                    onUndo = { viewModel.undo() },
                    onRedo = { viewModel.redo() }
                )

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
                                        .padding(top = 8.dp, bottom = 12.dp, start = 8.dp, end = 8.dp)
                                ) {
                                    // Single Unified Horizontal Scrolling Action Bar (All icons scroll together)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState())
                                            .padding(horizontal = 4.dp, vertical = 2.dp),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Edit Text Content (if text element)
                                        if (selectedElem is TemplateElement.Text || (selectedElem is TemplateElement.PhotoCard && selectedId == selectedElem.captionId)) {
                                            EditorActionIconBtn(
                                                selected = textEditDialogVisible,
                                                onClick = { textEditDialogVisible = true },
                                                icon = Icons.Outlined.Edit,
                                                contentDescription = "Edit Text"
                                            )
                                            EditorActionIconBtn(
                                                selected = activeSubTool == ActiveSubTool.FONT_FAMILY,
                                                onClick = { activeSubTool = if (activeSubTool == ActiveSubTool.FONT_FAMILY) ActiveSubTool.NONE else ActiveSubTool.FONT_FAMILY },
                                                icon = Icons.Outlined.Title,
                                                contentDescription = "Font Family"
                                            )
                                            EditorActionIconBtn(
                                                selected = activeSubTool == ActiveSubTool.FONT_SIZE,
                                                onClick = { activeSubTool = if (activeSubTool == ActiveSubTool.FONT_SIZE) ActiveSubTool.NONE else ActiveSubTool.FONT_SIZE },
                                                icon = Icons.Outlined.FormatSize,
                                                contentDescription = "Font Size"
                                            )
                                            EditorActionIconBtn(
                                                selected = activeSubTool == ActiveSubTool.TEXT_COLOR,
                                                onClick = { activeSubTool = if (activeSubTool == ActiveSubTool.TEXT_COLOR) ActiveSubTool.NONE else ActiveSubTool.TEXT_COLOR },
                                                icon = Icons.Outlined.FormatColorFill,
                                                contentDescription = "Color & Fill"
                                            )
                                            EditorActionIconBtn(
                                                selected = activeSubTool == ActiveSubTool.TEXT_SPACING,
                                                onClick = { activeSubTool = if (activeSubTool == ActiveSubTool.TEXT_SPACING) ActiveSubTool.NONE else ActiveSubTool.TEXT_SPACING },
                                                icon = Icons.Outlined.FormatLineSpacing,
                                                contentDescription = "Spacing"
                                            )
                                            EditorActionIconBtn(
                                                selected = activeSubTool == ActiveSubTool.TEXT_BG,
                                                onClick = { activeSubTool = if (activeSubTool == ActiveSubTool.TEXT_BG) ActiveSubTool.NONE else ActiveSubTool.TEXT_BG },
                                                icon = Icons.Outlined.Palette,
                                                contentDescription = "Text Highlight"
                                            )
                                        }

                                        // Replace Photo (if Photo Slot / Card)
                                        if (selectedElem is TemplateElement.PhotoSlot || (selectedElem is TemplateElement.PhotoCard && selectedId == selectedElem.photoSlotId)) {
                                            EditorActionIconBtn(
                                                selected = false,
                                                onClick = {
                                                    targetPhotoSlotId = selectedId
                                                    photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                                },
                                                icon = Icons.Outlined.Image,
                                                contentDescription = "Replace Photo"
                                            )
                                        }

                                        // Shape Controls
                                        if (selectedElem is TemplateElement.Shape) {
                                            EditorActionIconBtn(
                                                selected = activeSubTool == ActiveSubTool.TEXT_COLOR,
                                                onClick = { activeSubTool = if (activeSubTool == ActiveSubTool.TEXT_COLOR) ActiveSubTool.NONE else ActiveSubTool.TEXT_COLOR },
                                                icon = Icons.Outlined.Palette,
                                                contentDescription = "Fill Color"
                                            )
                                            EditorActionIconBtn(
                                                selected = activeSubTool == ActiveSubTool.SHAPE_TYPE,
                                                onClick = { activeSubTool = if (activeSubTool == ActiveSubTool.SHAPE_TYPE) ActiveSubTool.NONE else ActiveSubTool.SHAPE_TYPE },
                                                icon = Icons.Outlined.Category,
                                                contentDescription = "Shape Type"
                                            )

                                            val shapeTypeStr = selectedElem.shapeType.uppercase()
                                            if (shapeTypeStr == "RECTANGLE" || shapeTypeStr == "ROUNDED_RECTANGLE") {
                                                EditorActionIconBtn(
                                                    selected = activeSubTool == ActiveSubTool.CORNER_RADIUS,
                                                    onClick = { activeSubTool = if (activeSubTool == ActiveSubTool.CORNER_RADIUS) ActiveSubTool.NONE else ActiveSubTool.CORNER_RADIUS },
                                                    icon = Icons.Outlined.RoundedCorner,
                                                    contentDescription = "Corners"
                                                )
                                            }
                                            if (shapeTypeStr == "POLYGON" || shapeTypeStr == "STAR") {
                                                EditorActionIconBtn(
                                                    selected = activeSubTool == ActiveSubTool.GEOMETRY,
                                                    onClick = { activeSubTool = if (activeSubTool == ActiveSubTool.GEOMETRY) ActiveSubTool.NONE else ActiveSubTool.GEOMETRY },
                                                    icon = Icons.Outlined.Star,
                                                    contentDescription = "Geometry"
                                                )
                                            }

                                            EditorActionIconBtn(
                                                selected = activeSubTool == ActiveSubTool.STROKE_ADVANCED,
                                                onClick = { activeSubTool = if (activeSubTool == ActiveSubTool.STROKE_ADVANCED) ActiveSubTool.NONE else ActiveSubTool.STROKE_ADVANCED },
                                                icon = Icons.Outlined.BorderColor,
                                                contentDescription = "Stroke"
                                            )
                                            EditorActionIconBtn(
                                                selected = false,
                                                onClick = { viewModel.swapShapeFillAndStroke(selectedId) },
                                                icon = Icons.Outlined.SwapHoriz,
                                                contentDescription = "Swap Fill/Stroke"
                                            )
                                        }

                                        // Background Controls
                                        if (selectedElem is TemplateElement.Background || selectedId == "bg") {
                                            EditorActionIconBtn(
                                                selected = activeSubTool == ActiveSubTool.TEXT_COLOR,
                                                onClick = { activeSubTool = if (activeSubTool == ActiveSubTool.TEXT_COLOR) ActiveSubTool.NONE else ActiveSubTool.TEXT_COLOR },
                                                icon = Icons.Outlined.Palette,
                                                contentDescription = "Canvas Color"
                                            )
                                            EditorActionIconBtn(
                                                selected = activeSubTool == ActiveSubTool.BG_TEXTURE,
                                                onClick = { activeSubTool = if (activeSubTool == ActiveSubTool.BG_TEXTURE) ActiveSubTool.NONE else ActiveSubTool.BG_TEXTURE },
                                                icon = Icons.Outlined.Texture,
                                                contentDescription = "Texture"
                                            )
                                        }

                                        // Common Controls: Opacity, Effects, Rotate, Flip, Transform Precision
                                        if (selectedElem !is TemplateElement.Background) {
                                            EditorActionIconBtn(
                                                selected = activeSubTool == ActiveSubTool.OPACITY,
                                                onClick = { activeSubTool = if (activeSubTool == ActiveSubTool.OPACITY) ActiveSubTool.NONE else ActiveSubTool.OPACITY },
                                                icon = Icons.Outlined.Opacity,
                                                contentDescription = "Opacity"
                                            )
                                            EditorActionIconBtn(
                                                selected = activeSubTool == ActiveSubTool.EFFECTS,
                                                onClick = { activeSubTool = if (activeSubTool == ActiveSubTool.EFFECTS) ActiveSubTool.NONE else ActiveSubTool.EFFECTS },
                                                icon = Icons.Outlined.Tune,
                                                contentDescription = "Effects"
                                            )
                                            EditorActionIconBtn(
                                                selected = activeSubTool == ActiveSubTool.ROTATE,
                                                onClick = { activeSubTool = if (activeSubTool == ActiveSubTool.ROTATE) ActiveSubTool.NONE else ActiveSubTool.ROTATE },
                                                icon = Icons.AutoMirrored.Outlined.RotateRight,
                                                contentDescription = "Rotate"
                                            )
                                            EditorActionIconBtn(
                                                selected = state.getFlipX(selectedId),
                                                onClick = { viewModel.toggleElementFlipX(selectedId) },
                                                icon = Icons.Outlined.SwapHoriz,
                                                contentDescription = "Flip H"
                                            )
                                            EditorActionIconBtn(
                                                selected = state.getFlipY(selectedId),
                                                onClick = { viewModel.toggleElementFlipY(selectedId) },
                                                icon = Icons.Outlined.SwapVert,
                                                contentDescription = "Flip V"
                                            )
                                            EditorActionIconBtn(
                                                selected = showTransformPrecisionDialog,
                                                onClick = { showTransformPrecisionDialog = true },
                                                icon = Icons.Outlined.Transform,
                                                contentDescription = "Precision"
                                            )
                                        }

                                        // Vertical Divider separating tools from layer actions
                                        Spacer(
                                            modifier = Modifier
                                                .padding(horizontal = 4.dp)
                                                .width(1.dp)
                                                .height(24.dp)
                                                .background(Color(0xFFE0E0E0))
                                        )

                                        // Layer Actions (Lock, Duplicate, Delete, Close)
                                        EditorActionIconBtn(
                                            selected = isLocked,
                                            onClick = { viewModel.toggleElementLock(selectedId) },
                                            icon = if (isLocked) Icons.Outlined.Lock else Icons.Outlined.LockOpen,
                                            contentDescription = "Lock"
                                        )
                                        if (selectedElem !is TemplateElement.Background) {
                                            EditorActionIconBtn(
                                                selected = false,
                                                onClick = { viewModel.duplicateElement(selectedId) },
                                                icon = Icons.Outlined.ContentCopy,
                                                contentDescription = "Duplicate"
                                            )
                                            EditorActionIconBtn(
                                                selected = false,
                                                onClick = { viewModel.deleteElement(selectedId) },
                                                icon = Icons.Outlined.Delete,
                                                contentDescription = "Delete"
                                            )
                                        }
                                        EditorActionIconBtn(
                                            selected = false,
                                            onClick = {
                                                viewModel.selectElement(null)
                                                activeSubTool = ActiveSubTool.NONE
                                            },
                                            icon = Icons.Outlined.Close,
                                            contentDescription = "Close"
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Sub-Tool Expansion Panel
                                    EditorSubToolContainer(
                                        activeSubTool = activeSubTool,
                                        selectedId = selectedId,
                                        selectedElem = selectedElem,
                                        state = state,
                                        viewModel = viewModel,
                                        onOpenColorPicker = { targetId, currentHex, mode ->
                                            colorPickerTargetId = targetId
                                            colorPickerInputHex = currentHex
                                            colorPickerMode = mode
                                            showColorPickerDialog = true
                                        },
                                        onOpenGradientBuilder = { targetId ->
                                            colorPickerTargetId = targetId
                                            showGradientBuilderDialog = true
                                        },
                                        onOpenEyedropper = { mode ->
                                            colorPickerMode = mode
                                            viewModel.setEyedropperActive(true)
                                        }
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))


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
        EditorLayersPanel(
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


