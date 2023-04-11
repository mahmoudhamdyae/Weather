package com.mahmoudhamdyae.weather.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mahmoudhamdyae.weather.data.repository.LocationPreferencesRepository
import com.mahmoudhamdyae.weather.domain.repository.WeatherRepository
import com.mahmoudhamdyae.weather.domain.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val repository: WeatherRepository,
    private val preferences: LocationPreferencesRepository
): ViewModel() {

    private var _uiState = MutableStateFlow(WeatherUiState())
    val uiState = _uiState.asStateFlow()

    fun loadWeatherInfo() {
        viewModelScope.launch {

            preferences.readLatitude.collect { latitude ->
                preferences.readLongitude.collect { longitude ->

                    if (latitude != null && longitude != null) {
                        _uiState.update {
                            WeatherUiState(isLoading = true, error = null)
                        }
                        when (val result = repository
                            .getWeatherData(latitude, longitude)) {
                            is Resource.Success -> {
                                _uiState.update {
                                    WeatherUiState(
                                        weatherInfo = result.data,
                                        isLoading = false,
                                        error = null
                                    )
                                }
                            }
                            is Resource.Error -> {
                                _uiState.update {
                                    WeatherUiState(
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
    }

    fun writeLocationToPreferences(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            preferences.saveLocationPreference(latitude, longitude)
            _uiState.update { WeatherUiState(latitude = latitude, longitude = longitude) }

            loadWeatherInfo()
        }
    }
}