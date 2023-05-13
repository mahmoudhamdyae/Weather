package com.mahmoudhamdyae.weather

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
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
    modifier: Modifier = Modifier,
    viewModel: WeatherViewModel = viewModel(),
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current
    val activityResultLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            // Handle Permission
            if (isGranted) {
                // Permission is granted
                getLastLocation(context, viewModel::writeLocationToPreferences)
            } else {
                // Permission is denied
                Toast.makeText(context, R.string.permission_denied_toast, Toast.LENGTH_SHORT).show()
            }
        }

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
                onMenuGetMyLocationItemClickListener = {
                    getLastLocation(context, viewModel::writeLocationToPreferences, activityResultLauncher)
                                                       },
                onMenuNavigateToMapItemClickListener = {
                    navController.navigate(WeatherScreen.MAPS.name)
                }
            )
        }
    ) { innerPadding ->

        val uiState by viewModel.uiState.collectAsStateWithLifecycle()

        NavHost(
            navController = navController,
            startDestination = WeatherScreen.Start.name,
            modifier = modifier.padding(innerPadding)
        ) {
            composable(route = WeatherScreen.Start.name) {
                MainScreen(
                    uiState = uiState,
                    isPermissionGranted = isPermissionGranted(context),
                    grantPermission = { grantPermission(activityResultLauncher) },
                    isGpsEnabled = isGpsEnabled(context),
                    getLastLocation = {
                        getLastLocation(context, viewModel::writeLocationToPreferences, activityResultLauncher)
                    }
                )
            }
            composable(route = WeatherScreen.MAPS.name) {
                MapsScreen(
                    uiState = uiState,
                    onMapClicked = viewModel::writeLocationToPreferences
                )
            }
        }
    }
}

fun isPermissionGranted(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
}

fun isGpsEnabled(context: Context): Boolean {
    val locationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    return locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) ||
            locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
}

private fun grantPermission(activityResultLauncher: ManagedActivityResultLauncher<String, Boolean>) {
    activityResultLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
}

private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
@SuppressLint("MissingPermission")
private fun getLastLocation(
    context: Context,
    writeLocationToPreferences: (Double, Double) -> Unit,
    activityResultLauncher: ManagedActivityResultLauncher<String, Boolean>? = null
) {
    fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
    if (isPermissionGranted(context)) {
        if (isGpsEnabled(context)) {
            fusedLocationProviderClient.lastLocation.addOnCompleteListener { task ->
                val location: Location? = task.result
                if (location == null) {
                    requestNewLocationData(context, writeLocationToPreferences)
                } else {
                    writeLocationToPreferences(location.latitude, location.longitude)
                }
            }
        } else {
            Toast.makeText(context, R.string.turn_on_location_toast, Toast.LENGTH_LONG).show()
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            context.startActivity(intent)
        }
    } else {
        if (activityResultLauncher != null) {
            grantPermission(activityResultLauncher)
        }
    }
}

@SuppressLint("MissingPermission")
@Suppress("DEPRECATION")
private fun requestNewLocationData(context: Context, writeLocationToPreferences: (Double, Double) -> Unit,) {
    val mLocationRequest = LocationRequest()
    mLocationRequest.apply {
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        interval = 0
        fastestInterval = 0
        numUpdates = 1
    }

    fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
    fusedLocationProviderClient.requestLocationUpdates(
        mLocationRequest, object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val mLastLocation: Location = locationResult.lastLocation!!
                writeLocationToPreferences(mLastLocation.latitude, mLastLocation.longitude)
            }
        },
        Looper.myLooper()
    )
}
