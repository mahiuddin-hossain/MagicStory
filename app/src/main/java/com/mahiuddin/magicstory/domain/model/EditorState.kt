package com.mahiuddin.magicstory.domain.model

data class GuideLine(
    val isVertical: Boolean,
    val positionNorm: Float,
    val colorHex: String = "#C91F2C"
)

data class EditorSnapshot(
    val editedTexts: Map<String, String>,
    val photoUris: Map<String, String>,
    val elementPositions: Map<String, Pair<Float, Float>> = emptyMap(),
    val elementSizes: Map<String, Pair<Float, Float>> = emptyMap(),
    val elementRotations: Map<String, Float> = emptyMap(),
    val elementFlips: Map<String, Pair<Boolean, Boolean>> = emptyMap(),
    val textFontSizes: Map<String, Float> = emptyMap(),
    val textColors: Map<String, String> = emptyMap(),
    val textFontWeights: Map<String, String> = emptyMap(),
    val textAlignments: Map<String, String> = emptyMap(),
    val textLetterSpacings: Map<String, Float> = emptyMap(),
    val textLineSpacings: Map<String, Float> = emptyMap(),
    val textCases: Map<String, String> = emptyMap(),
    val textBgColors: Map<String, String> = emptyMap(),
    val textShadowColors: Map<String, String> = emptyMap(),
    val customFontUris: Map<String, String> = emptyMap(),
    val lockedElementIds: Set<String> = emptySet(),
    val hiddenElementIds: Set<String> = emptySet(),
    val elementZOrders: Map<String, Int> = emptyMap(),
    val bgGradientColors: List<String>? = null,
    val bgGradientAngle: Float = 0f,
    val bgTextureType: String = "NONE",
    val deletedElementIds: Set<String> = emptySet()
)

data class MagicStoryEditorState(
    val template: TemplateManifest,
    val editedTexts: Map<String, String> = emptyMap(),
    val photoUris: Map<String, String> = emptyMap(),
    val elementPositions: Map<String, Pair<Float, Float>> = emptyMap(),
    val elementSizes: Map<String, Pair<Float, Float>> = emptyMap(),
    val elementRotations: Map<String, Float> = emptyMap(),
    val elementFlips: Map<String, Pair<Boolean, Boolean>> = emptyMap(),
    val textFontSizes: Map<String, Float> = emptyMap(),
    val textColors: Map<String, String> = emptyMap(),
    val textFontWeights: Map<String, String> = emptyMap(),
    val textAlignments: Map<String, String> = emptyMap(),
    val textLetterSpacings: Map<String, Float> = emptyMap(),
    val textLineSpacings: Map<String, Float> = emptyMap(),
    val textCases: Map<String, String> = emptyMap(),
    val textBgColors: Map<String, String> = emptyMap(),
    val textShadowColors: Map<String, String> = emptyMap(),
    val customFontUris: Map<String, String> = emptyMap(),
    val lockedElementIds: Set<String> = emptySet(),
    val hiddenElementIds: Set<String> = emptySet(),
    val elementZOrders: Map<String, Int> = emptyMap(),
    val bgGradientColors: List<String>? = null,
    val bgGradientAngle: Float = 0f,
    val bgTextureType: String = "NONE",
    val deletedElementIds: Set<String> = emptySet(),
    val selectedElementId: String? = null,
    val multiSelectedElementIds: Set<String> = emptySet(),
    val snappingGuideLines: List<GuideLine> = emptyList(),
    val canvasZoom: Float = 1.0f,
    val canvasPanX: Float = 0f,
    val canvasPanY: Float = 0f,
    val exportResolution: ExportResolution = ExportResolution.STORY,
    val exportFormat: ExportFormat = ExportFormat.JPEG,
    val exportScale: ExportScale = ExportScale.SCALE_1X,
    val history: List<EditorSnapshot> = emptyList(),
    val historyIndex: Int = -1,
    val isExporting: Boolean = false,
    val exportResultUri: String? = null,
    val errorMessage: String? = null
) {
    val canUndo: Boolean
        get() = historyIndex > 0

    val canRedo: Boolean
        get() = historyIndex >= 0 && historyIndex < history.size - 1

    fun getTextValue(elementId: String, defaultValue: String): String {
        val raw = editedTexts[elementId] ?: defaultValue
        return when (textCases[elementId]?.uppercase()) {
            "UPPERCASE" -> raw.uppercase()
            "LOWERCASE" -> raw.lowercase()
            else -> raw
        }
    }

    fun getPhotoUri(slotId: String, defaultAsset: String?): String? {
        return photoUris[slotId] ?: defaultAsset
    }

    fun getElementX(elementId: String, defaultX: Float): Float {
        return elementPositions[elementId]?.first ?: defaultX
    }

    fun getElementY(elementId: String, defaultY: Float): Float {
        return elementPositions[elementId]?.second ?: defaultY
    }

    fun getElementW(elementId: String, defaultW: Float): Float {
        return elementSizes[elementId]?.first ?: defaultW
    }

    fun getElementH(elementId: String, defaultH: Float): Float {
        return elementSizes[elementId]?.second ?: defaultH
    }

    fun getElementRotation(elementId: String, defaultRotation: Float): Float {
        return elementRotations[elementId] ?: defaultRotation
    }

    fun getElementZIndex(elementId: String, defaultZIndex: Int): Int {
        return elementZOrders[elementId] ?: defaultZIndex
    }

    fun getFlipX(elementId: String): Boolean {
        return elementFlips[elementId]?.first ?: false
    }

    fun getFlipY(elementId: String): Boolean {
        return elementFlips[elementId]?.second ?: false
    }

    fun getFontSize(elementId: String, defaultFontSize: Float): Float {
        return textFontSizes[elementId] ?: defaultFontSize
    }

    fun getTextColor(elementId: String, defaultColor: String): String {
        return textColors[elementId] ?: defaultColor
    }

    fun getTextFontWeight(elementId: String, defaultWeight: String): String {
        return textFontWeights[elementId] ?: defaultWeight
    }

    fun getTextAlignment(elementId: String, defaultAlignment: String): String {
        return textAlignments[elementId] ?: defaultAlignment
    }

    fun getLetterSpacing(elementId: String, defaultSpacing: Float = 0f): Float {
        return textLetterSpacings[elementId] ?: defaultSpacing
    }

    fun getLineSpacing(elementId: String, defaultSpacing: Float = 1.0f): Float {
        return textLineSpacings[elementId] ?: defaultSpacing
    }

    fun getTextBgColor(elementId: String): String? {
        return textBgColors[elementId]
    }

    fun getTextShadowColor(elementId: String): String? {
        return textShadowColors[elementId]
    }

    fun getCustomFontUri(elementId: String): String? {
        return customFontUris[elementId]
    }

    fun isElementLocked(elementId: String): Boolean {
        return lockedElementIds.contains(elementId)
    }

    fun isElementHidden(elementId: String): Boolean {
        return hiddenElementIds.contains(elementId)
    }

    fun isElementDeleted(elementId: String): Boolean {
        return deletedElementIds.contains(elementId)
    }

    fun isElementSelected(elementId: String): Boolean {
        return selectedElementId == elementId || multiSelectedElementIds.contains(elementId)
    }
}
