package com.mahmoudhamdyae.weather.ui.viewmodels

import com.mahmoudhamdyae.weather.domain.weather.WeatherInfo

data class WeatherState(
    val weatherInfo: WeatherInfo? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)