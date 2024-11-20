package com.thingstodo

import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.thingstodo.model.SearchViewModel

@Preview
@Composable
fun MapScreenPreview() {
    MapScreen()
}

@Composable
fun MapScreen(searchViewModel: SearchViewModel = viewModel()) {
    val context = LocalContext.current

    val userLocation by searchViewModel.userLocation.collectAsState()
    val searchState by searchViewModel.searchQuery.collectAsState()
    val placesOfInterest by searchViewModel.placesOfInterest.collectAsState()
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    UserLocationRequest(fusedLocationClient, searchViewModel::updateUserLocation, searchViewModel::updatePlacesOfInterest)
    Map(userLocation, placesOfInterest, fusedLocationClient)
}

@Composable
fun UserLocationRequest(
    fusedLocationClient: FusedLocationProviderClient,
    updateUserLocation: (LatLng) -> Unit,
    updatePlacesOfInterest: (LatLng, PlacesClient) -> Unit
) {
    val context = LocalContext.current

    val onSuccessListener = OnSuccessListener<Location> {
        it?.let {
            // Update the user's location in the state
            val userLatLng = LatLng(it.latitude, it.longitude)
            updateUserLocation(userLatLng)
            updatePlacesOfInterest(userLatLng, Places.createClient(context))
        }
    }

    // Handle permission requests for accessing fine location
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Fetch the user's location and update the camera if permission is granted
            fetchUserLocation(context, fusedLocationClient, onSuccessListener)
        } else {
            // Handle the case when permission is denied
            Toast.makeText(context, "Location permission is not granted.", Toast.LENGTH_LONG).show()
        }
    }

    // Request the location permission when the composable is launched
    LaunchedEffect(Unit) {
        when (PackageManager.PERMISSION_GRANTED) {
            // Check if the location permission is already granted
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) -> {
                // Fetch the user's location and update the camera
                fetchUserLocation(context, fusedLocationClient, onSuccessListener)
            }
            else -> {
                // Request the location permission if it has not been granted
                permissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }
}

@Composable
fun Map(
    userLocation: LatLng,
    placesOfInterest: List<Place>,
    fusedLocationClient: FusedLocationProviderClient
) {
    val cameraPositionState = rememberCameraPositionState()

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
    ) {
        userLocation.let {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(it, 15f)
        }

        placesOfInterest.forEach {
            val location = it.location
            val title = it.displayName

            if (location != null && title != null) {
                Marker(
                    state = MarkerState(location),
                    title = it.displayName
                )
            }
        }
    }
}

private fun fetchUserLocation(
    context: Context,
    fusedLocationClient: FusedLocationProviderClient,
    onSuccessListener: OnSuccessListener<Location>
) {
    // Check if the location permission is granted
    if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
        try {
            // Fetch the last known location
            fusedLocationClient.lastLocation.addOnSuccessListener(onSuccessListener)
        } catch (e: SecurityException) {
            Toast.makeText(context, "Permission for location access was revoked: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    } else {
        Toast.makeText(context, "Location permission is not granted.", Toast.LENGTH_LONG).show()
    }
}