package com.mahmoudhamdyae.weather.ui

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.mahmoudhamdyae.weather.ui.viewmodels.WeatherViewModel

@Composable
fun MapsScreen(
    context: Context,
    viewModel: WeatherViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState = viewModel.state

    viewModel.readLocationFromPreferencesAndLoad()

    var myLocation =
        LatLng(uiState.latitude ?: 31.045162, uiState.longitude ?: 31.399642)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(myLocation, 1f)
    }

    GoogleMap(
        modifier = modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        onMapClick = {
            viewModel.writeLocationAndLoad(it.latitude, it.longitude)
            myLocation = LatLng(it.latitude, it.longitude)
            Toast.makeText(context, "Location Updated", Toast.LENGTH_SHORT).show()
        }
    ) {
        if (uiState.latitude != null) {
            Marker(
                state = MarkerState(position = myLocation),
                title = "My Last Location",
                snippet = "Marker in my last location"
            )
        }
    }
}