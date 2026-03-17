package com.weatheralert.app.utils

import com.weatheralert.app.models.AlertType
import com.weatheralert.app.models.CalamityAlert
import com.weatheralert.app.models.WeatherResponse

object CalamityDetector {

    private const val FLOOD_RAIN_3H_MM = 50.0
    private const val HEAVY_RAIN_1H_MM = 20.0
    private const val EXTREME_HEAT_CELSIUS = 38.0
    private const val EXTREME_COLD_CELSIUS = 5.0
    private const val STRONG_WIND_MS = 20.0
    private const val LOW_HUMIDITY_PERCENT = 20
    private const val THUNDERSTORM_ID_START = 200
    private const val THUNDERSTORM_ID_END = 232

    fun detectCalamities(weather: WeatherResponse): List<CalamityAlert> {
        val alerts = mutableListOf<CalamityAlert>()
        val city = weather.name

        weather.rain?.threeHour?.let { rain3h ->
            if (rain3h > FLOOD_RAIN_3H_MM) {
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
            if (rain1h > HEAVY_RAIN_1H_MM && (weather.rain.threeHour ?: 0.0) <= FLOOD_RAIN_3H_MM) {
                alerts.add(CalamityAlert(
                    type = AlertType.HEAVY_RAIN,
                    title = "Heavy Rain Warning",
                    description = "Heavy rainfall of ${String.format("%.1f", rain1h)}mm/h is occurring.",
                    severity = CalamityAlert.Severity.HIGH,
                    city = city
                ))
            }
        }

        if (weather.main.temp > EXTREME_HEAT_CELSIUS) {
            alerts.add(CalamityAlert(
                type = AlertType.EXTREME_HEAT,
                title = "Extreme Heat Advisory",
                description = "Temperature is ${String.format("%.1f", weather.main.temp)}°C. Stay hydrated and avoid direct sunlight.",
                severity = CalamityAlert.Severity.HIGH,
                city = city
            ))
        }

        if (weather.main.temp < EXTREME_COLD_CELSIUS) {
            alerts.add(CalamityAlert(
                type = AlertType.EXTREME_COLD,
                title = "Extreme Cold Advisory",
                description = "Temperature is ${String.format("%.1f", weather.main.temp)}°C. Dress warmly and avoid exposure.",
                severity = CalamityAlert.Severity.HIGH,
                city = city
            ))
        }

        weather.wind?.let { wind ->
            if (wind.speed > STRONG_WIND_MS) {
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
            if (condition.id in THUNDERSTORM_ID_START..THUNDERSTORM_ID_END) {
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
        if (hasDryCondition && noRain && weather.main.humidity < LOW_HUMIDITY_PERCENT) {
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
