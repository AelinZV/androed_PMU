package com.example.myapplication

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface GameResultDao {

    @Insert
    fun insert(result: GameResultEntity): Long

    @Query(
        """
        SELECT
            game_results.playerId AS playerId,
            players.fullName AS playerName,
            game_results.score AS score,
            game_results.difficulty AS difficulty,
            game_results.hits AS hits,
            game_results.misses AS misses,
            game_results.accuracy AS accuracy,
            game_results.playedAt AS playedAt,
            game_results.durationSeconds AS durationSeconds
        FROM game_results
        INNER JOIN players ON players.id = game_results.playerId
        ORDER BY game_results.score DESC, game_results.playedAt DESC
        """
    )
    fun getAllRecords(): List<RecordItem>

    @Query(
        """
        SELECT
            COUNT(*) AS gamesCount,
            COALESCE(SUM(durationSeconds), 0) AS totalTimeSeconds,
            COALESCE(AVG(durationSeconds), 0.0) AS averageTimeSeconds
        FROM game_results
        WHERE playerId = :playerId
        """
    )
    fun getPlayerStats(playerId: Long): PlayerStats
}
