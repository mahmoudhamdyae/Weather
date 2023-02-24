package com.mahmoudhamdyae.weather.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val LATITUDE_KEY = doublePreferencesKey("LATITUDE")
private val LONGITUDE_KEY = doublePreferencesKey("LONGITUDE")

class LocationPreferencesRepository(
    private val dataStore: DataStore<Preferences>
) {

    val readLatitude: Flow<Double?> = dataStore.data
        .map { preferences ->
            preferences[LATITUDE_KEY]
        }

    val readLongitude: Flow<Double?> = dataStore.data
        .map { preferences ->
            preferences[LONGITUDE_KEY]
        }

    suspend fun saveLocation(latitude: Double, longitude: Double) {
        dataStore.edit { preferences ->
            preferences[LATITUDE_KEY] = latitude
            preferences[LONGITUDE_KEY] = longitude
        }
    }
}