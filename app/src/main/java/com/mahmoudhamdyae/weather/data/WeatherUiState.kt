package com.mahmoudhamdyae.weather.data

import com.mahmoudhamdyae.weather.domain.weather.WeatherInfo

data class WeatherUiState(
    val weatherInfo: WeatherInfo? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)