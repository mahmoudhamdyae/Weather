package com.mahmoudhamdyae.weather.data.repository

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

class LocationPreferencesRepository(
    private val dataStore: DataStore<Preferences>
) {

    private companion object {
        val LATITUDE_KEY = doublePreferencesKey("LATITUDE")
        val LONGITUDE_KEY = doublePreferencesKey("LONGITUDE")
        const val TAG = "LocationPreferencesRepo"
    }

    val readLatitude: Flow<Double?> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading latitude preferences.", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map { preferences ->
            preferences[LATITUDE_KEY]
        }

    val readLongitude: Flow<Double?> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading longitude preferences.", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map { preferences ->
            preferences[LONGITUDE_KEY]
        }

    suspend fun saveLocationPreference(latitude: Double, longitude: Double) {
        dataStore.edit { preferences ->
            preferences[LATITUDE_KEY] = latitude
            preferences[LONGITUDE_KEY] = longitude
        }
    }
}