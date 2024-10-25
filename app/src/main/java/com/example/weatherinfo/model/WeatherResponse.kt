package com.example.weatherdata.model

data class WeatherResponse(
    val weather: List<Weather>,
    val base: String,
    val main: Main,
    val visibility: Int,
    val dt: Long,
    val timezone: Int,
    val id: Long,
    val name: String,
    val cod: Int
)

data class Weather(
    val description: String,
    val icon: String
)

data class Main(
    val temp: Double,
    val feels_like: Double,
    val temp_min: Double,
    val temp_max: Double,
    val pressure: Int,
    val humidity: Int,
    val sea_level: Int?,
    val grnd_level: Int?
)


