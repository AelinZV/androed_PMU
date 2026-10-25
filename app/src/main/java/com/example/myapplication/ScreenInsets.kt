package com.example.myapplication

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

/**
 * Отступы от системных панелей Android, в том числе при edge-to-edge на новых SDK.
 * Не добавляем отступ повторно на каждом вызове insets: берём исходные padding.
 */
object ScreenInsets {
    fun apply(view: View, additionalTopDp: Int = 0, additionalBottomDp: Int = 0) {
        val density = view.resources.displayMetrics.density
        val top = (additionalTopDp * density).toInt()
        val bottom = (additionalBottomDp * density).toInt()
        val startPadding = view.paddingLeft
        val topPadding = view.paddingTop
        val endPadding = view.paddingRight
        val bottomPadding = view.paddingBottom
        ViewCompat.setOnApplyWindowInsetsListener(view) { target, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or
                    WindowInsetsCompat.Type.displayCutout()
            )
            target.setPadding(
                startPadding + bars.left,
                topPadding + bars.top + top,
                endPadding + bars.right,
                bottomPadding + bars.bottom + bottom
            )
            insets
        }
        ViewCompat.requestApplyInsets(view)
    }
}
