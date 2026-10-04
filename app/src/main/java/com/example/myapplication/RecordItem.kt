package com.example.myapplication

data class RecordItem(
    val playerId: Long,
    val playerName: String,
    val score: Int,
    val difficulty: String,
    val hits: Int,
    val misses: Int,
    val accuracy: Double,
    val playedAt: Long,
    val durationSeconds: Int
)
