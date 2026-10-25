package com.example.myapplication

import kotlin.random.Random

/** Состояние конкретного насекомого. Никаких Android View здесь нет. */
enum class SeasonBugMode {
    MOVING,
    FROZEN,     // зима: остановка подо льдом
    SLEEPING,   // осень: временный сон
    BOOSTED     // весна: кратковременный рывок
}

/**
 * Все времена указаны в миллисекундах SystemClock.elapsedRealtime().
 * Состояние не зависит от размера экрана и сохраняется вместе с BugSnapshot.
 */
data class SeasonBugState(
    val mode: SeasonBugMode = SeasonBugMode.MOVING,
    val effectUntilMs: Long = 0L,
    val nextEventAtMs: Long = Long.MAX_VALUE
)

/** Чистая логика сезонных событий, пригодная для обычных JUnit-тестов. */
object SeasonEffects {
    private fun nextDelay(season: Season, random: Random): Long = when (season) {
        Season.WINTER -> random.nextLong(5_000L, 9_001L)
        Season.SPRING -> random.nextLong(4_000L, 8_001L)
        Season.SUMMER -> Long.MAX_VALUE
        Season.AUTUMN -> random.nextLong(5_000L, 10_001L)
    }

    private fun effectLength(season: Season, random: Random): Long = when (season) {
        Season.WINTER -> random.nextLong(2_000L, 4_001L)
        Season.SPRING -> random.nextLong(1_500L, 3_001L)
        Season.SUMMER -> 0L
        Season.AUTUMN -> random.nextLong(2_000L, 4_001L)
    }

    /** Вызывается один раз при появлении нового жука. */
    fun initial(season: Season, nowMs: Long, random: Random = Random.Default): SeasonBugState =
        SeasonBugState(nextEventAtMs = schedule(season, nowMs, random))

    private fun schedule(season: Season, nowMs: Long, random: Random): Long =
        if (season == Season.SUMMER) Long.MAX_VALUE else nowMs + nextDelay(season, random)

    /** Обновляет состояния по часам, а не по количеству кадров (16 мс). */
    fun advance(
        season: Season,
        current: SeasonBugState,
        nowMs: Long,
        random: Random = Random.Default
    ): SeasonBugState {
        if (season == Season.SUMMER) return SeasonBugState()

        if (current.mode != SeasonBugMode.MOVING) {
            if (nowMs < current.effectUntilMs) return current
            return SeasonBugState(nextEventAtMs = schedule(season, nowMs, random))
        }

        if (current.nextEventAtMs == Long.MAX_VALUE) return initial(season, nowMs, random)
        if (nowMs < current.nextEventAtMs) return current

        val mode = when (season) {
            Season.WINTER -> SeasonBugMode.FROZEN
            Season.SPRING -> SeasonBugMode.BOOSTED
            Season.AUTUMN -> SeasonBugMode.SLEEPING
            Season.SUMMER -> SeasonBugMode.MOVING
        }
        return SeasonBugState(
            mode = mode,
            effectUntilMs = nowMs + effectLength(season, random)
        )
    }

    /** Первый тап по замороженному жуку только разбивает лёд. */
    fun breakIce(
        current: SeasonBugState,
        nowMs: Long,
        random: Random = Random.Default
    ): SeasonBugState = if (current.mode == SeasonBugMode.FROZEN) {
        SeasonBugState(nextEventAtMs = schedule(Season.WINTER, nowMs, random))
    } else current

    /** Множитель к скорости с учётом временного эффекта. */
    fun movementMultiplier(state: SeasonBugState): Float = when (state.mode) {
        SeasonBugMode.MOVING -> 1f
        SeasonBugMode.BOOSTED -> 1.65f
        SeasonBugMode.FROZEN, SeasonBugMode.SLEEPING -> 0f
    }
}
