package com.mahiuddin.magicstory.template.renderer

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.SweepGradient
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import com.mahiuddin.magicstory.domain.model.GradientConfig
import com.mahiuddin.magicstory.domain.model.LayerEffectConfig
import com.mahiuddin.magicstory.domain.model.MagicStoryEditorState
import com.mahiuddin.magicstory.domain.model.TemplateElement
import com.mahiuddin.magicstory.domain.model.TemplateManifest
import com.mahiuddin.magicstory.util.BitmapUtils
import java.util.Random

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
        val sortedElements = manifest.elements
            .filter { it.visible && !editorState.isElementHidden(it.id) }
            .sortedBy { editorState.getElementZIndex(it.id, it.zIndex) }

        val scaleFactor = width / manifest.referenceWidth.toFloat()

        sortedElements.forEach { element ->
            if (editorState.isElementDeleted(element.id)) return@forEach

            val opacity = editorState.getElementOpacity(element.id, element.opacity)
            canvas.save()
            if (opacity < 1.0f) {
                val alpha = (opacity * 255).toInt().coerceIn(0, 255)
                canvas.saveLayerAlpha(0f, 0f, width, height, alpha)
            }

            when (element) {
                is TemplateElement.Background -> renderBackground(canvas, width, height, element, editorState, scaleFactor)
                is TemplateElement.Text -> renderText(canvas, width, height, element, manifest, editorState, scaleFactor)
                is TemplateElement.Shape -> renderShape(canvas, width, height, element, manifest, editorState, scaleFactor)
                is TemplateElement.PhotoSlot -> renderPhotoSlot(canvas, width, height, element, manifest, editorState, context, scaleFactor, bitmapCache)
                is TemplateElement.PhotoCard -> renderPhotoCard(canvas, width, height, element, manifest, editorState, context, scaleFactor, bitmapCache)
                is TemplateElement.PillCta -> renderPillCta(canvas, width, height, element, manifest, editorState, scaleFactor)
            }

            if (opacity < 1.0f) {
                canvas.restore()
            }
            canvas.restore()
        }

        // Draw Snapping Guide Lines (Editor-Only)
        if (!editorState.isExporting && editorState.snappingGuideLines.isNotEmpty()) {
            editorState.snappingGuideLines.forEach { guide ->
                val guidePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = safeParseColor(guide.colorHex)
                    style = Paint.Style.STROKE
                    strokeWidth = 2.5f * scaleFactor
                    pathEffect = DashPathEffect(floatArrayOf(12f * scaleFactor, 8f * scaleFactor), 0f)
                }
                if (guide.isVertical) {
                    val x = guide.positionNorm * width
                    canvas.drawLine(x, 0f, x, height, guidePaint)
                } else {
                    val y = guide.positionNorm * height
                    canvas.drawLine(0f, y, width, y, guidePaint)
                }
            }
        }
    }

    private fun drawCheckerboardBackground(canvas: Canvas, width: Float, height: Float) {
        val checkSize = 10f
        val lightPaint = Paint().apply { color = Color.parseColor("#E4E4E4") }
        val darkPaint = Paint().apply { color = Color.parseColor("#B8B8B8") }

        val rows = (height / checkSize).toInt() + 1
        val cols = (width / checkSize).toInt() + 1

        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val paint = if ((r + c) % 2 == 0) lightPaint else darkPaint
                canvas.drawRect(c * checkSize, r * checkSize, (c + 1) * checkSize, (r + 1) * checkSize, paint)
            }
        }
    }

    fun renderElementThumbnail(
        canvas: Canvas,
        width: Float,
        height: Float,
        element: TemplateElement,
        manifest: TemplateManifest,
        editorState: MagicStoryEditorState,
        context: Context
    ) {
        drawCheckerboardBackground(canvas, width, height)

        val scaleFactor = width / manifest.referenceWidth.toFloat()
        canvas.save()

        when (element) {
            is TemplateElement.Background -> renderBackground(canvas, width, height, element, editorState, scaleFactor)
            is TemplateElement.Text -> renderText(canvas, width, height, element, manifest, editorState, scaleFactor)
            is TemplateElement.Shape -> renderShape(canvas, width, height, element, manifest, editorState, scaleFactor)
            is TemplateElement.PhotoSlot -> renderPhotoSlot(canvas, width, height, element, manifest, editorState, context, scaleFactor, mutableMapOf())
            is TemplateElement.PhotoCard -> renderPhotoCard(canvas, width, height, element, manifest, editorState, context, scaleFactor, mutableMapOf())
            is TemplateElement.PillCta -> renderPillCta(canvas, width, height, element, manifest, editorState, scaleFactor)
        }

        canvas.restore()
    }

    private fun createGradientShader(
        rect: RectF,
        config: GradientConfig
    ): Shader {
        val effectiveColors = if (config.isReversed) config.colors.reversed() else config.colors
        val colorInts = effectiveColors.map { safeParseColor(it) }.toIntArray()
        val tileMode = when (config.tileMode.uppercase()) {
            "MIRROR" -> Shader.TileMode.MIRROR
            "REPEAT" -> Shader.TileMode.REPEAT
            else -> Shader.TileMode.CLAMP
        }

        val cx = rect.centerX()
        val cy = rect.centerY()

        return when (config.type.uppercase()) {
            "RADIAL" -> {
                val radius = (Math.hypot(rect.width().toDouble(), rect.height().toDouble()) / 2.0).toFloat().coerceAtLeast(10f)
                RadialGradient(cx, cy, radius, colorInts, null, tileMode)
            }
            "SWEEP" -> {
                SweepGradient(cx, cy, colorInts, null)
            }
            else -> {
                val rad = Math.toRadians(config.angle.toDouble())
                val halfW = rect.width() / 2f
                val halfH = rect.height() / 2f

                val dx = (Math.cos(rad) * halfW).toFloat()
                val dy = (Math.sin(rad) * halfH).toFloat()

                val x0 = cx - dx
                val y0 = cy - dy
                val x1 = cx + dx
                val y1 = cy + dy

                LinearGradient(x0, y0, x1, y1, colorInts, null, tileMode)
            }
        }
    }

    private fun renderBackground(
        canvas: Canvas,
        canvasW: Float,
        canvasH: Float,
        bg: TemplateElement.Background,
        editorState: MagicStoryEditorState,
        scaleFactor: Float
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val gradConfig = editorState.bgGradientConfig ?: if (!bg.gradientColors.isNullOrEmpty()) GradientConfig(bg.gradientColors, bg.gradientAngle) else null

        if (gradConfig != null && gradConfig.colors.size >= 2) {
            paint.shader = createGradientShader(RectF(0f, 0f, canvasW, canvasH), gradConfig)
        } else {
            paint.color = safeParseColor(editorState.getTextColor("bg", bg.color))
        }
        canvas.drawRect(0f, 0f, canvasW, canvasH, paint)

        // Draw Background Texture Overlay
        drawBackgroundTexture(canvas, canvasW, canvasH, editorState.bgTextureType, scaleFactor)
    }

    private fun drawBackgroundTexture(
        canvas: Canvas,
        canvasW: Float,
        canvasH: Float,
        textureType: String,
        scaleFactor: Float
    ) {
        if (textureType.equals("NONE", ignoreCase = true)) return

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
        }

        when (textureType.uppercase()) {
            "NOISE" -> {
                val random = Random(12345)
                paint.color = Color.argb(14, 0, 0, 0)
                paint.strokeWidth = 1.5f * scaleFactor
                val count = (canvasW * canvasH / 1000f).toInt().coerceIn(300, 3000)
                for (i in 0 until count) {
                    val rx = random.nextFloat() * canvasW
                    val ry = random.nextFloat() * canvasH
                    canvas.drawPoint(rx, ry, paint)
                }
            }
            "PAPER" -> {
                val random = Random(54321)
                paint.color = Color.argb(18, 255, 255, 255)
                paint.strokeWidth = 2f * scaleFactor
                val lines = (canvasH / (18f * scaleFactor)).toInt().coerceIn(20, 200)
                for (i in 0 until lines) {
                    val y = i * 18f * scaleFactor + (random.nextFloat() * 4f)
                    canvas.drawLine(0f, y, canvasW, y, paint)
                }
            }
            "CANVAS" -> {
                paint.color = Color.argb(12, 0, 0, 0)
                paint.strokeWidth = 1f * scaleFactor
                val step = 16f * scaleFactor
                var x = 0f
                while (x <= canvasW) {
                    canvas.drawLine(x, 0f, x, canvasH, paint)
                    x += step
                }
                var y = 0f
                while (y <= canvasH) {
                    canvas.drawLine(0f, y, canvasW, y, paint)
                    y += step
                }
            }
        }
    }

    private fun renderText(
        canvas: Canvas,
        canvasW: Float,
        canvasH: Float,
        textElem: TemplateElement.Text,
        manifest: TemplateManifest,
        editorState: MagicStoryEditorState,
        scaleFactor: Float
    ) {
        val textValue = editorState.getTextValue(textElem.id, textElem.defaultText)
        if (textValue.isEmpty()) return

        val refW = manifest.referenceWidth.toFloat()
        val refH = manifest.referenceHeight.toFloat()

        val elemX = editorState.getElementX(textElem.id, textElem.x) * canvasW
        val elemY = editorState.getElementY(textElem.id, textElem.y) * canvasH
        val elemW = editorState.getElementW(textElem.id, textElem.width) * refW * scaleFactor
        val elemH = editorState.getElementH(textElem.id, textElem.height) * refH * scaleFactor
        val elemRotation = editorState.getElementRotation(textElem.id, textElem.rotation)

        val flipX = editorState.getFlipX(textElem.id)
        val flipY = editorState.getFlipY(textElem.id)

        val centerX = elemX + elemW / 2f
        val centerY = elemY + elemH / 2f

        val rect = RectF(elemX, elemY, elemX + elemW, elemY + elemH)
        val effect = editorState.getLayerEffect(textElem.id)

        canvas.save()
        if (elemRotation != 0f) {
            canvas.rotate(elemRotation, centerX, centerY)
        }
        if (flipX || flipY) {
            canvas.scale(if (flipX) -1f else 1f, if (flipY) -1f else 1f, centerX, centerY)
        }

        // Draw Outer Glow pass
        drawOuterGlow(canvas, rect, effect, scaleFactor)

        // Draw Text Background Pill if configured
        val bgHex = editorState.getTextBgColor(textElem.id)
        if (!bgHex.isNullOrBlank()) {
            val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = safeParseColor(bgHex)
                style = Paint.Style.FILL
            }
            val pillRect = RectF(elemX - 6f * scaleFactor, elemY - 4f * scaleFactor, elemX + elemW + 6f * scaleFactor, elemY + elemH + 4f * scaleFactor)
            canvas.drawRoundRect(pillRect, 10f * scaleFactor, 10f * scaleFactor, bgPaint)
        }

        val fontSize = editorState.getFontSize(textElem.id, textElem.fontSize)
        val textColor = editorState.getTextColor(textElem.id, textElem.color)
        val fontWeight = editorState.getTextFontWeight(textElem.id, textElem.fontWeight)
        val textAlign = editorState.getTextAlignment(textElem.id, textElem.textAlignment)
        val letterSpacing = editorState.getLetterSpacing(textElem.id, 0f)
        val lineSpacing = editorState.getLineSpacing(textElem.id, textElem.lineSpacing)

        val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            val customGrad = editorState.getElementGradient(textElem.id)
            if (customGrad != null && customGrad.colors.size >= 2) {
                shader = createGradientShader(RectF(elemX, elemY, elemX + elemW, elemY + elemH), customGrad)
            } else {
                color = safeParseColor(textColor)
            }
            textSize = fontSize * scaleFactor
            this.letterSpacing = letterSpacing

            // Professional Drop Shadow Layer
            if (effect.dropShadowColor != null && effect.dropShadowRadius > 0f) {
                val colorInt = safeParseColor(effect.dropShadowColor)
                val alphaInt = (effect.dropShadowOpacity.coerceIn(0f, 1f) * Color.alpha(colorInt)).toInt().coerceIn(0, 255)
                val shadowColor = Color.argb(alphaInt, Color.red(colorInt), Color.green(colorInt), Color.blue(colorInt))

                setShadowLayer(
                    effect.dropShadowRadius * scaleFactor,
                    effect.dropShadowDx * scaleFactor,
                    effect.dropShadowDy * scaleFactor,
                    shadowColor
                )
            } else {
                val shadowHex = editorState.getTextShadowColor(textElem.id)
                if (!shadowHex.isNullOrBlank()) {
                    setShadowLayer(8f * scaleFactor, 3f * scaleFactor, 3f * scaleFactor, safeParseColor(shadowHex))
                }
            }

            val style = when (fontWeight.uppercase()) {
                "BOLD", "EXTRA_BOLD" -> Typeface.BOLD
                else -> Typeface.NORMAL
            }

            val familyKey = editorState.getTextFontFamily(textElem.id, textElem.fontFamily)

            typeface = when (familyKey.uppercase()) {
                "SERIF" -> Typeface.create(Typeface.SERIF, style)
                "SCRIPT", "CURSIVE" -> Typeface.create("cursive", if (style == Typeface.BOLD) Typeface.BOLD_ITALIC else Typeface.ITALIC)
                "MONOSPACE" -> Typeface.create(Typeface.MONOSPACE, style)
                "CASUAL" -> Typeface.create("casual", style)
                "BLACK", "SANS_SERIF_BLACK" -> Typeface.create("sans-serif-black", Typeface.BOLD)
                "CONDENSED", "SANS_SERIF_CONDENSED" -> Typeface.create("sans-serif-condensed", style)
                "LIGHT", "SANS_SERIF_LIGHT" -> Typeface.create("sans-serif-light", style)
                "MEDIUM", "SANS_SERIF_MEDIUM" -> Typeface.create("sans-serif-medium", style)
                else -> Typeface.create(Typeface.SANS_SERIF, style)
            }
        }

        val alignment = when (textAlign.uppercase()) {
            "LEFT" -> Layout.Alignment.ALIGN_NORMAL
            "RIGHT" -> Layout.Alignment.ALIGN_OPPOSITE
            else -> Layout.Alignment.ALIGN_CENTER
        }

        val staticLayout = StaticLayout.Builder.obtain(
            textValue, 0, textValue.length, textPaint, elemW.toInt().coerceAtLeast(10)
        )
            .setAlignment(alignment)
            .setLineSpacing(0f, lineSpacing)
            .setIncludePad(false)
            .build()

        val layoutHeight = staticLayout.height
        val startY = elemY + (elemH - layoutHeight) / 2f

        // Draw Text Stroke Outline if configured
        val stroke = editorState.getElementStroke(textElem.id)
        if (stroke != null && stroke.second > 0f) {
            val strokePaint = TextPaint(textPaint).apply {
                style = Paint.Style.STROKE
                strokeWidth = stroke.second * scaleFactor * 2f
                color = safeParseColor(stroke.first)
                shader = null
            }

            val strokeLayout = StaticLayout.Builder.obtain(
                textValue, 0, textValue.length, strokePaint, elemW.toInt().coerceAtLeast(10)
            )
                .setAlignment(alignment)
                .setLineSpacing(0f, lineSpacing)
                .setIncludePad(false)
                .build()

            canvas.save()
            canvas.translate(elemX, startY)
            strokeLayout.draw(canvas)
            canvas.restore()
        }

        canvas.save()
        canvas.translate(elemX, startY)
        staticLayout.draw(canvas)
        canvas.restore()

        // Draw Inner Glow pass
        drawInnerGlow(canvas, rect, effect, scaleFactor)

        val isSelected = !editorState.isExporting && editorState.isElementSelected(textElem.id)
        drawSelectionOutline(canvas, RectF(elemX, elemY, elemX + elemW, elemY + elemH), isSelected, scaleFactor)

        canvas.restore()
    }

    private fun renderShape(
        canvas: Canvas,
        canvasW: Float,
        canvasH: Float,
        shape: TemplateElement.Shape,
        manifest: TemplateManifest,
        editorState: MagicStoryEditorState,
        scaleFactor: Float
    ) {
        val refW = manifest.referenceWidth.toFloat()
        val refH = manifest.referenceHeight.toFloat()

        val x = editorState.getElementX(shape.id, shape.x) * canvasW
        val y = editorState.getElementY(shape.id, shape.y) * canvasH
        val w = editorState.getElementW(shape.id, shape.width) * refW * scaleFactor
        val h = editorState.getElementH(shape.id, shape.height) * refH * scaleFactor
        val rotation = editorState.getElementRotation(shape.id, shape.rotation)

        val flipX = editorState.getFlipX(shape.id)
        val flipY = editorState.getFlipY(shape.id)

        val rect = RectF(x, y, x + w, y + h)
        val effect = editorState.getLayerEffect(shape.id)
        val cornerRadius = shape.cornerRadius * scaleFactor

        canvas.save()
        if (rotation != 0f) {
            canvas.rotate(rotation, rect.centerX(), rect.centerY())
        }
        if (flipX || flipY) {
            canvas.scale(if (flipX) -1f else 1f, if (flipY) -1f else 1f, rect.centerX(), rect.centerY())
        }

        // Draw Outer Glow
        drawOuterGlow(canvas, rect, effect, scaleFactor, cornerRadius)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        if (effect.dropShadowColor != null && effect.dropShadowRadius > 0f) {
            val colorInt = safeParseColor(effect.dropShadowColor)
            val alphaInt = (effect.dropShadowOpacity.coerceIn(0f, 1f) * Color.alpha(colorInt)).toInt().coerceIn(0, 255)
            val shadowColor = Color.argb(alphaInt, Color.red(colorInt), Color.green(colorInt), Color.blue(colorInt))

            paint.setShadowLayer(
                effect.dropShadowRadius * scaleFactor,
                effect.dropShadowDx * scaleFactor,
                effect.dropShadowDy * scaleFactor,
                shadowColor
            )
        } else if (shape.shadowColor != null && shape.shadowRadius > 0f) {
            paint.setShadowLayer(
                shape.shadowRadius * scaleFactor,
                shape.shadowDx * scaleFactor,
                shape.shadowDy * scaleFactor,
                safeParseColor(shape.shadowColor)
            )
        }

        val customGrad = editorState.getElementGradient(shape.id)
        if (customGrad != null && customGrad.colors.size >= 2) {
            paint.shader = createGradientShader(rect, customGrad)
        } else {
            paint.color = safeParseColor(editorState.getTextColor(shape.id, shape.color))
        }
        paint.style = Paint.Style.FILL

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

        drawCustomStroke(canvas, rect, shape.id, editorState, scaleFactor, cornerRadius)
        drawInnerGlow(canvas, rect, effect, scaleFactor, cornerRadius)

        val isSelected = !editorState.isExporting && editorState.isElementSelected(shape.id)
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
        manifest: TemplateManifest,
        editorState: MagicStoryEditorState,
        context: Context,
        scaleFactor: Float,
        bitmapCache: MutableMap<String, Bitmap>
    ) {
        val refW = manifest.referenceWidth.toFloat()
        val refH = manifest.referenceHeight.toFloat()

        val x = editorState.getElementX(slot.id, slot.x) * canvasW
        val y = editorState.getElementY(slot.id, slot.y) * canvasH
        val w = editorState.getElementW(slot.id, slot.width) * refW * scaleFactor
        val h = editorState.getElementH(slot.id, slot.height) * refH * scaleFactor
        val rotation = editorState.getElementRotation(slot.id, slot.rotation)

        val flipX = editorState.getFlipX(slot.id)
        val flipY = editorState.getFlipY(slot.id)

        val destRect = RectF(x, y, x + w, y + h)
        val effect = editorState.getLayerEffect(slot.id)
        val cornerRadius = slot.cornerRadius * scaleFactor

        val photoUri = editorState.getPhotoUri(slot.id, slot.defaultAsset)
        val bitmap = getOrLoadBitmap(context, photoUri, w.toInt(), h.toInt(), bitmapCache)

        canvas.save()
        if (rotation != 0f) {
            canvas.rotate(rotation, destRect.centerX(), destRect.centerY())
        }
        if (flipX || flipY) {
            canvas.scale(if (flipX) -1f else 1f, if (flipY) -1f else 1f, destRect.centerX(), destRect.centerY())
        }

        drawOuterGlow(canvas, destRect, effect, scaleFactor, cornerRadius)
        drawDarkGradientPlaceholder(canvas, destRect, cornerRadius)
        BitmapUtils.drawCoverBitmap(canvas, bitmap, destRect, cornerRadius)

        drawCustomStroke(canvas, destRect, slot.id, editorState, scaleFactor, cornerRadius)
        drawInnerGlow(canvas, destRect, effect, scaleFactor, cornerRadius)

        val isSelected = !editorState.isExporting && editorState.isElementSelected(slot.id)
        drawSelectionOutline(canvas, destRect, isSelected, scaleFactor)

        canvas.restore()
    }

    private fun renderPhotoCard(
        canvas: Canvas,
        canvasW: Float,
        canvasH: Float,
        card: TemplateElement.PhotoCard,
        manifest: TemplateManifest,
        editorState: MagicStoryEditorState,
        context: Context,
        scaleFactor: Float,
        bitmapCache: MutableMap<String, Bitmap>
    ) {
        val refW = manifest.referenceWidth.toFloat()
        val refH = manifest.referenceHeight.toFloat()

        val cardX = editorState.getElementX(card.id, card.x) * canvasW
        val cardY = editorState.getElementY(card.id, card.y) * canvasH
        val cardW = editorState.getElementW(card.id, card.width) * refW * scaleFactor
        val cardH = editorState.getElementH(card.id, card.height) * refH * scaleFactor
        val rotation = editorState.getElementRotation(card.id, card.rotation)

        val flipX = editorState.getFlipX(card.id)
        val flipY = editorState.getFlipY(card.id)

        val cardRect = RectF(cardX, cardY, cardX + cardW, cardY + cardH)
        val effect = editorState.getLayerEffect(card.id)
        val cardCornerRadius = card.cornerRadius * scaleFactor

        val centerX = cardRect.centerX()
        val centerY = cardRect.centerY()

        canvas.save()
        canvas.rotate(rotation, centerX, centerY)
        if (flipX || flipY) {
            canvas.scale(if (flipX) -1f else 1f, if (flipY) -1f else 1f, centerX, centerY)
        }

        drawOuterGlow(canvas, cardRect, effect, scaleFactor, cardCornerRadius)

        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = safeParseColor(card.backgroundColor)
            style = Paint.Style.FILL
            if (effect.dropShadowColor != null && effect.dropShadowRadius > 0f) {
                val colorInt = safeParseColor(effect.dropShadowColor)
                val alphaInt = (effect.dropShadowOpacity.coerceIn(0f, 1f) * Color.alpha(colorInt)).toInt().coerceIn(0, 255)
                val shadowColor = Color.argb(alphaInt, Color.red(colorInt), Color.green(colorInt), Color.blue(colorInt))

                setShadowLayer(
                    effect.dropShadowRadius * scaleFactor,
                    effect.dropShadowDx * scaleFactor,
                    effect.dropShadowDy * scaleFactor,
                    shadowColor
                )
            } else if (card.shadowColor != null && card.shadowRadius > 0f) {
                setShadowLayer(
                    card.shadowRadius * scaleFactor,
                    card.shadowDx * scaleFactor,
                    card.shadowDy * scaleFactor,
                    safeParseColor(card.shadowColor)
                )
            }
        }
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

        drawCustomStroke(canvas, cardRect, card.id, editorState, scaleFactor, cardCornerRadius)
        drawInnerGlow(canvas, cardRect, effect, scaleFactor, cardCornerRadius)

        val isSelected = !editorState.isExporting && (editorState.isElementSelected(card.id) || editorState.isElementSelected(card.photoSlotId))
        drawSelectionOutline(canvas, cardRect, isSelected, scaleFactor)

        canvas.restore()
    }

    private fun renderPillCta(
        canvas: Canvas,
        canvasW: Float,
        canvasH: Float,
        pillCta: TemplateElement.PillCta,
        manifest: TemplateManifest,
        editorState: MagicStoryEditorState,
        scaleFactor: Float
    ) {
        val refW = manifest.referenceWidth.toFloat()
        val refH = manifest.referenceHeight.toFloat()

        val x = editorState.getElementX(pillCta.id, pillCta.x) * canvasW
        val y = editorState.getElementY(pillCta.id, pillCta.y) * canvasH
        val w = editorState.getElementW(pillCta.id, pillCta.width) * refW * scaleFactor
        val h = editorState.getElementH(pillCta.id, pillCta.height) * refH * scaleFactor
        val rotation = editorState.getElementRotation(pillCta.id, pillCta.rotation)

        val flipX = editorState.getFlipX(pillCta.id)
        val flipY = editorState.getFlipY(pillCta.id)

        val containerRect = RectF(x, y, x + w, y + h)
        val effect = editorState.getLayerEffect(pillCta.id)
        val cornerRadius = pillCta.cornerRadius * scaleFactor

        canvas.save()
        if (rotation != 0f) {
            canvas.rotate(rotation, containerRect.centerX(), containerRect.centerY())
        }
        if (flipX || flipY) {
            canvas.scale(if (flipX) -1f else 1f, if (flipY) -1f else 1f, containerRect.centerX(), containerRect.centerY())
        }

        drawOuterGlow(canvas, containerRect, effect, scaleFactor, cornerRadius)

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
            val customGrad = editorState.getElementGradient(pillCta.id)
            if (customGrad != null && customGrad.colors.size >= 2) {
                shader = createGradientShader(btnRect, customGrad)
            } else {
                color = safeParseColor(pillCta.buttonBgColor)
            }
            style = Paint.Style.FILL
        }
        val btnCornerRadius = (pillCta.cornerRadius * 0.8f) * scaleFactor
        canvas.drawRoundRect(btnRect, btnCornerRadius, btnCornerRadius, btnBgPaint)

        val buttonText = editorState.getTextValue(pillCta.buttonTextId, pillCta.buttonDefaultText)
        if (buttonText.isNotEmpty()) {
            val buttonFontSize = editorState.getFontSize(pillCta.buttonTextId, pillCta.buttonFontSize)
            val btnTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = safeParseColor(pillCta.buttonTextColor)
                textSize = buttonFontSize * scaleFactor
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            }

            val staticLayout = StaticLayout.Builder.obtain(
                buttonText, 0, buttonText.length, btnTextPaint, btnW.toInt().coerceAtLeast(10)
            )
                .setAlignment(Layout.Alignment.ALIGN_CENTER)
                .build()

            val textY = btnY + (btnH - staticLayout.height) / 2f
            canvas.save()
            canvas.translate(btnX, textY)
            staticLayout.draw(canvas)
            canvas.restore()
        }

        drawCustomStroke(canvas, containerRect, pillCta.id, editorState, scaleFactor, cornerRadius)
        drawInnerGlow(canvas, containerRect, effect, scaleFactor, cornerRadius)

        val isSelected = !editorState.isExporting && (editorState.isElementSelected(pillCta.id) || editorState.isElementSelected(pillCta.leftTextId) || editorState.isElementSelected(pillCta.buttonTextId))
        drawSelectionOutline(canvas, containerRect, isSelected, scaleFactor)

        canvas.restore()
    }

    private fun drawOuterGlow(
        canvas: Canvas,
        rect: RectF,
        effect: LayerEffectConfig,
        scaleFactor: Float,
        cornerRadius: Float = 0f
    ) {
        if (effect.outerGlowColor == null || effect.outerGlowRadius <= 0f) return

        val glowColorInt = safeParseColor(effect.outerGlowColor)
        val alphaInt = (effect.outerGlowOpacity.coerceIn(0f, 1f) * Color.alpha(glowColorInt)).toInt().coerceIn(0, 255)
        val finalGlowColor = Color.argb(alphaInt, Color.red(glowColorInt), Color.green(glowColorInt), Color.blue(glowColorInt))

        val spreadOffset = effect.outerGlowSpread * scaleFactor
        val glowRect = RectF(rect).apply {
            inset(-spreadOffset, -spreadOffset)
        }

        // Pass 1: Soft outer halo blur
        val outerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = finalGlowColor
            style = Paint.Style.FILL
            setShadowLayer(
                effect.outerGlowRadius * scaleFactor,
                0f,
                0f,
                finalGlowColor
            )
        }

        val effCorner = if (cornerRadius > 0f) cornerRadius + spreadOffset else 0f
        if (effCorner > 0f) {
            canvas.drawRoundRect(glowRect, effCorner, effCorner, outerPaint)
        } else {
            canvas.drawRect(glowRect, outerPaint)
        }

        // Pass 2: Intense core spread
        if (spreadOffset > 0f) {
            val corePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = finalGlowColor
                style = Paint.Style.FILL
                setShadowLayer(
                    effect.outerGlowRadius * 0.4f * scaleFactor,
                    0f,
                    0f,
                    finalGlowColor
                )
            }
            if (effCorner > 0f) {
                canvas.drawRoundRect(glowRect, effCorner, effCorner, corePaint)
            } else {
                canvas.drawRect(glowRect, corePaint)
            }
        }
    }

    private fun drawInnerGlow(
        canvas: Canvas,
        rect: RectF,
        effect: LayerEffectConfig,
        scaleFactor: Float,
        cornerRadius: Float = 0f
    ) {
        if (effect.innerGlowColor == null || effect.innerGlowRadius <= 0f) return

        val glowColorInt = safeParseColor(effect.innerGlowColor)
        val alphaInt = (effect.innerGlowOpacity.coerceIn(0f, 1f) * Color.alpha(glowColorInt)).toInt().coerceIn(0, 255)
        val finalGlowColor = Color.argb(alphaInt, Color.red(glowColorInt), Color.green(glowColorInt), Color.blue(glowColorInt))

        canvas.save()
        val clipPath = Path()
        if (cornerRadius > 0f) {
            clipPath.addRoundRect(rect, cornerRadius, cornerRadius, Path.Direction.CW)
        } else {
            clipPath.addRect(rect, Path.Direction.CW)
        }
        canvas.clipPath(clipPath)

        val chokeOffset = effect.innerGlowChoke * scaleFactor
        val strokeW = (effect.innerGlowRadius + effect.innerGlowChoke) * scaleFactor * 0.8f

        val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = finalGlowColor
            style = Paint.Style.STROKE
            strokeWidth = strokeW
            setShadowLayer(
                effect.innerGlowRadius * scaleFactor,
                0f,
                0f,
                finalGlowColor
            )
        }

        val halfStroke = strokeW / 2f
        val insetRect = RectF(rect).apply {
            inset(halfStroke + chokeOffset, halfStroke + chokeOffset)
        }

        val effCorner = (cornerRadius - halfStroke - chokeOffset).coerceAtLeast(0f)
        if (effCorner > 0f) {
            canvas.drawRoundRect(insetRect, effCorner, effCorner, glowPaint)
        } else {
            canvas.drawRect(insetRect, glowPaint)
        }
        canvas.restore()
    }

    private fun drawCustomStroke(
        canvas: Canvas,
        rect: RectF,
        elementId: String,
        editorState: MagicStoryEditorState,
        scaleFactor: Float,
        cornerRadius: Float = 0f
    ) {
        val stroke = editorState.getElementStroke(elementId) ?: return
        val (colorHex, widthDp) = stroke
        if (widthDp <= 0f) return

        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = safeParseColor(colorHex)
            style = Paint.Style.STROKE
            strokeWidth = widthDp * scaleFactor
        }

        if (cornerRadius > 0f) {
            canvas.drawRoundRect(rect, cornerRadius, cornerRadius, strokePaint)
        } else {
            canvas.drawRect(rect, strokePaint)
        }
    }

    private fun drawSelectionOutline(
        canvas: Canvas,
        rect: RectF,
        isSelected: Boolean,
        scaleFactor: Float
    ) {
        if (!isSelected) return

        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#C91F2C")
            style = Paint.Style.STROKE
            strokeWidth = 2.5f * scaleFactor
        }
        canvas.drawRect(rect, strokePaint)

        val handlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#C91F2C")
            style = Paint.Style.FILL
        }
        val whitePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }

        val handleRadius = 6.5f * scaleFactor
        val corners = arrayOf(
            Pair(rect.left, rect.top),
            Pair(rect.right, rect.top),
            Pair(rect.left, rect.bottom),
            Pair(rect.right, rect.bottom)
        )

        corners.forEach { (cx, cy) ->
            canvas.drawCircle(cx, cy, handleRadius, handlePaint)
            canvas.drawCircle(cx, cy, handleRadius * 0.5f, whitePaint)
        }

        val topCenterX = rect.centerX()
        val topCenterY = rect.top - (22f * scaleFactor)

        val stemPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#C91F2C")
            style = Paint.Style.STROKE
            strokeWidth = 2f * scaleFactor
        }
        canvas.drawLine(rect.centerX(), rect.top, topCenterX, topCenterY, stemPaint)

        canvas.drawCircle(topCenterX, topCenterY, handleRadius * 1.1f, handlePaint)
        canvas.drawCircle(topCenterX, topCenterY, handleRadius * 0.6f, whitePaint)
    }

    fun sampleColorAt(
        context: Context,
        manifest: TemplateManifest,
        editorState: MagicStoryEditorState,
        normX: Float,
        normY: Float,
        width: Int = 360,
        height: Int = 640
    ): String {
        return try {
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val tempCanvas = Canvas(bitmap)
            renderToCanvas(tempCanvas, width.toFloat(), height.toFloat(), manifest, editorState, context)

            val px = (normX * width).toInt().coerceIn(0, width - 1)
            val py = (normY * height).toInt().coerceIn(0, height - 1)
            val pixel = bitmap.getPixel(px, py)
            bitmap.recycle()

            String.format("#%06X", 0xFFFFFF and pixel)
        } catch (e: Exception) {
            "#C91F2C"
        }
    }

    private fun getOrLoadBitmap(
        context: Context,
        uriString: String?,
        reqW: Int,
        reqH: Int,
        bitmapCache: MutableMap<String, Bitmap>
    ): Bitmap {
        val key = uriString ?: "fallback"
        return bitmapCache.getOrPut(key) {
            BitmapUtils.loadBitmap(context, uriString, reqW.coerceAtLeast(100), reqH.coerceAtLeast(100))
        }
    }

    private fun safeParseColor(colorHex: String?): Int {
        if (colorHex.isNullOrBlank()) return Color.TRANSPARENT
        return try {
            Color.parseColor(colorHex)
        } catch (e: Exception) {
            Color.BLACK
        }
    }
}
