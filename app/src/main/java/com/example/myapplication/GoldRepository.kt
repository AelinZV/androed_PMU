package com.example.myapplication

import android.content.Context
import android.util.Xml
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import org.xmlpull.v1.XmlPullParser
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object GoldRepository {
    data class GoldRate(val rublesPerGram: Double, val date: String)

    private val api: CbrApi = Retrofit.Builder()
        .baseUrl("https://www.cbr.ru/")
        .build()
        .create(CbrApi::class.java)

    /** Возвращает актуальный результат или кэш при отсутствии интернета. */
    fun refresh(context: Context, callback: (GoldRate?) -> Unit) {
        val appContext = context.applicationContext
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.US)
        val to = dateFormat.format(calendar.time)
        calendar.add(Calendar.DAY_OF_MONTH, -14) // учитываем выходные/праздники
        val from = dateFormat.format(calendar.time)

        api.getMetalRates(from, to).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                val rate = if (response.isSuccessful) {
                    try {
                        response.body()?.use { body -> parse(body) }
                    } catch (_: Exception) { null }
                } else null

                if (rate != null) GoldRateStorage.save(appContext, rate.rublesPerGram, rate.date)
                callback(rate ?: cached(appContext))
            }

            override fun onFailure(call: Call<ResponseBody>, error: Throwable) {
                callback(cached(appContext))
            }
        })
    }

    private fun cached(context: Context): GoldRate? =
        GoldRateStorage.getRate(context)?.let {
            GoldRate(it, GoldRateStorage.getDate(context))
        }

    /** xml_metall.asp: Record Code=1 - золото; учитываем цену Sell в рублях за грамм. */
    private fun parse(body: ResponseBody): GoldRate? {
        val parser = Xml.newPullParser()
        parser.setInput(body.byteStream(), null) // кодировка берётся из XML-декларации ЦБ
        var recordDate = ""
        var recordIsGold = false
        var price: Double? = null
        var latest: GoldRate? = null
        var latestDate: Long = Long.MIN_VALUE
        val format = SimpleDateFormat("dd.MM.yyyy", Locale.US).apply { isLenient = false }

        while (parser.eventType != XmlPullParser.END_DOCUMENT) {
            when (parser.eventType) {
                XmlPullParser.START_TAG -> {
                    if (parser.name.equals("Record", ignoreCase = true)) {
                        recordIsGold = parser.getAttributeValue(null, "Code") == "1"
                        recordDate = parser.getAttributeValue(null, "Date") ?: ""
                        price = null
                    } else if (recordIsGold && parser.name.equals("Sell", ignoreCase = true)) {
                        price = parser.nextText().trim().replace(',', '.').toDoubleOrNull()
                    }
                }
                XmlPullParser.END_TAG -> if (parser.name.equals("Record", ignoreCase = true)) {
                    if (recordIsGold && price != null && price!! > 0) {
                        val dateValue = try { format.parse(recordDate)?.time } catch (_: Exception) { null }
                        if (dateValue != null && dateValue >= latestDate) {
                            latestDate = dateValue
                            latest = GoldRate(price!!, recordDate)
                        }
                    }
                    recordIsGold = false
                }
            }
            parser.next()
        }
        return latest
    }
}
