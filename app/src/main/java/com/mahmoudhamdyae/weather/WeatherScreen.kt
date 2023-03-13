package com.mahmoudhamdyae.weather

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mahmoudhamdyae.weather.ui.MainScreen
import com.mahmoudhamdyae.weather.ui.MapsScreen
import com.mahmoudhamdyae.weather.ui.viewmodels.WeatherViewModel

enum class WeatherScreen(@StringRes val title: Int) {
    Start(title = R.string.app_name),
    MAPS(title = R.string.maps_title)
}

@Composable
fun WeatherAppBar(
    currentScreen: WeatherScreen,
    canNavigateBack: Boolean,
    navigateUp: () -> Unit,
    onMenuGetMyLocationItemClickListener: () -> Unit,
    onMenuNavigateToMapItemClickListener: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    TopAppBar(
        title = { Text(stringResource(id = currentScreen.title)) },
        modifier = modifier,
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = navigateUp) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back_button)
                    )
                }
            }
        },
        actions = {
            if (!canNavigateBack) {
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
                    DropdownMenuItem(onClick = {
                        onMenuGetMyLocationItemClickListener()
                        showMenu = false
                    }) {
                        Text(stringResource(id = R.string.menu_get_my_location))
                    }
                    // On the main screen
                    DropdownMenuItem(onClick = {
                        onMenuNavigateToMapItemClickListener()
                        showMenu = false
                    }) {
                        Text(stringResource(id = R.string.menu_get_location_from_map))
                    }
                }
            }
        }
    )
}

@Composable
fun WeatherScreen(
    isPermissionGranted: Boolean,
    grantPermission: () -> Unit,
    isGpsEnabled: Boolean,
    getLastLocation: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WeatherViewModel = viewModel(),
    navController: NavHostController = rememberNavController()
) {
    // Get current back stack entry
    val backStackEntry by navController.currentBackStackEntryAsState()

    // Get the name of the current screen
    val currentScreen = WeatherScreen.valueOf(
        backStackEntry?.destination?.route ?: WeatherScreen.Start.name
    )

    Scaffold(
        topBar = {
            WeatherAppBar(
                currentScreen = currentScreen,
                canNavigateBack = navController.previousBackStackEntry != null,
                navigateUp = { navController.navigateUp() },
                onMenuGetMyLocationItemClickListener = { getLastLocation() },
                onMenuNavigateToMapItemClickListener = {
                    navController.navigate(WeatherScreen.MAPS.name)
                }
            )
        }
    ) { innerPadding ->
        val uiState = viewModel.uiState
        NavHost(
            navController = navController,
            startDestination = WeatherScreen.Start.name,
            modifier = modifier.padding(innerPadding)
        ) {
            composable(route = WeatherScreen.Start.name) {
                MainScreen(
                    uiState = uiState,
                    isPermissionGranted = isPermissionGranted,
                    grantPermission = { grantPermission() },
                    isGpsEnabled = isGpsEnabled,
                    getLastLocation = { getLastLocation() }
                )
            }
            composable(route = WeatherScreen.MAPS.name) {
                MapsScreen(
                    context = LocalContext.current,
                    uiState = uiState,
                    onMapClicked = viewModel::writeLocationToPreferences
                )
            }
        }
    }
}