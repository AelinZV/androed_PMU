package com.example.myapplication

import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.abs
import kotlin.random.Random

class GameActivity : AppCompatActivity() {

    private lateinit var gameField: FrameLayout
    private lateinit var textScore: TextView
    private lateinit var textTime: TextView

    // Список всех насекомых, которые сейчас находятся на поле
    private val bugs = mutableListOf<Bug>()

    // Изображения насекомых
    private val bugImages = listOf(
        R.drawable.zhuk1,
        R.drawable.pawuk,
        R.drawable.murash
    )

    // Текущее количество очков
    private var score = 0

    // Handler нужен для постоянного движения насекомых
    private val handler =
        Handler(Looper.getMainLooper())

    // Показывает, продолжается ли игра
    private var gameRunning = true

    // Задержка между обновлениями положения насекомых
    private val updateDelay = 16L


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_game)

        // -----------------------------------------
        // Получаем элементы интерфейса
        // -----------------------------------------

        gameField =
            findViewById(R.id.gameField)

        textScore =
            findViewById(R.id.textScore)

        textTime =
            findViewById(R.id.textTime)


        // -----------------------------------------
        // Нажатие по пустому месту = промах
        // -----------------------------------------

        gameField.setOnClickListener {

            if (gameRunning) {

                score -= 5

                updateScore()
            }
        }


        // -----------------------------------------
        // Ждём, пока Android определит размеры поля
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

        // Если игра закончилась, новых насекомых не создаём
        if (!gameRunning) {
            return
        }

        // Проверяем максимальное количество насекомых
        if (bugs.size >= GameSettings.maxCockroaches) {
            return
        }


        // Создаём изображение насекомого
        val bugImage = ImageView(this)


        // -----------------------------------------
        // Выбираем случайное изображение
        // -----------------------------------------

        val randomBugImage =
            bugImages.random()

        bugImage.setImageResource(
            randomBugImage
        )


        // -----------------------------------------
        // Размер насекомого
        // -----------------------------------------

        val size = 100

        val params =
            FrameLayout.LayoutParams(
                size,
                size
            )

        bugImage.layoutParams = params


        // -----------------------------------------
        // Определяем границы игрового поля
        // -----------------------------------------

        val maxX =
            (gameField.width - size)
                .coerceAtLeast(1)

        val maxY =
            (gameField.height - size)
                .coerceAtLeast(1)


        // -----------------------------------------
        // Случайная начальная позиция
        // -----------------------------------------

        val x =
            Random.nextInt(maxX).toFloat()

        val y =
            Random.nextInt(maxY).toFloat()

        bugImage.x = x
        bugImage.y = y


        // -----------------------------------------
        // Скорость движения
        // -----------------------------------------

        val speed =
            GameSettings.speed.toFloat()


        // Случайная скорость по X и Y
        var dx =
            Random.nextFloat() * speed + 1

        var dy =
            Random.nextFloat() * speed + 1


        // Случайное направление по горизонтали
        if (Random.nextBoolean()) {
            dx = -dx
        }


        // Случайное направление по вертикали
        if (Random.nextBoolean()) {
            dy = -dy
        }


        // -----------------------------------------
        // Создаём объект Bug
        // -----------------------------------------

        val bug =
            Bug(
                bugImage,
                x,
                y,
                dx,
                dy
            )


        // Добавляем насекомое в список
        bugs.add(bug)


        // Добавляем изображение на игровое поле
        gameField.addView(bugImage)


        // -----------------------------------------
        // Нажатие непосредственно по насекомому
        // -----------------------------------------

        bugImage.setOnClickListener {

            if (!gameRunning) {
                return@setOnClickListener
            }


            // За попадание даём 10 очков
            score += 10


            // Обновляем количество очков
            updateScore()


            // Удаляем насекомое
            removeBug(bug)


            // Создаём новое насекомое
            createBug()
        }
    }


    // -----------------------------------------
    // Удаление насекомого
    // -----------------------------------------

    private fun removeBug(bug: Bug) {

        // Удаляем изображение с игрового поля
        gameField.removeView(
            bug.imageView
        )


        // Удаляем объект из списка
        bugs.remove(bug)
    }


    // -----------------------------------------
    // Запуск движения
    // -----------------------------------------

    private fun startMovement() {

        handler.post(

            object : Runnable {

                override fun run() {

                    // Если игра закончилась,
                    // больше ничего не обновляем
                    if (!gameRunning) {
                        return
                    }


                    // Перемещаем всех насекомых
                    moveBugs()


                    // Через 16 миллисекунд
                    // снова запускаем этот код
                    handler.postDelayed(
                        this,
                        updateDelay
                    )
                }
            }
        )
    }


    // -----------------------------------------
    // Перемещение всех насекомых
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


            // ---------------------------------
            // Передаём новые координаты ImageView
            // ---------------------------------

            bug.imageView.x = bug.x
            bug.imageView.y = bug.y
        }
    }


    // -----------------------------------------
    // Таймер игры
    // -----------------------------------------

    private fun startTimer() {

        // Получаем длительность раунда из настроек
        val duration =
            GameSettings.roundDuration * 1000L


        object : CountDownTimer(
            duration,
            1000
        ) {

            // Вызывается каждую секунду
            override fun onTick(
                millisUntilFinished: Long
            ) {

                val seconds =
                    millisUntilFinished / 1000


                textTime.text =
                    "Время: $seconds"
            }


            // Вызывается после окончания времени
            override fun onFinish() {

                gameRunning = false


                textTime.text =
                    "Время: 0"


                endGame()
            }

        }.start()
    }


    // -----------------------------------------
    // Обновление количества очков
    // -----------------------------------------

    private fun updateScore() {

        textScore.text =
            "Очки: $score"
    }


    // -----------------------------------------
    // Конец игры
    // -----------------------------------------

    private fun endGame() {

        // Останавливаем движение насекомых
        handler.removeCallbacksAndMessages(
            null
        )


        // Показываем результат
        Toast.makeText(
            this,
            "Игра окончена! Очки: $score",
            Toast.LENGTH_LONG
        ).show()
    }


    // -----------------------------------------
    // Закрытие GameActivity
    // -----------------------------------------

    override fun onDestroy() {

        super.onDestroy()


        // Останавливаем игру
        gameRunning = false


        // Останавливаем Handler
        handler.removeCallbacksAndMessages(
            null
        )
    }
}