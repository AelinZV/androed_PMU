package com.example.myapplication

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.widget.ImageView

/** Только визуальная индикация: ледяная оболочка и надпись Zzz. */
object SeasonBugDecoration {
    fun apply(image: ImageView, mode: SeasonBugMode) {
        image.foreground = when (mode) {
            SeasonBugMode.FROZEN -> EffectOverlay(ice = true)
            SeasonBugMode.SLEEPING -> EffectOverlay(ice = false)
            SeasonBugMode.MOVING, SeasonBugMode.BOOSTED -> null
        }
        image.alpha = if (mode == SeasonBugMode.SLEEPING) 0.75f else 1f
        image.scaleX = if (mode == SeasonBugMode.BOOSTED) 1.08f else 1f
        image.scaleY = if (mode == SeasonBugMode.BOOSTED) 1.08f else 1f
    }

    private class EffectOverlay(private val ice: Boolean) : Drawable() {
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private var opacity = 255

        override fun draw(canvas: Canvas) {
            val w = bounds.width().toFloat()
            val h = bounds.height().toFloat()
            if (w <= 0f || h <= 0f) return
            val left = bounds.left.toFloat()
            val top = bounds.top.toFloat()

            if (ice) {
                // Полупрозрачный голубой кубик поверх изображения жука.
                val rect = RectF(left + 2f, top + 2f, left + w - 2f, top + h - 2f)
                paint.style = Paint.Style.FILL
                paint.color = Color.argb(opacity * 105 / 255, 92, 202, 249)
                canvas.drawRoundRect(rect, w * .16f, h * .16f, paint)
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = w.coerceAtMost(h) * .055f
                paint.color = Color.argb(opacity * 220 / 255, 224, 249, 255)
                canvas.drawRoundRect(rect, w * .16f, h * .16f, paint)
                paint.style = Paint.Style.FILL
                paint.color = Color.argb(opacity * 230 / 255, 246, 253, 255)
                paint.textAlign = Paint.Align.CENTER
                paint.textSize = w.coerceAtMost(h) * .24f
                paint.isFakeBoldText = true
                canvas.drawText("❄", left + w * .77f, top + h * .30f, paint)
                paint.isFakeBoldText = false
            } else {
                paint.style = Paint.Style.FILL
                paint.color = Color.argb(opacity, 96, 55, 181)
                paint.textAlign = Paint.Align.RIGHT
                paint.textSize = w.coerceAtMost(h) * .30f
                paint.isFakeBoldText = true
                paint.setShadowLayer(2f, 1f, 1f, Color.WHITE)
                canvas.drawText("Zzz", left + w * .94f, top + h * .29f, paint)
                paint.clearShadowLayer()
                paint.isFakeBoldText = false
            }
        }

        override fun setAlpha(alpha: Int) { opacity = alpha.coerceIn(0, 255); invalidateSelf() }
        override fun setColorFilter(colorFilter: ColorFilter?) { paint.colorFilter = colorFilter; invalidateSelf() }
        @Deprecated("Deprecated in Java")
        override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
    }
}
