package com.mahmoudhamdyae.weather.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mahmoudhamdyae.weather.data.repository.LocationPreferencesRepository
import com.mahmoudhamdyae.weather.domain.repository.WeatherRepository
import com.mahmoudhamdyae.weather.domain.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val repository: WeatherRepository,
    private val preferences: LocationPreferencesRepository
): ViewModel() {

    var state by mutableStateOf(WeatherUiState())
        private set

    fun loadWeatherInfo() {
        viewModelScope.launch {

            preferences.readLatitude.collect { latitude ->
                preferences.readLongitude.collect { longitude ->

                    if (latitude != null && longitude != null) {
                        state = state.copy(
                            isLoading = true,
                            error = null
                        )
                        when (val result = repository
                            .getWeatherData(latitude, longitude)) {
                            is Resource.Success -> {
                                state = state.copy(
                                    weatherInfo = result.data,
                                    isLoading = false,
                                    error = null
                                )
                            }
                            is Resource.Error -> {
                                state = state.copy(
                                    weatherInfo = null,
                                    isLoading = false,
                                    error = result.message
                                )
                            }
                        }
                    }
                }
            }


        }
    }

    fun writeLocationToPreferences(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            preferences.saveLocationPreference(latitude, longitude)
            state = state.copy(latitude = latitude, longitude = longitude)

            loadWeatherInfo()
        }
    }
}