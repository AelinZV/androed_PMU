package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import kotlin.math.roundToInt
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.abs
import kotlin.random.Random

class GameActivity : AppCompatActivity(), SensorEventListener {

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

    // Handler игрового цикла и бонуса
    private val handler = Handler(Looper.getMainLooper())

    // Продолжается ли игра
    private var gameRunning = true

    // Примерно 60 обновлений в секунду
    private val updateDelay = 16L

    // Время начала текущего раунда
    private var gameStartedAt = 0L

    // =================================================
    // ЛР №6. Бонус и наклон телефона
    // =================================================

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null

    // После нажатия на бонус движение начинает учитывать наклон
    private var tiltModeEnabled = false

    // Текущее значение наклона по двум осям
    private var tiltX = 0f
    private var tiltY = 0f

    // Насколько сильно наклон влияет на движение
    private val tiltStrength = 0.75f

    // Текущий бонус на поле
    private var currentBonus: ImageView? = null

    // По заданию бонус появляется каждые 15 секунд
    private val bonusIntervalMs = 15_000L

    // Если бонус не нажать, через 8 секунд он исчезает
    private val bonusLifetimeMs = 8_000L

    // Звук при активации бонуса
    private var screamPlayer: MediaPlayer? = null

    private val bonusRunnable = object : Runnable {
        override fun run() {
            if (!gameRunning) {
                return
            }

            createBonus()
            handler.postDelayed(this, bonusIntervalMs)
        }
    }

    // ЛР №7. Золотой жук Chrysina resplendens появляется каждые 20 секунд.
    private val goldenIntervalMs = 20_000L
    private val goldenLifetimeMs = 9_000L
    private var currentGoldenBug: ImageView? = null
    private var goldRublesPerGram: Double? = null

    private val goldenRunnable = object : Runnable {
        override fun run() {
            if (!gameRunning) return
            createGoldenBug()
            handler.postDelayed(this, goldenIntervalMs)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_game)

        gameField = findViewById(R.id.gameField)
        textScore = findViewById(R.id.textScore)
        textTime = findViewById(R.id.textTime)

        goldRublesPerGram = GoldRateStorage.getRate(this)
        GoldRepository.refresh(this) { latest ->
            if (!isFinishing && !isDestroyed && latest != null) {
                goldRublesPerGram = latest.rublesPerGram
            }
        }

        // Получаем системный сервис датчиков
        sensorManager =
            getSystemService(Context.SENSOR_SERVICE) as SensorManager

        accelerometer =
            sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

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
            startBonusTimer()
            startGoldenBugTimer()
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
        // 55% - обычный тип
        // 35% - быстрый тип
        // 10% - редкий тип
        // -----------------------------------------

        val chance = Random.nextInt(100)

        val type = when {
            chance < 10 -> BugType.RARE
            chance < 45 -> BugType.FAST
            else -> BugType.NORMAL
        }

        // -----------------------------------------
        // Характеристики в зависимости от типа
        // -----------------------------------------

        val imageResource: Int
        val size: Int
        val speedMultiplier: Float
        val points: Int

        when (type) {
            // Обычный тип
            BugType.NORMAL -> {
                imageResource = R.drawable.phcel
                size = 100
                speedMultiplier = 1.0f
                points = 10
            }

            // Быстрый тип
            BugType.FAST -> {
                imageResource = R.drawable.skarabeychik
                size = 80
                speedMultiplier = 1.8f
                points = 20
            }

            // Редкий тип
            BugType.RARE -> {
                imageResource = R.drawable.pawuchok
                size = 120
                speedMultiplier = 1.2f
                points = 50
            }
        }

        // -----------------------------------------
        // Создаём ImageView
        // -----------------------------------------

        val bugImage = ImageView(this)
        bugImage.setImageResource(imageResource)

        val params = FrameLayout.LayoutParams(size, size)
        bugImage.layoutParams = params

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

        val x = Random.nextInt(maxX).toFloat()
        val y = Random.nextInt(maxY).toFloat()

        bugImage.x = x
        bugImage.y = y

        // -----------------------------------------
        // Скорость
        // -----------------------------------------

        val speed =
            GameSettings.speed * speedMultiplier

        var dx =
            Random.nextFloat() * speed + 1

        var dy =
            Random.nextFloat() * speed + 1

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
        gameField.addView(bugImage)

        // -----------------------------------------
        // Попадание по насекомому
        // -----------------------------------------

        bugImage.setOnClickListener {
            if (!gameRunning) {
                return@setOnClickListener
            }

            hits++
            score += bug.points
            updateScore()

            removeBug(bug)
            createBug()
        }
    }

    // -----------------------------------------
    // Удаление насекомого
    // -----------------------------------------

    private fun removeBug(bug: Bug) {
        gameField.removeView(bug.imageView)
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
                    handler.postDelayed(this, updateDelay)
                }
            }
        )
    }

    // -----------------------------------------
    // Движение всех насекомых
    // -----------------------------------------

    private fun moveBugs() {
        val forceX =
            if (tiltModeEnabled) {
                tiltX * tiltStrength
            } else {
                0f
            }

        val forceY =
            if (tiltModeEnabled) {
                tiltY * tiltStrength
            } else {
                0f
            }

        for (bug in bugs) {
            // Обычное движение + влияние наклона после активации бонуса
            bug.x += bug.dx + forceX
            bug.y += bug.dy + forceY

            // Левая граница
            if (bug.x <= 0) {
                bug.x = 0f
                bug.dx = abs(bug.dx)
            }

            // Правая граница
            if (
                bug.x + bug.imageView.width
                >= gameField.width
            ) {
                bug.x =
                    (
                            gameField.width -
                                    bug.imageView.width
                            ).toFloat()

                bug.dx = -abs(bug.dx)
            }

            // Верхняя граница
            if (bug.y <= 0) {
                bug.y = 0f
                bug.dy = abs(bug.dy)
            }

            // Нижняя граница
            if (
                bug.y + bug.imageView.height
                >= gameField.height
            ) {
                bug.y =
                    (
                            gameField.height -
                                    bug.imageView.height
                            ).toFloat()

                bug.dy = -abs(bug.dy)
            }

            bug.imageView.x = bug.x
            bug.imageView.y = bug.y
        }
    }

    // =================================================
    // ЛР №7. Золотой жук Chrysina resplendens и курс золота ЦБ
    // =================================================

    private fun startGoldenBugTimer() {
        handler.postDelayed(goldenRunnable, goldenIntervalMs)
    }

    private fun createGoldenBug() {
        if (!gameRunning || currentGoldenBug != null) return

        val rate = goldRublesPerGram ?: GoldRateStorage.getRate(this)
        if (rate == null || rate <= 0.0) return // курс пока неизвестен

        val image = ImageView(this)
        image.setImageResource(R.drawable.chrysina_resplendens)
        image.contentDescription = "Золотой жук Chrysina resplendens"

        val size = (105 * resources.displayMetrics.density).roundToInt()
        image.layoutParams = FrameLayout.LayoutParams(size, size)

        val maxX = (gameField.width - size).coerceAtLeast(0)
        val maxY = (gameField.height - size).coerceAtLeast(0)
        image.x = Random.nextInt(maxX + 1).toFloat()
        image.y = Random.nextInt(maxY + 1).toFloat()

        currentGoldenBug = image
        gameField.addView(image)

        image.setOnClickListener {
            if (!gameRunning || currentGoldenBug !== image) return@setOnClickListener
            val points = calculateGoldenBugPoints(rate)
            score += points
            hits++
            updateScore()
            Toast.makeText(this, "Золотой жук: +$points очков", Toast.LENGTH_SHORT).show()
            removeGoldenBug()
        }

        handler.postDelayed({
            if (currentGoldenBug === image) removeGoldenBug()
        }, goldenLifetimeMs)
    }

    private fun calculateGoldenBugPoints(rate: Double): Int =
        (rate / 100.0).roundToInt().coerceAtLeast(1)

    private fun removeGoldenBug() {
        val image = currentGoldenBug ?: return
        gameField.removeView(image)
        currentGoldenBug = null
    }

    // =================================================
    // ЛР №6. Появление бонуса каждые 15 секунд
    // =================================================

    private fun startBonusTimer() {
        handler.postDelayed(
            bonusRunnable,
            bonusIntervalMs
        )
    }

    // -----------------------------------------
    // Создание бонуса
    // -----------------------------------------

    private fun createBonus() {
        if (!gameRunning) {
            return
        }

        // На поле одновременно может находиться только один бонус
        if (currentBonus != null) {
            return
        }

        val bonusImage = ImageView(this)

        // Бонус ЛР №6
        bonusImage.setImageResource(
            R.drawable.vredina
        )

        bonusImage.contentDescription =
            "Бонус управления наклоном"

        val size = 135

        bonusImage.layoutParams =
            FrameLayout.LayoutParams(
                size,
                size
            )

        val maxX =
            (gameField.width - size)
                .coerceAtLeast(1)

        val maxY =
            (gameField.height - size)
                .coerceAtLeast(1)

        bonusImage.x =
            Random.nextInt(maxX).toFloat()

        bonusImage.y =
            Random.nextInt(maxY).toFloat()

        currentBonus = bonusImage
        gameField.addView(bonusImage)

        bonusImage.setOnClickListener {
            if (!gameRunning) {
                return@setOnClickListener
            }

            activateTiltBonus()
            removeBonus()
        }

        // Если игрок не успел нажать — бонус исчезает
        handler.postDelayed(
            {
                if (currentBonus === bonusImage) {
                    removeBonus()
                }
            },
            bonusLifetimeMs
        )
    }

    // -----------------------------------------
    // Удаление бонуса с игрового поля
    // -----------------------------------------

    private fun removeBonus() {
        val bonus = currentBonus ?: return

        gameField.removeView(bonus)
        currentBonus = null
    }

    // -----------------------------------------
    // Активация бонуса
    // -----------------------------------------

    private fun activateTiltBonus() {
        if (accelerometer == null) {
            Toast.makeText(
                this,
                "На устройстве нет акселерометра",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        tiltModeEnabled = true
        tiltX = 0f
        tiltY = 0f

        registerAccelerometer()
        playBugScream()

        Toast.makeText(
            this,
            "Бонус активирован! Наклоняйте телефон",
            Toast.LENGTH_SHORT
        ).show()
    }

    // =================================================
    // ЛР №6. Работа с акселерометром
    // =================================================

    private fun registerAccelerometer() {
        val sensor = accelerometer ?: return

        sensorManager.unregisterListener(this)

        sensorManager.registerListener(
            this,
            sensor,
            SensorManager.SENSOR_DELAY_GAME
        )
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (!gameRunning || !tiltModeEnabled) {
            return
        }

        if (event?.sensor?.type != Sensor.TYPE_ACCELEROMETER) {
            return
        }

        // Для портретного режима:
        // X отвечает за движение влево/вправо,
        // Y — вверх/вниз.
        // Знак X инвертирован, чтобы движение ощущалось естественно.
        val newTiltX = -event.values[0]
        val newTiltY = event.values[1]

        // Небольшое сглаживание показаний, чтобы насекомые не дёргались
        tiltX =
            tiltX * 0.75f +
                    newTiltX * 0.25f

        tiltY =
            tiltY * 0.75f +
                    newTiltY * 0.25f
    }

    override fun onAccuracyChanged(
        sensor: Sensor?,
        accuracy: Int
    ) {
        // Для этой лабораторной дополнительная обработка не нужна
    }

    override fun onResume() {
        super.onResume()

        // Если бонус уже был активирован, после возврата в Activity
        // снова начинаем получать данные акселерометра
        if (tiltModeEnabled) {
            registerAccelerometer()
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    // =================================================
    // ЛР №6. Звуковой эффект
    // =================================================

    private fun playBugScream() {
        screamPlayer?.release()
        screamPlayer = null

        val player =
            MediaPlayer.create(
                this,
                R.raw.bug_scream
            )

        screamPlayer = player

        player?.setOnCompletionListener {
            it.release()

            if (screamPlayer === it) {
                screamPlayer = null
            }
        }

        player?.start()
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
                textTime.text = "Время: 0"

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
        // Останавливаем движение и появление бонусов
        handler.removeCallbacksAndMessages(null)

        removeBonus()
        removeGoldenBug()
        sensorManager.unregisterListener(this)

        val intent =
            Intent(
                this,
                GameResultActivity::class.java
            )

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
        finish()
    }

    override fun onDestroy() {
        gameRunning = false

        handler.removeCallbacksAndMessages(null)
        sensorManager.unregisterListener(this)

        screamPlayer?.release()
        screamPlayer = null

        super.onDestroy()
    }
}
