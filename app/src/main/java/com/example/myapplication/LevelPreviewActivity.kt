package com.example.myapplication

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

/** Предпросмотр без игрового таймера. Играть запускает обычную GameActivity. */
class LevelPreviewActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_ID = "level_id"
        const val EXTRA_NAME = "level_name"
        const val EXTRA_SEASON = "level_season"
        const val EXTRA_MAX = "level_max_bugs"
        const val EXTRA_SPEED = "level_speed"
        const val EXTRA_DURATION = "level_duration"
        const val EXTRA_COMMON = "level_common"
        const val EXTRA_RARE = "level_rare"
        const val EXTRA_VERY_RARE = "level_very_rare"
        const val EXTRA_ULTRA_RARE = "level_ultra_rare"

        fun putLevel(intent: Intent, config: LevelConfig) = intent.apply {
            putExtra(EXTRA_ID, config.id)
            putExtra(EXTRA_NAME, config.name)
            putExtra(EXTRA_SEASON, config.season.name)
            putExtra(EXTRA_MAX, config.maxBugs)
            putExtra(EXTRA_SPEED, config.speed)
            putExtra(EXTRA_DURATION, config.durationSeconds)
            putExtra(EXTRA_COMMON, config.commonBugId)
            putExtra(EXTRA_RARE, config.rareBugId)
            putExtra(EXTRA_VERY_RARE, config.veryRareBugId)
            putExtra(EXTRA_ULTRA_RARE, config.ultraRareBugId)
        }
        fun readLevel(intent: Intent): LevelConfig? {
            val name = intent.getStringExtra(EXTRA_NAME) ?: return null
            return LevelConfig(
                id = intent.getLongExtra(EXTRA_ID, 0L),
                name = name,
                season = runCatching { Season.valueOf(intent.getStringExtra(EXTRA_SEASON) ?: "SPRING") }.getOrDefault(Season.SPRING),
                maxBugs = intent.getIntExtra(EXTRA_MAX, 10),
                speed = intent.getIntExtra(EXTRA_SPEED, 5),
                durationSeconds = intent.getIntExtra(EXTRA_DURATION, 120),
                commonBugId = intent.getStringExtra(EXTRA_COMMON) ?: "bee",
                rareBugId = intent.getStringExtra(EXTRA_RARE) ?: "scarab",
                veryRareBugId = intent.getStringExtra(EXTRA_VERY_RARE) ?: "superrare1",
                ultraRareBugId = intent.getStringExtra(EXTRA_ULTRA_RARE) ?: "ultrarare1"
            )
        }
        fun start(context: Context, config: LevelConfig) {
            context.startActivity(putLevel(Intent(context, LevelPreviewActivity::class.java), config))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val level = readLevel(intent) ?: run { finish(); return }
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 24)
            setBackgroundColor(when (level.season) {
                Season.WINTER -> Color.rgb(222, 239, 250)
                Season.SPRING -> Color.rgb(225, 246, 218)
                Season.SUMMER -> Color.rgb(248, 242, 189)
                Season.AUTUMN -> Color.rgb(245, 225, 202)
            })
        }
        val scroll = ScrollView(this)
        scroll.addView(root)
        setContentView(scroll)
        ScreenInsets.apply(scroll, additionalTopDp = 10, additionalBottomDp = 12)
        val dp = resources.displayMetrics.density
        root.addView(TextView(this).apply {
            text = "ПРЕДПРОСМОТР УРОВНЯ"
            textSize = 15f
            gravity = Gravity.CENTER_VERTICAL
            setTextColor(Color.WHITE)
            setPadding((14 * dp).toInt(), (12 * dp).toInt(), (14 * dp).toInt(), (12 * dp).toInt())
            background = GradientDrawable().apply {
                setColor(Color.rgb(102, 76, 166))
                cornerRadius = 14 * dp
            }
        })
        root.addView(TextView(this).apply {
            text = level.name
            textSize = 24f
            setPadding(0, (12 * dp).toInt(), 0, (8 * dp).toInt())
        })
        root.addView(ImageView(this).apply {
            setImageResource(level.season.backgroundRes)
            scaleType = ImageView.ScaleType.CENTER_CROP
            contentDescription = "Фон уровня: ${level.season}"
        }, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            (180 * resources.displayMetrics.density).toInt()
        ))
        root.addView(TextView(this).apply {
            text = "Сезон: ${level.season}\nЖуков на поле: ${level.maxBugs}\n" +
                "Базовая скорость: ${level.speed}\nРаунд: ${level.durationSeconds} с\n" +
                "Шансы появления: 55% / 35% / 9% / 1%"
            textSize = 18f
        })
        root.addView(TextView(this).apply {
            text = when (level.season) {
                Season.WINTER -> "❄ Зима: жуки медленные и периодически замерзают. Первое нажатие разбивает лёд."
                Season.SPRING -> "🌱 Весна: жуки периодически ускоряются на несколько секунд."
                Season.SUMMER -> "☀ Лето: жуки постоянно очень быстро движутся."
                Season.AUTUMN -> "🍂 Осень: жуки могут заснуть и временно остановиться."
            }
            textSize = 17f
        })
        listOf(
            LevelRarity.COMMON to level.commonBugId,
            LevelRarity.RARE to level.rareBugId,
            LevelRarity.VERY_RARE to level.veryRareBugId,
            LevelRarity.ULTRA_RARE to level.ultraRareBugId
        ).forEach { (rarity, id) ->
            val item = BugCatalog.find(id, rarity)
            val rarityLabel = when (rarity) {
                LevelRarity.COMMON -> "Обычный"
                LevelRarity.RARE -> "Редкий"
                LevelRarity.VERY_RARE -> "Суперредкий"
                LevelRarity.ULTRA_RARE -> "Ультраредкий"
            }
            root.addView(TextView(this).apply {
                text = "$rarityLabel: ${item.label} (${rarity.chance}%)"
                textSize = 17f
            })
            root.addView(ImageView(this).apply {
                setImageResource(item.image)
                scaleType = ImageView.ScaleType.FIT_CENTER
            }, LinearLayout.LayoutParams(120, 120))
        }
        root.addView(Button(this).apply {
            text = "Запустить уровень"
            setOnClickListener {
                startActivity(putLevel(Intent(this@LevelPreviewActivity, GameActivity::class.java), level))
            }
        })
        root.addView(Button(this).apply { text = "Вернуться к редактированию"; setOnClickListener { finish() } })
    }
}
