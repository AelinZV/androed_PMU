package com.example.myapplication

import androidx.lifecycle.ViewModel

// Модель жука хранит относительные координаты (0..1), без ImageView и Context.
data class BugSnapshot(
    val id: Int,
    val type: BugType,
    val x: Float,
    val y: Float,
    val dx: Float,
    val dy: Float,
    val seasonState: SeasonBugState = SeasonBugState()
)

data class SpriteSnapshot(val x: Float, val y: Float, val expiresAt: Long)

class GameViewModel : ViewModel() {
    var initialized = false
    var score = 0
    var hits = 0
    var misses = 0
    var nextBugId = 1
    var startedAt = 0L
    var endsAt = 0L
    var tiltEnabled = false
    var nextBonusAt = 0L
    var nextGoldenAt = 0L
    // Продолжение фоновой музыки после поворота игрового экрана.
    var seasonAudioPositionMs = 0
    val bugs = mutableListOf<BugSnapshot>()
    var bonus: SpriteSnapshot? = null
    var goldenBug: SpriteSnapshot? = null
}
