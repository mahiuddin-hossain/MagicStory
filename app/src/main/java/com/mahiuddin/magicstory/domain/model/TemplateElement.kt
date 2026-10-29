package com.mahiuddin.magicstory.domain.model

sealed class TemplateElement {
    abstract val id: String
    abstract val zIndex: Int
    abstract val visible: Boolean
    abstract val opacity: Float

    data class Background(
        override val id: String,
        override val zIndex: Int = 0,
        override val visible: Boolean = true,
        override val opacity: Float = 1.0f,
        val color: String = "#FFFFFF",
        val gradientColors: List<String>? = null,
        val gradientAngle: Float = 0f
    ) : TemplateElement()

    data class Text(
        override val id: String,
        override val zIndex: Int,
        override val visible: Boolean = true,
        override val opacity: Float = 1.0f,
        val x: Float,
        val y: Float,
        val width: Float,
        val height: Float,
        val rotation: Float = 0f,
        val defaultText: String,
        val color: String = "#000000",
        val fontSize: Float = 16f,
        val fontWeight: String = "NORMAL", // NORMAL, BOLD, EXTRA_BOLD
        val fontFamily: String = "SANS_SERIF", // SANS_SERIF, SERIF, SCRIPT
        val textAlignment: String = "CENTER", // LEFT, CENTER, RIGHT
        val lineSpacing: Float = 1.0f,
        val editable: Boolean = true,
        val label: String? = null
    ) : TemplateElement()

    data class Shape(
        override val id: String,
        override val zIndex: Int,
        override val visible: Boolean = true,
        override val opacity: Float = 1.0f,
        val x: Float,
        val y: Float,
        val width: Float,
        val height: Float,
        val rotation: Float = 0f,
        val shapeType: String = "RECTANGLE", // RECTANGLE, ROUNDED_RECTANGLE, CIRCLE, LINE
        val color: String = "#FFFFFF",
        val borderColor: String? = null,
        val borderWidth: Float = 0f,
        val cornerRadius: Float = 0f,
        val shadowColor: String? = null,
        val shadowRadius: Float = 0f,
        val shadowDx: Float = 0f,
        val shadowDy: Float = 0f
    ) : TemplateElement()

    data class PhotoSlot(
        override val id: String,
        override val zIndex: Int,
        override val visible: Boolean = true,
        override val opacity: Float = 1.0f,
        val x: Float,
        val y: Float,
        val width: Float,
        val height: Float,
        val rotation: Float = 0f,
        val defaultAsset: String? = null,
        val cornerRadius: Float = 0f,
        val fitMode: String = "COVER", // COVER, CONTAIN
        val editable: Boolean = true,
        val label: String? = null
    ) : TemplateElement()

    data class PhotoCard(
        override val id: String,
        override val zIndex: Int,
        override val visible: Boolean = true,
        override val opacity: Float = 1.0f,
        val x: Float,
        val y: Float,
        val width: Float,
        val height: Float,
        val rotation: Float = -6f,
        val backgroundColor: String = "#FFFFFF",
        val cornerRadius: Float = 16f,
        val shadowColor: String? = "#33000000",
        val shadowRadius: Float = 16f,
        val shadowDx: Float = 0f,
        val shadowDy: Float = 8f,
        val photoSlotId: String = "card_photo_slot",
        val photoSlotRelX: Float = 0.04f,
        val photoSlotRelY: Float = 0.04f,
        val photoSlotRelWidth: Float = 0.92f,
        val photoSlotRelHeight: Float = 0.78f,
        val defaultAsset: String? = null,
        val photoCornerRadius: Float = 12f,
        val captionId: String = "card_caption",
        val captionRelX: Float = 0.04f,
        val captionRelY: Float = 0.84f,
        val captionRelWidth: Float = 0.92f,
        val captionRelHeight: Float = 0.12f,
        val captionDefaultText: String = "women's style",
        val captionColor: String = "#A91521",
        val captionFontFamily: String = "SCRIPT",
        val captionFontSize: Float = 28f
    ) : TemplateElement()

    data class PillCta(
        override val id: String,
        override val zIndex: Int,
        override val visible: Boolean = true,
        override val opacity: Float = 1.0f,
        val x: Float,
        val y: Float,
        val width: Float,
        val height: Float,
        val rotation: Float = 0f,
        val borderColor: String = "#FFFFFF",
        val borderWidth: Float = 2f,
        val cornerRadius: Float = 30f,
        val leftTextId: String = "cta_left_text",
        val leftDefaultText: String = "DISCOVER MORE AT",
        val leftColor: String = "#FFFFFF",
        val leftFontSize: Float = 12f,
        val buttonTextId: String = "cta_button_text",
        val buttonDefaultText: String = "MAGICSTORY.COM",
        val buttonTextColor: String = "#A91521",
        val buttonBgColor: String = "#FFFFFF",
        val buttonFontSize: Float = 12f
    ) : TemplateElement()
}
