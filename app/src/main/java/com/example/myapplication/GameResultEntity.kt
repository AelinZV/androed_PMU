package com.example.myapplication

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

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
    indices = [
        Index(value = ["playerId"])
    ]
)
data class GameResultEntity(
    val playerId: Long,
    val score: Int,
    val hits: Int,
    val misses: Int,
    val accuracy: Double,
    val difficulty: String,
    val playedAt: Long,
    val durationSeconds: Int,

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0
)
