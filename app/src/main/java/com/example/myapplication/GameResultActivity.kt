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

        setContentView(
            R.layout.activity_game_result
        )


        val textResultScore =
            findViewById<TextView>(
                R.id.textResultScore
            )

        val textHits =
            findViewById<TextView>(
                R.id.textHits
            )

        val textMisses =
            findViewById<TextView>(
                R.id.textMisses
            )

        val textAccuracy =
            findViewById<TextView>(
                R.id.textAccuracy
            )

        val buttonRestart =
            findViewById<Button>(
                R.id.buttonRestart
            )

        val buttonBack =
            findViewById<Button>(
                R.id.buttonBack
            )


        // -----------------------------------------
        // Получаем результаты из GameActivity
        // -----------------------------------------

        val score =
            intent.getIntExtra(
                "SCORE",
                0
            )

        val hits =
            intent.getIntExtra(
                "HITS",
                0
            )

        val misses =
            intent.getIntExtra(
                "MISSES",
                0
            )


        // -----------------------------------------
        // Рассчитываем точность
        // -----------------------------------------

        val totalClicks =
            hits + misses


        val accuracy =
            if (totalClicks > 0) {

                hits * 100.0 /
                        totalClicks

            } else {

                0.0
            }


        // -----------------------------------------
        // Вывод результатов
        // -----------------------------------------

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


        // -----------------------------------------
        // Повторная игра
        // -----------------------------------------

        buttonRestart.setOnClickListener {

            val intent =
                Intent(
                    this,
                    GameActivity::class.java
                )

            startActivity(intent)

            finish()
        }


        // -----------------------------------------
        // Возвращение в главное меню
        // -----------------------------------------

        buttonBack.setOnClickListener {

            finish()
        }
    }
}