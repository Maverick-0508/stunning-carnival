package com.weatheralert.app.utils

object LocationHelper {
    fun getDefaultCity(): String = "Nairobi"

    fun getKenyanCities(): List<String> = listOf(
        "Nairobi", "Mombasa", "Kisumu", "Nakuru", "Eldoret",
        "Thika", "Malindi", "Kitale", "Garissa", "Nyeri"
    )
}
