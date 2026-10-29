package com.mahiuddin.magicstory.domain.model

data class TemplateManifest(
    val schemaVersion: Int = 1,
    val id: String,
    val title: String,
    val category: String, // Stories, Portrait Posts, Fashion, Minimal, Promotional
    val description: String = "",
    val aspectRatio: String = "9:16", // 9:16, 4:5, 1:1
    val referenceWidth: Int = 1080,
    val referenceHeight: Int = 1920,
    val previewAsset: String? = null,
    val elements: List<TemplateElement>
) {
    val numericAspectRatio: Float
        get() {
            val parts = aspectRatio.split(":")
            if (parts.size == 2) {
                val w = parts[0].toFloatOrNull()
                val h = parts[1].toFloatOrNull()
                if (w != null && h != null && h > 0) {
                    return w / h
                }
            }
            return 9f / 16f
        }
}

enum class ExportResolution(val label: String, val width: Int, val height: Int) {
    STORY("Instagram Story (1080x1920)", 1080, 1920),
    PORTRAIT("Portrait Post (1080x1350)", 1080, 1350),
    SQUARE("Square Post (1080x1080)", 1080, 1080)
}
