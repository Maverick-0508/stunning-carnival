package com.weatheralert.app.models

import com.google.gson.annotations.SerializedName

data class WeatherResponse(
    val coord: Coord?,
    val weather: List<WeatherCondition>,
    val main: Main,
    val wind: Wind?,
    val rain: Rain?,
    val clouds: Clouds?,
    val sys: Sys?,
    val name: String,
    val dt: Long
)

data class Coord(val lon: Double, val lat: Double)

data class WeatherCondition(
    val id: Int,
    val main: String,
    val description: String,
    val icon: String
)

data class Main(
    val temp: Double,
    @SerializedName("feels_like") val feelsLike: Double,
    @SerializedName("temp_min") val tempMin: Double,
    @SerializedName("temp_max") val tempMax: Double,
    val pressure: Int,
    val humidity: Int
)

data class Wind(val speed: Double, val deg: Int?, val gust: Double?)

data class Rain(
    @SerializedName("1h") val oneHour: Double?,
    @SerializedName("3h") val threeHour: Double?
)

data class Clouds(val all: Int)

data class Sys(
    val country: String?,
    val sunrise: Long?,
    val sunset: Long?
)

data class ForecastResponse(
    val list: List<ForecastItem>,
    val city: City
)

data class ForecastItem(
    val dt: Long,
    val main: Main,
    val weather: List<WeatherCondition>,
    val wind: Wind?,
    val rain: Rain?,
    @SerializedName("dt_txt") val dtTxt: String
)

data class City(
    val id: Int,
    val name: String,
    val country: String
)
