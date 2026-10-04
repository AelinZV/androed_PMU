package com.example.myapplication

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import java.util.Locale
import kotlin.math.roundToInt

class RecordsFragment : Fragment(R.layout.fragment_records) {

    private lateinit var listRecords: ListView
    private lateinit var textEmptyRecords: TextView

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        listRecords = view.findViewById(R.id.listRecords)
        textEmptyRecords = view.findViewById(R.id.textEmptyRecords)

        loadRecords()
    }

    override fun onResume() {
        super.onResume()
        loadRecords()
    }

    private fun loadRecords() {

        val database =
            AppDatabase.getInstance(requireContext().applicationContext)

        Thread {

            val records =
                database.gameResultDao().getAllRecords()

            activity?.runOnUiThread {

                if (!isAdded) {
                    return@runOnUiThread
                }

                if (records.isEmpty()) {
                    textEmptyRecords.visibility = View.VISIBLE
                    listRecords.visibility = View.GONE
                    return@runOnUiThread
                }

                textEmptyRecords.visibility = View.GONE
                listRecords.visibility = View.VISIBLE

                val rows = records.mapIndexed { index, record ->

                    val accuracyText = String.format(
                        Locale.getDefault(),
                        "%.1f%%",
                        record.accuracy
                    )

                    "${index + 1}. ${record.playerName}\n" +
                            "Очки: ${record.score} | Сложность: ${record.difficulty}\n" +
                            "Попадания: ${record.hits} | Промахи: ${record.misses} | Точность: $accuracyText"
                }

                listRecords.adapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_list_item_1,
                    rows
                )

                // Нажатие на запись открывает дополнительную статистику игрока.
                listRecords.setOnItemClickListener { _, _, position, _ ->
                    showRecordDetails(records[position])
                }
            }
        }.start()
    }

    private fun showRecordDetails(record: RecordItem) {

        val database =
            AppDatabase.getInstance(requireContext().applicationContext)

        Thread {

            val stats =
                database.gameResultDao().getPlayerStats(record.playerId)

            activity?.runOnUiThread {

                if (!isAdded) {
                    return@runOnUiThread
                }

                val accuracyText = String.format(
                    Locale.getDefault(),
                    "%.1f%%",
                    record.accuracy
                )

                val message =
                    "Выбранная игра:\n" +
                            "Очки: ${record.score}\n" +
                            "Сложность: ${record.difficulty}\n" +
                            "Попадания: ${record.hits}\n" +
                            "Промахи: ${record.misses}\n" +
                            "Точность: $accuracyText\n" +
                            "Длительность: ${formatTime(record.durationSeconds.toDouble())}\n\n" +
                            "Общая статистика игрока:\n" +
                            "Сыграно игр: ${stats.gamesCount}\n" +
                            "Среднее время игры: ${formatTime(stats.averageTimeSeconds)}\n" +
                            "Общее время игры: ${formatTime(stats.totalTimeSeconds.toDouble())}"

                AlertDialog.Builder(requireContext())
                    .setTitle(record.playerName)
                    .setMessage(message)
                    .setPositiveButton("Закрыть", null)
                    .show()
            }
        }.start()
    }

    private fun formatTime(seconds: Double): String {

        val totalSeconds =
            seconds.roundToInt().coerceAtLeast(0)

        val hours =
            totalSeconds / 3600

        val minutes =
            (totalSeconds % 3600) / 60

        val secondsLeft =
            totalSeconds % 60

        return when {
            hours > 0 ->
                "$hours ч $minutes мин $secondsLeft сек"

            minutes > 0 ->
                "$minutes мин $secondsLeft сек"

            else ->
                "$secondsLeft сек"
        }
    }
}
