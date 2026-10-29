package com.mahiuddin.magicstory.data.model

import com.mahiuddin.magicstory.domain.model.TemplateElement
import com.mahiuddin.magicstory.domain.model.TemplateManifest
import kotlinx.serialization.Serializable

@Serializable
data class TemplateManifestDto(
    val schemaVersion: Int = 1,
    val id: String,
    val title: String,
    val category: String,
    val description: String? = "",
    val aspectRatio: String? = "9:16",
    val referenceWidth: Int? = 1080,
    val referenceHeight: Int? = 1920,
    val previewAsset: String? = null,
    val elements: List<TemplateElementDto> = emptyList()
) {
    fun toDomain(): TemplateManifest {
        return TemplateManifest(
            schemaVersion = schemaVersion,
            id = id,
            title = title,
            category = category,
            description = description ?: "",
            aspectRatio = aspectRatio ?: "9:16",
            referenceWidth = referenceWidth ?: 1080,
            referenceHeight = referenceHeight ?: 1920,
            previewAsset = previewAsset,
            elements = elements.mapNotNull { it.toDomain() }
        )
    }
}

@Serializable
data class TemplateElementDto(
    val type: String,
    val id: String,
    val zIndex: Int = 0,
    val visible: Boolean = true,
    val opacity: Float = 1.0f,
    val x: Float = 0f,
    val y: Float = 0f,
    val width: Float = 0f,
    val height: Float = 0f,
    val rotation: Float = 0f,
    val color: String? = null,
    val backgroundColor: String? = null,
    val gradientColors: List<String>? = null,
    val gradientAngle: Float = 0f,
    val defaultText: String? = null,
    val fontSize: Float = 16f,
    val fontWeight: String? = "NORMAL",
    val fontFamily: String? = "SANS_SERIF",
    val textAlignment: String? = "CENTER",
    val lineSpacing: Float = 1.0f,
    val editable: Boolean = true,
    val label: String? = null,
    val shapeType: String? = "RECTANGLE",
    val borderColor: String? = null,
    val borderWidth: Float = 0f,
    val cornerRadius: Float = 0f,
    val shadowColor: String? = null,
    val shadowRadius: Float = 0f,
    val shadowDx: Float = 0f,
    val shadowDy: Float = 0f,
    val defaultAsset: String? = null,
    val fitMode: String? = "COVER",
    val photoSlotId: String? = null,
    val photoSlotRelX: Float? = 0.04f,
    val photoSlotRelY: Float? = 0.04f,
    val photoSlotRelWidth: Float? = 0.92f,
    val photoSlotRelHeight: Float? = 0.78f,
    val photoCornerRadius: Float? = 12f,
    val captionId: String? = null,
    val captionRelX: Float? = 0.04f,
    val captionRelY: Float? = 0.84f,
    val captionRelWidth: Float? = 0.92f,
    val captionRelHeight: Float? = 0.12f,
    val captionDefaultText: String? = null,
    val captionColor: String? = null,
    val captionFontFamily: String? = null,
    val captionFontSize: Float? = 28f,
    val leftTextId: String? = null,
    val leftDefaultText: String? = null,
    val leftColor: String? = null,
    val leftFontSize: Float? = 12f,
    val buttonTextId: String? = null,
    val buttonDefaultText: String? = null,
    val buttonTextColor: String? = null,
    val buttonBgColor: String? = null,
    val buttonFontSize: Float? = 12f
) {
    fun toDomain(): TemplateElement? {
        return when (type.lowercase()) {
            "background" -> TemplateElement.Background(
                id = id,
                zIndex = zIndex,
                visible = visible,
                opacity = opacity,
                color = color ?: backgroundColor ?: "#FFFFFF",
                gradientColors = gradientColors,
                gradientAngle = gradientAngle
            )
            "text" -> TemplateElement.Text(
                id = id,
                zIndex = zIndex,
                visible = visible,
                opacity = opacity,
                x = x,
                y = y,
                width = width,
                height = height,
                rotation = rotation,
                defaultText = defaultText ?: "",
                color = color ?: "#000000",
                fontSize = fontSize,
                fontWeight = fontWeight ?: "NORMAL",
                fontFamily = fontFamily ?: "SANS_SERIF",
                textAlignment = textAlignment ?: "CENTER",
                lineSpacing = lineSpacing,
                editable = editable,
                label = label
            )
            "shape" -> TemplateElement.Shape(
                id = id,
                zIndex = zIndex,
                visible = visible,
                opacity = opacity,
                x = x,
                y = y,
                width = width,
                height = height,
                rotation = rotation,
                shapeType = shapeType ?: "RECTANGLE",
                color = color ?: backgroundColor ?: "#FFFFFF",
                borderColor = borderColor,
                borderWidth = borderWidth,
                cornerRadius = cornerRadius,
                shadowColor = shadowColor,
                shadowRadius = shadowRadius,
                shadowDx = shadowDx,
                shadowDy = shadowDy
            )
            "photoslot", "photo_slot", "image" -> TemplateElement.PhotoSlot(
                id = id,
                zIndex = zIndex,
                visible = visible,
                opacity = opacity,
                x = x,
                y = y,
                width = width,
                height = height,
                rotation = rotation,
                defaultAsset = defaultAsset,
                cornerRadius = cornerRadius,
                fitMode = fitMode ?: "COVER",
                editable = editable,
                label = label
            )
            "photocard", "photo_card" -> TemplateElement.PhotoCard(
                id = id,
                zIndex = zIndex,
                visible = visible,
                opacity = opacity,
                x = x,
                y = y,
                width = width,
                height = height,
                rotation = rotation,
                backgroundColor = backgroundColor ?: color ?: "#FFFFFF",
                cornerRadius = cornerRadius,
                shadowColor = shadowColor ?: "#33000000",
                shadowRadius = shadowRadius,
                shadowDx = shadowDx,
                shadowDy = shadowDy,
                photoSlotId = photoSlotId ?: "${id}_slot",
                photoSlotRelX = photoSlotRelX ?: 0.04f,
                photoSlotRelY = photoSlotRelY ?: 0.04f,
                photoSlotRelWidth = photoSlotRelWidth ?: 0.92f,
                photoSlotRelHeight = photoSlotRelHeight ?: 0.78f,
                defaultAsset = defaultAsset,
                photoCornerRadius = photoCornerRadius ?: 12f,
                captionId = captionId ?: "${id}_caption",
                captionRelX = captionRelX ?: 0.04f,
                captionRelY = captionRelY ?: 0.84f,
                captionRelWidth = captionRelWidth ?: 0.92f,
                captionRelHeight = captionRelHeight ?: 0.12f,
                captionDefaultText = captionDefaultText ?: "women's style",
                captionColor = captionColor ?: "#A91521",
                captionFontFamily = captionFontFamily ?: "SCRIPT",
                captionFontSize = captionFontSize ?: 28f
            )
            "pillcta", "pill_cta" -> TemplateElement.PillCta(
                id = id,
                zIndex = zIndex,
                visible = visible,
                opacity = opacity,
                x = x,
                y = y,
                width = width,
                height = height,
                rotation = rotation,
                borderColor = borderColor ?: "#FFFFFF",
                borderWidth = borderWidth,
                cornerRadius = cornerRadius,
                leftTextId = leftTextId ?: "${id}_left",
                leftDefaultText = leftDefaultText ?: "DISCOVER MORE AT",
                leftColor = leftColor ?: "#FFFFFF",
                leftFontSize = leftFontSize ?: 12f,
                buttonTextId = buttonTextId ?: "${id}_btn",
                buttonDefaultText = buttonDefaultText ?: "MAGICSTORY.COM",
                buttonTextColor = buttonTextColor ?: "#A91521",
                buttonBgColor = buttonBgColor ?: "#FFFFFF",
                buttonFontSize = buttonFontSize ?: 12f
            )
            else -> null // Safely skip unknown element types
        }
    }
}
