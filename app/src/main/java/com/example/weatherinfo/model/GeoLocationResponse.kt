package com.example.weatherdata.model

data class GeoLocationResponse(
    val name: String,
    val lat: Double,
    val lon: Double,
    val country: String,
    val state: String? = null
)