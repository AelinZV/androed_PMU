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
            view.findViewById<SeekBar>(
                R.id.seekSpeed
            )

        val textSpeed =
            view.findViewById<TextView>(
                R.id.textSpeed
            )


        val seekCockroaches =
            view.findViewById<SeekBar>(
                R.id.seekCockroaches
            )

        val textCockroaches =
            view.findViewById<TextView>(
                R.id.textCockroaches
            )


        val seekBonus =
            view.findViewById<SeekBar>(
                R.id.seekBonus
            )

        val textBonus =
            view.findViewById<TextView>(
                R.id.textBonus
            )


        val seekRound =
            view.findViewById<SeekBar>(
                R.id.seekRound
            )

        val textRound =
            view.findViewById<TextView>(
                R.id.textRound
            )


        // -----------------------------------------
        // Скорость игры
        // Значение от 1 до 10
        // -----------------------------------------

        seekSpeed.max = 9
        seekSpeed.progress = 4

        textSpeed.text =
            "Скорость игры: 5"

        seekSpeed.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {

                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {

                    val speed =
                        progress + 1

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
        // Максимальное количество тараканов
        // Значение от 1 до 20
        // -----------------------------------------

        seekCockroaches.max = 19
        seekCockroaches.progress = 9

        textCockroaches.text =
            "Максимальное количество тараканов: 10"

        seekCockroaches.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {

                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {

                    val count =
                        progress + 1

                    textCockroaches.text =
                        "Максимальное количество тараканов: $count"
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
        seekBonus.progress = 9

        textBonus.text =
            "Интервал появления бонусов: 10 сек"

        seekBonus.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {

                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {

                    val seconds =
                        progress + 1

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
        // -----------------------------------------

        seekRound.max = 9
        seekRound.progress = 3

        textRound.text =
            "Длительность раунда: 120 сек"

        seekRound.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {

                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {

                    val seconds =
                        (progress + 1) * 30

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