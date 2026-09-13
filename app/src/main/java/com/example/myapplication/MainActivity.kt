package com.example.myapplication

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CalendarView
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.Calendar

data class Player(
    val fullName: String,
    val gender: String,
    val course: String,
    val difficulty: String,
    val birthDate: String,
    val zodiac: String
)

class MainActivity : AppCompatActivity() {

    private lateinit var editFullName: EditText
    private lateinit var radioGender: RadioGroup
    private lateinit var spinnerCourse: Spinner
    private lateinit var seekDifficulty: SeekBar
    private lateinit var textDifficulty: TextView
    private lateinit var calendarBirth: CalendarView
    private lateinit var imageZodiac: ImageView
    private lateinit var buttonRegister: Button
    private lateinit var textResult: TextView

    private var selectedDay = 0
    private var selectedMonth = 0
    private var selectedYear = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        editFullName =
            findViewById(R.id.editFullName)

        radioGender =
            findViewById(R.id.radioGender)

        spinnerCourse =
            findViewById(R.id.spinnerCourse)

        seekDifficulty =
            findViewById(R.id.seekDifficulty)

        textDifficulty =
            findViewById(R.id.textDifficulty)

        calendarBirth =
            findViewById(R.id.calendarBirth)

        imageZodiac =
            findViewById(R.id.imageZodiac)

        buttonRegister =
            findViewById(R.id.buttonRegister)

        textResult =
            findViewById(R.id.textResult)

        // ---------- КУРС ----------

        val courses = arrayOf(
            "1 курс",
            "2 курс",
            "3 курс",
            "4 курс"
        )

        val courseAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            courses
        )

        courseAdapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        spinnerCourse.adapter = courseAdapter

        // ---------- СЛОЖНОСТЬ ----------

        seekDifficulty.max = 4
        seekDifficulty.progress = 0

        seekDifficulty.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {

                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {

                    textDifficulty.text = when (progress) {
                        0 -> "Лёгкий"
                        1 -> "Ниже среднего"
                        2 -> "Средний"
                        3 -> "Сложный"
                        4 -> "Очень сложный"
                        else -> "Лёгкий"
                    }
                }

                override fun onStartTrackingTouch(
                    seekBar: SeekBar?
                ) {
                }

                override fun onStopTrackingTouch(
                    seekBar: SeekBar?
                ) {
                }
            }
        )

        // ---------- ДАТА ----------

        val calendar = Calendar.getInstance()

        selectedDay =
            calendar.get(Calendar.DAY_OF_MONTH)

        selectedMonth =
            calendar.get(Calendar.MONTH)

        selectedYear =
            calendar.get(Calendar.YEAR)

        calendarBirth.maxDate =
            System.currentTimeMillis()

        calendarBirth.setOnDateChangeListener {
                _, year, month, dayOfMonth ->

            selectedYear = year
            selectedMonth = month
            selectedDay = dayOfMonth

            val zodiac = getZodiacSign(
                selectedDay,
                selectedMonth + 1
            )

            setZodiacImage(zodiac)
        }

        // ---------- КНОПКА ----------

        buttonRegister.setOnClickListener {
            registerPlayer()
        }
    }

    // ---------- РЕГИСТРАЦИЯ ----------

    private fun registerPlayer() {

        val fullName =
            editFullName.text.toString().trim()

        val gender = when (
            radioGender.checkedRadioButtonId
        ) {

            R.id.radioMale -> "Мужской"

            R.id.radioFemale -> "Женский"

            else -> "Не указан"
        }

        val course =
            spinnerCourse.selectedItem.toString()

        val difficulty =
            textDifficulty.text.toString()

        val birthDate = String.format(
            "%02d.%02d.%04d",
            selectedDay,
            selectedMonth + 1,
            selectedYear
        )

        val zodiac = getZodiacSign(
            selectedDay,
            selectedMonth + 1
        )

        val player = Player(
            fullName = fullName,
            gender = gender,
            course = course,
            difficulty = difficulty,
            birthDate = birthDate,
            zodiac = zodiac
        )

        // ---------- ВЫВОД РЕЗУЛЬТАТА ----------

        textResult.text = """
            Игрок зарегистрирован!

            ФИО: ${player.fullName}
            Пол: ${player.gender}
            Курс: ${player.course}
            Уровень сложности: ${player.difficulty}
            Дата рождения: ${player.birthDate}
            Знак зодиака: ${player.zodiac}
        """.trimIndent()

        // Показываем соответствующий знак
        setZodiacImage(player.zodiac)
    }

    // ---------- ЗОДИАК ----------

    private fun getZodiacSign(
        day: Int,
        month: Int
    ): String {

        return when (month) {

            1 -> if (day >= 20) "Водолей"
            else "Козерог"

            2 -> if (day >= 19) "Рыбы"
            else "Водолей"

            3 -> if (day >= 21) "Овен"
            else "Рыбы"

            4 -> if (day >= 20) "Телец"
            else "Овен"

            5 -> if (day >= 21) "Близнецы"
            else "Телец"

            6 -> if (day >= 21) "Рак"
            else "Близнецы"

            7 -> if (day >= 23) "Лев"
            else "Рак"

            8 -> if (day >= 23) "Дева"
            else "Лев"

            9 -> if (day >= 23) "Весы"
            else "Дева"

            10 -> if (day >= 23) "Скорпион"
            else "Весы"

            11 -> if (day >= 22) "Стрелец"
            else "Скорпион"

            12 -> if (day >= 22) "Козерог"
            else "Стрелец"

            else -> "Неизвестно"
        }
    }

    // ---------- ИЗОБРАЖЕНИЕ ----------

    private fun setZodiacImage(zodiac: String) {

        val imageResource = when (zodiac) {

            "Овен" -> R.drawable.oven
            "Телец" -> R.drawable.telec
            "Близнецы" -> R.drawable.bliznecy
            "Рак" -> R.drawable.rak
            "Лев" -> R.drawable.lev
            "Дева" -> R.drawable.deva
            "Весы" -> R.drawable.vesy
            "Скорпион" -> R.drawable.skorpion
            "Стрелец" -> R.drawable.strelec
            "Козерог" -> R.drawable.kozerog
            "Водолей" -> R.drawable.vodoley
            "Рыбы" -> R.drawable.riba

            else -> android.R.drawable.ic_menu_help
        }

        imageZodiac.setImageResource(imageResource)
    }
}