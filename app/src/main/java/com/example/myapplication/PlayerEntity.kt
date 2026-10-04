package com.example.myapplication

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "players")
data class PlayerEntity(
    val fullName: String,
    val gender: String,
    val course: String,
    val difficulty: String,
    val birthDate: String,
    val zodiac: String,

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0
)
