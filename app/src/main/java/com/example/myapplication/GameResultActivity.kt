package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

class GameResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_game_result)

        val textResultScore =
            findViewById<TextView>(R.id.textResultScore)

        val textHits =
            findViewById<TextView>(R.id.textHits)

        val textMisses =
            findViewById<TextView>(R.id.textMisses)

        val textAccuracy =
            findViewById<TextView>(R.id.textAccuracy)

        val textSaveStatus =
            findViewById<TextView>(R.id.textSaveStatus)

        val buttonRestart =
            findViewById<Button>(R.id.buttonRestart)

        val buttonBack =
            findViewById<Button>(R.id.buttonBack)


        // Получаем результаты из GameActivity.
        val score =
            intent.getIntExtra("SCORE", 0)

        val hits =
            intent.getIntExtra("HITS", 0)

        val misses =
            intent.getIntExtra("MISSES", 0)

        val durationSeconds =
            intent.getIntExtra("DURATION_SECONDS", 0)


        // Рассчитываем точность.
        val totalClicks =
            hits + misses

        val accuracy =
            if (totalClicks > 0) {
                hits * 100.0 / totalClicks
            } else {
                0.0
            }


        // Выводим статистику.
        textResultScore.text =
            "Очки: $score"

        textHits.text =
            "Попадания: $hits"

        textMisses.text =
            "Промахи: $misses"

        textAccuracy.text =
            String.format(
                Locale.getDefault(),
                "Точность: %.1f%%",
                accuracy
            )


        // ЛР №5: сохраняем результат в Room только для выбранного игрока.
        // savedInstanceState == null защищает от повторной записи при пересоздании Activity.
        if (savedInstanceState == null) {
            saveResult(
                score = score,
                hits = hits,
                misses = misses,
                accuracy = accuracy,
                durationSeconds = durationSeconds,
                textSaveStatus = textSaveStatus
            )
        }


        // Повторная игра — выбранный игрок остаётся активным.
        buttonRestart.setOnClickListener {

            val restartIntent =
                Intent(
                    this,
                    GameActivity::class.java
                )

            startActivity(restartIntent)
            finish()
        }


        // Возвращаемся на главный экран приложения.
        buttonBack.setOnClickListener {
            finish()
        }
    }


    // =================================================
    // ЛР №5. Сохранение результата игры в Room
    // =================================================

    private fun saveResult(
        score: Int,
        hits: Int,
        misses: Int,
        accuracy: Double,
        durationSeconds: Int,
        textSaveStatus: TextView
    ) {

        val playerId =
            PlayerSession.playerId

        // Если игра была запущена без регистрации,
        // результат в базу не записываем.
        if (playerId == null) {
            textSaveStatus.text =
                "Результат не сохранён: игрок не выбран"
            return
        }

        val result =
            GameResultEntity(
                playerId,
                score,
                hits,
                misses,
                accuracy,
                PlayerSession.difficulty,
                System.currentTimeMillis(),
                durationSeconds
            )

        val database =
            AppDatabase.getInstance(applicationContext)

        textSaveStatus.text =
            "Сохранение результата..."

        Thread {

            database.gameResultDao().insert(result)

            runOnUiThread {
                textSaveStatus.text =
                    "Результат сохранён: ${PlayerSession.fullName}"
            }
        }.start()
    }
}
