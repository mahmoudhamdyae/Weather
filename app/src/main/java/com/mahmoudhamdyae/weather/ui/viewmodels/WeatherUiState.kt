package com.mahmoudhamdyae.weather.ui.viewmodels

import com.mahmoudhamdyae.weather.domain.weather.WeatherInfo

data class WeatherUiState(
    val weatherInfo: WeatherInfo? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
)