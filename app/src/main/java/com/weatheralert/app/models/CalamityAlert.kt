package com.weatheralert.app.models

data class CalamityAlert(
    val type: AlertType,
    val title: String,
    val description: String,
    val severity: Severity,
    val timestamp: Long = System.currentTimeMillis(),
    val city: String = ""
) {
    enum class Severity { LOW, MEDIUM, HIGH, CRITICAL }
}
