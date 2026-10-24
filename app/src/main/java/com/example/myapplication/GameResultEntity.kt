package com.example.myapplication

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** Один завершённый раунд. levelId == null означает классическую игру. */
@Entity(
    tableName = "game_results",
    foreignKeys = [
        ForeignKey(
            entity = PlayerEntity::class,
            parentColumns = ["id"],
            childColumns = ["playerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["playerId"]), Index(value = ["levelId"])]
)
data class GameResultEntity(
    val playerId: Long,
    val score: Int,
    val hits: Int,
    val misses: Int,
    val accuracy: Double,
    val difficulty: String,
    val playedAt: Long,
    val durationSeconds: Int = 0,
    /** id > 0 — сохранённый уровень; 0 — уровень из предпросмотра без сохранения. */
    val levelId: Long? = null,
    /** Снимок названия, чтобы рекорды остались после удаления уровня. */
    val levelName: String? = null,
    @PrimaryKey(autoGenerate = true) val id: Long = 0
)
