package com.mahmoudhamdyae.weather.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun MapsScreen(
    modifier: Modifier = Modifier
) {
    val singapore = LatLng(1.35, 103.87)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(singapore, 10f)
    }
    GoogleMap(
        modifier = modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState
    ) {
        Marker(
            state = MarkerState(position = singapore),
            title = "Singapore",
            snippet = "Marker in Singapore"
        )
    }

//    var uiSettings by remember { mutableStateOf(MapUiSettings()) }
//var properties by remember {
//  mutableStateOf(MapProperties(mapType = MapType.SATELLITE))
//}
//
//Box(Modifier.fillMaxSize()) {
//  GoogleMap(
//    modifier = Modifier.matchParentSize(),
//    properties = properties,
//    uiSettings = uiSettings
//  )
//  Switch(
//    checked = uiSettings.zoomControlsEnabled,
//    onCheckedChange = {
//      uiSettings = uiSettings.copy(zoomControlsEnabled = it)
//    }
//  )
//}
}