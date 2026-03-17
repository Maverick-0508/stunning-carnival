package com.weatheralert.app.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.weatheralert.app.MainActivity
import com.weatheralert.app.R
import com.weatheralert.app.models.AlertType
import com.weatheralert.app.models.CalamityAlert

class NotificationHelper(private val context: Context) {

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            AlertType.values().forEach { alertType ->
                val channel = NotificationChannel(
                    alertType.notificationChannelId,
                    alertType.displayName,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Notifications for ${alertType.displayName}"
                    enableVibration(true)
                }
                notificationManager.createNotificationChannel(channel)
            }
            val generalChannel = NotificationChannel(
                GENERAL_CHANNEL_ID,
                "Weather Updates",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "General weather update notifications"
            }
            notificationManager.createNotificationChannel(generalChannel)
        }
    }

    fun showCalamityNotification(alert: CalamityAlert) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val iconRes = when (alert.type) {
            AlertType.FLOOD -> R.drawable.ic_flood
            AlertType.THUNDERSTORM -> R.drawable.ic_storm
            AlertType.EXTREME_HEAT, AlertType.EXTREME_COLD -> R.drawable.ic_temperature_high
            else -> R.drawable.ic_notification
        }

        val priority = when (alert.severity) {
            CalamityAlert.Severity.CRITICAL -> NotificationCompat.PRIORITY_MAX
            CalamityAlert.Severity.HIGH -> NotificationCompat.PRIORITY_HIGH
            CalamityAlert.Severity.MEDIUM -> NotificationCompat.PRIORITY_DEFAULT
            CalamityAlert.Severity.LOW -> NotificationCompat.PRIORITY_LOW
        }

        val notification = NotificationCompat.Builder(context, alert.type.notificationChannelId)
            .setSmallIcon(iconRes)
            .setContentTitle(alert.title)
            .setContentText(alert.description)
            .setStyle(NotificationCompat.BigTextStyle().bigText(alert.description))
            .setPriority(priority)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(alert.type.ordinal, notification)
        } catch (e: SecurityException) {
            // Permission not granted
        }
    }

    companion object {
        const val GENERAL_CHANNEL_ID = "general_weather_channel"
    }
}
