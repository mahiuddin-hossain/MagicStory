package com.mahiuddin.magicstory.domain.model

data class TemplateManifest(
    val schemaVersion: Int = 1,
    val id: String,
    val title: String,
    val category: String,
    val description: String = "",
    val aspectRatio: String = "9:16",
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

enum class ExportResolution(val label: String, val width: Int, val height: Int, val ratioStr: String) {
    STORY("Instagram Story (9:16)", 1080, 1920, "9:16"),
    PORTRAIT("Portrait Post (4:5)", 1080, 1350, "4:5"),
    SQUARE("Square Post (1:1)", 1080, 1080, "1:1"),
    LANDSCAPE("Landscape (16:9)", 1920, 1080, "16:9")
}

enum class ExportFormat(val extension: String, val mimeType: String) {
    JPEG("jpg", "image/jpeg"),
    PNG("png", "image/png"),
    PDF("pdf", "application/pdf")
}

enum class ExportScale(val label: String, val multiplier: Float) {
    SCALE_1X("1x Standard (HD)", 1.0f),
    SCALE_2X("2x Full HD (2K)", 2.0f),
    SCALE_3X("3x Ultra HD (4K)", 3.0f)
}
