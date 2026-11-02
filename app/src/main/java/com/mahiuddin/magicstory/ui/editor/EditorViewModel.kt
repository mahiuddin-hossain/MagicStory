package com.mahiuddin.magicstory.ui.editor

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mahiuddin.magicstory.domain.model.EditorSnapshot
import com.mahiuddin.magicstory.domain.model.ExportFormat
import com.mahiuddin.magicstory.domain.model.ExportResolution
import com.mahiuddin.magicstory.domain.model.ExportScale
import com.mahiuddin.magicstory.domain.model.GradientConfig
import com.mahiuddin.magicstory.domain.model.GuideLine
import com.mahiuddin.magicstory.domain.model.LayerEffectConfig
import com.mahiuddin.magicstory.domain.model.MagicStoryEditorState
import com.mahiuddin.magicstory.domain.model.TemplateElement
import com.mahiuddin.magicstory.domain.repository.TemplateRepository
import com.mahiuddin.magicstory.template.export.ImageExporter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class ZOrderAction {
    BRING_TO_FRONT,
    SEND_TO_BACK,
    MOVE_UP,
    MOVE_DOWN
}

class EditorViewModel(
    private val repository: TemplateRepository
) : ViewModel() {

    private val _state = MutableStateFlow<MagicStoryEditorState?>(null)
    val state: StateFlow<MagicStoryEditorState?> = _state.asStateFlow()

    fun loadTemplate(templateId: String) {
        viewModelScope.launch {
            val manifest = repository.getTemplateById(templateId)
            if (manifest != null) {
                val initialSnapshot = EditorSnapshot(
                    editedTexts = emptyMap(),
                    photoUris = emptyMap(),
                    elementPositions = emptyMap(),
                    elementSizes = emptyMap(),
                    elementRotations = emptyMap(),
                    elementFlips = emptyMap(),
                    textFontSizes = emptyMap(),
                    textColors = emptyMap(),
                    textFontWeights = emptyMap(),
                    textAlignments = emptyMap(),
                    textLetterSpacings = emptyMap(),
                    textLineSpacings = emptyMap(),
                    textCases = emptyMap(),
                    textBgColors = emptyMap(),
                    textShadowColors = emptyMap(),
                    textFontFamilies = emptyMap(),
                    customFontUris = emptyMap(),
                    lockedElementIds = emptySet(),
                    hiddenElementIds = emptySet(),
                    elementZOrders = emptyMap(),
                    bgGradientConfig = null,
                    bgTextureType = "NONE",
                    elementGradients = emptyMap(),
                    elementStrokes = emptyMap(),
                    elementOpacities = emptyMap(),
                    elementEffects = emptyMap(),
                    deletedElementIds = emptySet()
                )
                _state.value = MagicStoryEditorState(
                    template = manifest,
                    history = listOf(initialSnapshot),
                    historyIndex = 0
                )
            }
        }
    }

    fun selectElement(elementId: String?) {
        _state.value = _state.value?.copy(selectedElementId = elementId)
    }

    fun setSnappingGuideLines(lines: List<GuideLine>) {
        _state.value = _state.value?.copy(snappingGuideLines = lines)
    }

    fun toggleElementLock(elementId: String) {
        val currentState = _state.value ?: return
        val newLocked = currentState.lockedElementIds.toMutableSet().apply {
            if (contains(elementId)) remove(elementId) else add(elementId)
        }

        pushState(
            currentState,
            currentState.editedTexts,
            currentState.photoUris,
            currentState.elementPositions,
            currentState.elementSizes,
            currentState.elementRotations,
            currentState.elementFlips,
            currentState.textFontSizes,
            currentState.textColors,
            currentState.textFontWeights,
            currentState.textAlignments,
            currentState.textLetterSpacings,
            currentState.textLineSpacings,
            currentState.textCases,
            currentState.textBgColors,
            currentState.textShadowColors,
            currentState.textFontFamilies,
            currentState.customFontUris,
            newLocked,
            currentState.hiddenElementIds,
            currentState.elementZOrders,
            currentState.bgGradientConfig,
            currentState.bgTextureType,
            currentState.elementGradients,
            currentState.elementStrokes,
            currentState.elementOpacities,
            currentState.elementEffects,
            currentState.deletedElementIds
        )
    }

    fun toggleElementHide(elementId: String) {
        val currentState = _state.value ?: return
        val newHidden = currentState.hiddenElementIds.toMutableSet().apply {
            if (contains(elementId)) remove(elementId) else add(elementId)
        }

        pushState(
            currentState,
            currentState.editedTexts,
            currentState.photoUris,
            currentState.elementPositions,
            currentState.elementSizes,
            currentState.elementRotations,
            currentState.elementFlips,
            currentState.textFontSizes,
            currentState.textColors,
            currentState.textFontWeights,
            currentState.textAlignments,
            currentState.textLetterSpacings,
            currentState.textLineSpacings,
            currentState.textCases,
            currentState.textBgColors,
            currentState.textShadowColors,
            currentState.textFontFamilies,
            currentState.customFontUris,
            currentState.lockedElementIds,
            newHidden,
            currentState.elementZOrders,
            currentState.bgGradientConfig,
            currentState.bgTextureType,
            currentState.elementGradients,
            currentState.elementStrokes,
            currentState.elementOpacities,
            currentState.elementEffects,
            currentState.deletedElementIds
        )
    }

    fun moveElementZOrder(elementId: String, action: ZOrderAction) {
        val currentState = _state.value ?: return
        val sorted = currentState.template.elements.sortedBy { currentState.getElementZIndex(it.id, it.zIndex) }
        val currentZ = currentState.getElementZIndex(elementId, sorted.find { it.id == elementId }?.zIndex ?: 0)

        val newZOrders = currentState.elementZOrders.toMutableMap()

        when (action) {
            ZOrderAction.BRING_TO_FRONT -> {
                val maxZ = (sorted.maxOfOrNull { currentState.getElementZIndex(it.id, it.zIndex) } ?: 0) + 10
                newZOrders[elementId] = maxZ
            }
            ZOrderAction.SEND_TO_BACK -> {
                val minZ = (sorted.minOfOrNull { currentState.getElementZIndex(it.id, it.zIndex) } ?: 0) - 10
                newZOrders[elementId] = minZ
            }
            ZOrderAction.MOVE_UP -> {
                newZOrders[elementId] = currentZ + 10
            }
            ZOrderAction.MOVE_DOWN -> {
                newZOrders[elementId] = currentZ - 10
            }
        }

        pushState(
            currentState,
            currentState.editedTexts,
            currentState.photoUris,
            currentState.elementPositions,
            currentState.elementSizes,
            currentState.elementRotations,
            currentState.elementFlips,
            currentState.textFontSizes,
            currentState.textColors,
            currentState.textFontWeights,
            currentState.textAlignments,
            currentState.textLetterSpacings,
            currentState.textLineSpacings,
            currentState.textCases,
            currentState.textBgColors,
            currentState.textShadowColors,
            currentState.textFontFamilies,
            currentState.customFontUris,
            currentState.lockedElementIds,
            currentState.hiddenElementIds,
            newZOrders,
            currentState.bgGradientConfig,
            currentState.bgTextureType,
            currentState.elementGradients,
            currentState.elementStrokes,
            currentState.elementOpacities,
            currentState.elementEffects,
            currentState.deletedElementIds
        )
    }

    fun reorderLayerByOffset(elementId: String, delta: Int) {
        val currentState = _state.value ?: return
        val elements = currentState.template.elements.filter { !currentState.isElementDeleted(it.id) }
            .sortedByDescending { currentState.getElementZIndex(it.id, it.zIndex) }

        val currentIndex = elements.indexOfFirst { it.id == elementId }
        if (currentIndex == -1) return

        val targetIndex = (currentIndex + delta).coerceIn(0, elements.size - 1)
        if (currentIndex == targetIndex) return

        val newZOrders = currentState.elementZOrders.toMutableMap()
        val step = 10
        val baseZ = 1000

        val newOrderedList = elements.toMutableList().apply {
            val moved = removeAt(currentIndex)
            add(targetIndex, moved)
        }

        newOrderedList.reversed().forEachIndexed { index, elem ->
            newZOrders[elem.id] = baseZ + (index * step)
        }

        pushState(
            currentState,
            currentState.editedTexts,
            currentState.photoUris,
            currentState.elementPositions,
            currentState.elementSizes,
            currentState.elementRotations,
            currentState.elementFlips,
            currentState.textFontSizes,
            currentState.textColors,
            currentState.textFontWeights,
            currentState.textAlignments,
            currentState.textLetterSpacings,
            currentState.textLineSpacings,
            currentState.textCases,
            currentState.textBgColors,
            currentState.textShadowColors,
            currentState.textFontFamilies,
            currentState.customFontUris,
            currentState.lockedElementIds,
            currentState.hiddenElementIds,
            newZOrders,
            currentState.bgGradientConfig,
            currentState.bgTextureType,
            currentState.elementGradients,
            currentState.elementStrokes,
            currentState.elementOpacities,
            currentState.elementEffects,
            currentState.deletedElementIds
        )
    }

    fun updateElementGradient(elementId: String, config: GradientConfig?) {
        val currentState = _state.value ?: return
        val newGradients = currentState.elementGradients.toMutableMap().apply {
            if (config == null) remove(elementId) else put(elementId, config)
        }

        val isBg = elementId == "bg" || currentState.template.elements.find { it.id == elementId } is TemplateElement.Background

        pushState(
            currentState,
            currentState.editedTexts,
            currentState.photoUris,
            currentState.elementPositions,
            currentState.elementSizes,
            currentState.elementRotations,
            currentState.elementFlips,
            currentState.textFontSizes,
            currentState.textColors,
            currentState.textFontWeights,
            currentState.textAlignments,
            currentState.textLetterSpacings,
            currentState.textLineSpacings,
            currentState.textCases,
            currentState.textBgColors,
            currentState.textShadowColors,
            currentState.textFontFamilies,
            currentState.customFontUris,
            currentState.lockedElementIds,
            currentState.hiddenElementIds,
            currentState.elementZOrders,
            if (isBg) config else currentState.bgGradientConfig,
            currentState.bgTextureType,
            newGradients,
            currentState.elementStrokes,
            currentState.elementOpacities,
            currentState.elementEffects,
            currentState.deletedElementIds
        )
    }

    fun updateBgGradient(colors: List<String>?, angle: Float) {
        val config = if (colors != null) GradientConfig(colors = colors, angle = angle) else null
        updateElementGradient("bg", config)
    }

    fun updateTextColor(elementId: String, colorHex: String) {
        val currentState = _state.value ?: return
        val newColors = currentState.textColors.toMutableMap().apply {
            put(elementId, colorHex)
        }
        val newGradients = currentState.elementGradients.toMutableMap().apply {
            remove(elementId)
        }

        val isBg = elementId == "bg" || currentState.template.elements.find { it.id == elementId } is TemplateElement.Background

        pushState(
            currentState,
            currentState.editedTexts,
            currentState.photoUris,
            currentState.elementPositions,
            currentState.elementSizes,
            currentState.elementRotations,
            currentState.elementFlips,
            currentState.textFontSizes,
            newColors,
            currentState.textFontWeights,
            currentState.textAlignments,
            currentState.textLetterSpacings,
            currentState.textLineSpacings,
            currentState.textCases,
            currentState.textBgColors,
            currentState.textShadowColors,
            currentState.textFontFamilies,
            currentState.customFontUris,
            currentState.lockedElementIds,
            currentState.hiddenElementIds,
            currentState.elementZOrders,
            if (isBg) null else currentState.bgGradientConfig,
            currentState.bgTextureType,
            newGradients,
            currentState.elementStrokes,
            currentState.elementOpacities,
            currentState.elementEffects,
            currentState.deletedElementIds
        )
    }

    fun updateElementStroke(elementId: String, colorHex: String?, strokeWidthDp: Float) {
        val currentState = _state.value ?: return
        val newStrokes = currentState.elementStrokes.toMutableMap().apply {
            if (colorHex == null || strokeWidthDp <= 0f) remove(elementId) else put(elementId, Pair(colorHex, strokeWidthDp))
        }

        pushState(
            currentState,
            currentState.editedTexts,
            currentState.photoUris,
            currentState.elementPositions,
            currentState.elementSizes,
            currentState.elementRotations,
            currentState.elementFlips,
            currentState.textFontSizes,
            currentState.textColors,
            currentState.textFontWeights,
            currentState.textAlignments,
            currentState.textLetterSpacings,
            currentState.textLineSpacings,
            currentState.textCases,
            currentState.textBgColors,
            currentState.textShadowColors,
            currentState.textFontFamilies,
            currentState.customFontUris,
            currentState.lockedElementIds,
            currentState.hiddenElementIds,
            currentState.elementZOrders,
            currentState.bgGradientConfig,
            currentState.bgTextureType,
            currentState.elementGradients,
            newStrokes,
            currentState.elementOpacities,
            currentState.elementEffects,
            currentState.deletedElementIds
        )
    }

    fun updateElementOpacity(elementId: String, opacity: Float) {
        val currentState = _state.value ?: return
        val newOpacities = currentState.elementOpacities.toMutableMap().apply {
            put(elementId, opacity.coerceIn(0f, 1f))
        }

        pushState(
            currentState,
            currentState.editedTexts,
            currentState.photoUris,
            currentState.elementPositions,
            currentState.elementSizes,
            currentState.elementRotations,
            currentState.elementFlips,
            currentState.textFontSizes,
            currentState.textColors,
            currentState.textFontWeights,
            currentState.textAlignments,
            currentState.textLetterSpacings,
            currentState.textLineSpacings,
            currentState.textCases,
            currentState.textBgColors,
            currentState.textShadowColors,
            currentState.textFontFamilies,
            currentState.customFontUris,
            currentState.lockedElementIds,
            currentState.hiddenElementIds,
            currentState.elementZOrders,
            currentState.bgGradientConfig,
            currentState.bgTextureType,
            currentState.elementGradients,
            currentState.elementStrokes,
            newOpacities,
            currentState.elementEffects,
            currentState.deletedElementIds
        )
    }

    fun updateDropShadow(
        elementId: String,
        colorHex: String?,
        radiusDp: Float = 12f,
        angleDeg: Float = 45f,
        distanceDp: Float = 8f,
        spreadDp: Float = 0f,
        opacity: Float = 0.6f
    ) {
        val currentState = _state.value ?: return
        val currentEffect = currentState.getLayerEffect(elementId)
        val newEffect = currentEffect.copy(
            dropShadowColor = colorHex,
            dropShadowRadius = radiusDp,
            dropShadowAngle = angleDeg,
            dropShadowDistance = distanceDp,
            dropShadowSpread = spreadDp,
            dropShadowOpacity = opacity
        )
        val newEffects = currentState.elementEffects.toMutableMap().apply {
            put(elementId, newEffect)
        }

        pushState(
            currentState,
            currentState.editedTexts,
            currentState.photoUris,
            currentState.elementPositions,
            currentState.elementSizes,
            currentState.elementRotations,
            currentState.elementFlips,
            currentState.textFontSizes,
            currentState.textColors,
            currentState.textFontWeights,
            currentState.textAlignments,
            currentState.textLetterSpacings,
            currentState.textLineSpacings,
            currentState.textCases,
            currentState.textBgColors,
            currentState.textShadowColors,
            currentState.textFontFamilies,
            currentState.customFontUris,
            currentState.lockedElementIds,
            currentState.hiddenElementIds,
            currentState.elementZOrders,
            currentState.bgGradientConfig,
            currentState.bgTextureType,
            currentState.elementGradients,
            currentState.elementStrokes,
            currentState.elementOpacities,
            newEffects,
            currentState.deletedElementIds
        )
    }

    fun updateOuterGlow(
        elementId: String,
        colorHex: String?,
        radiusDp: Float = 16f,
        spreadDp: Float = 4f,
        opacity: Float = 0.8f
    ) {
        val currentState = _state.value ?: return
        val currentEffect = currentState.getLayerEffect(elementId)
        val newEffect = currentEffect.copy(
            outerGlowColor = colorHex,
            outerGlowRadius = radiusDp,
            outerGlowSpread = spreadDp,
            outerGlowOpacity = opacity
        )
        val newEffects = currentState.elementEffects.toMutableMap().apply {
            put(elementId, newEffect)
        }

        pushState(
            currentState,
            currentState.editedTexts,
            currentState.photoUris,
            currentState.elementPositions,
            currentState.elementSizes,
            currentState.elementRotations,
            currentState.elementFlips,
            currentState.textFontSizes,
            currentState.textColors,
            currentState.textFontWeights,
            currentState.textAlignments,
            currentState.textLetterSpacings,
            currentState.textLineSpacings,
            currentState.textCases,
            currentState.textBgColors,
            currentState.textShadowColors,
            currentState.textFontFamilies,
            currentState.customFontUris,
            currentState.lockedElementIds,
            currentState.hiddenElementIds,
            currentState.elementZOrders,
            currentState.bgGradientConfig,
            currentState.bgTextureType,
            currentState.elementGradients,
            currentState.elementStrokes,
            currentState.elementOpacities,
            newEffects,
            currentState.deletedElementIds
        )
    }

    fun updateInnerGlow(
        elementId: String,
        colorHex: String?,
        radiusDp: Float = 12f,
        chokeDp: Float = 2f,
        opacity: Float = 0.7f,
        source: String = "EDGE"
    ) {
        val currentState = _state.value ?: return
        val currentEffect = currentState.getLayerEffect(elementId)
        val newEffect = currentEffect.copy(
            innerGlowColor = colorHex,
            innerGlowRadius = radiusDp,
            innerGlowChoke = chokeDp,
            innerGlowOpacity = opacity,
            innerGlowSource = source
        )
        val newEffects = currentState.elementEffects.toMutableMap().apply {
            put(elementId, newEffect)
        }

        pushState(
            currentState,
            currentState.editedTexts,
            currentState.photoUris,
            currentState.elementPositions,
            currentState.elementSizes,
            currentState.elementRotations,
            currentState.elementFlips,
            currentState.textFontSizes,
            currentState.textColors,
            currentState.textFontWeights,
            currentState.textAlignments,
            currentState.textLetterSpacings,
            currentState.textLineSpacings,
            currentState.textCases,
            currentState.textBgColors,
            currentState.textShadowColors,
            currentState.textFontFamilies,
            currentState.customFontUris,
            currentState.lockedElementIds,
            currentState.hiddenElementIds,
            currentState.elementZOrders,
            currentState.bgGradientConfig,
            currentState.bgTextureType,
            currentState.elementGradients,
            currentState.elementStrokes,
            currentState.elementOpacities,
            newEffects,
            currentState.deletedElementIds
        )
    }

    fun addShapeElement(shapeType: String) {
        val currentState = _state.value ?: return
        val currentElements = currentState.template.elements

        val maxZ = (currentElements.maxOfOrNull { currentState.getElementZIndex(it.id, it.zIndex) } ?: 0) + 10
        val timestamp = System.currentTimeMillis() % 100000
        val newId = "shape_${shapeType.lowercase()}_$timestamp"

        val cornerRadius = if (shapeType.equals("ROUNDED_RECTANGLE", ignoreCase = true)) 16f else 0f
        val initX = 0.35f
        val initY = 0.38f
        val initW = 0.3f
        val initH = 0.24f

        val newShape = TemplateElement.Shape(
            id = newId,
            zIndex = maxZ,
            visible = true,
            opacity = 1.0f,
            x = initX,
            y = initY,
            width = initW,
            height = initH,
            rotation = 0f,
            shapeType = shapeType.uppercase(),
            color = "#C91F2C",
            cornerRadius = cornerRadius
        )

        val newPositions = currentState.elementPositions.toMutableMap().apply {
            put(newId, Pair(initX, initY))
        }
        val newSizes = currentState.elementSizes.toMutableMap().apply {
            put(newId, Pair(initW, initH))
        }
        val newZOrders = currentState.elementZOrders.toMutableMap().apply {
            put(newId, maxZ)
        }

        val updatedManifest = currentState.template.copy(
            elements = currentState.template.elements + newShape
        )

        pushState(
            currentState.copy(template = updatedManifest, selectedElementId = newId),
            currentState.editedTexts,
            currentState.photoUris,
            newPositions,
            newSizes,
            currentState.elementRotations,
            currentState.elementFlips,
            currentState.textFontSizes,
            currentState.textColors,
            currentState.textFontWeights,
            currentState.textAlignments,
            currentState.textLetterSpacings,
            currentState.textLineSpacings,
            currentState.textCases,
            currentState.textBgColors,
            currentState.textShadowColors,
            currentState.textFontFamilies,
            currentState.customFontUris,
            currentState.lockedElementIds,
            currentState.hiddenElementIds,
            newZOrders,
            currentState.bgGradientConfig,
            currentState.bgTextureType,
            currentState.elementGradients,
            currentState.elementStrokes,
            currentState.elementOpacities,
            currentState.elementEffects,
            currentState.deletedElementIds
        )
    }

    fun addImageElement(uri: Uri) {
        val currentState = _state.value ?: return
        val currentElements = currentState.template.elements

        val maxZ = (currentElements.maxOfOrNull { currentState.getElementZIndex(it.id, it.zIndex) } ?: 0) + 10
        val timestamp = System.currentTimeMillis() % 100000
        val newId = "inserted_photo_$timestamp"

        val initX = 0.25f
        val initY = 0.28f
        val initW = 0.5f
        val initH = 0.35f

        val newPhotoSlot = TemplateElement.PhotoSlot(
            id = newId,
            zIndex = maxZ,
            visible = true,
            opacity = 1.0f,
            x = initX,
            y = initY,
            width = initW,
            height = initH,
            rotation = 0f,
            defaultAsset = uri.toString(),
            cornerRadius = 12f,
            fitMode = "COVER",
            label = "Inserted Image"
        )

        val newPhotoUris = currentState.photoUris.toMutableMap().apply {
            put(newId, uri.toString())
        }
        val newPositions = currentState.elementPositions.toMutableMap().apply {
            put(newId, Pair(initX, initY))
        }
        val newSizes = currentState.elementSizes.toMutableMap().apply {
            put(newId, Pair(initW, initH))
        }
        val newZOrders = currentState.elementZOrders.toMutableMap().apply {
            put(newId, maxZ)
        }

        val updatedManifest = currentState.template.copy(
            elements = currentState.template.elements + newPhotoSlot
        )

        pushState(
            currentState.copy(template = updatedManifest, selectedElementId = newId),
            currentState.editedTexts,
            newPhotoUris,
            newPositions,
            newSizes,
            currentState.elementRotations,
            currentState.elementFlips,
            currentState.textFontSizes,
            currentState.textColors,
            currentState.textFontWeights,
            currentState.textAlignments,
            currentState.textLetterSpacings,
            currentState.textLineSpacings,
            currentState.textCases,
            currentState.textBgColors,
            currentState.textShadowColors,
            currentState.textFontFamilies,
            currentState.customFontUris,
            currentState.lockedElementIds,
            currentState.hiddenElementIds,
            newZOrders,
            currentState.bgGradientConfig,
            currentState.bgTextureType,
            currentState.elementGradients,
            currentState.elementStrokes,
            currentState.elementOpacities,
            currentState.elementEffects,
            currentState.deletedElementIds
        )
    }

    fun updateShapeType(elementId: String, shapeType: String) {
        val currentState = _state.value ?: return
        val elements = currentState.template.elements.map { elem ->
            if (elem.id == elementId && elem is TemplateElement.Shape) {
                elem.copy(shapeType = shapeType.uppercase())
            } else {
                elem
            }
        }
        val updatedManifest = currentState.template.copy(elements = elements)
        _state.value = currentState.copy(template = updatedManifest)
        commitGestureEnd()
    }

    fun updateShapeCornerRadius(elementId: String, radiusDp: Float) {
        val currentState = _state.value ?: return
        val elements = currentState.template.elements.map { elem ->
            if (elem.id == elementId && elem is TemplateElement.Shape) {
                elem.copy(
                    cornerRadius = radiusDp,
                    topLeftRadius = radiusDp,
                    topRightRadius = radiusDp,
                    bottomRightRadius = radiusDp,
                    bottomLeftRadius = radiusDp,
                    isCornerLinked = true
                )
            } else {
                elem
            }
        }
        val updatedManifest = currentState.template.copy(elements = elements)
        _state.value = currentState.copy(template = updatedManifest)
        commitGestureEnd()
    }

    fun updateShapeCornerRadii(
        elementId: String,
        topLeftDp: Float,
        topRightDp: Float,
        bottomRightDp: Float,
        bottomLeftDp: Float,
        isLinked: Boolean
    ) {
        val currentState = _state.value ?: return
        val elements = currentState.template.elements.map { elem ->
            if (elem.id == elementId && elem is TemplateElement.Shape) {
                elem.copy(
                    topLeftRadius = topLeftDp,
                    topRightRadius = topRightDp,
                    bottomRightRadius = bottomRightDp,
                    bottomLeftRadius = bottomLeftDp,
                    isCornerLinked = isLinked
                )
            } else {
                elem
            }
        }
        val updatedManifest = currentState.template.copy(elements = elements)
        _state.value = currentState.copy(template = updatedManifest)
        commitGestureEnd()
    }

    fun updateShapeGeometry(
        elementId: String,
        polygonSides: Int = 5,
        starPoints: Int = 5,
        starInnerRadiusRatio: Float = 0.5f,
        customPathData: String? = null
    ) {
        val currentState = _state.value ?: return
        val elements = currentState.template.elements.map { elem ->
            if (elem.id == elementId && elem is TemplateElement.Shape) {
                elem.copy(
                    polygonSides = polygonSides.coerceIn(3, 16),
                    starPoints = starPoints.coerceIn(3, 16),
                    starInnerRadiusRatio = starInnerRadiusRatio.coerceIn(0.1f, 0.9f),
                    customPathData = customPathData ?: elem.customPathData
                )
            } else {
                elem
            }
        }
        val updatedManifest = currentState.template.copy(elements = elements)
        _state.value = currentState.copy(template = updatedManifest)
        commitGestureEnd()
    }

    fun updateShapeStrokeStyle(
        elementId: String,
        strokeStyle: String = "SOLID",
        strokeCap: String = "BUTT",
        strokeJoin: String = "MITER",
        strokeOpacity: Float = 1.0f
    ) {
        val currentState = _state.value ?: return
        val elements = currentState.template.elements.map { elem ->
            if (elem.id == elementId && elem is TemplateElement.Shape) {
                elem.copy(
                    strokeStyle = strokeStyle.uppercase(),
                    strokeCap = strokeCap.uppercase(),
                    strokeJoin = strokeJoin.uppercase(),
                    strokeOpacity = strokeOpacity.coerceIn(0f, 1f)
                )
            } else {
                elem
            }
        }
        val updatedManifest = currentState.template.copy(elements = elements)
        _state.value = currentState.copy(template = updatedManifest)
        commitGestureEnd()
    }

    fun updateShapeTransform(
        elementId: String,
        x: Float? = null,
        y: Float? = null,
        width: Float? = null,
        height: Float? = null,
        scaleX: Float? = null,
        scaleY: Float? = null,
        skewX: Float? = null,
        skewY: Float? = null,
        rotation: Float? = null,
        isConstrained: Boolean? = null
    ) {
        val currentState = _state.value ?: return
        val elements = currentState.template.elements.map { elem ->
            if (elem.id == elementId && elem is TemplateElement.Shape) {
                elem.copy(
                    x = x ?: elem.x,
                    y = y ?: elem.y,
                    width = width ?: elem.width,
                    height = height ?: elem.height,
                    scaleX = scaleX ?: elem.scaleX,
                    scaleY = scaleY ?: elem.scaleY,
                    skewX = skewX ?: elem.skewX,
                    skewY = skewY ?: elem.skewY,
                    rotation = rotation ?: elem.rotation,
                    isConstrained = isConstrained ?: elem.isConstrained
                )
            } else {
                elem
            }
        }
        val newPositions = if (x != null || y != null) {
            currentState.elementPositions.toMutableMap().apply {
                put(elementId, Pair(x ?: currentState.getElementX(elementId, 0f), y ?: currentState.getElementY(elementId, 0f)))
            }
        } else currentState.elementPositions

        val newSizes = if (width != null || height != null) {
            currentState.elementSizes.toMutableMap().apply {
                put(elementId, Pair(width ?: currentState.getElementW(elementId, 0.1f), height ?: currentState.getElementH(elementId, 0.1f)))
            }
        } else currentState.elementSizes

        val newRotations = if (rotation != null) {
            currentState.elementRotations.toMutableMap().apply {
                put(elementId, rotation)
            }
        } else currentState.elementRotations

        val updatedManifest = currentState.template.copy(elements = elements)
        _state.value = currentState.copy(
            template = updatedManifest,
            elementPositions = newPositions,
            elementSizes = newSizes,
            elementRotations = newRotations
        )
        commitGestureEnd()
    }

    fun updateShapeFillType(elementId: String, fillType: String) {
        val currentState = _state.value ?: return
        val elements = currentState.template.elements.map { elem ->
            if (elem.id == elementId && elem is TemplateElement.Shape) {
                elem.copy(fillType = fillType.uppercase())
            } else {
                elem
            }
        }
        val updatedManifest = currentState.template.copy(elements = elements)
        _state.value = currentState.copy(template = updatedManifest)
        commitGestureEnd()
    }

    fun swapShapeFillAndStroke(elementId: String) {
        val currentState = _state.value ?: return
        val shape = currentState.template.elements.find { it.id == elementId } as? TemplateElement.Shape ?: return

        val currentFill = currentState.getTextColor(elementId, shape.color)
        val currentStroke = currentState.getElementStroke(elementId)

        val newStrokeHex = currentFill
        val newFillHex = currentStroke?.first ?: shape.borderColor ?: "#C91F2C"
        val strokeW = currentStroke?.second ?: (if (shape.borderWidth > 0f) shape.borderWidth else 4f)

        updateTextColor(elementId, newFillHex)
        updateElementStroke(elementId, newStrokeHex, strokeW)
    }

    fun addSavedSwatch(colorHex: String) {
        val currentState = _state.value ?: return
        val newSwatches = (listOf(colorHex) + currentState.savedSwatches).distinct().take(15)
        _state.value = currentState.copy(savedSwatches = newSwatches)
    }

    fun setEyedropperActive(active: Boolean) {
        _state.value = _state.value?.copy(isEyedropperActive = active)
    }

    fun updateEyedropperSample(colorHex: String?, touchX: Float = 0f, touchY: Float = 0f) {
        _state.value = _state.value?.copy(
            eyedropperSampledColor = colorHex,
            eyedropperTouchPos = Pair(touchX, touchY)
        )
    }

    fun updateBgTexture(textureType: String) {
        val currentState = _state.value ?: return
        pushState(
            currentState,
            currentState.editedTexts,
            currentState.photoUris,
            currentState.elementPositions,
            currentState.elementSizes,
            currentState.elementRotations,
            currentState.elementFlips,
            currentState.textFontSizes,
            currentState.textColors,
            currentState.textFontWeights,
            currentState.textAlignments,
            currentState.textLetterSpacings,
            currentState.textLineSpacings,
            currentState.textCases,
            currentState.textBgColors,
            currentState.textShadowColors,
            currentState.textFontFamilies,
            currentState.customFontUris,
            currentState.lockedElementIds,
            currentState.hiddenElementIds,
            currentState.elementZOrders,
            currentState.bgGradientConfig,
            textureType,
            currentState.elementGradients,
            currentState.elementStrokes,
            currentState.elementOpacities,
            currentState.elementEffects,
            currentState.deletedElementIds
        )
    }

    fun toggleElementFlipX(elementId: String) {
        val currentState = _state.value ?: return
        val currentFlip = currentState.elementFlips[elementId] ?: Pair(false, false)
        val newFlips = currentState.elementFlips.toMutableMap().apply {
            put(elementId, Pair(!currentFlip.first, currentFlip.second))
        }

        pushState(
            currentState,
            currentState.editedTexts,
            currentState.photoUris,
            currentState.elementPositions,
            currentState.elementSizes,
            currentState.elementRotations,
            newFlips,
            currentState.textFontSizes,
            currentState.textColors,
            currentState.textFontWeights,
            currentState.textAlignments,
            currentState.textLetterSpacings,
            currentState.textLineSpacings,
            currentState.textCases,
            currentState.textBgColors,
            currentState.textShadowColors,
            currentState.textFontFamilies,
            currentState.customFontUris,
            currentState.lockedElementIds,
            currentState.hiddenElementIds,
            currentState.elementZOrders,
            currentState.bgGradientConfig,
            currentState.bgTextureType,
            currentState.elementGradients,
            currentState.elementStrokes,
            currentState.elementOpacities,
            currentState.elementEffects,
            currentState.deletedElementIds
        )
    }

    fun toggleElementFlipY(elementId: String) {
        val currentState = _state.value ?: return
        val currentFlip = currentState.elementFlips[elementId] ?: Pair(false, false)
        val newFlips = currentState.elementFlips.toMutableMap().apply {
            put(elementId, Pair(currentFlip.first, !currentFlip.second))
        }

        pushState(
            currentState,
            currentState.editedTexts,
            currentState.photoUris,
            currentState.elementPositions,
            currentState.elementSizes,
            currentState.elementRotations,
            newFlips,
            currentState.textFontSizes,
            currentState.textColors,
            currentState.textFontWeights,
            currentState.textAlignments,
            currentState.textLetterSpacings,
            currentState.textLineSpacings,
            currentState.textCases,
            currentState.textBgColors,
            currentState.textShadowColors,
            currentState.textFontFamilies,
            currentState.customFontUris,
            currentState.lockedElementIds,
            currentState.hiddenElementIds,
            currentState.elementZOrders,
            currentState.bgGradientConfig,
            currentState.bgTextureType,
            currentState.elementGradients,
            currentState.elementStrokes,
            currentState.elementOpacities,
            currentState.elementEffects,
            currentState.deletedElementIds
        )
    }

    fun updateText(elementId: String, newText: String) {
        val currentState = _state.value ?: return
        val newTexts = currentState.editedTexts.toMutableMap().apply {
            put(elementId, newText)
        }

        pushState(
            currentState,
            newTexts,
            currentState.photoUris,
            currentState.elementPositions,
            currentState.elementSizes,
            currentState.elementRotations,
            currentState.elementFlips,
            currentState.textFontSizes,
            currentState.textColors,
            currentState.textFontWeights,
            currentState.textAlignments,
            currentState.textLetterSpacings,
            currentState.textLineSpacings,
            currentState.textCases,
            currentState.textBgColors,
            currentState.textShadowColors,
            currentState.textFontFamilies,
            currentState.customFontUris,
            currentState.lockedElementIds,
            currentState.hiddenElementIds,
            currentState.elementZOrders,
            currentState.bgGradientConfig,
            currentState.bgTextureType,
            currentState.elementGradients,
            currentState.elementStrokes,
            currentState.elementOpacities,
            currentState.elementEffects,
            currentState.deletedElementIds
        )
    }

    fun updatePhoto(slotId: String, uri: Uri) {
        val currentState = _state.value ?: return
        val newPhotos = currentState.photoUris.toMutableMap().apply {
            put(slotId, uri.toString())
        }

        pushState(
            currentState,
            currentState.editedTexts,
            newPhotos,
            currentState.elementPositions,
            currentState.elementSizes,
            currentState.elementRotations,
            currentState.elementFlips,
            currentState.textFontSizes,
            currentState.textColors,
            currentState.textFontWeights,
            currentState.textAlignments,
            currentState.textLetterSpacings,
            currentState.textLineSpacings,
            currentState.textCases,
            currentState.textBgColors,
            currentState.textShadowColors,
            currentState.textFontFamilies,
            currentState.customFontUris,
            currentState.lockedElementIds,
            currentState.hiddenElementIds,
            currentState.elementZOrders,
            currentState.bgGradientConfig,
            currentState.bgTextureType,
            currentState.elementGradients,
            currentState.elementStrokes,
            currentState.elementOpacities,
            currentState.elementEffects,
            currentState.deletedElementIds
        )
    }

    fun updateElementPosition(elementId: String, newX: Float, newY: Float) {
        val currentState = _state.value ?: return
        if (currentState.isElementLocked(elementId)) return

        val newPositions = currentState.elementPositions.toMutableMap().apply {
            put(elementId, Pair(newX, newY))
        }

        _state.value = currentState.copy(
            elementPositions = newPositions,
            selectedElementId = elementId
        )
    }

    fun updateElementSize(elementId: String, newW: Float, newH: Float) {
        val currentState = _state.value ?: return
        if (currentState.isElementLocked(elementId)) return

        val newSizes = currentState.elementSizes.toMutableMap().apply {
            put(elementId, Pair(newW, newH))
        }

        _state.value = currentState.copy(
            elementSizes = newSizes,
            selectedElementId = elementId
        )
    }

    fun updateElementRotation(elementId: String, rotation: Float) {
        val currentState = _state.value ?: return
        if (currentState.isElementLocked(elementId)) return

        val normalizedRotation = (rotation % 360f + 360f) % 360f
        val newRotations = currentState.elementRotations.toMutableMap().apply {
            put(elementId, normalizedRotation)
        }

        _state.value = currentState.copy(
            elementRotations = newRotations,
            selectedElementId = elementId
        )
    }

    fun updateFontSize(elementId: String, newSizeSp: Float) {
        val currentState = _state.value ?: return
        val newSizes = currentState.textFontSizes.toMutableMap().apply {
            put(elementId, newSizeSp)
        }

        pushState(
            currentState,
            currentState.editedTexts,
            currentState.photoUris,
            currentState.elementPositions,
            currentState.elementSizes,
            currentState.elementRotations,
            currentState.elementFlips,
            newSizes,
            currentState.textColors,
            currentState.textFontWeights,
            currentState.textAlignments,
            currentState.textLetterSpacings,
            currentState.textLineSpacings,
            currentState.textCases,
            currentState.textBgColors,
            currentState.textShadowColors,
            currentState.textFontFamilies,
            currentState.customFontUris,
            currentState.lockedElementIds,
            currentState.hiddenElementIds,
            currentState.elementZOrders,
            currentState.bgGradientConfig,
            currentState.bgTextureType,
            currentState.elementGradients,
            currentState.elementStrokes,
            currentState.elementOpacities,
            currentState.elementEffects,
            currentState.deletedElementIds
        )
    }

    fun updateFontFamily(elementId: String, fontFamily: String) {
        val currentState = _state.value ?: return
        val newFonts = currentState.textFontFamilies.toMutableMap().apply {
            put(elementId, fontFamily)
        }

        pushState(
            currentState,
            currentState.editedTexts,
            currentState.photoUris,
            currentState.elementPositions,
            currentState.elementSizes,
            currentState.elementRotations,
            currentState.elementFlips,
            currentState.textFontSizes,
            currentState.textColors,
            currentState.textFontWeights,
            currentState.textAlignments,
            currentState.textLetterSpacings,
            currentState.textLineSpacings,
            currentState.textCases,
            currentState.textBgColors,
            currentState.textShadowColors,
            newFonts,
            currentState.customFontUris,
            currentState.lockedElementIds,
            currentState.hiddenElementIds,
            currentState.elementZOrders,
            currentState.bgGradientConfig,
            currentState.bgTextureType,
            currentState.elementGradients,
            currentState.elementStrokes,
            currentState.elementOpacities,
            currentState.elementEffects,
            currentState.deletedElementIds
        )
    }

    fun updateFontWeight(elementId: String, weight: String) {
        val currentState = _state.value ?: return
        val newWeights = currentState.textFontWeights.toMutableMap().apply {
            put(elementId, weight)
        }

        pushState(
            currentState,
            currentState.editedTexts,
            currentState.photoUris,
            currentState.elementPositions,
            currentState.elementSizes,
            currentState.elementRotations,
            currentState.elementFlips,
            currentState.textFontSizes,
            currentState.textColors,
            newWeights,
            currentState.textAlignments,
            currentState.textLetterSpacings,
            currentState.textLineSpacings,
            currentState.textCases,
            currentState.textBgColors,
            currentState.textShadowColors,
            currentState.textFontFamilies,
            currentState.customFontUris,
            currentState.lockedElementIds,
            currentState.hiddenElementIds,
            currentState.elementZOrders,
            currentState.bgGradientConfig,
            currentState.bgTextureType,
            currentState.elementGradients,
            currentState.elementStrokes,
            currentState.elementOpacities,
            currentState.elementEffects,
            currentState.deletedElementIds
        )
    }

    fun updateTextAlignment(elementId: String, alignment: String) {
        val currentState = _state.value ?: return
        val newAlignments = currentState.textAlignments.toMutableMap().apply {
            put(elementId, alignment)
        }

        pushState(
            currentState,
            currentState.editedTexts,
            currentState.photoUris,
            currentState.elementPositions,
            currentState.elementSizes,
            currentState.elementRotations,
            currentState.elementFlips,
            currentState.textFontSizes,
            currentState.textColors,
            currentState.textFontWeights,
            newAlignments,
            currentState.textLetterSpacings,
            currentState.textLineSpacings,
            currentState.textCases,
            currentState.textBgColors,
            currentState.textShadowColors,
            currentState.textFontFamilies,
            currentState.customFontUris,
            currentState.lockedElementIds,
            currentState.hiddenElementIds,
            currentState.elementZOrders,
            currentState.bgGradientConfig,
            currentState.bgTextureType,
            currentState.elementGradients,
            currentState.elementStrokes,
            currentState.elementOpacities,
            currentState.elementEffects,
            currentState.deletedElementIds
        )
    }

    fun updateTextLetterSpacing(elementId: String, letterSpacing: Float) {
        val currentState = _state.value ?: return
        val newSpacings = currentState.textLetterSpacings.toMutableMap().apply {
            put(elementId, letterSpacing)
        }

        pushState(
            currentState,
            currentState.editedTexts,
            currentState.photoUris,
            currentState.elementPositions,
            currentState.elementSizes,
            currentState.elementRotations,
            currentState.elementFlips,
            currentState.textFontSizes,
            currentState.textColors,
            currentState.textFontWeights,
            currentState.textAlignments,
            newSpacings,
            currentState.textLineSpacings,
            currentState.textCases,
            currentState.textBgColors,
            currentState.textShadowColors,
            currentState.textFontFamilies,
            currentState.customFontUris,
            currentState.lockedElementIds,
            currentState.hiddenElementIds,
            currentState.elementZOrders,
            currentState.bgGradientConfig,
            currentState.bgTextureType,
            currentState.elementGradients,
            currentState.elementStrokes,
            currentState.elementOpacities,
            currentState.elementEffects,
            currentState.deletedElementIds
        )
    }

    fun updateTextLineSpacing(elementId: String, lineSpacing: Float) {
        val currentState = _state.value ?: return
        val newSpacings = currentState.textLineSpacings.toMutableMap().apply {
            put(elementId, lineSpacing)
        }

        pushState(
            currentState,
            currentState.editedTexts,
            currentState.photoUris,
            currentState.elementPositions,
            currentState.elementSizes,
            currentState.elementRotations,
            currentState.elementFlips,
            currentState.textFontSizes,
            currentState.textColors,
            currentState.textFontWeights,
            currentState.textAlignments,
            currentState.textLetterSpacings,
            newSpacings,
            currentState.textCases,
            currentState.textBgColors,
            currentState.textShadowColors,
            currentState.textFontFamilies,
            currentState.customFontUris,
            currentState.lockedElementIds,
            currentState.hiddenElementIds,
            currentState.elementZOrders,
            currentState.bgGradientConfig,
            currentState.bgTextureType,
            currentState.elementGradients,
            currentState.elementStrokes,
            currentState.elementOpacities,
            currentState.elementEffects,
            currentState.deletedElementIds
        )
    }

    fun toggleTextCase(elementId: String) {
        val currentState = _state.value ?: return
        val currentCase = currentState.textCases[elementId] ?: "NORMAL"
        val nextCase = when (currentCase.uppercase()) {
            "NORMAL" -> "UPPERCASE"
            "UPPERCASE" -> "LOWERCASE"
            else -> "NORMAL"
        }
        val newCases = currentState.textCases.toMutableMap().apply {
            put(elementId, nextCase)
        }

        pushState(
            currentState,
            currentState.editedTexts,
            currentState.photoUris,
            currentState.elementPositions,
            currentState.elementSizes,
            currentState.elementRotations,
            currentState.elementFlips,
            currentState.textFontSizes,
            currentState.textColors,
            currentState.textFontWeights,
            currentState.textAlignments,
            currentState.textLetterSpacings,
            currentState.textLineSpacings,
            newCases,
            currentState.textBgColors,
            currentState.textShadowColors,
            currentState.textFontFamilies,
            currentState.customFontUris,
            currentState.lockedElementIds,
            currentState.hiddenElementIds,
            currentState.elementZOrders,
            currentState.bgGradientConfig,
            currentState.bgTextureType,
            currentState.elementGradients,
            currentState.elementStrokes,
            currentState.elementOpacities,
            currentState.elementEffects,
            currentState.deletedElementIds
        )
    }

    fun updateTextBgColor(elementId: String, colorHex: String?) {
        val currentState = _state.value ?: return
        val newBgs = currentState.textBgColors.toMutableMap().apply {
            if (colorHex == null) remove(elementId) else put(elementId, colorHex)
        }

        pushState(
            currentState,
            currentState.editedTexts,
            currentState.photoUris,
            currentState.elementPositions,
            currentState.elementSizes,
            currentState.elementRotations,
            currentState.elementFlips,
            currentState.textFontSizes,
            currentState.textColors,
            currentState.textFontWeights,
            currentState.textAlignments,
            currentState.textLetterSpacings,
            currentState.textLineSpacings,
            currentState.textCases,
            newBgs,
            currentState.textShadowColors,
            currentState.textFontFamilies,
            currentState.customFontUris,
            currentState.lockedElementIds,
            currentState.hiddenElementIds,
            currentState.elementZOrders,
            currentState.bgGradientConfig,
            currentState.bgTextureType,
            currentState.elementGradients,
            currentState.elementStrokes,
            currentState.elementOpacities,
            currentState.elementEffects,
            currentState.deletedElementIds
        )
    }

    fun updateTextShadowColor(elementId: String, colorHex: String?) {
        val currentState = _state.value ?: return
        val newShadows = currentState.textShadowColors.toMutableMap().apply {
            if (colorHex == null) remove(elementId) else put(elementId, colorHex)
        }

        pushState(
            currentState,
            currentState.editedTexts,
            currentState.photoUris,
            currentState.elementPositions,
            currentState.elementSizes,
            currentState.elementRotations,
            currentState.elementFlips,
            currentState.textFontSizes,
            currentState.textColors,
            currentState.textFontWeights,
            currentState.textAlignments,
            currentState.textLetterSpacings,
            currentState.textLineSpacings,
            currentState.textCases,
            currentState.textBgColors,
            newShadows,
            currentState.textFontFamilies,
            currentState.customFontUris,
            currentState.lockedElementIds,
            currentState.hiddenElementIds,
            currentState.elementZOrders,
            currentState.bgGradientConfig,
            currentState.bgTextureType,
            currentState.elementGradients,
            currentState.elementStrokes,
            currentState.elementOpacities,
            currentState.elementEffects,
            currentState.deletedElementIds
        )
    }

    fun setExportFormat(format: ExportFormat) {
        _state.value = _state.value?.copy(exportFormat = format)
    }

    fun setExportScale(scale: ExportScale) {
        _state.value = _state.value?.copy(exportScale = scale)
    }

    fun commitGestureEnd() {
        val currentState = _state.value ?: return
        pushState(
            currentState,
            currentState.editedTexts,
            currentState.photoUris,
            currentState.elementPositions,
            currentState.elementSizes,
            currentState.elementRotations,
            currentState.elementFlips,
            currentState.textFontSizes,
            currentState.textColors,
            currentState.textFontWeights,
            currentState.textAlignments,
            currentState.textLetterSpacings,
            currentState.textLineSpacings,
            currentState.textCases,
            currentState.textBgColors,
            currentState.textShadowColors,
            currentState.textFontFamilies,
            currentState.customFontUris,
            currentState.lockedElementIds,
            currentState.hiddenElementIds,
            currentState.elementZOrders,
            currentState.bgGradientConfig,
            currentState.bgTextureType,
            currentState.elementGradients,
            currentState.elementStrokes,
            currentState.elementOpacities,
            currentState.elementEffects,
            currentState.deletedElementIds
        )
    }

    fun duplicateElement(elementId: String) {
        val currentState = _state.value ?: return
        val element = currentState.template.elements.find { it.id == elementId } ?: return

        val currentX = currentState.getElementX(elementId, when (element) {
            is TemplateElement.Text -> element.x
            is TemplateElement.Shape -> element.x
            is TemplateElement.PhotoSlot -> element.x
            is TemplateElement.PhotoCard -> element.x
            is TemplateElement.PillCta -> element.x
            else -> 0.1f
        })

        val currentY = currentState.getElementY(elementId, when (element) {
            is TemplateElement.Text -> element.y
            is TemplateElement.Shape -> element.y
            is TemplateElement.PhotoSlot -> element.y
            is TemplateElement.PhotoCard -> element.y
            is TemplateElement.PillCta -> element.y
            else -> 0.1f
        })

        val duplicatedId = "${elementId}_copy_${System.currentTimeMillis() % 10000}"
        val shiftedX = (currentX + 0.04f).coerceAtMost(0.8f)
        val shiftedY = (currentY + 0.04f).coerceAtMost(0.8f)

        val newPositions = currentState.elementPositions.toMutableMap().apply {
            put(duplicatedId, Pair(shiftedX, shiftedY))
        }

        pushState(
            currentState,
            currentState.editedTexts,
            currentState.photoUris,
            newPositions,
            currentState.elementSizes,
            currentState.elementRotations,
            currentState.elementFlips,
            currentState.textFontSizes,
            currentState.textColors,
            currentState.textFontWeights,
            currentState.textAlignments,
            currentState.textLetterSpacings,
            currentState.textLineSpacings,
            currentState.textCases,
            currentState.textBgColors,
            currentState.textShadowColors,
            currentState.textFontFamilies,
            currentState.customFontUris,
            currentState.lockedElementIds,
            currentState.hiddenElementIds,
            currentState.elementZOrders,
            currentState.bgGradientConfig,
            currentState.bgTextureType,
            currentState.elementGradients,
            currentState.elementStrokes,
            currentState.elementOpacities,
            currentState.elementEffects,
            currentState.deletedElementIds
        )

        selectElement(duplicatedId)
    }

    fun deleteElement(elementId: String) {
        val currentState = _state.value ?: return
        val newDeleted = currentState.deletedElementIds.toMutableSet().apply {
            add(elementId)
        }

        pushState(
            currentState,
            currentState.editedTexts,
            currentState.photoUris,
            currentState.elementPositions,
            currentState.elementSizes,
            currentState.elementRotations,
            currentState.elementFlips,
            currentState.textFontSizes,
            currentState.textColors,
            currentState.textFontWeights,
            currentState.textAlignments,
            currentState.textLetterSpacings,
            currentState.textLineSpacings,
            currentState.textCases,
            currentState.textBgColors,
            currentState.textShadowColors,
            currentState.textFontFamilies,
            currentState.customFontUris,
            currentState.lockedElementIds,
            currentState.hiddenElementIds,
            currentState.elementZOrders,
            currentState.bgGradientConfig,
            currentState.bgTextureType,
            currentState.elementGradients,
            currentState.elementStrokes,
            currentState.elementOpacities,
            currentState.elementEffects,
            newDeleted
        )

        selectElement(null)
    }

    fun setExportResolution(resolution: ExportResolution) {
        _state.value = _state.value?.copy(exportResolution = resolution)
    }

    private fun pushState(
        currentState: MagicStoryEditorState,
        newTexts: Map<String, String>,
        newPhotos: Map<String, String>,
        newPositions: Map<String, Pair<Float, Float>>,
        newSizes: Map<String, Pair<Float, Float>>,
        newRotations: Map<String, Float>,
        newFlips: Map<String, Pair<Boolean, Boolean>>,
        newSizesSp: Map<String, Float>,
        newColors: Map<String, String>,
        newWeights: Map<String, String>,
        newAlignments: Map<String, String>,
        newLetterSpacings: Map<String, Float>,
        newLineSpacings: Map<String, Float>,
        newCases: Map<String, String>,
        newTextBgs: Map<String, String>,
        newTextShadows: Map<String, String>,
        newFontFamilies: Map<String, String>,
        newCustomFonts: Map<String, String>,
        newLocked: Set<String>,
        newHidden: Set<String>,
        newZOrders: Map<String, Int>,
        newBgGradientConfig: GradientConfig?,
        newBgTexture: String,
        newGradients: Map<String, GradientConfig>,
        newStrokes: Map<String, Pair<String, Float>>,
        newOpacities: Map<String, Float>,
        newEffects: Map<String, LayerEffectConfig>,
        newDeleted: Set<String>
    ) {
        val newSnapshot = EditorSnapshot(
            editedTexts = newTexts,
            photoUris = newPhotos,
            elementPositions = newPositions,
            elementSizes = newSizes,
            elementRotations = newRotations,
            elementFlips = newFlips,
            textFontSizes = newSizesSp,
            textColors = newColors,
            textFontWeights = newWeights,
            textAlignments = newAlignments,
            textLetterSpacings = newLetterSpacings,
            textLineSpacings = newLineSpacings,
            textCases = newCases,
            textBgColors = newTextBgs,
            textShadowColors = newTextShadows,
            textFontFamilies = newFontFamilies,
            customFontUris = newCustomFonts,
            lockedElementIds = newLocked,
            hiddenElementIds = newHidden,
            elementZOrders = newZOrders,
            bgGradientConfig = newBgGradientConfig,
            bgTextureType = newBgTexture,
            elementGradients = newGradients,
            elementStrokes = newStrokes,
            elementOpacities = newOpacities,
            elementEffects = newEffects,
            deletedElementIds = newDeleted
        )
        val trimmedHistory = currentState.history.take(currentState.historyIndex + 1) + newSnapshot

        _state.value = currentState.copy(
            editedTexts = newTexts,
            photoUris = newPhotos,
            elementPositions = newPositions,
            elementSizes = newSizes,
            elementRotations = newRotations,
            elementFlips = newFlips,
            textFontSizes = newSizesSp,
            textColors = newColors,
            textFontWeights = newWeights,
            textAlignments = newAlignments,
            textLetterSpacings = newLetterSpacings,
            textLineSpacings = newLineSpacings,
            textCases = newCases,
            textBgColors = newTextBgs,
            textShadowColors = newTextShadows,
            textFontFamilies = newFontFamilies,
            customFontUris = newCustomFonts,
            lockedElementIds = newLocked,
            hiddenElementIds = newHidden,
            elementZOrders = newZOrders,
            bgGradientConfig = newBgGradientConfig,
            bgTextureType = newBgTexture,
            elementGradients = newGradients,
            elementStrokes = newStrokes,
            elementOpacities = newOpacities,
            elementEffects = newEffects,
            deletedElementIds = newDeleted,
            history = trimmedHistory,
            historyIndex = trimmedHistory.size - 1
        )
    }

    fun undo() {
        val currentState = _state.value ?: return
        if (currentState.canUndo) {
            val prevIndex = currentState.historyIndex - 1
            val prevSnapshot = currentState.history[prevIndex]
            _state.value = currentState.copy(
                editedTexts = prevSnapshot.editedTexts,
                photoUris = prevSnapshot.photoUris,
                elementPositions = prevSnapshot.elementPositions,
                elementSizes = prevSnapshot.elementSizes,
                elementRotations = prevSnapshot.elementRotations,
                elementFlips = prevSnapshot.elementFlips,
                textFontSizes = prevSnapshot.textFontSizes,
                textColors = prevSnapshot.textColors,
                textFontWeights = prevSnapshot.textFontWeights,
                textAlignments = prevSnapshot.textAlignments,
                textLetterSpacings = prevSnapshot.textLetterSpacings,
                textLineSpacings = prevSnapshot.textLineSpacings,
                textCases = prevSnapshot.textCases,
                textBgColors = prevSnapshot.textBgColors,
                textShadowColors = prevSnapshot.textShadowColors,
                textFontFamilies = prevSnapshot.textFontFamilies,
                customFontUris = prevSnapshot.customFontUris,
                lockedElementIds = prevSnapshot.lockedElementIds,
                hiddenElementIds = prevSnapshot.hiddenElementIds,
                elementZOrders = prevSnapshot.elementZOrders,
                bgGradientConfig = prevSnapshot.bgGradientConfig,
                bgTextureType = prevSnapshot.bgTextureType,
                elementGradients = prevSnapshot.elementGradients,
                elementStrokes = prevSnapshot.elementStrokes,
                elementOpacities = prevSnapshot.elementOpacities,
                elementEffects = prevSnapshot.elementEffects,
                deletedElementIds = prevSnapshot.deletedElementIds,
                historyIndex = prevIndex
            )
        }
    }

    fun redo() {
        val currentState = _state.value ?: return
        if (currentState.canRedo) {
            val nextIndex = currentState.historyIndex + 1
            val nextSnapshot = currentState.history[nextIndex]
            _state.value = currentState.copy(
                editedTexts = nextSnapshot.editedTexts,
                photoUris = nextSnapshot.photoUris,
                elementPositions = nextSnapshot.elementPositions,
                elementSizes = nextSnapshot.elementSizes,
                elementRotations = nextSnapshot.elementRotations,
                elementFlips = nextSnapshot.elementFlips,
                textFontSizes = nextSnapshot.textFontSizes,
                textColors = nextSnapshot.textColors,
                textFontWeights = nextSnapshot.textFontWeights,
                textAlignments = nextSnapshot.textAlignments,
                textLetterSpacings = nextSnapshot.textLetterSpacings,
                textLineSpacings = nextSnapshot.textLineSpacings,
                textCases = nextSnapshot.textCases,
                textBgColors = nextSnapshot.textBgColors,
                textShadowColors = nextSnapshot.textShadowColors,
                textFontFamilies = nextSnapshot.textFontFamilies,
                customFontUris = nextSnapshot.customFontUris,
                lockedElementIds = nextSnapshot.lockedElementIds,
                hiddenElementIds = nextSnapshot.hiddenElementIds,
                elementZOrders = nextSnapshot.elementZOrders,
                bgGradientConfig = nextSnapshot.bgGradientConfig,
                bgTextureType = nextSnapshot.bgTextureType,
                elementGradients = nextSnapshot.elementGradients,
                elementStrokes = nextSnapshot.elementStrokes,
                elementOpacities = nextSnapshot.elementOpacities,
                elementEffects = nextSnapshot.elementEffects,
                deletedElementIds = nextSnapshot.deletedElementIds,
                historyIndex = nextIndex
            )
        }
    }

    fun resetToDefaults() {
        val currentState = _state.value ?: return
        val defaultSnapshot = EditorSnapshot(
            emptyMap(), emptyMap(), emptyMap(), emptyMap(), emptyMap(),
            emptyMap(), emptyMap(), emptyMap(), emptyMap(), emptyMap(),
            emptyMap(), emptyMap(), emptyMap(), emptyMap(), emptyMap(),
            emptyMap(), emptyMap(), emptySet(), emptySet(), emptyMap(), null, "NONE", emptyMap(), emptyMap(), emptyMap(), emptyMap(), emptySet()
        )
        _state.value = currentState.copy(
            editedTexts = emptyMap(),
            photoUris = emptyMap(),
            elementPositions = emptyMap(),
            elementSizes = emptyMap(),
            elementRotations = emptyMap(),
            elementFlips = emptyMap(),
            textFontSizes = emptyMap(),
            textColors = emptyMap(),
            textFontWeights = emptyMap(),
            textAlignments = emptyMap(),
            textLetterSpacings = emptyMap(),
            textLineSpacings = emptyMap(),
            textCases = emptyMap(),
            textBgColors = emptyMap(),
            textShadowColors = emptyMap(),
            textFontFamilies = emptyMap(),
            customFontUris = emptyMap(),
            lockedElementIds = emptySet(),
            hiddenElementIds = emptySet(),
            elementZOrders = emptyMap(),
            bgGradientConfig = null,
            bgTextureType = "NONE",
            elementGradients = emptyMap(),
            elementStrokes = emptyMap(),
            elementOpacities = emptyMap(),
            elementEffects = emptyMap(),
            deletedElementIds = emptySet(),
            selectedElementId = null,
            snappingGuideLines = emptyList(),
            history = listOf(defaultSnapshot),
            historyIndex = 0
        )
    }

    fun exportImage(context: Context) {
        val currentState = _state.value ?: return
        _state.value = currentState.copy(isExporting = true, errorMessage = null)

        viewModelScope.launch {
            val result = ImageExporter.exportImage(
                context = context,
                manifest = currentState.template,
                editorState = currentState
            )

            result.fold(
                onSuccess = { uri ->
                    _state.value = _state.value?.copy(
                        isExporting = false,
                        exportResultUri = uri.toString()
                    )
                },
                onFailure = { error ->
                    _state.value = _state.value?.copy(
                        isExporting = false,
                        errorMessage = error.localizedMessage ?: "Failed to export image."
                    )
                }
            )
        }
    }

    fun clearExportResult() {
        _state.value = _state.value?.copy(exportResultUri = null, errorMessage = null)
    }
}
