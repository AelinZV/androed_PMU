package com.example.myapplication

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CalendarView
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import java.util.Calendar

class RegistrationFragment : Fragment(R.layout.fragment_registration) {

    private lateinit var editFullName: EditText
    private lateinit var radioGender: RadioGroup
    private lateinit var spinnerCourse: Spinner

    private lateinit var seekDifficulty: SeekBar
    private lateinit var textDifficulty: TextView

    private lateinit var calendarBirth: CalendarView
    private lateinit var buttonSelectDate: Button
    private lateinit var textSelectedDate: TextView

    private lateinit var imageZodiac: ImageView
    private lateinit var buttonRegister: Button
    private lateinit var textResult: TextView

    private var selectedDay = 0
    private var selectedMonth = 0
    private var selectedYear = 0

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        // Получаем элементы интерфейса

        editFullName = view.findViewById(R.id.editFullName)
        radioGender = view.findViewById(R.id.radioGender)
        spinnerCourse = view.findViewById(R.id.spinnerCourse)

        seekDifficulty = view.findViewById(R.id.seekDifficulty)
        textDifficulty = view.findViewById(R.id.textDifficulty)

        calendarBirth = view.findViewById(R.id.calendarBirth)
        buttonSelectDate = view.findViewById(R.id.buttonSelectDate)
        textSelectedDate = view.findViewById(R.id.textSelectedDate)

        imageZodiac = view.findViewById(R.id.imageZodiac)
        buttonRegister = view.findViewById(R.id.buttonRegister)
        textResult = view.findViewById(R.id.textResult)


        // -----------------------------------------
        // Настройка курса
        // -----------------------------------------

        val courses = arrayOf(
            "1 курс",
            "2 курс",
            "3 курс",
            "4 курс"
        )

        val courseAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            courses
        )

        courseAdapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        spinnerCourse.adapter = courseAdapter


        // -----------------------------------------
        // Настройка сложности
        // -----------------------------------------

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

                        0 -> "Новичок"

                        1 -> "Я уже смешарик"

                        2 -> "Средний"

                        3 -> "Сложный"

                        4 -> "Дальше Бога нет.."

                        else -> "Новичок"
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


        // -----------------------------------------
        // Устанавливаем сегодняшнюю дату
        // -----------------------------------------

        val calendar = Calendar.getInstance()

        selectedDay =
            calendar.get(Calendar.DAY_OF_MONTH)

        selectedMonth =
            calendar.get(Calendar.MONTH)

        selectedYear =
            calendar.get(Calendar.YEAR)

        // Будущую дату выбрать нельзя

        calendarBirth.maxDate =
            System.currentTimeMillis()

        calendarBirth.date =
            calendar.timeInMillis

        updateDate()

        // Показываем знак зодиака

        setZodiacImage(
            getZodiacSign(
                selectedDay,
                selectedMonth + 1
            )
        )


        // -----------------------------------------
        // Выбор даты в CalendarView
        // -----------------------------------------

        calendarBirth.setOnDateChangeListener {
                _, year, month, dayOfMonth ->

            selectedYear = year
            selectedMonth = month
            selectedDay = dayOfMonth

            updateDate()

            val zodiac = getZodiacSign(
                selectedDay,
                selectedMonth + 1
            )

            setZodiacImage(zodiac)
        }


        // -----------------------------------------
        // Кнопка выбора конкретной даты
        // -----------------------------------------

        buttonSelectDate.setOnClickListener {

            showDatePicker()
        }


        // -----------------------------------------
        // Кнопка регистрации
        // -----------------------------------------

        buttonRegister.setOnClickListener {

            registerPlayer()
        }
    }


    // =================================================
    // Выбор конкретной даты
    // =================================================

    private fun showDatePicker() {

        val dialog = DatePickerDialog(

            requireContext(),

            { _, year, month, dayOfMonth ->

                selectedYear = year
                selectedMonth = month
                selectedDay = dayOfMonth

                val calendar = Calendar.getInstance()

                calendar.set(
                    selectedYear,
                    selectedMonth,
                    selectedDay
                )

                calendarBirth.date =
                    calendar.timeInMillis

                updateDate()

                val zodiac = getZodiacSign(
                    selectedDay,
                    selectedMonth + 1
                )

                setZodiacImage(zodiac)
            },

            selectedYear,
            selectedMonth,
            selectedDay
        )

        // Нельзя выбрать будущую дату

        dialog.datePicker.maxDate =
            System.currentTimeMillis()

        dialog.show()
    }


    // =================================================
    // Обновление текста даты
    // =================================================

    private fun updateDate() {

        val date = String.format(
            "%02d.%02d.%04d",
            selectedDay,
            selectedMonth + 1,
            selectedYear
        )

        textSelectedDate.text =
            "Выбранная дата: $date"
    }


    // =================================================
    // Регистрация игрока
    // =================================================

    private fun registerPlayer() {

        // ФИО

        val fullName =
            editFullName.text.toString().trim()

        if (fullName.isEmpty()) {

            editFullName.error =
                "Введите ФИО"

            return
        }


        // Пол

        val gender =
            when (radioGender.checkedRadioButtonId) {

                R.id.radioMale ->
                    "Мужской"

                R.id.radioFemale ->
                    "Женский"

                else ->
                    "Не указан"
            }


        // Курс

        val course =
            spinnerCourse.selectedItem.toString()


        // Сложность

        val difficulty =
            textDifficulty.text.toString()


        // Дата рождения

        val birthDate = String.format(
            "%02d.%02d.%04d",
            selectedDay,
            selectedMonth + 1,
            selectedYear
        )


        // Знак зодиака

        val zodiac =
            getZodiacSign(
                selectedDay,
                selectedMonth + 1
            )


        // Создаём игрока

        val player = Player(
            fullName = fullName,
            gender = gender,
            course = course,
            difficulty = difficulty,
            birthDate = birthDate,
            zodiac = zodiac
        )


        // Выводим информацию

        textResult.text = """

            Игрок зарегистрирован!

            ФИО: ${player.fullName}
            Пол: ${player.gender}
            Курс: ${player.course}
            Уровень сложности: ${player.difficulty}
            Дата рождения: ${player.birthDate}
            Знак зодиака: ${player.zodiac}

        """.trimIndent()


        // Показываем изображение

        setZodiacImage(player.zodiac)
    }


    // =================================================
    // Определение знака зодиака
    // =================================================

    private fun getZodiacSign(
        day: Int,
        month: Int
    ): String {

        return when (month) {

            1 -> if (day >= 20)
                "Водолей"
            else
                "Козерог"

            2 -> if (day >= 19)
                "Рыбы"
            else
                "Водолей"

            3 -> if (day >= 21)
                "Овен"
            else
                "Рыбы"

            4 -> if (day >= 20)
                "Телец"
            else
                "Овен"

            5 -> if (day >= 21)
                "Близнецы"
            else
                "Телец"

            6 -> if (day >= 21)
                "Рак"
            else
                "Близнецы"

            7 -> if (day >= 23)
                "Лев"
            else
                "Рак"

            8 -> if (day >= 23)
                "Дева"
            else
                "Лев"

            9 -> if (day >= 23)
                "Весы"
            else
                "Дева"

            10 -> if (day >= 23)
                "Скорпион"
            else
                "Весы"

            11 -> if (day >= 22)
                "Стрелец"
            else
                "Скорпион"

            12 -> if (day >= 22)
                "Козерог"
            else
                "Стрелец"

            else ->
                "Неизвестно"
        }
    }


    // =================================================
    // Выбор изображения зодиака
    // =================================================

    private fun setZodiacImage(
        zodiac: String
    ) {

        val imageResource =
            when (zodiac) {

                "Овен" ->
                    R.drawable.oven

                "Телец" ->
                    R.drawable.telec

                "Близнецы" ->
                    R.drawable.bliznecy

                "Рак" ->
                    R.drawable.rak

                "Лев" ->
                    R.drawable.lev

                "Дева" ->
                    R.drawable.deva

                "Весы" ->
                    R.drawable.vesy

                "Скорпион" ->
                    R.drawable.skorpion

                "Стрелец" ->
                    R.drawable.strelec

                "Козерог" ->
                    R.drawable.kozerog

                "Водолей" ->
                    R.drawable.vodoley

                "Рыбы" ->
                    R.drawable.riba

                else ->
                    android.R.drawable.ic_menu_help
            }

        imageZodiac.setImageResource(
            imageResource
        )
    }
}