package com.weatheralert.app.utils

import com.weatheralert.app.models.AlertType
import com.weatheralert.app.models.CalamityAlert
import com.weatheralert.app.models.WeatherResponse

object CalamityDetector {

    fun detectCalamities(weather: WeatherResponse): List<CalamityAlert> {
        val alerts = mutableListOf<CalamityAlert>()
        val city = weather.name

        weather.rain?.threeHour?.let { rain3h ->
            if (rain3h > 50) {
                alerts.add(CalamityAlert(
                    type = AlertType.FLOOD,
                    title = "Flood Warning",
                    description = "Extreme rainfall of ${String.format("%.1f", rain3h)}mm in 3 hours detected. Flood risk is HIGH.",
                    severity = CalamityAlert.Severity.CRITICAL,
                    city = city
                ))
            }
        }

        weather.rain?.oneHour?.let { rain1h ->
            if (rain1h > 20 && (weather.rain.threeHour ?: 0.0) <= 50) {
                alerts.add(CalamityAlert(
                    type = AlertType.HEAVY_RAIN,
                    title = "Heavy Rain Warning",
                    description = "Heavy rainfall of ${String.format("%.1f", rain1h)}mm/h is occurring.",
                    severity = CalamityAlert.Severity.HIGH,
                    city = city
                ))
            }
        }

        if (weather.main.temp > 38) {
            alerts.add(CalamityAlert(
                type = AlertType.EXTREME_HEAT,
                title = "Extreme Heat Advisory",
                description = "Temperature is ${String.format("%.1f", weather.main.temp)}°C. Stay hydrated and avoid direct sunlight.",
                severity = CalamityAlert.Severity.HIGH,
                city = city
            ))
        }

        if (weather.main.temp < 5) {
            alerts.add(CalamityAlert(
                type = AlertType.EXTREME_COLD,
                title = "Extreme Cold Advisory",
                description = "Temperature is ${String.format("%.1f", weather.main.temp)}°C. Dress warmly and avoid exposure.",
                severity = CalamityAlert.Severity.HIGH,
                city = city
            ))
        }

        weather.wind?.let { wind ->
            if (wind.speed > 20) {
                alerts.add(CalamityAlert(
                    type = AlertType.STRONG_WIND,
                    title = "Strong Wind Warning",
                    description = "Wind speed of ${String.format("%.1f", wind.speed)} m/s detected. Secure loose objects.",
                    severity = CalamityAlert.Severity.HIGH,
                    city = city
                ))
            }
        }

        weather.weather.forEach { condition ->
            if (condition.id in 200..232) {
                alerts.add(CalamityAlert(
                    type = AlertType.THUNDERSTORM,
                    title = "Thunderstorm Warning",
                    description = "Thunderstorm activity detected: ${condition.description}. Seek shelter immediately.",
                    severity = CalamityAlert.Severity.CRITICAL,
                    city = city
                ))
                return@forEach
            }
        }

        val hasDryCondition = weather.weather.any {
            it.description.lowercase().contains("clear") ||
            it.main.lowercase() == "clear"
        }
        val noRain = (weather.rain?.oneHour ?: 0.0) == 0.0 && (weather.rain?.threeHour ?: 0.0) == 0.0
        if (hasDryCondition && noRain && weather.main.humidity < 20) {
            alerts.add(CalamityAlert(
                type = AlertType.DROUGHT,
                title = "Drought Advisory",
                description = "Very low humidity (${weather.main.humidity}%) with no rainfall detected. Drought conditions may be developing.",
                severity = CalamityAlert.Severity.MEDIUM,
                city = city
            ))
        }

        return alerts
    }
}
