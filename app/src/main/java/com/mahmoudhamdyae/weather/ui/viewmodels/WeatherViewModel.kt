package com.mahmoudhamdyae.weather.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mahmoudhamdyae.weather.data.WeatherUiState
import com.mahmoudhamdyae.weather.data.repository.LocationPreferencesRepository
import com.mahmoudhamdyae.weather.domain.repository.WeatherRepository
import com.mahmoudhamdyae.weather.domain.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val repository: WeatherRepository,
    private val dataStore: DataStore<Preferences>
): ViewModel() {

    var state by mutableStateOf(WeatherUiState())
        private set

    private fun loadWeatherInfo(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            state = state.copy(
                isLoading = true,
                error = null
            )
            when (val result = repository.getWeatherData(latitude, longitude)) {
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

    fun writeLocationAndLoad(latitude: Double, longitude: Double) {
        writeLocationToPreferences(latitude, longitude)
        loadWeatherInfo(latitude, longitude)
    }

    private fun writeLocationToPreferences(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            LocationPreferencesRepository(dataStore).saveLocation(latitude, longitude)
        }
    }

    fun readLocationFromPreferencesAndLoad() {
        val latitudeFlow = LocationPreferencesRepository(dataStore).readLatitude
        val longitudeFlow = LocationPreferencesRepository(dataStore).readLongitude
        viewModelScope.launch {
            state = state.copy(
                isLoading = true,
                error = null
            )
            latitudeFlow.collect { latitude ->
                longitudeFlow.collect { longitude ->
                    if (latitude != null && longitude != null) {
                        state = state.copy(
                            latitude = latitude,
                            longitude = longitude
                        )
                        loadWeatherInfo(latitude, longitude)
                    } else {
                        state = state.copy(
                            isLoading = false,
                            error =
                            "Couldn't retrieve location. Make sure to grant permission and enable GPS"
                        )
                    }
                }
            }
        }
    }
}