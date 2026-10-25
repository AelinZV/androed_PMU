package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

/** Экран результата: сохраняет как обычные игры, так и пользовательские уровни. */
class GameResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_result)

        val textScore = findViewById<TextView>(R.id.textResultScore)
        val textHits = findViewById<TextView>(R.id.textHits)
        val textMisses = findViewById<TextView>(R.id.textMisses)
        val textAccuracy = findViewById<TextView>(R.id.textAccuracy)
        val textSaveStatus = findViewById<TextView>(R.id.textSaveStatus)
        val buttonRestart = findViewById<Button>(R.id.buttonRestart)
        val buttonBack = findViewById<Button>(R.id.buttonBack)

        val score = intent.getIntExtra("SCORE", 0)
        val hits = intent.getIntExtra("HITS", 0)
        val misses = intent.getIntExtra("MISSES", 0)
        val durationSeconds = intent.getIntExtra("DURATION_SECONDS", 0)
        val level = LevelPreviewActivity.readLevel(intent)
        val total = hits + misses
        val accuracy = if (total > 0) hits * 100.0 / total else 0.0

        textScore.text = "Очки: $score"
        textHits.text = "Попадания: $hits"
        textMisses.text = "Промахи: $misses"
        textAccuracy.text = String.format(Locale.getDefault(), "Точность: %.1f%%", accuracy)

        // Одна запись на раунд. После поворота экрана повторно не записываем.
        if (savedInstanceState == null) {
            saveResult(score, hits, misses, accuracy, durationSeconds, level, textSaveStatus)
        } else {
            textSaveStatus.text = "Результат этого раунда уже обработан"
        }

        buttonRestart.setOnClickListener {
            val replay = Intent(this, GameActivity::class.java)
            // Важное исправление: не теряем уровень при повторной игре.
            level?.let { LevelPreviewActivity.putLevel(replay, it) }
            startActivity(replay)
            finish()
        }
        buttonBack.setOnClickListener { finish() }
    }

    private fun saveResult(
        score: Int,
        hits: Int,
        misses: Int,
        accuracy: Double,
        durationSeconds: Int,
        level: LevelConfig?,
        status: TextView
    ) {
        val playerId = PlayerSession.playerId
        if (playerId == null) {
            status.text = "Результат не сохранён: сначала выберите игрока"
            return
        }

        val record = GameResultEntity(
            playerId = playerId,
            score = score,
            hits = hits,
            misses = misses,
            accuracy = accuracy,
            difficulty = PlayerSession.difficulty,
            playedAt = System.currentTimeMillis(),
            durationSeconds = durationSeconds.coerceAtLeast(0),
            levelId = level?.id,
            levelName = level?.name
        )
        status.text = "Сохранение результата..."
        Thread {
            val error = runCatching {
                AppDatabase.getInstance(applicationContext).gameResultDao().insert(record)
            }.exceptionOrNull()
            runOnUiThread {
                if (isFinishing || isDestroyed) return@runOnUiThread
                status.text = if (error == null) {
                    if (level == null) "Результат обычной игры сохранён"
                    else "Результат уровня «${level.name}» сохранён"
                } else {
                    "Ошибка сохранения: ${error.localizedMessage ?: "неизвестная ошибка"}"
                }
            }
        }.start()
    }
}
