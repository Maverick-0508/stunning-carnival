package com.weatheralert.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.weatheralert.app.MainActivity
import com.weatheralert.app.R
import com.weatheralert.app.utils.PreferencesManager

class WeatherWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        appWidgetIds.forEach { appWidgetId ->
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == "com.weatheralert.app.UPDATE_WIDGET") {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(
                android.content.ComponentName(context, WeatherWidgetProvider::class.java)
            )
            onUpdate(context, appWidgetManager, appWidgetIds)
        }
    }

    companion object {
        fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val prefs = PreferencesManager(context)
            val views = RemoteViews(context.packageName, R.layout.widget_weather)

            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

            val sharedPrefs = context.getSharedPreferences("widget_data", Context.MODE_PRIVATE)
            val temp = sharedPrefs.getString("current_temp", "--") ?: "--"
            val description = sharedPrefs.getString("current_description", "Tap to refresh") ?: "Tap to refresh"
            val alertText = sharedPrefs.getString("active_alerts", "") ?: ""

            views.setTextViewText(R.id.widget_city, prefs.city)
            views.setTextViewText(R.id.widget_temperature, "$temp°C")
            views.setTextViewText(R.id.widget_description, description)
            if (alertText.isNotEmpty()) {
                views.setTextViewText(R.id.widget_alert_text, "⚠ $alertText")
            } else {
                views.setTextViewText(R.id.widget_alert_text, "No active alerts")
            }

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
