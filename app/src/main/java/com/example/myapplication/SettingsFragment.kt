package com.example.myapplication

import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import android.widget.TextView
import androidx.fragment.app.Fragment

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        val seekSpeed =
            view.findViewById<SeekBar>(R.id.seekSpeed)

        val textSpeed =
            view.findViewById<TextView>(R.id.textSpeed)

        val seekCockroaches =
            view.findViewById<SeekBar>(R.id.seekCockroaches)

        val textCockroaches =
            view.findViewById<TextView>(R.id.textCockroaches)

        val seekBonus =
            view.findViewById<SeekBar>(R.id.seekBonus)

        val textBonus =
            view.findViewById<TextView>(R.id.textBonus)

        val seekRound =
            view.findViewById<SeekBar>(R.id.seekRound)

        val textRound =
            view.findViewById<TextView>(R.id.textRound)


        // -----------------------------------------
        // Скорость игры
        // Значение от 1 до 10
        // -----------------------------------------

        seekSpeed.max = 9

        // Восстанавливаем сохранённое значение
        seekSpeed.progress =
            GameSettings.speed - 1

        textSpeed.text =
            "Скорость игры: ${GameSettings.speed}"

        seekSpeed.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {

                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {

                    val speed =
                        progress + 1

                    // Сохраняем скорость для игры
                    GameSettings.speed = speed

                    textSpeed.text =
                        "Скорость игры: $speed"
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
        // Максимальное количество крокозябр
        // Значение от 1 до 20
        // -----------------------------------------

        seekCockroaches.max = 19

        seekCockroaches.progress =
            GameSettings.maxCockroaches - 1

        textCockroaches.text =
            "Максимальное количество крокозябр: ${GameSettings.maxCockroaches}"

        seekCockroaches.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {

                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {

                    val count =
                        progress + 1

                    // Сохраняем количество тараканов
                    GameSettings.maxCockroaches = count

                    textCockroaches.text =
                        "Максимальное количество крокозябр: $count"
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
        // Интервал появления бонусов
        // Значение от 1 до 60 секунд
        // -----------------------------------------

        seekBonus.max = 59

        seekBonus.progress =
            GameSettings.bonusInterval - 1

        textBonus.text =
            "Интервал появления бонусов: ${GameSettings.bonusInterval} сек"

        seekBonus.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {

                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {

                    val seconds =
                        progress + 1

                    // Сохраняем интервал бонусов
                    GameSettings.bonusInterval = seconds

                    textBonus.text =
                        "Интервал появления бонусов: $seconds сек"
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
        // Длительность раунда
        // Значение от 30 до 300 секунд
        // Шаг 30 секунд
        // -----------------------------------------

        seekRound.max = 9

        seekRound.progress =
            GameSettings.roundDuration / 30 - 1

        textRound.text =
            "Длительность раунда: ${GameSettings.roundDuration} сек"

        seekRound.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {

                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {

                    val seconds =
                        (progress + 1) * 30

                    // Сохраняем длительность раунда
                    GameSettings.roundDuration = seconds

                    textRound.text =
                        "Длительность раунда: $seconds сек"
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