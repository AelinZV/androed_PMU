package com.example.myapplication

/** Строка рейтинга; поля должны совпадать с алиасами SQL-запроса Room. */
data class RecordItem(
    val playerId: Long,
    val playerName: String,
    val score: Int,
    val difficulty: String,
    val hits: Int,
    val misses: Int,
    val accuracy: Double,
    val playedAt: Long,
    val durationSeconds: Int,
    val levelId: Long?,
    val levelName: String?
)

/** Исторический уровень из таблицы результатов, даже если редактор его удалил. */
data class LevelHistoryItem(
    val id: Long,
    val name: String,
    val lastPlayedAt: Long
)
