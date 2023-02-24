package com.mahmoudhamdyae.weather.ui.viewmodels

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mahmoudhamdyae.weather.data.WeatherUiState
import com.mahmoudhamdyae.weather.data.repository.LocationPreferencesRepository
import com.mahmoudhamdyae.weather.domain.repository.WeatherRepository
import com.mahmoudhamdyae.weather.domain.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val repository: WeatherRepository,
    private val dataStore: DataStore<Preferences>
): ViewModel() {

//    var _uiState by mutableStateOf(WeatherUiState())
//        private set

    private val _uiState = MutableStateFlow(WeatherUiState())
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    private fun loadWeatherInfo(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            _uiState.update { currentState ->
                currentState.copy(
                    isLoading = true,
                    error = null
                )
            }
            when (val result = repository.getWeatherData(latitude, longitude)) {
                is Resource.Success -> {
                    _uiState.update { currentState ->
                        currentState.copy(
                            weatherInfo = result.data,
                            isLoading = false,
                            error = null
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update { currentState ->
                        currentState.copy(
                            weatherInfo = null,
                            isLoading = false,
                            error = result.message
                        )
                    }
                }
            }
        }
    }

    fun writeLocationAndLoad(latitude: Double? = null, longitude: Double? = null) {
        writeLocationToPreferences(latitude ?: 0.0, longitude ?: 0.0)
        loadWeatherInfo(latitude ?: 0.0, longitude ?: 0.0)
    }

    private fun writeLocationToPreferences(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            LocationPreferencesRepository(dataStore).saveLocation(latitude, longitude)
        }
    }

    fun readLocationFromPreferences() {
        val latitudeFlow = LocationPreferencesRepository(dataStore).readLatitude
        val longitudeFlow = LocationPreferencesRepository(dataStore).readLongitude
        viewModelScope.launch {
            _uiState.update { currentState ->
                currentState.copy(
                    isLoading = true,
                    error = null
                )
            }
            latitudeFlow.collect { latitude ->
                longitudeFlow.collect { longitude ->
                    if (latitude != 0.0 && longitude != 0.0) {
                        loadWeatherInfo(latitude, longitude)
                    } else {
                        _uiState.update { currentState ->
                            currentState.copy(
                                isLoading = false,
                                error =
                                "Couldn't retrieve location. Make sure to grant permission and enable GPS",
                            )
                        }
                    }
                }
            }
        }
    }
}