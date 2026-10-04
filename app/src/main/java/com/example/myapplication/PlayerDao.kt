package com.example.myapplication

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface PlayerDao {

    @Insert
    fun insert(player: PlayerEntity): Long

    @Query("SELECT * FROM players ORDER BY fullName ASC")
    fun getAllPlayers(): List<PlayerEntity>

    @Query("SELECT * FROM players WHERE id = :playerId LIMIT 1")
    fun getById(playerId: Long): PlayerEntity?
}
