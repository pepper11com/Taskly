package com.example.new_app.screens.map

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ContentAlpha
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.maps.android.compose.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay


@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun LocationPicker(
    modifier: Modifier = Modifier,
    onLocationSelected: (LatLng) -> Unit,
    onLocationNameSet: (String) -> Unit,
    showMapAndSearch: MutableState<Boolean>,
    locationDisplay: MutableState<String>,
) {
    val marker = remember { mutableStateOf<MarkerOptions?>(null) }
    val cameraPositionState = rememberCameraPositionState {
        position = defaultCameraPosition
    }
    val searchQuery = remember { mutableStateOf("") }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val cameraUpdate = remember { mutableStateOf<CameraUpdate?>(null) }
    val showAutocompleteResults = remember { mutableStateOf(true) }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val interactionSource = remember { MutableInteractionSource() }
    val isUserInput = remember { mutableStateOf(true) }

    LaunchedEffect(searchQuery.value) {
        if (searchQuery.value.isNotEmpty() && isUserInput.value) {
            showAutocompleteResults.value = true
        } else {
            coroutineScope.launch {
                delay(300L)
                if (searchQuery.value.isEmpty()) {
                    showAutocompleteResults.value = false
                }
            }
        }
    }

    Column(
        modifier = modifier
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            GoogleMapView(
                cameraPositionState = cameraPositionState,
                onMapLongClick = { latLng ->
                    val newMarker = MarkerOptions()
                        .position(latLng)
                        .icon(BitmapDescriptorFactory.defaultMarker())

                    marker.value = newMarker
                    onLocationSelected(latLng)
                    locationDisplay.value = "${latLng.latitude}, ${latLng.longitude}"
                    onLocationNameSet(locationDisplay.value)
                },
                marker = marker.value,
                cameraUpdate = cameraUpdate.value
            )

            Box(modifier = Modifier.align(Alignment.TopStart)) {
                TextField(
                    interactionSource = interactionSource,
                    value = searchQuery.value,
                    onValueChange = { newValue ->
                        isUserInput.value = true
                        searchQuery.value = newValue
                    },
                    label = { Text("Search location") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .background(
                            color = Color(0xA6F2F2F2),
                            shape = RoundedCornerShape(4.dp)
                        ),
                    colors = TextFieldDefaults.colors(
//                        focusedBorderColor = MaterialTheme.colorScheme.secondary,

                        focusedLabelColor = MaterialTheme.colorScheme.secondary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.secondary,
//                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = ContentAlpha.disabled),
//                        textColor = Color.Black
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = {
                        searchLocation(
                            searchQuery.value,
                            cameraPositionState,
                            context,
                            onLocationNotFound = {
                                // Handle location not found, e.g., show a message
                            },
                            coroutineScope = coroutineScope,
                            cameraUpdate = cameraUpdate,
                            onLocationNameSet = { locationName ->
                                onLocationNameSet(locationName)
                            },
                            onLocationSelected = onLocationSelected
                        )
                        showAutocompleteResults.value = false
                    }),
                    singleLine = true
                )

                if (searchQuery.value.isNotEmpty() && showAutocompleteResults.value) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(top = 72.dp),
                        shape = RoundedCornerShape(4.dp),
                        color = Color.Black.copy(alpha = 0.8f),
//                        elevation = 4.dp
                    ) {
                        PlaceAutocomplete(
                            query = searchQuery.value,
                            context = context,
                            onItemClick = { place ->
                                place.latLng?.let { latLng ->
                                    onLocationSelected(latLng)
                                    coroutineScope.launch {
                                        Log.d("LocationPicker", "Animating camera position to $latLng")
                                        cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                                    }
                                    locationDisplay.value = place.address ?: "${latLng.latitude}, ${latLng.longitude}"
                                    onLocationNameSet(locationDisplay.value)

                                }
                                isUserInput.value = false
                                searchQuery.value = place.address ?: ""
                                keyboardController?.hide()
                                focusManager.clearFocus()
                                showAutocompleteResults.value = false
                            },
                            cameraUpdate = cameraUpdate,
                            coroutineScope = coroutineScope,
                        )
                    }
                }
            }
        }
    }
}



fun searchLocation(
    query: String,
    cameraPositionState: CameraPositionState,
    context: Context,
    onLocationNotFound: () -> Unit,
    coroutineScope: CoroutineScope,
    cameraUpdate: MutableState<CameraUpdate?>,
    onLocationNameSet: (String) -> Unit,
    onLocationSelected: (LatLng) -> Unit,
) {
    Log.d("searchLocation", "Searching for location: $query")

    val placesClient = Places.createClient(context)
    val request = FindAutocompletePredictionsRequest.builder()
        .setQuery(query)
        .build()

    placesClient.findAutocompletePredictions(request).addOnSuccessListener { response ->
        Log.d("searchLocation", "Received autocomplete predictions")

        if (response.autocompletePredictions.isNotEmpty()) {
            val placeId = response.autocompletePredictions[0].placeId
            placesClient.fetchPlace(
                FetchPlaceRequest.newInstance(placeId, listOf(Place.Field.LAT_LNG, Place.Field.ADDRESS))
            ).addOnSuccessListener { placeResponse ->
                placeResponse.place.latLng?.let { latLng ->
                    Log.d("searchLocation", "Fetched place details: $latLng")
                    coroutineScope.launch {
                        cameraUpdate.value = CameraUpdateFactory.newLatLngZoom(latLng, 15f)
                    }
                    onLocationSelected(latLng)
                    onLocationNameSet(placeResponse.place.address ?: "${latLng.latitude}, ${latLng.longitude}")
                }
            }.addOnFailureListener { exception ->
                Log.e("searchLocation", "Failed to fetch place details", exception)
                onLocationNotFound()
            }
        } else {
            Log.w("searchLocation", "No autocomplete predictions found")
            onLocationNotFound()
        }
    }.addOnFailureListener { exception ->
        Log.e("searchLocation", "Failed to find autocomplete predictions", exception)
        onLocationNotFound()
    }
}


@Composable
fun PlaceAutocomplete(
    query: String,
    context: Context,
    onItemClick: (Place) -> Unit,
    cameraUpdate: MutableState<CameraUpdate?>,
    coroutineScope: CoroutineScope,
) {
    val predictions = remember { mutableStateOf<List<AutocompletePrediction>>(emptyList()) }
    val placesClient = Places.createClient(context)

    LaunchedEffect(query) {
        if (query.isNotEmpty()) {
            val request = FindAutocompletePredictionsRequest.builder()
                .setQuery(query)
                .build()

            placesClient.findAutocompletePredictions(request).addOnSuccessListener { response ->
                predictions.value = response.autocompletePredictions
            }.addOnFailureListener { exception ->
                Log.e("PlaceAutocomplete", "Failed to find autocomplete predictions", exception)
            }
        } else {
            predictions.value = emptyList()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        LazyColumn(
            modifier = Modifier
                .background(Color.White),
        ) {
            items(predictions.value) { prediction ->
                Text(
                    text = prediction.getFullText(null).toString(),
                    color = Color.Black,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            placesClient
                                .fetchPlace(
                                    FetchPlaceRequest.newInstance(
                                        prediction.placeId,
                                        listOf(Place.Field.LAT_LNG, Place.Field.ADDRESS)
                                    )
                                )
                                .addOnSuccessListener { placeResponse ->
                                    placeResponse.place.latLng?.let { latLng ->
                                        Log.d("PlaceAutocomplete", "Fetched place details: $latLng")
                                        onItemClick(placeResponse.place)
                                        coroutineScope.launch {
                                            cameraUpdate.value =
                                                CameraUpdateFactory.newLatLngZoom(latLng, 15f)
                                        }
                                    }

                                }
                                .addOnFailureListener { exception ->
                                    Log.e(
                                        "PlaceAutocomplete",
                                        "Failed to fetch place details",
                                        exception
                                    )
                                }
                        }
                        .padding(16.dp)
                )
            }
        }
    }
}


@Composable
fun GoogleMapView(
    modifier: Modifier = Modifier,
    cameraPositionState: CameraPositionState = rememberCameraPositionState(),
    onMapLongClick: (LatLng) -> Unit = {},
    marker: MarkerOptions? = null,
    cameraUpdate: CameraUpdate? = null
) {
    val googleMap = remember { mutableStateOf<GoogleMap?>(null) }
    val markerState = remember { mutableStateOf<Marker?>(null) }

    LaunchedEffect(marker) {
        marker?.let {
            googleMap.value?.let { map ->
                markerState.value?.remove()
                markerState.value = map.addMarker(marker)
            }
        }
    }
    LaunchedEffect(cameraUpdate) {
        cameraUpdate?.let { update ->
            googleMap.value?.animateCamera(update)
        }
    }


    AndroidView(
        modifier = modifier,
        factory = { context ->
            MapView(context).apply {
                onCreate(Bundle())
                onResume()
                getMapAsync { map ->
                    googleMap.value = map
                    map.setOnMapLongClickListener(onMapLongClick)

                    marker?.let { markerOptions ->
                        markerState.value?.remove()
                        markerState.value = map.addMarker(markerOptions)
                    }
                }
            }
        },
        update = { mapView ->
            mapView.getMapAsync { map ->
                marker?.let { markerOptions ->
                    markerState.value?.remove()
                    markerState.value = map.addMarker(markerOptions)
                }
            }
        }
    )
}
val singapore = LatLng(1.35, 103.87)
val defaultCameraPosition = CameraPosition.fromLatLngZoom(singapore, 11f)

