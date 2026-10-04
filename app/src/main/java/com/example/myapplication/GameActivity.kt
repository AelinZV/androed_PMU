package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.abs
import kotlin.random.Random

class GameActivity : AppCompatActivity() {

    private lateinit var gameField: FrameLayout
    private lateinit var textScore: TextView
    private lateinit var textTime: TextView

    // Все насекомые, которые сейчас находятся на поле
    private val bugs = mutableListOf<Bug>()

    // Текущие очки
    private var score = 0

    // Количество попаданий
    private var hits = 0

    // Количество промахов
    private var misses = 0

    // Следующий уникальный ID насекомого
    private var nextBugId = 1

    // Используется для движения насекомых
    private val handler =
        Handler(Looper.getMainLooper())

    // Продолжается ли игра
    private var gameRunning = true

    // Примерно 60 обновлений в секунду
    private val updateDelay = 16L

    // Время начала текущего раунда
    private var gameStartedAt = 0L


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_game)

        gameField =
            findViewById(R.id.gameField)

        textScore =
            findViewById(R.id.textScore)

        textTime =
            findViewById(R.id.textTime)


        // -----------------------------------------
        // Промах
        // -----------------------------------------

        gameField.setOnClickListener {

            if (gameRunning) {

                misses++

                score -= 5

                updateScore()
            }
        }


        // -----------------------------------------
        // Ждём определения размеров игрового поля
        // -----------------------------------------

        gameField.post {

            createInitialBugs()

            startMovement()

            startTimer()
        }
    }


    // -----------------------------------------
    // Создание начальных насекомых
    // -----------------------------------------

    private fun createInitialBugs() {

        repeat(GameSettings.maxCockroaches) {

            createBug()
        }
    }


    // -----------------------------------------
    // Создание одного насекомого
    // -----------------------------------------

    private fun createBug() {

        if (!gameRunning) {
            return
        }

        if (bugs.size >= GameSettings.maxCockroaches) {
            return
        }


        // -----------------------------------------
        // Определяем тип насекомого
        //
        // 55% - обычный муравей
        // 35% - быстрый жук
        // 10% - редкий паук
        // -----------------------------------------

        val chance =
            Random.nextInt(100)

        val type =
            when {

                chance < 10 ->
                    BugType.RARE

                chance < 45 ->
                    BugType.FAST

                else ->
                    BugType.NORMAL
            }


        // -----------------------------------------
        // Характеристики в зависимости от типа
        // -----------------------------------------

        val imageResource: Int
        val size: Int
        val speedMultiplier: Float
        val points: Int


        when (type) {

            // Обычный муравей
            BugType.NORMAL -> {

                imageResource =
                    R.drawable.murash

                size = 100

                speedMultiplier = 1.0f

                points = 10
            }


            // Быстрый жук
            BugType.FAST -> {

                imageResource =
                    R.drawable.zhuk1

                size = 80

                speedMultiplier = 1.8f

                points = 20
            }


            // Редкий паук
            BugType.RARE -> {

                imageResource =
                    R.drawable.pawuk

                size = 120

                speedMultiplier = 1.2f

                points = 50
            }
        }


        // -----------------------------------------
        // Создаём ImageView
        // -----------------------------------------

        val bugImage =
            ImageView(this)

        bugImage.setImageResource(
            imageResource
        )


        val params =
            FrameLayout.LayoutParams(
                size,
                size
            )

        bugImage.layoutParams =
            params


        // -----------------------------------------
        // Границы игрового поля
        // -----------------------------------------

        val maxX =
            (gameField.width - size)
                .coerceAtLeast(1)

        val maxY =
            (gameField.height - size)
                .coerceAtLeast(1)


        // -----------------------------------------
        // Случайная позиция
        // -----------------------------------------

        val x =
            Random.nextInt(maxX)
                .toFloat()

        val y =
            Random.nextInt(maxY)
                .toFloat()

        bugImage.x = x
        bugImage.y = y


        // -----------------------------------------
        // Скорость
        // -----------------------------------------

        val speed =
            GameSettings.speed *
                    speedMultiplier


        var dx =
            Random.nextFloat() *
                    speed + 1

        var dy =
            Random.nextFloat() *
                    speed + 1


        // Случайное направление
        if (Random.nextBoolean()) {
            dx = -dx
        }

        if (Random.nextBoolean()) {
            dy = -dy
        }


        // -----------------------------------------
        // Создаём объект Bug
        // -----------------------------------------

        val bug =
            Bug(
                id = nextBugId++,
                imageView = bugImage,
                x = x,
                y = y,
                dx = dx,
                dy = dy,
                size = size,
                type = type,
                points = points
            )


        bugs.add(bug)

        gameField.addView(
            bugImage
        )


        // -----------------------------------------
        // Попадание по насекомому
        // -----------------------------------------

        bugImage.setOnClickListener {

            if (!gameRunning) {
                return@setOnClickListener
            }


            // Увеличиваем количество попаданий
            hits++


            // Добавляем стоимость конкретного насекомого
            score += bug.points


            updateScore()


            // Удаляем пойманное насекомое
            removeBug(bug)


            // Создаём новое
            createBug()
        }
    }


    // -----------------------------------------
    // Удаление насекомого
    // -----------------------------------------

    private fun removeBug(bug: Bug) {

        gameField.removeView(
            bug.imageView
        )

        bugs.remove(bug)
    }


    // -----------------------------------------
    // Запуск движения
    // -----------------------------------------

    private fun startMovement() {

        handler.post(

            object : Runnable {

                override fun run() {

                    if (!gameRunning) {
                        return
                    }


                    moveBugs()


                    handler.postDelayed(
                        this,
                        updateDelay
                    )
                }
            }
        )
    }


    // -----------------------------------------
    // Движение всех насекомых
    // -----------------------------------------

    private fun moveBugs() {

        for (bug in bugs) {


            // Изменяем координаты
            bug.x += bug.dx
            bug.y += bug.dy


            // ---------------------------------
            // Левая граница
            // ---------------------------------

            if (bug.x <= 0) {

                bug.x = 0f

                bug.dx =
                    abs(bug.dx)
            }


            // ---------------------------------
            // Правая граница
            // ---------------------------------

            if (
                bug.x + bug.imageView.width
                >= gameField.width
            ) {

                bug.x =
                    (
                            gameField.width -
                                    bug.imageView.width
                            ).toFloat()

                bug.dx =
                    -abs(bug.dx)
            }


            // ---------------------------------
            // Верхняя граница
            // ---------------------------------

            if (bug.y <= 0) {

                bug.y = 0f

                bug.dy =
                    abs(bug.dy)
            }


            // ---------------------------------
            // Нижняя граница
            // ---------------------------------

            if (
                bug.y + bug.imageView.height
                >= gameField.height
            ) {

                bug.y =
                    (
                            gameField.height -
                                    bug.imageView.height
                            ).toFloat()

                bug.dy =
                    -abs(bug.dy)
            }


            // Передаём новые координаты изображению
            bug.imageView.x =
                bug.x

            bug.imageView.y =
                bug.y
        }
    }


    // -----------------------------------------
    // Таймер раунда
    // -----------------------------------------

    private fun startTimer() {

        gameStartedAt = System.currentTimeMillis()

        val duration =
            GameSettings.roundDuration *
                    1000L


        object : CountDownTimer(
            duration,
            1000
        ) {

            override fun onTick(
                millisUntilFinished: Long
            ) {

                val seconds =
                    millisUntilFinished / 1000


                textTime.text =
                    "Время: $seconds"
            }


            override fun onFinish() {

                gameRunning = false


                textTime.text =
                    "Время: 0"


                endGame()
            }

        }.start()
    }


    // -----------------------------------------
    // Обновление очков
    // -----------------------------------------

    private fun updateScore() {

        textScore.text =
            "Очки: $score"
    }


    // -----------------------------------------
    // Завершение игры
    // -----------------------------------------

    private fun endGame() {

        // Останавливаем движение
        handler.removeCallbacksAndMessages(
            null
        )


        // Переходим на экран результатов
        val intent =
            Intent(
                this,
                GameResultActivity::class.java
            )


        // Передаём результаты игры
        intent.putExtra(
            "SCORE",
            score
        )

        intent.putExtra(
            "HITS",
            hits
        )

        intent.putExtra(
            "MISSES",
            misses
        )

        val durationSeconds =
            ((System.currentTimeMillis() - gameStartedAt) / 1000L)
                .toInt()
                .coerceAtLeast(0)

        intent.putExtra(
            "DURATION_SECONDS",
            durationSeconds
        )


        startActivity(intent)


        // Закрываем текущую игру
        finish()
    }


    override fun onDestroy() {

        super.onDestroy()

        gameRunning = false

        handler.removeCallbacksAndMessages(
            null
        )
    }
}