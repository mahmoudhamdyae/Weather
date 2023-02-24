package com.mahmoudhamdyae.weather.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mahmoudhamdyae.weather.R
import com.mahmoudhamdyae.weather.ui.theme.DarkBlue
import com.mahmoudhamdyae.weather.ui.theme.DeepBlue
import com.mahmoudhamdyae.weather.ui.viewmodels.WeatherViewModel

@Composable
fun WeatherAppBar(
    onMenuGetMyLocationItemClickListener: () -> Unit,
    onMenuNavigateToMapItemClickListener: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    TopAppBar(
        title = { Text(stringResource(id = R.string.app_name)) },
        modifier = modifier,
        actions = {
            IconButton(onClick = { showMenu = !showMenu }) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = "More",
                )
            }
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(onClick = onMenuGetMyLocationItemClickListener) {
                    Text(stringResource(id = R.string.menu_get_my_location))
                }
                DropdownMenuItem(onClick = onMenuNavigateToMapItemClickListener) {
                    Text(stringResource(id = R.string.menu_get_location_from_map))
                }
            }
        }
    )
}

@Composable
fun WeatherScreen(
    onMenuGetMyLocationItemClickListener: () -> Unit,
    onMenuNavigateToMapItemClickListener: () -> Unit,
    isPermissionGranted: Boolean,
    grantPermission: () -> Unit,
    isGpsEnabled: Boolean,
    getLastLocation: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WeatherViewModel = viewModel()
) {
    Scaffold(
        topBar = {
            WeatherAppBar(
                onMenuGetMyLocationItemClickListener = { onMenuGetMyLocationItemClickListener() },
                onMenuNavigateToMapItemClickListener = { onMenuNavigateToMapItemClickListener() }
    ) }
    ) {
        Box(
            modifier = modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(DarkBlue)
            ) {
                WeatherCard(
                    state = viewModel.state,
                    backgroundColor = DeepBlue
                )
                Spacer(modifier = Modifier.height(16.dp))
                WeatherForecast(state = viewModel.state)
            }
            if (viewModel.state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            viewModel.state.error?.let { error ->
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