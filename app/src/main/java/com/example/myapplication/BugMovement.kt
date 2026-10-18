package com.example.myapplication

import kotlin.math.abs

/** Чистая игровая логика: изменение координат и отражение от границ поля. */
object BugMovement {
    data class Position(
        val x: Float,
        val y: Float,
        val dx: Float,
        val dy: Float
    )

    /**
     * x, y — относительные координаты от 0 до 1.
     * dx, dy и влияние наклона заданы в пикселях за игровой шаг.
     * availableWidth/Height — доступная область перемещения в пикселях.
     */
    fun move(
        x: Float,
        y: Float,
        dx: Float,
        dy: Float,
        availableWidth: Int,
        availableHeight: Int,
        forceX: Float = 0f,
        forceY: Float = 0f
    ): Position {
        val width = availableWidth.coerceAtLeast(1)
        val height = availableHeight.coerceAtLeast(1)
        var nextX = x + (dx + forceX) / width
        var nextY = y + (dy + forceY) / height
        var nextDx = dx
        var nextDy = dy

        if (nextX <= 0f) {
            nextX = 0f
            nextDx = abs(nextDx)
        } else if (nextX >= 1f) {
            nextX = 1f
            nextDx = -abs(nextDx)
        }
        if (nextY <= 0f) {
            nextY = 0f
            nextDy = abs(nextDy)
        } else if (nextY >= 1f) {
            nextY = 1f
            nextDy = -abs(nextDy)
        }
        return Position(nextX, nextY, nextDx, nextDy)
    }
}
