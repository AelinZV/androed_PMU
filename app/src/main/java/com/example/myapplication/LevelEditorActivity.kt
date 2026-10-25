package com.example.myapplication

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.widget.FrameLayout
import android.view.View
import android.app.AlertDialog
import java.util.Locale
import android.view.Gravity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

/** Отдельная страница с шаблонами, редактированием, сохранением и предпросмотром. */
class LevelEditorActivity : AppCompatActivity() {
    private lateinit var nameInput: EditText
    private lateinit var templateSpinner: Spinner
    private lateinit var seasonSpinner: Spinner
    private lateinit var bugsSeek: SeekBar
    private lateinit var speedSeek: SeekBar
    private lateinit var durationSeek: SeekBar
    private lateinit var bugsValue: TextView
    private lateinit var speedValue: TextView
    private lateinit var durationValue: TextView
    private lateinit var savedContainer: LinearLayout
    private lateinit var editorScroll: ScrollView
    private lateinit var saveButton: Button
    private var saveInProgress = false
    private var editingId: Long = 0L
    private val chosen = mutableMapOf<LevelRarity, String>()
    private val slotLabels = mutableMapOf<LevelRarity, TextView>()
    private val slotImages = mutableMapOf<LevelRarity, ImageView>()
    private val db by lazy { LevelDatabase.get(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "Редактор уровня"
        val scroll = ScrollView(this)
        editorScroll = scroll
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(14), dp(18), dp(24))
        }
        scroll.addView(root)
        setContentView(scroll)
        ScreenInsets.apply(scroll, additionalTopDp = 8, additionalBottomDp = 12)

        root.addView(editorHeader())
        root.addView(text("Выбери шаблон или создай собственный уровень"))
        templateSpinner = spinner(listOf("Свой уровень") + LevelDefaults.templates.map { it.name })
        root.addView(templateSpinner)
        root.addView(button("Применить выбранный шаблон") {
            val position = templateSpinner.selectedItemPosition
            if (position > 0) applyLevel(LevelDefaults.templates[position - 1].copy(id = 0L))
            else resetCustom()
        })

        root.addView(text("Название уровня"))
        nameInput = EditText(this).apply { hint = "Например, Ледяная ловушка"; setSingleLine(true) }
        root.addView(nameInput)
        root.addView(text("Время года"))
        seasonSpinner = spinner(listOf("Весна", "Лето", "Осень", "Зима"))
        root.addView(seasonSpinner)
        root.addView(text("Зима — лёд и остановки; весна — рывки; лето — высокая скорость; осень — сон."))

        root.addView(heading("Набор жуков — слот-машина"))
        root.addView(text("Вероятности: 55% / 35% / 9% / 1%. Стрелки выбирают конкретный вид."))
        slot(root, LevelRarity.COMMON, "Обычный — 55%")
        slot(root, LevelRarity.RARE, "Редкий — 35%")
        slot(root, LevelRarity.VERY_RARE, "Суперредкий — 9%")
        slot(root, LevelRarity.ULTRA_RARE, "Ультраредкий — 1%")

        bugsValue = text("")
        speedValue = text("")
        durationValue = text("")
        bugsSeek = seek(root, "Максимум жуков", 1, 25, 10, bugsValue)
        speedSeek = seek(root, "Базовая скорость", 1, 10, 5, speedValue)
        durationSeek = seek(root, "Длительность (сек)", 30, 300, 120, durationValue, 10)

        root.addView(button("Предварительный просмотр") { openPreview() })
        saveButton = button("Сохранить уровень") { saveLevel() }
        root.addView(saveButton)
        root.addView(heading("Сохранённые уровни"))
        root.addView(text("Нажми на карточку, чтобы играть, редактировать или удалить уровень."))
        savedContainer = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        root.addView(savedContainer)

        applyLevel(LevelDefaults.templates.first().copy(id = 0L))
        loadSavedLevels()
    }

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
    private fun editorHeader() = TextView(this).apply {
        text = "✏  РЕДАКТОР УРОВНЯ"
        textSize = 20f
        setTextColor(Color.WHITE)
        gravity = Gravity.CENTER_VERTICAL
        setPadding(dp(16), dp(14), dp(16), dp(14))
        background = GradientDrawable().apply {
            setColor(Color.rgb(102, 76, 166))
            cornerRadius = dp(14).toFloat()
        }
    }
    private fun heading(s: String) = TextView(this).apply { text = s; textSize = 22f; setPadding(0, dp(14), 0, dp(8)) }
    private fun text(s: String) = TextView(this).apply { text = s; textSize = 16f; setPadding(0, dp(6), 0, dp(6)) }
    private fun button(s: String, action: () -> Unit) = Button(this).apply { text = s; setOnClickListener { action() } }
    private fun spinner(items: List<String>) = Spinner(this).apply {
        adapter = ArrayAdapter(this@LevelEditorActivity, android.R.layout.simple_spinner_dropdown_item, items)
    }
    private fun seek(root: LinearLayout, label: String, min: Int, max: Int, initial: Int, display: TextView, step: Int = 1): SeekBar {
        root.addView(display)
        val bar = SeekBar(this).apply { this.max = (max - min) / step }
        fun update(value: Int) { display.text = "$label: $value" }
        bar.progress = (initial - min) / step
        update(initial)
        bar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) { update(min + progress * step) }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        root.addView(bar)
        return bar
    }
    private fun slot(root: LinearLayout, rarity: LevelRarity, label: String) {
        root.addView(text(label))
        val line = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        val previous = button("▲") { rotate(rarity, -1) }
        val next = button("▼") { rotate(rarity, 1) }
        val picture = ImageView(this).apply {
            scaleType = ImageView.ScaleType.FIT_CENTER
            contentDescription = "Выбранный вид насекомого"
        }
        val title = text("")
        slotLabels[rarity] = title
        slotImages[rarity] = picture
        // Слот-машина: крупная картинка и настоящие стрелки сверху/снизу.
        line.addView(picture, LinearLayout.LayoutParams(dp(145), dp(124)))
        val arrows = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        arrows.addView(previous, LinearLayout.LayoutParams(dp(75), dp(62)))
        arrows.addView(next, LinearLayout.LayoutParams(dp(75), dp(62)))
        line.addView(arrows)
        root.addView(line)
        root.addView(title)
    }
    private fun rotate(rarity: LevelRarity, direction: Int) {
        val variants = BugCatalog.variants(rarity)
        if (variants.isEmpty()) return
        val position = variants.indexOfFirst { it.id == chosen[rarity] }.coerceAtLeast(0)
        chosen[rarity] = variants[(position + direction + variants.size) % variants.size].id
        renderSlot(rarity)
    }
    private fun renderSlot(rarity: LevelRarity) {
        val species = BugCatalog.find(chosen[rarity] ?: "", rarity)
        chosen[rarity] = species.id
        val variants = BugCatalog.variants(rarity)
        val index = variants.indexOfFirst { it.id == species.id } + 1
        slotLabels[rarity]?.text = "${species.label} ($index / ${variants.size})"
        slotImages[rarity]?.setImageResource(species.image)
    }
    private fun selectedSeason() = arrayOf(Season.SPRING, Season.SUMMER, Season.AUTUMN, Season.WINTER)[seasonSpinner.selectedItemPosition]
    private fun formLevel() = LevelConfig(
        editingId, nameInput.text.toString().trim(), selectedSeason(), bugsSeek.progress + 1,
        speedSeek.progress + 1, 30 + durationSeek.progress * 10,
        chosen.getValue(LevelRarity.COMMON), chosen.getValue(LevelRarity.RARE),
        chosen.getValue(LevelRarity.VERY_RARE), chosen.getValue(LevelRarity.ULTRA_RARE)
    )
    private fun applyLevel(level: LevelConfig) {
        editingId = level.id
        nameInput.setText(level.name)
        seasonSpinner.setSelection(level.season.ordinal)
        bugsSeek.progress = level.maxBugs - 1
        speedSeek.progress = level.speed - 1
        durationSeek.progress = (level.durationSeconds - 30) / 10
        chosen[LevelRarity.COMMON] = level.commonBugId
        chosen[LevelRarity.RARE] = level.rareBugId
        chosen[LevelRarity.VERY_RARE] = level.veryRareBugId
        chosen[LevelRarity.ULTRA_RARE] = level.ultraRareBugId
        LevelRarity.values().forEach(::renderSlot)
    }
    private fun resetCustom() = applyLevel(LevelDefaults.templates.first().copy(id = 0L, name = ""))
    private fun openPreview() {
        val level = formLevel()
        val error = LevelRules.validate(level)
        if (error != null) { Toast.makeText(this, error, Toast.LENGTH_LONG).show(); return }
        LevelPreviewActivity.start(this, level)
    }
    /** Сравниваем названия без учёта регистра и лишних пробелов. */
    private fun normalizedName(name: String): String =
        name.trim().replace(Regex("\\s+"), " ").lowercase(Locale.ROOT)

    private fun saveLevel() {
        if (saveInProgress) return
        val level = formLevel()
        val error = LevelRules.validate(level)
        if (error != null) {
            Toast.makeText(this, error, Toast.LENGTH_LONG).show()
            return
        }

        saveInProgress = true
        saveButton.isEnabled = false
        Thread {
            // Проверяем дубликаты в фоне, как и обычные операции Room.
            // Совпадение собственного имени при редактировании допускается.
            val result = runCatching {
                val duplicate = db.levelDao().listAll().firstOrNull { saved ->
                    saved.id != level.id &&
                        normalizedName(saved.name) == normalizedName(level.name)
                }
                if (duplicate != null) {
                    SaveResult.Duplicate(duplicate)
                } else {
                    val insertedId = db.levelDao().save(LevelEntity.from(level))
                    SaveResult.Saved(if (level.id == 0L) insertedId else level.id)
                }
            }
            runOnUiThread {
                saveInProgress = false
                saveButton.isEnabled = true
                if (isFinishing || isDestroyed) return@runOnUiThread
                result.fold(
                    onSuccess = { outcome ->
                        when (outcome) {
                            is SaveResult.Duplicate -> {
                                nameInput.error = "Уровень с таким названием уже существует"
                                AlertDialog.Builder(this)
                                    .setTitle("Название уже занято")
                                    .setMessage(
                                        "Уровень «${outcome.level.name}» уже сохранён. " +
                                            "Введите другое название или откройте существующий уровень для редактирования."
                                    )
                                    .setNegativeButton("Изменить название", null)
                                    .setPositiveButton("Открыть уровень") { _, _ ->
                                        applyLevel(outcome.level.toConfig())
                                        editorScroll.smoothScrollTo(0, 0)
                                    }
                                    .show()
                            }
                            is SaveResult.Saved -> {
                                editingId = outcome.id
                                Toast.makeText(this, "Уровень сохранён", Toast.LENGTH_SHORT).show()
                                loadSavedLevels()
                            }
                        }
                    },
                    onFailure = { exception ->
                        AlertDialog.Builder(this)
                            .setTitle("Не удалось сохранить уровень")
                            .setMessage(exception.localizedMessage ?: "Ошибка базы данных")
                            .setPositiveButton("Понятно", null)
                            .show()
                    }
                )
            }
        }.start()
    }

    private sealed class SaveResult {
        data class Duplicate(val level: LevelEntity) : SaveResult()
        data class Saved(val id: Long) : SaveResult()
    }

    private fun seasonTitle(season: Season): String = when (season) {
        Season.SPRING -> "Весна"
        Season.SUMMER -> "Лето"
        Season.AUTUMN -> "Осень"
        Season.WINTER -> "Зима"
    }

    private fun seasonIcon(season: Season): String = when (season) {
        Season.SPRING -> "🌱"
        Season.SUMMER -> "☀"
        Season.AUTUMN -> "🍂"
        Season.WINTER -> "❄"
    }

    /** Одна кликабельная карточка с выбранным фоном сезона вместо трёх кнопок. */
    private fun savedLevelCard(level: LevelConfig): View {
        val card = FrameLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(144)
            ).apply { setMargins(0, dp(8), 0, dp(8)) }
            background = GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = dp(16).toFloat()
            }
            clipToOutline = true
            elevation = dp(3).toFloat()
            isClickable = true
            isFocusable = true
            contentDescription = "Уровень ${level.name}. ${seasonTitle(level.season)}. Нажмите для выбора действия"
            setOnClickListener { showLevelActions(level) }
        }

        card.addView(ImageView(this).apply {
            setImageResource(level.season.backgroundRes)
            scaleType = ImageView.ScaleType.CENTER_CROP
            contentDescription = null // Смысл карточки озвучен через её описание.
        }, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        ))

        // Затемнение позволяет читать белый текст на любом сезонном фоне.
        card.addView(View(this).apply {
            background = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(Color.argb(70, 0, 0, 0), Color.argb(195, 0, 0, 0))
            )
        }, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        ))

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.BOTTOM
            setPadding(dp(15), dp(12), dp(15), dp(12))
        }
        content.addView(TextView(this).apply {
            text = "${seasonIcon(level.season)}  ${level.name}"
            textSize = 20f
            setTextColor(Color.WHITE)
            setTypeface(null, android.graphics.Typeface.BOLD)
            maxLines = 2
            ellipsize = android.text.TextUtils.TruncateAt.END
        })
        content.addView(TextView(this).apply {
            text = "${seasonTitle(level.season)}  ·  ${level.maxBugs} жуков  ·  " +
                "скорость ${level.speed}  ·  ${level.durationSeconds} сек"
            textSize = 13f
            setTextColor(Color.WHITE)
            maxLines = 2
        })
        content.addView(TextView(this).apply {
            text = "Нажмите, чтобы выбрать действие  ⋯"
            textSize = 12f
            setTextColor(Color.rgb(232, 232, 232))
        })
        card.addView(content, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        ))
        return card
    }

    private fun showLevelActions(level: LevelConfig) {
        AlertDialog.Builder(this)
            .setTitle(level.name)
            .setItems(arrayOf("▶ Играть", "✏ Редактировать", "🗑 Удалить")) { _, action ->
                when (action) {
                    0 -> {
                        val gameIntent = LevelPreviewActivity.putLevel(
                            Intent(this, GameActivity::class.java), level
                        )
                        startActivity(gameIntent)
                    }
                    1 -> {
                        applyLevel(level)
                        editorScroll.smoothScrollTo(0, 0)
                        Toast.makeText(this, "Редактирование: ${level.name}", Toast.LENGTH_SHORT).show()
                    }
                    2 -> confirmDelete(level)
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun confirmDelete(level: LevelConfig) {
        AlertDialog.Builder(this)
            .setTitle("Удалить уровень?")
            .setMessage("Уровень «${level.name}» будет удалён. Ранее заработанные рекорды сохранятся в истории.")
            .setNegativeButton("Отмена", null)
            .setPositiveButton("Удалить") { _, _ ->
                Thread {
                    val result = runCatching { db.levelDao().delete(level.id) }
                    runOnUiThread {
                        if (isFinishing || isDestroyed) return@runOnUiThread
                        result.onSuccess {
                            if (editingId == level.id) resetCustom()
                            loadSavedLevels()
                        }.onFailure { error ->
                            Toast.makeText(this, error.localizedMessage ?: "Ошибка удаления", Toast.LENGTH_LONG).show()
                        }
                    }
                }.start()
            }
            .show()
    }

    private fun loadSavedLevels() {
        Thread {
            val result = runCatching { db.levelDao().listAll() }
            runOnUiThread {
                if (isFinishing || isDestroyed) return@runOnUiThread
                result.onSuccess { levels ->
                    savedContainer.removeAllViews()
                    if (levels.isEmpty()) {
                        savedContainer.addView(text("Пока нет сохранённых уровней"))
                    } else {
                        levels.forEach { entity ->
                            savedContainer.addView(savedLevelCard(entity.toConfig()))
                        }
                    }
                }.onFailure { error ->
                    Toast.makeText(this, error.localizedMessage ?: "Ошибка загрузки уровней", Toast.LENGTH_LONG).show()
                }
            }
        }.start()
    }
}
