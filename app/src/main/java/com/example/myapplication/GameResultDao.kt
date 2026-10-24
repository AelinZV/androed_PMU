package com.example.myapplication

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface GameResultDao {
    @Insert
    fun insert(result: GameResultEntity): Long

    /** Классическая игра: лучшие очки каждого игрока независимо от сложности. */
    @Query("""
        SELECT gr.playerId, p.fullName AS playerName,
               gr.score, gr.difficulty, gr.hits, gr.misses, gr.accuracy,
               gr.playedAt, gr.durationSeconds, gr.levelId, gr.levelName
        FROM game_results gr
        INNER JOIN players p ON p.id = gr.playerId
        WHERE gr.levelId IS NULL
          AND NOT EXISTS (
              SELECT 1 FROM game_results better
              WHERE better.playerId = gr.playerId
                AND better.levelId IS NULL
                AND (better.score > gr.score
                  OR (better.score = gr.score AND better.playedAt > gr.playedAt)
                  OR (better.score = gr.score AND better.playedAt = gr.playedAt
                      AND better.id > gr.id))
          )
        ORDER BY gr.score DESC, gr.playedAt DESC, gr.id DESC
    """)
    fun getBestRecordsOverall(): List<RecordItem>

    /** Классическая игра: лучший результат игрока на заданной сложности. */
    @Query("""
        SELECT gr.playerId, p.fullName AS playerName,
               gr.score, gr.difficulty, gr.hits, gr.misses, gr.accuracy,
               gr.playedAt, gr.durationSeconds, gr.levelId, gr.levelName
        FROM game_results gr
        INNER JOIN players p ON p.id = gr.playerId
        WHERE gr.levelId IS NULL AND gr.difficulty = :difficulty
          AND NOT EXISTS (
              SELECT 1 FROM game_results better
              WHERE better.playerId = gr.playerId
                AND better.levelId IS NULL AND better.difficulty = :difficulty
                AND (better.score > gr.score
                  OR (better.score = gr.score AND better.playedAt > gr.playedAt)
                  OR (better.score = gr.score AND better.playedAt = gr.playedAt
                      AND better.id > gr.id))
          )
        ORDER BY gr.score DESC, gr.playedAt DESC, gr.id DESC
    """)
    fun getBestRecordsByDifficulty(difficulty: String): List<RecordItem>

    /**
     * Пользовательский уровень. id>0 устойчив к переименованию; id=0 —
     * временный уровень из предпросмотра, поэтому дополнительно сравниваем имя.
     */
    @Query("""
        SELECT gr.playerId, p.fullName AS playerName,
               gr.score, gr.difficulty, gr.hits, gr.misses, gr.accuracy,
               gr.playedAt, gr.durationSeconds, gr.levelId, gr.levelName
        FROM game_results gr
        INNER JOIN players p ON p.id = gr.playerId
        WHERE gr.levelId = :levelId
          AND (:levelId != 0 OR gr.levelName = :levelName)
          AND NOT EXISTS (
              SELECT 1 FROM game_results better
              WHERE better.playerId = gr.playerId
                AND better.levelId = :levelId
                AND (:levelId != 0 OR better.levelName = :levelName)
                AND (better.score > gr.score
                  OR (better.score = gr.score AND better.playedAt > gr.playedAt)
                  OR (better.score = gr.score AND better.playedAt = gr.playedAt
                      AND better.id > gr.id))
          )
        ORDER BY gr.score DESC, gr.playedAt DESC, gr.id DESC
    """)
    fun getBestRecordsByLevel(levelId: Long, levelName: String): List<RecordItem>

    /** История результатов нужна для отображения удалённых/временных уровней. */
    @Query("""
        SELECT levelId AS id, levelName AS name, MAX(playedAt) AS lastPlayedAt
        FROM game_results
        WHERE levelId IS NOT NULL AND levelName IS NOT NULL
        GROUP BY levelId, levelName
        ORDER BY lastPlayedAt DESC
    """)
    fun getPlayedLevels(): List<LevelHistoryItem>

    @Query("""
        SELECT COUNT(*) AS gamesCount,
               COALESCE(SUM(durationSeconds), 0) AS totalTimeSeconds,
               COALESCE(AVG(durationSeconds), 0.0) AS averageTimeSeconds
        FROM game_results
        WHERE playerId = :playerId AND levelId IS NULL
    """)
    fun getPlayerStats(playerId: Long): PlayerStats

    @Query("""
        SELECT COUNT(*) AS gamesCount,
               COALESCE(SUM(durationSeconds), 0) AS totalTimeSeconds,
               COALESCE(AVG(durationSeconds), 0.0) AS averageTimeSeconds
        FROM game_results
        WHERE playerId = :playerId AND levelId IS NULL AND difficulty = :difficulty
    """)
    fun getPlayerStatsByDifficulty(playerId: Long, difficulty: String): PlayerStats

    @Query("""
        SELECT COUNT(*) AS gamesCount,
               COALESCE(SUM(durationSeconds), 0) AS totalTimeSeconds,
               COALESCE(AVG(durationSeconds), 0.0) AS averageTimeSeconds
        FROM game_results
        WHERE playerId = :playerId AND levelId = :levelId
          AND (:levelId != 0 OR levelName = :levelName)
    """)
    fun getPlayerStatsByLevel(playerId: Long, levelId: Long, levelName: String): PlayerStats
}
