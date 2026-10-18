package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.SystemClock
import org.koin.androidx.viewmodel.ext.android.viewModel
import android.os.Handler
import android.os.Looper
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import kotlin.math.roundToInt
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random

class GameActivity : AppCompatActivity(), SensorEventListener {

    private val gameViewModel: GameViewModel by viewModel()

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
    private var roundEndsAt = 0L

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
    private var bonusExpiresAt = 0L

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
            gameViewModel.nextBonusAt = SystemClock.elapsedRealtime() + bonusIntervalMs
            handler.postDelayed(this, bonusIntervalMs)
        }
    }

    // ЛР №7. Золотой жук Chrysina resplendens появляется каждые 20 секунд.
    private val goldenIntervalMs = 20_000L
    private val goldenLifetimeMs = 9_000L
    private var currentGoldenBug: ImageView? = null
    private var goldenExpiresAt = 0L
    private var goldRublesPerGram: Double? = null

    private val goldenRunnable = object : Runnable {
        override fun run() {
            if (!gameRunning) return
            createGoldenBug()
            gameViewModel.nextGoldenAt = SystemClock.elapsedRealtime() + goldenIntervalMs
            handler.postDelayed(this, goldenIntervalMs)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_game)

        gameField = findViewById(R.id.gameField)
        textScore = findViewById(R.id.textScore)
        textTime = findViewById(R.id.textTime)

        score = gameViewModel.score
        hits = gameViewModel.hits
        misses = gameViewModel.misses
        nextBugId = gameViewModel.nextBugId
        tiltModeEnabled = gameViewModel.tiltEnabled
        gameStartedAt = gameViewModel.startedAt
        roundEndsAt = gameViewModel.endsAt
        updateScore()

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
                score = GameScoreCalculator.afterMiss(score)
                updateScore()
            }
        }

        // -----------------------------------------
        // Ждём определения размеров игрового поля
        // -----------------------------------------

        gameField.post {
            if (isFinishing || isDestroyed) return@post
            if (gameViewModel.initialized) {
                gameViewModel.bugs.forEach { createBug(it) }
                restoreSpecialBugs()
            } else {
                createInitialBugs()
                gameViewModel.initialized = true
            }
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

    private fun createBug(saved: BugSnapshot? = null) {
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

        val type = saved?.type ?: when {
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
        val points = GameScoreCalculator.pointsForBug(type)

        when (type) {
            // Обычный тип
            BugType.NORMAL -> {
                imageResource = R.drawable.phcel
                size = 100
                speedMultiplier = 1.0f
            }

            // Быстрый тип
            BugType.FAST -> {
                imageResource = R.drawable.skarabeychik
                size = 80
                speedMultiplier = 1.8f
            }

            // Редкий тип
            BugType.RARE -> {
                imageResource = R.drawable.pawuchok
                size = 120
                speedMultiplier = 1.2f
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

        // Координаты модели логические: от 0 до 1.
        val x = saved?.x ?: Random.nextFloat()
        val y = saved?.y ?: Random.nextFloat()

        bugImage.x = x * maxX
        bugImage.y = y * maxY

        // -----------------------------------------
        // Скорость
        // -----------------------------------------

        val speed =
            GameSettings.speed * speedMultiplier

        var dx = saved?.dx ?: (Random.nextFloat() * speed + 1)

        var dy = saved?.dy ?: (Random.nextFloat() * speed + 1)

        // Случайное направление
        if (saved == null && Random.nextBoolean()) {
            dx = -dx
        }

        if (saved == null && Random.nextBoolean()) {
            dy = -dy
        }

        // -----------------------------------------
        // Создаём объект Bug
        // -----------------------------------------

        val bug =
            Bug(
                id = saved?.id ?: nextBugId++,
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
            score = GameScoreCalculator.afterHit(score, bug.points)
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
<<<<<<< HEAD
        // Скорость хранится в пикселях за кадр, но координаты жука — в [0, 1].
        // На другом экране абсолютные позиции пересчитываются из модели.
        val forceX = if (tiltModeEnabled) tiltX * tiltStrength else 0f
        val forceY = if (tiltModeEnabled) tiltY * tiltStrength else 0f
        for (bug in bugs) {
            val availableWidth = (gameField.width - bug.size).coerceAtLeast(1)
            val availableHeight = (gameField.height - bug.size).coerceAtLeast(1)
            bug.x += (bug.dx + forceX) / availableWidth
            bug.y += (bug.dy + forceY) / availableHeight

            if (bug.x <= 0f) {
                bug.x = 0f
                bug.dx = abs(bug.dx)
            } else if (bug.x >= 1f) {
                bug.x = 1f
                bug.dx = -abs(bug.dx)
            }
            if (bug.y <= 0f) {
                bug.y = 0f
                bug.dy = abs(bug.dy)
            } else if (bug.y >= 1f) {
                bug.y = 1f
                bug.dy = -abs(bug.dy)
            }
=======
        val forceX = if (tiltModeEnabled) tiltX * tiltStrength else 0f
        val forceY = if (tiltModeEnabled) tiltY * tiltStrength else 0f

        for (bug in bugs) {
            val availableWidth = (gameField.width - bug.size).coerceAtLeast(1)
            val availableHeight = (gameField.height - bug.size).coerceAtLeast(1)

            // Вся математика перемещения находится в BugMovement.
            val next = BugMovement.move(
                x = bug.x,
                y = bug.y,
                dx = bug.dx,
                dy = bug.dy,
                availableWidth = availableWidth,
                availableHeight = availableHeight,
                forceX = forceX,
                forceY = forceY
            )
            bug.x = next.x
            bug.y = next.y
            bug.dx = next.dx
            bug.dy = next.dy

            // Activity отвечает только за отображение рассчитанной позиции.
>>>>>>> 573315b (Подсчёт очков)
            bug.imageView.x = bug.x * availableWidth
            bug.imageView.y = bug.y * availableHeight
        }
    }

    // =================================================
    // ЛР №7. Золотой жук Chrysina resplendens и курс золота ЦБ
    // =================================================

    private fun startGoldenBugTimer() {
        val now = SystemClock.elapsedRealtime()
        if (gameViewModel.nextGoldenAt == 0L) gameViewModel.nextGoldenAt = now + goldenIntervalMs
        handler.postDelayed(goldenRunnable, (gameViewModel.nextGoldenAt - now).coerceAtLeast(0L))
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
        val restored = gameViewModel.goldenBug
        image.x = restored?.let { it.x * maxX } ?: Random.nextInt(maxX + 1).toFloat()
        image.y = restored?.let { it.y * maxY } ?: Random.nextInt(maxY + 1).toFloat()

        currentGoldenBug = image
        gameField.addView(image)

        image.setOnClickListener {
            if (!gameRunning || currentGoldenBug !== image) return@setOnClickListener
            val points = GameScoreCalculator.pointsForGoldenBug(rate)
            score = GameScoreCalculator.afterHit(score, points)
            hits++
            updateScore()
            Toast.makeText(this, "Золотой жук: +$points очков", Toast.LENGTH_SHORT).show()
            removeGoldenBug()
        }

        goldenExpiresAt = gameViewModel.goldenBug?.expiresAt
            ?: (SystemClock.elapsedRealtime() + goldenLifetimeMs)
        gameViewModel.goldenBug = null
        handler.postDelayed({
            if (currentGoldenBug === image) removeGoldenBug()
        }, (goldenExpiresAt - SystemClock.elapsedRealtime()).coerceAtLeast(0L))
    }

    private fun removeGoldenBug() {
        val image = currentGoldenBug ?: return
        gameField.removeView(image)
        currentGoldenBug = null
    }

    // =================================================
    // ЛР №6. Появление бонуса каждые 15 секунд
    // =================================================

    private fun startBonusTimer() {
        val now = SystemClock.elapsedRealtime()
        if (gameViewModel.nextBonusAt == 0L) gameViewModel.nextBonusAt = now + bonusIntervalMs
        handler.postDelayed(bonusRunnable, (gameViewModel.nextBonusAt - now).coerceAtLeast(0L))
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

        val restored = gameViewModel.bonus
        bonusImage.x = restored?.let { it.x * maxX } ?: Random.nextInt(maxX).toFloat()
        bonusImage.y = restored?.let { it.y * maxY } ?: Random.nextInt(maxY).toFloat()

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
        bonusExpiresAt = gameViewModel.bonus?.expiresAt
            ?: (SystemClock.elapsedRealtime() + bonusLifetimeMs)
        gameViewModel.bonus = null
        handler.postDelayed(
            {
                if (currentBonus === bonusImage) {
                    removeBonus()
                }
            },
            (bonusExpiresAt - SystemClock.elapsedRealtime()).coerceAtLeast(0L)
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
                R.raw.m
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
        val now = SystemClock.elapsedRealtime()
        if (roundEndsAt == 0L) {
            roundEndsAt = now + GameSettings.roundDuration * 1000L
            gameStartedAt = System.currentTimeMillis()
        }
        gameViewModel.startedAt = gameStartedAt
        gameViewModel.endsAt = roundEndsAt
        val ticker = object : Runnable {
            override fun run() {
                if (!gameRunning) return
                val remaining = (roundEndsAt - SystemClock.elapsedRealtime()).coerceAtLeast(0L)
                textTime.text = "Время: ${(remaining + 999L) / 1000L}"
                if (remaining == 0L) {
                    gameRunning = false
                    endGame()
                } else {
                    handler.postDelayed(this, 200L)
                }
            }
        }
        handler.post(ticker)
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
        gameViewModel.initialized = false
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

    override fun onSaveInstanceState(outState: Bundle) {
        snapshotGame()
        super.onSaveInstanceState(outState)
    }

    override fun onStop() {
        snapshotGame()
        super.onStop()
    }

    // Снимок модели. Позиции 0..1 не зависят от размера экрана.
    private fun snapshotGame() {
        if (!gameViewModel.initialized || !gameRunning) return
        gameViewModel.score = score
        gameViewModel.hits = hits
        gameViewModel.misses = misses
        gameViewModel.nextBugId = nextBugId
        gameViewModel.tiltEnabled = tiltModeEnabled
        gameViewModel.startedAt = gameStartedAt
        gameViewModel.endsAt = roundEndsAt
        gameViewModel.bugs.clear()
        bugs.forEach { bug ->
            gameViewModel.bugs.add(BugSnapshot(
                bug.id, bug.type,
                bug.x.coerceIn(0f, 1f),
                bug.y.coerceIn(0f, 1f),
                bug.dx, bug.dy
            ))
        }
        gameViewModel.bonus = currentBonus?.let { image ->
            SpriteSnapshot(
                (image.x / (gameField.width - image.width).coerceAtLeast(1)).coerceIn(0f, 1f),
                (image.y / (gameField.height - image.height).coerceAtLeast(1)).coerceIn(0f, 1f),
                bonusExpiresAt
            )
        }
        gameViewModel.goldenBug = currentGoldenBug?.let { image ->
            SpriteSnapshot(
                (image.x / (gameField.width - image.width).coerceAtLeast(1)).coerceIn(0f, 1f),
                (image.y / (gameField.height - image.height).coerceAtLeast(1)).coerceIn(0f, 1f),
                goldenExpiresAt
            )
        }
    }

    private fun restoreSpecialBugs() {
        val now = SystemClock.elapsedRealtime()
        if ((gameViewModel.bonus?.expiresAt ?: 0L) > now) createBonus()
        else gameViewModel.bonus = null
        if ((gameViewModel.goldenBug?.expiresAt ?: 0L) > now) createGoldenBug()
        else gameViewModel.goldenBug = null
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
