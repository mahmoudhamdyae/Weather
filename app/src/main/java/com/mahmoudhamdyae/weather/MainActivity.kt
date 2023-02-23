package com.mahmoudhamdyae.weather

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
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
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.mahmoudhamdyae.weather.ui.WeatherCard
import com.mahmoudhamdyae.weather.ui.WeatherForecast
import com.mahmoudhamdyae.weather.ui.WeatherViewModel
import com.mahmoudhamdyae.weather.ui.theme.DarkBlue
import com.mahmoudhamdyae.weather.ui.theme.DeepBlue
import com.mahmoudhamdyae.weather.ui.theme.WeatherTheme
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: WeatherViewModel by viewModels()
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private val activityResultLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            // Handle Permission
            if (isGranted) {
                // Permission is granted
                getLastLocation()
            } else {
                // Permission is denied
                Toast.makeText(this, R.string.permission_denied_toast, Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        viewModel.readLocationFromPreferences()

        setContent {
            WeatherTheme {
                Box(
                    modifier = Modifier.fillMaxSize()
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
                        ErrorMessage(error)
                    }
                }
            }
        }
    }

    @Composable
    fun ErrorMessage(error: String, modifier: Modifier = Modifier) {
        Column(verticalArrangement = Arrangement.Center, modifier = modifier) {
            Text(
                text = error,
                color = Color.Red,
                textAlign = TextAlign.Center
            )
            Button(onClick = {
                if (!isPermissionGranted()) {
                    grantPermission()
                } else if (!isGpsEnabled()) {
                    getLastLocation()
                }
            }) {
                if (!isPermissionGranted()) {
                    Text(text = stringResource(id = R.string.button_text_permission))
                } else if (!isGpsEnabled()) {
                    Text(text = stringResource(id = R.string.button_text_gps))
                } else {
                    getLastLocation()
                }
            }
        }
    }

    private fun isPermissionGranted() : Boolean {
        return ContextCompat.checkSelfPermission(
            applicationContext,
            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun grantPermission() {
        activityResultLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    private fun isGpsEnabled(): Boolean {
        val locationManager = application.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    @Suppress("DEPRECATION")
    private fun requestNewLocationData() {
        val mLocationRequest = LocationRequest()
        mLocationRequest.apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 0
            fastestInterval = 0
            numUpdates = 1
        }

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationProviderClient.requestLocationUpdates(
            mLocationRequest, mLocationCallback,
            Looper.myLooper()
        )
    }

    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val mLastLocation: Location? = locationResult.lastLocation
            viewModel.writeLocationAndLoad(mLastLocation?.latitude, mLastLocation?.longitude)
        }
    }

    private fun getLastLocation() {
        if (isGpsEnabled()) {
            fusedLocationProviderClient.lastLocation.addOnCompleteListener(this) { task ->
                val location: Location? = task.result
                if (location == null) {
                    requestNewLocationData()
                } else {
                    viewModel.writeLocationAndLoad(location.latitude, location.longitude)
                }
            }
        } else {
            Toast.makeText(this, R.string.turn_on_location_toast, Toast.LENGTH_LONG).show()
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.readLocationFromPreferences()
    }
}