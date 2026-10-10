package com.example.myapplication

import android.content.Context

object GoldRateStorage {
    private const val PREFS = "gold_rates"
    private const val RATE = "gold_rubles_per_gram"
    private const val DATE = "gold_rate_date"

    fun save(context: Context, rate: Double, date: String) {
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(RATE, rate.toString())
            .putString(DATE, date)
            .apply()
    }

    fun getRate(context: Context): Double? =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(RATE, null)?.toDoubleOrNull()

    fun getDate(context: Context): String =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(DATE, "") ?: ""
}
