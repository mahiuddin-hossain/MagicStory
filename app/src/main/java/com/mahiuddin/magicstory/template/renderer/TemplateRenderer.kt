package com.mahiuddin.magicstory.template.renderer

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import com.mahiuddin.magicstory.domain.model.MagicStoryEditorState
import com.mahiuddin.magicstory.domain.model.TemplateElement
import com.mahiuddin.magicstory.domain.model.TemplateManifest
import com.mahiuddin.magicstory.util.BitmapUtils

object TemplateRenderer {

    fun renderToCanvas(
        canvas: Canvas,
        width: Float,
        height: Float,
        manifest: TemplateManifest,
        editorState: MagicStoryEditorState,
        context: Context,
        bitmapCache: MutableMap<String, Bitmap> = mutableMapOf()
    ) {
        val sortedElements = manifest.elements.filter { it.visible }.sortedBy { it.zIndex }
        val scaleFactor = width / manifest.referenceWidth.toFloat()

        sortedElements.forEach { element ->
            canvas.save()
            if (element.opacity < 1.0f) {
                val alpha = (element.opacity * 255).toInt().coerceIn(0, 255)
                canvas.saveLayerAlpha(0f, 0f, width, height, alpha)
            }

            when (element) {
                is TemplateElement.Background -> renderBackground(canvas, width, height, element)
                is TemplateElement.Text -> renderText(canvas, width, height, element, editorState, scaleFactor)
                is TemplateElement.Shape -> renderShape(canvas, width, height, element, editorState, scaleFactor)
                is TemplateElement.PhotoSlot -> renderPhotoSlot(canvas, width, height, element, editorState, context, scaleFactor, bitmapCache)
                is TemplateElement.PhotoCard -> renderPhotoCard(canvas, width, height, element, editorState, context, scaleFactor, bitmapCache)
                is TemplateElement.PillCta -> renderPillCta(canvas, width, height, element, editorState, scaleFactor)
            }

            if (element.opacity < 1.0f) {
                canvas.restore()
            }
            canvas.restore()
        }
    }

    private fun renderBackground(canvas: Canvas, canvasW: Float, canvasH: Float, bg: TemplateElement.Background) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val colors = bg.gradientColors
        if (!colors.isNullOrEmpty() && colors.size >= 2) {
            val colorInts = colors.map { safeParseColor(it) }.toIntArray()
            val gradient = LinearGradient(
                0f, 0f, canvasW, canvasH,
                colorInts,
                null,
                Shader.TileMode.CLAMP
            )
            paint.shader = gradient
        } else {
            paint.color = safeParseColor(bg.color)
        }
        canvas.drawRect(0f, 0f, canvasW, canvasH, paint)
    }

    private fun renderText(
        canvas: Canvas,
        canvasW: Float,
        canvasH: Float,
        textElem: TemplateElement.Text,
        editorState: MagicStoryEditorState,
        scaleFactor: Float
    ) {
        val textValue = editorState.getTextValue(textElem.id, textElem.defaultText)
        if (textValue.isEmpty()) return

        val elemX = editorState.getElementX(textElem.id, textElem.x) * canvasW
        val elemY = editorState.getElementY(textElem.id, textElem.y) * canvasH
        val elemW = textElem.width * canvasW
        val elemH = textElem.height * canvasH

        val centerX = elemX + elemW / 2f
        val centerY = elemY + elemH / 2f

        canvas.save()
        if (textElem.rotation != 0f) {
            canvas.rotate(textElem.rotation, centerX, centerY)
        }

        val fontSize = editorState.getFontSize(textElem.id, textElem.fontSize)

        val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = safeParseColor(textElem.color)
            textSize = fontSize * scaleFactor

            val style = when (textElem.fontWeight.uppercase()) {
                "BOLD", "EXTRA_BOLD" -> Typeface.BOLD
                else -> Typeface.NORMAL
            }

            typeface = when (textElem.fontFamily.uppercase()) {
                "SERIF" -> Typeface.create(Typeface.SERIF, style)
                "SCRIPT", "CURSIVE" -> Typeface.create("cursive", style)
                else -> Typeface.create(Typeface.SANS_SERIF, style)
            }
        }

        val alignment = when (textElem.textAlignment.uppercase()) {
            "LEFT" -> Layout.Alignment.ALIGN_NORMAL
            "RIGHT" -> Layout.Alignment.ALIGN_OPPOSITE
            else -> Layout.Alignment.ALIGN_CENTER
        }

        val staticLayout = StaticLayout.Builder.obtain(
            textValue, 0, textValue.length, textPaint, elemW.toInt().coerceAtLeast(10)
        )
            .setAlignment(alignment)
            .setLineSpacing(0f, textElem.lineSpacing)
            .setIncludePad(false)
            .build()

        val layoutHeight = staticLayout.height
        val startY = elemY + (elemH - layoutHeight) / 2f

        canvas.save()
        canvas.translate(elemX, startY)
        staticLayout.draw(canvas)
        canvas.restore()

        val isSelected = !editorState.isExporting && editorState.selectedElementId == textElem.id
        drawSelectionOutline(canvas, RectF(elemX, elemY, elemX + elemW, elemY + elemH), isSelected, scaleFactor)

        canvas.restore()
    }

    private fun renderShape(
        canvas: Canvas,
        canvasW: Float,
        canvasH: Float,
        shape: TemplateElement.Shape,
        editorState: MagicStoryEditorState,
        scaleFactor: Float
    ) {
        val x = editorState.getElementX(shape.id, shape.x) * canvasW
        val y = editorState.getElementY(shape.id, shape.y) * canvasH
        val w = shape.width * canvasW
        val h = shape.height * canvasH

        val rect = RectF(x, y, x + w, y + h)

        canvas.save()
        if (shape.rotation != 0f) {
            canvas.rotate(shape.rotation, rect.centerX(), rect.centerY())
        }

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        if (shape.shadowColor != null && shape.shadowRadius > 0f) {
            paint.setShadowLayer(
                shape.shadowRadius * scaleFactor,
                shape.shadowDx * scaleFactor,
                shape.shadowDy * scaleFactor,
                safeParseColor(shape.shadowColor)
            )
        }

        paint.color = safeParseColor(shape.color)
        paint.style = Paint.Style.FILL

        val cornerRadius = shape.cornerRadius * scaleFactor

        when (shape.shapeType.uppercase()) {
            "ROUNDED_RECTANGLE" -> canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint)
            "CIRCLE" -> canvas.drawOval(rect, paint)
            "LINE" -> {
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = shape.borderWidth * scaleFactor
                canvas.drawLine(x, y + h / 2f, x + w, y + h / 2f, paint)
            }
            else -> canvas.drawRect(rect, paint)
        }

        if (shape.borderColor != null && shape.borderWidth > 0f) {
            val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = safeParseColor(shape.borderColor)
                style = Paint.Style.STROKE
                strokeWidth = shape.borderWidth * scaleFactor
            }
            if (shape.shapeType.uppercase() == "ROUNDED_RECTANGLE") {
                canvas.drawRoundRect(rect, cornerRadius, cornerRadius, strokePaint)
            } else if (shape.shapeType.uppercase() == "CIRCLE") {
                canvas.drawOval(rect, strokePaint)
            } else {
                canvas.drawRect(rect, strokePaint)
            }
        }

        val isSelected = !editorState.isExporting && editorState.selectedElementId == shape.id
        drawSelectionOutline(canvas, rect, isSelected, scaleFactor)

        canvas.restore()
    }

    private fun drawDarkGradientPlaceholder(canvas: Canvas, rect: RectF, cornerRadius: Float) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val gradient = LinearGradient(
            rect.left, rect.top, rect.right, rect.bottom,
            intArrayOf(
                Color.parseColor("#383032"),
                Color.parseColor("#221D1E"),
                Color.parseColor("#141112")
            ),
            floatArrayOf(0.0f, 0.5f, 1.0f),
            Shader.TileMode.CLAMP
        )
        paint.shader = gradient
        if (cornerRadius > 0f) {
            canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint)
        } else {
            canvas.drawRect(rect, paint)
        }
    }

    private fun renderPhotoSlot(
        canvas: Canvas,
        canvasW: Float,
        canvasH: Float,
        slot: TemplateElement.PhotoSlot,
        editorState: MagicStoryEditorState,
        context: Context,
        scaleFactor: Float,
        bitmapCache: MutableMap<String, Bitmap>
    ) {
        val x = editorState.getElementX(slot.id, slot.x) * canvasW
        val y = editorState.getElementY(slot.id, slot.y) * canvasH
        val w = slot.width * canvasW
        val h = slot.height * canvasH
        val destRect = RectF(x, y, x + w, y + h)

        val photoUri = editorState.getPhotoUri(slot.id, slot.defaultAsset)
        val bitmap = getOrLoadBitmap(context, photoUri, w.toInt(), h.toInt(), bitmapCache)

        canvas.save()
        if (slot.rotation != 0f) {
            canvas.rotate(slot.rotation, destRect.centerX(), destRect.centerY())
        }

        val cornerRadius = slot.cornerRadius * scaleFactor
        drawDarkGradientPlaceholder(canvas, destRect, cornerRadius)
        BitmapUtils.drawCoverBitmap(canvas, bitmap, destRect, cornerRadius)

        val isSelected = !editorState.isExporting && editorState.selectedElementId == slot.id
        drawSelectionOutline(canvas, destRect, isSelected, scaleFactor)

        canvas.restore()
    }

    private fun renderPhotoCard(
        canvas: Canvas,
        canvasW: Float,
        canvasH: Float,
        card: TemplateElement.PhotoCard,
        editorState: MagicStoryEditorState,
        context: Context,
        scaleFactor: Float,
        bitmapCache: MutableMap<String, Bitmap>
    ) {
        val cardX = editorState.getElementX(card.id, card.x) * canvasW
        val cardY = editorState.getElementY(card.id, card.y) * canvasH
        val cardW = card.width * canvasW
        val cardH = card.height * canvasH
        val cardRect = RectF(cardX, cardY, cardX + cardW, cardY + cardH)

        val centerX = cardRect.centerX()
        val centerY = cardRect.centerY()

        canvas.save()
        canvas.rotate(card.rotation, centerX, centerY)

        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = safeParseColor(card.backgroundColor)
            style = Paint.Style.FILL
            if (card.shadowColor != null && card.shadowRadius > 0f) {
                setShadowLayer(
                    card.shadowRadius * scaleFactor,
                    card.shadowDx * scaleFactor,
                    card.shadowDy * scaleFactor,
                    safeParseColor(card.shadowColor)
                )
            }
        }
        val cardCornerRadius = card.cornerRadius * scaleFactor
        canvas.drawRoundRect(cardRect, cardCornerRadius, cardCornerRadius, bgPaint)

        val photoX = cardX + card.photoSlotRelX * cardW
        val photoY = cardY + card.photoSlotRelY * cardH
        val photoW = card.photoSlotRelWidth * cardW
        val photoH = card.photoSlotRelHeight * cardH
        val photoRect = RectF(photoX, photoY, photoX + photoW, photoY + photoH)

        val photoUri = editorState.getPhotoUri(card.photoSlotId, card.defaultAsset)
        val bitmap = getOrLoadBitmap(context, photoUri, photoW.toInt(), photoH.toInt(), bitmapCache)

        val photoCornerRadius = card.photoCornerRadius * scaleFactor
        drawDarkGradientPlaceholder(canvas, photoRect, photoCornerRadius)
        BitmapUtils.drawCoverBitmap(canvas, bitmap, photoRect, photoCornerRadius)

        val captionText = editorState.getTextValue(card.captionId, card.captionDefaultText)
        if (captionText.isNotEmpty()) {
            val capX = cardX + card.captionRelX * cardW
            val capY = cardY + card.captionRelY * cardH
            val capW = card.captionRelWidth * cardW
            val capH = card.captionRelHeight * cardH

            val captionFontSize = editorState.getFontSize(card.captionId, card.captionFontSize)

            val captionPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = safeParseColor(card.captionColor)
                textSize = captionFontSize * scaleFactor
                typeface = Typeface.create("cursive", Typeface.ITALIC)
            }

            val alignment = Layout.Alignment.ALIGN_CENTER
            val staticLayout = StaticLayout.Builder.obtain(
                captionText, 0, captionText.length, captionPaint, capW.toInt().coerceAtLeast(10)
            )
                .setAlignment(alignment)
                .setIncludePad(false)
                .build()

            val textY = capY + (capH - staticLayout.height) / 2f
            canvas.save()
            canvas.translate(capX, textY)
            staticLayout.draw(canvas)
            canvas.restore()
        }

        val isSelected = !editorState.isExporting && (editorState.selectedElementId == card.id || editorState.selectedElementId == card.photoSlotId)
        drawSelectionOutline(canvas, cardRect, isSelected, scaleFactor)

        canvas.restore()
    }

    private fun renderPillCta(
        canvas: Canvas,
        canvasW: Float,
        canvasH: Float,
        pillCta: TemplateElement.PillCta,
        editorState: MagicStoryEditorState,
        scaleFactor: Float
    ) {
        val x = editorState.getElementX(pillCta.id, pillCta.x) * canvasW
        val y = editorState.getElementY(pillCta.id, pillCta.y) * canvasH
        val w = pillCta.width * canvasW
        val h = pillCta.height * canvasH
        val containerRect = RectF(x, y, x + w, y + h)

        canvas.save()
        if (pillCta.rotation != 0f) {
            canvas.rotate(pillCta.rotation, containerRect.centerX(), containerRect.centerY())
        }

        val cornerRadius = pillCta.cornerRadius * scaleFactor

        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = safeParseColor(pillCta.borderColor)
            style = Paint.Style.STROKE
            strokeWidth = pillCta.borderWidth * scaleFactor
        }
        canvas.drawRoundRect(containerRect, cornerRadius, cornerRadius, borderPaint)

        val leftText = editorState.getTextValue(pillCta.leftTextId, pillCta.leftDefaultText)
        if (leftText.isNotEmpty()) {
            val leftFontSize = editorState.getFontSize(pillCta.leftTextId, pillCta.leftFontSize)
            val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = safeParseColor(pillCta.leftColor)
                textSize = leftFontSize * scaleFactor
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            }

            val leftW = w * 0.52f
            val staticLayout = StaticLayout.Builder.obtain(
                leftText, 0, leftText.length, textPaint, leftW.toInt().coerceAtLeast(10)
            )
                .setAlignment(Layout.Alignment.ALIGN_CENTER)
                .build()

            val textY = y + (h - staticLayout.height) / 2f
            canvas.save()
            canvas.translate(x + w * 0.02f, textY)
            staticLayout.draw(canvas)
            canvas.restore()
        }

        val btnPadding = h * 0.12f
        val btnX = x + w * 0.52f
        val btnY = y + btnPadding
        val btnW = w * 0.45f
        val btnH = h - (btnPadding * 2f)
        val btnRect = RectF(btnX, btnY, btnX + btnW, btnY + btnH)

        val btnBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = safeParseColor(pillCta.buttonBgColor)
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(btnRect, btnH / 2f, btnH / 2f, btnBgPaint)

        val btnText = editorState.getTextValue(pillCta.buttonTextId, pillCta.buttonDefaultText)
        if (btnText.isNotEmpty()) {
            val buttonFontSize = editorState.getFontSize(pillCta.buttonTextId, pillCta.buttonFontSize)
            val btnTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = safeParseColor(pillCta.buttonTextColor)
                textSize = buttonFontSize * scaleFactor
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            }

            val staticLayout = StaticLayout.Builder.obtain(
                btnText, 0, btnText.length, btnTextPaint, btnW.toInt().coerceAtLeast(10)
            )
                .setAlignment(Layout.Alignment.ALIGN_CENTER)
                .build()

            val textY = btnY + (btnH - staticLayout.height) / 2f
            canvas.save()
            canvas.translate(btnX, textY)
            staticLayout.draw(canvas)
            canvas.restore()
        }

        val isSelected = !editorState.isExporting && editorState.selectedElementId == pillCta.id
        drawSelectionOutline(canvas, containerRect, isSelected, scaleFactor)

        canvas.restore()
    }

    private fun drawSelectionOutline(canvas: Canvas, rect: RectF, isSelected: Boolean, scaleFactor: Float) {
        if (!isSelected) return
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#C91F2C")
            style = Paint.Style.STROKE
            strokeWidth = 3f * scaleFactor
        }
        canvas.drawRect(rect, strokePaint)

        val handleRadius = 8f * scaleFactor
        val handlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }
        val handleBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#C91F2C")
            style = Paint.Style.STROKE
            strokeWidth = 2f * scaleFactor
        }

        val corners = arrayOf(
            Pair(rect.left, rect.top),
            Pair(rect.right, rect.top),
            Pair(rect.left, rect.bottom),
            Pair(rect.right, rect.bottom)
        )
        corners.forEach { (cx, cy) ->
            canvas.drawCircle(cx, cy, handleRadius, handlePaint)
            canvas.drawCircle(cx, cy, handleRadius, handleBorder)
        }
    }

    private fun getOrLoadBitmap(
        context: Context,
        pathOrUri: String?,
        reqWidth: Int,
        reqHeight: Int,
        cache: MutableMap<String, Bitmap>
    ): Bitmap {
        val cacheKey = "$pathOrUri-${reqWidth}x$reqHeight"
        return cache.getOrPut(cacheKey) {
            BitmapUtils.loadBitmap(context, pathOrUri, reqWidth, reqHeight)
        }
    }

    private fun safeParseColor(colorStr: String?): Int {
        if (colorStr.isNullOrBlank()) return Color.TRANSPARENT
        return try {
            Color.parseColor(colorStr)
        } catch (_: Exception) {
            Color.WHITE
        }
    }
}
