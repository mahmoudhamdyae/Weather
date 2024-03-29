package com.mahmoudhamdyae.weather.ui

import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.mahmoudhamdyae.weather.ui.viewmodels.WeatherUiState

@Composable
fun MapsScreen(
    uiState: WeatherUiState,
    onMapClicked: (Double, Double)-> Unit,
    modifier: Modifier = Modifier,
) {

    val context = LocalContext.current

    var myLocation by rememberSaveable {
        mutableStateOf(LatLng(uiState.latitude ?: 31.045162, uiState.longitude ?: 31.399642))
    }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(myLocation, 1f)
    }

    GoogleMap(
        modifier = modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        onMapClick = {
            onMapClicked(it.latitude, it.longitude)
            myLocation = LatLng(it.latitude, it.longitude)
            Toast.makeText(context, "Location Updated", Toast.LENGTH_SHORT).show()
        }
    ) {
        if (myLocation.latitude != 31.045162) {
            Marker(
                state = MarkerState(position = myLocation),
                title = "My Last Location",
                snippet = "Marker in my last location"
            )
        }
    }
}