package com.weatheralert.app.models

enum class AlertType(val displayName: String, val notificationChannelId: String) {
    FLOOD("Flood Warning", "flood_channel"),
    EXTREME_HEAT("Extreme Heat Advisory", "heat_channel"),
    EXTREME_COLD("Extreme Cold Advisory", "cold_channel"),
    HEAVY_RAIN("Heavy Rain Warning", "rain_channel"),
    STRONG_WIND("Strong Wind Warning", "wind_channel"),
    THUNDERSTORM("Thunderstorm Warning", "storm_channel"),
    DROUGHT("Drought Advisory", "drought_channel")
}
