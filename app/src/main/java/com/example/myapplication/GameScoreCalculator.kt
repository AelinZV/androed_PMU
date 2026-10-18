package com.example.myapplication

import kotlin.math.roundToInt

/** Правила начисления очков отделены от Android UI. */
object GameScoreCalculator {
    const val MISS_PENALTY = 5

    fun pointsForBug(type: BugType): Int = when (type) {
        BugType.NORMAL -> 10
        BugType.FAST -> 20
        BugType.RARE -> 50
    }

    /** Золотой жук: одна сотая цены грамма золота ЦБ в рублях. */
    fun pointsForGoldenBug(rublesPerGram: Double): Int =
        (rublesPerGram / 100.0).roundToInt().coerceAtLeast(1)

    fun afterHit(score: Int, points: Int): Int = score + points

    fun afterMiss(score: Int): Int = score - MISS_PENALTY
}
