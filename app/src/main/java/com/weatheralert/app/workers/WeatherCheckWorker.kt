package com.weatheralert.app.workers

import android.content.Context
import android.content.Intent
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.weatheralert.app.api.WeatherApiClient
import com.weatheralert.app.notifications.NotificationHelper
import com.weatheralert.app.utils.CalamityDetector
import com.weatheralert.app.utils.PreferencesManager

class WeatherCheckWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val prefsManager = PreferencesManager(applicationContext)
        val apiKey = prefsManager.apiKey
        val city = prefsManager.city

        if (apiKey == PreferencesManager.DEFAULT_API_KEY || apiKey.isBlank()) {
            return Result.failure()
        }

        return try {
            val response = WeatherApiClient.apiService.getCurrentWeather(city, apiKey)
            if (response.isSuccessful) {
                val weatherData = response.body() ?: return Result.failure()
                val alerts = CalamityDetector.detectCalamities(weatherData)
                val notificationHelper = NotificationHelper(applicationContext)
                alerts.forEach { alert ->
                    if (prefsManager.isAlertTypeEnabled(alert.type)) {
                        notificationHelper.showCalamityNotification(alert)
                    }
                }
                val updateIntent = Intent("com.weatheralert.app.UPDATE_WIDGET")
                applicationContext.sendBroadcast(updateIntent)
                Result.success()
            } else {
                Result.retry()
            }
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "WeatherCheckWorker"
    }
}
