package com.example.myapplication

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import java.util.Locale

class GoldWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        manager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        updateAll(context)
        GoldRepository.refresh(context) { updateAll(context) }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_REFRESH) {
            GoldRepository.refresh(context) { updateAll(context) }
        }
    }

    companion object {
        private const val ACTION_REFRESH = "com.example.myapplication.REFRESH_GOLD_WIDGET"

        fun updateAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val component = ComponentName(context, GoldWidgetProvider::class.java)
            val ids = manager.getAppWidgetIds(component)
            ids.forEach { id ->
                val views = RemoteViews(context.packageName, R.layout.widget_gold)
                val rate = GoldRateStorage.getRate(context)
                val formatted = if (rate != null) {
                    String.format(Locale("ru", "RU"), "%,.2f ₽/г", rate)
                } else "Нет данных"
                views.setTextViewText(R.id.textGoldRate, formatted)
                views.setTextViewText(
                    R.id.textGoldDate,
                    GoldRateStorage.getDate(context).ifEmpty { "Курс ЦБ" }
                )
                val refresh = Intent(context, GoldWidgetProvider::class.java).apply {
                    action = ACTION_REFRESH
                }
                val pending = PendingIntent.getBroadcast(
                    context, 0, refresh,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.goldBall, pending)
                manager.updateAppWidget(id, views)
            }
        }
    }
}
