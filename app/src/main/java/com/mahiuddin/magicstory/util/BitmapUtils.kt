package com.mahiuddin.magicstory.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.media.ExifInterface
import android.net.Uri
import java.io.InputStream

object BitmapUtils {

    fun loadBitmap(context: Context, pathOrUri: String?, targetWidth: Int = 1080, targetHeight: Int = 1080): Bitmap {
        if (pathOrUri.isNullOrBlank()) {
            return generateFallbackBitmap(targetWidth, targetHeight, "MagicStory")
        }

        val nonNullPath = pathOrUri!!

        return try {
            val bitmap = when {
                nonNullPath.startsWith("content://") || nonNullPath.startsWith("file://") -> {
                    val uri = Uri.parse(nonNullPath)
                    loadFromUri(context, uri, targetWidth, targetHeight)
                }
                else -> {
                    loadFromAssets(context, nonNullPath, targetWidth, targetHeight)
                }
            }
            bitmap ?: generateFallbackBitmap(targetWidth, targetHeight, nonNullPath)
        } catch (e: Exception) {
            e.printStackTrace()
            generateFallbackBitmap(targetWidth, targetHeight, nonNullPath)
        }
    }

    private fun loadFromUri(context: Context, uri: Uri, reqWidth: Int, reqHeight: Int): Bitmap? {
        val resolver = context.contentResolver
        var inputStream: InputStream? = resolver.openInputStream(uri) ?: return null

        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeStream(inputStream, null, options)
        inputStream?.close()

        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
        options.inJustDecodeBounds = false

        inputStream = resolver.openInputStream(uri) ?: return null
        val decoded = BitmapFactory.decodeStream(inputStream, null, options)
        inputStream.close()

        if (decoded == null) return null

        return try {
            resolver.openInputStream(uri)?.use { stream ->
                val exif = ExifInterface(stream)
                val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
                rotateBitmapIfNeeded(decoded, orientation)
            } ?: decoded
        } catch (_: Exception) {
            decoded
        }
    }

    private fun loadFromAssets(context: Context, assetPath: String, reqWidth: Int, reqHeight: Int): Bitmap? {
        val cleanedPath = assetPath.removePrefix("assets/").removePrefix("/")
        return try {
            val inputStream = context.assets.open(cleanedPath)
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream.close()

            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
            options.inJustDecodeBounds = false

            val stream2 = context.assets.open(cleanedPath)
            val bitmap = BitmapFactory.decodeStream(stream2, null, options)
            stream2.close()
            bitmap
        } catch (_: Exception) {
            null
        }
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    private fun rotateBitmapIfNeeded(bitmap: Bitmap, orientation: Int): Bitmap {
        val degrees = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }

        if (degrees == 0f) return bitmap

        val matrix = Matrix().apply { postRotate(degrees) }
        val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        if (rotated != bitmap) {
            bitmap.recycle()
        }
        return rotated
    }

    fun drawCoverBitmap(canvas: Canvas, bitmap: Bitmap, destRect: RectF, cornerRadius: Float = 0f, paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)) {
        val bw = bitmap.width.toFloat()
        val bh = bitmap.height.toFloat()
        if (bw <= 0 || bh <= 0) return

        val scale = maxOf(destRect.width() / bw, destRect.height() / bh)
        val scaledW = bw * scale
        val scaledH = bh * scale
        val dx = destRect.left + (destRect.width() - scaledW) / 2f
        val dy = destRect.top + (destRect.height() - scaledH) / 2f

        val matrix = Matrix()
        matrix.postScale(scale, scale)
        matrix.postTranslate(dx, dy)

        canvas.save()
        if (cornerRadius > 0f) {
            val path = Path().apply {
                addRoundRect(destRect, cornerRadius, cornerRadius, Path.Direction.CW)
            }
            canvas.clipPath(path)
        } else {
            canvas.clipRect(destRect)
        }

        canvas.drawBitmap(bitmap, matrix, paint)
        canvas.restore()
    }

    fun generateFallbackBitmap(width: Int, height: Int, label: String): Bitmap {
        val w = if (width > 0) width else 800
        val h = if (height > 0) height else 1200
        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Subtle dark gradient ("halka gradient dark color")
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val gradient = LinearGradient(
            0f, 0f, w.toFloat(), h.toFloat(),
            intArrayOf(
                Color.parseColor("#383032"),
                Color.parseColor("#221D1E"),
                Color.parseColor("#141112")
            ),
            floatArrayOf(0.0f, 0.5f, 1.0f),
            Shader.TileMode.CLAMP
        )
        paint.shader = gradient
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), paint)

        // Subtle inner decorative frame
        val decPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(35, 255, 255, 255)
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }
        val padding = minOf(w, h) * 0.08f
        canvas.drawRoundRect(
            padding, padding, w - padding, h - padding,
            minOf(w, h) * 0.05f, minOf(w, h) * 0.05f,
            decPaint
        )

        // Camera / Image frame icon
        val iconPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(180, 255, 255, 255)
            style = Paint.Style.STROKE
            strokeWidth = minOf(w, h) * 0.012f
        }
        val centerX = w * 0.5f
        val centerY = h * 0.44f
        val iconSize = minOf(w, h) * 0.12f

        val iconRect = RectF(centerX - iconSize, centerY - iconSize * 0.75f, centerX + iconSize, centerY + iconSize * 0.75f)
        canvas.drawRoundRect(iconRect, iconSize * 0.2f, iconSize * 0.2f, iconPaint)
        canvas.drawCircle(centerX, centerY, iconSize * 0.35f, iconPaint)

        // Placeholder Text
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(220, 255, 255, 255)
            textSize = minOf(w, h) * 0.045f
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }
        canvas.drawText("ADD YOUR PHOTO", centerX, centerY + iconSize * 1.4f, textPaint)

        val subTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(140, 255, 255, 255)
            textSize = minOf(w, h) * 0.032f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        }
        canvas.drawText("Tap 'Replace Photo' below", centerX, centerY + iconSize * 1.9f, subTextPaint)

        return bitmap
    }
}
