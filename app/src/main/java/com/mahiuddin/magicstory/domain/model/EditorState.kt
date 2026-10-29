package com.mahiuddin.magicstory.domain.model

data class EditorSnapshot(
    val editedTexts: Map<String, String>,
    val photoUris: Map<String, String>,
    val elementPositions: Map<String, Pair<Float, Float>> = emptyMap(),
    val textFontSizes: Map<String, Float> = emptyMap()
)

data class MagicStoryEditorState(
    val template: TemplateManifest,
    val editedTexts: Map<String, String> = emptyMap(),
    val photoUris: Map<String, String> = emptyMap(),
    val elementPositions: Map<String, Pair<Float, Float>> = emptyMap(),
    val textFontSizes: Map<String, Float> = emptyMap(),
    val selectedElementId: String? = null,
    val exportResolution: ExportResolution = ExportResolution.STORY,
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
        return editedTexts[elementId] ?: defaultValue
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

    fun getFontSize(elementId: String, defaultFontSize: Float): Float {
        return textFontSizes[elementId] ?: defaultFontSize
    }
}
