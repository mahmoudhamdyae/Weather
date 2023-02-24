package com.mahmoudhamdyae.weather.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mahmoudhamdyae.weather.R
import com.mahmoudhamdyae.weather.data.WeatherUiState
import com.mahmoudhamdyae.weather.ui.theme.DarkBlue
import com.mahmoudhamdyae.weather.ui.theme.DeepBlue

@Composable
fun MainScreen(
    uiState: WeatherUiState,
    isPermissionGranted: Boolean,
    grantPermission: () -> Unit,
    isGpsEnabled: Boolean,
    getLastLocation: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBlue)
        ) {
            WeatherCard(
                weatherInfo = uiState.weatherInfo,
                backgroundColor = DeepBlue
            )
            Spacer(modifier = Modifier.height(16.dp))
            WeatherForecast(weatherInfo = uiState.weatherInfo)
        }
        if (uiState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        }
        uiState.error?.let { error ->
            ErrorMessage(
                error = error,
                isPermissionGranted = isPermissionGranted,
                grantPermission = { grantPermission() },
                isGpsEnabled = isGpsEnabled,
                getLastLocation = { getLastLocation() }
            )
        }
    }
}

@Composable
fun ErrorMessage(
    error: String,
    isPermissionGranted: Boolean,
    grantPermission: () -> Unit,
    isGpsEnabled: Boolean,
    getLastLocation: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxSize()
    ) {
        Text(
            text = error,
            color = Color.Red,
            textAlign = TextAlign.Center
        )
        Button(onClick = {
            if (!isPermissionGranted) {
                grantPermission()
            } else if (!isGpsEnabled) {
                getLastLocation()
            }
        }) {
            if (!isPermissionGranted) {
                Text(text = stringResource(id = R.string.button_text_permission))
            } else if (!isGpsEnabled) {
                Text(text = stringResource(id = R.string.button_text_gps))
            } else {
                getLastLocation()
            }
        }
    }
}