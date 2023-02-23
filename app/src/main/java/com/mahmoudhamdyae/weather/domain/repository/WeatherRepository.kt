package com.mahmoudhamdyae.weather.domain.repository

import com.mahmoudhamdyae.weather.domain.util.Resource
import com.mahmoudhamdyae.weather.domain.weather.WeatherInfo

interface WeatherRepository {
    suspend fun getWeatherData(latitude: Double, longitude: Double): Resource<WeatherInfo>
}