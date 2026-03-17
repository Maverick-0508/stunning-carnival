package com.weatheralert.app.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.weatheralert.app.models.AlertType
import com.weatheralert.app.models.CalamityAlert

class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val PREF_NAME = "weather_alert_prefs"
        private const val KEY_CITY = "city"
        private const val KEY_API_KEY = "api_key"
        private const val KEY_INTERVAL = "monitoring_interval"
        private const val KEY_STORED_ALERTS = "stored_alerts"
        private const val MAX_STORED_ALERTS = 50
        private const val DEFAULT_CITY = "Nairobi"
        // Get your free API key at https://openweathermap.org/appid
        const val DEFAULT_API_KEY = "YOUR_API_KEY_HERE"
        private const val DEFAULT_INTERVAL = 180
    }

    var city: String
        get() = prefs.getString(KEY_CITY, DEFAULT_CITY) ?: DEFAULT_CITY
        set(value) = prefs.edit().putString(KEY_CITY, value).apply()

    var apiKey: String
        get() = prefs.getString(KEY_API_KEY, DEFAULT_API_KEY) ?: DEFAULT_API_KEY
        set(value) = prefs.edit().putString(KEY_API_KEY, value).apply()

    var monitoringIntervalMinutes: Int
        get() = prefs.getInt(KEY_INTERVAL, DEFAULT_INTERVAL)
        set(value) = prefs.edit().putInt(KEY_INTERVAL, value).apply()

    fun isAlertTypeEnabled(alertType: AlertType): Boolean {
        return prefs.getBoolean("alert_${alertType.name}", true)
    }

    fun setAlertTypeEnabled(alertType: AlertType, enabled: Boolean) {
        prefs.edit().putBoolean("alert_${alertType.name}", enabled).apply()
    }

    fun saveAlerts(newAlerts: List<CalamityAlert>) {
        val existing = getStoredAlerts().toMutableList()
        existing.addAll(0, newAlerts)
        val trimmed = existing.take(MAX_STORED_ALERTS)
        prefs.edit().putString(KEY_STORED_ALERTS, gson.toJson(trimmed)).apply()
    }

    fun getStoredAlerts(): List<CalamityAlert> {
        val json = prefs.getString(KEY_STORED_ALERTS, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<CalamityAlert>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
