package com.example.myapplication

import android.content.Context
import android.media.MediaPlayer
import android.util.Log

/** Фоновая сезонная музыка только во время раунда. */
class SeasonSoundPlayer(private val context: Context) {
    private var player: MediaPlayer? = null

    fun prepare(season: Season, startPositionMs: Int = 0) {
        release()
        val soundRes = when (season) {
            Season.SPRING -> R.raw.season_spring
            Season.SUMMER -> R.raw.season_summer
            Season.AUTUMN -> R.raw.season_autumn
            Season.WINTER -> R.raw.season_winter
        }
        player = try {
            MediaPlayer.create(context, soundRes)?.apply {
                isLooping = true
                // Тише звуковых эффектов, чтобы был слышен «крик жуков» из ЛР №6.
                setVolume(0.35f, 0.35f)
                if (startPositionMs > 0 && duration > 0) {
                    seekTo(startPositionMs % duration)
                }
            }
        } catch (error: Exception) {
            Log.e("SeasonSound", "Не удалось подготовить музыку", error)
            null
        }
    }

    fun play() {
        try {
            player?.let { if (!it.isPlaying) it.start() }
        } catch (error: IllegalStateException) {
            Log.e("SeasonSound", "Не удалось запустить музыку", error)
        }
    }

    /** Останавливает звук при уходе приложения в фон, сохраняя позицию. */
    fun pause(): Int {
        val p = player ?: return 0
        return try {
            if (p.isPlaying) p.pause()
            p.currentPosition.coerceAtLeast(0)
        } catch (error: IllegalStateException) {
            Log.e("SeasonSound", "Ошибка при паузе музыки", error)
            0
        }
    }

    fun release() {
        player?.let {
            runCatching { it.release() }
        }
        player = null
    }
}
