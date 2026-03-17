package com.weatheralert.app.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.weatheralert.app.utils.PreferencesManager
import com.weatheralert.app.workers.WeatherCheckWorker
import java.util.concurrent.TimeUnit

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // WorkManager enforces a minimum periodic interval of 15 minutes.
            // The interval_values array only contains values >= 60, so this is always satisfied.
            val intervalMinutes = PreferencesManager(context).monitoringIntervalMinutes.toLong()
                .coerceAtLeast(15L)
            val workRequest = PeriodicWorkRequestBuilder<WeatherCheckWorker>(intervalMinutes, TimeUnit.MINUTES)
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WeatherCheckWorker.WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        }
    }
}
