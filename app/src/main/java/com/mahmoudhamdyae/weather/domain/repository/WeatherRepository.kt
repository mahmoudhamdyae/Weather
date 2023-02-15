package com.mahmoudhamdyae.weather.domain.repository

import com.mahmoudhamdyae.weather.domain.util.Resource
import com.mahmoudhamdyae.weather.domain.weather.WeatherInfo

interface WeatherRepository {
    suspend fun getWeatherData(lat: Double, long: Double): Resource<WeatherInfo>
}