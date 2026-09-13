package com.example.myapplication

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        // Получаем элементы интерфейса
        editFullName = findViewById(R.id.editFullName)
        radioGender = findViewById(R.id.radioGender)
        spinnerCourse = findViewById(R.id.spinnerCourse)
        seekDifficulty = findViewById(R.id.seekDifficulty)
        textDifficulty = findViewById(R.id.textDifficulty)

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
    }
}