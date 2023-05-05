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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ContentAlpha
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.isContainer
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
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
    locationDisplay: MutableState<String>,
    searchInput: MutableState<String>
) {
    val marker = remember { mutableStateOf<MarkerOptions?>(null) }
    val cameraPositionState = rememberCameraPositionState {
        position = defaultCameraPosition
    }
    val searchQuery = remember { mutableStateOf("") }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val cameraUpdate = remember { mutableStateOf<CameraUpdate?>(null) }
    val showAutocompleteResults = remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val interactionSource = remember { MutableInteractionSource() }
    val isUserInput = remember { mutableStateOf(true) }

    LaunchedEffect(searchQuery.value) {
        if (searchQuery.value.isNotEmpty() && isUserInput.value) {
            showAutocompleteResults.value = true
        }
//        else {
//            coroutineScope.launch {
//                delay(300L)
//                if (searchQuery.value.isEmpty()) {
//                    showAutocompleteResults.value = false
//                }
//            }
//        }
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

            Box(
                modifier = Modifier
//                    .align(Alignment.TopStart).semantics { isContainer = true }.zIndex(1f)
                    .fillMaxWidth()
            ) {
                SearchBar(
                    query = searchQuery.value,
                    onQueryChange = { newValue ->
                        isUserInput.value = true
                        searchQuery.value = newValue
                        searchInput.value = newValue
                    },
                    colors = SearchBarDefaults.colors(
                        containerColor = Color.Black,
                        dividerColor = Color.White,
                        inputFieldColors = TextFieldDefaults.colors(
                            disabledTextColor = Color.Black,
                            errorTextColor = Color.Black,
                            cursorColor = Color.White,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                            errorIndicatorColor = Color.Transparent,
                            focusedLabelColor = Color.Black,
                            unfocusedLabelColor = Color.Black,
                            errorLabelColor = Color.Black,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                        )
                    ),
                    onSearch = {
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
                    },
                    active = showAutocompleteResults.value,
                    onActiveChange = { isActive ->
                        showAutocompleteResults.value = isActive
                    },
                    modifier = Modifier.align(Alignment.TopCenter),

                    leadingIcon = {
                        //when clicked on the searchbar change icon from search to back arrow and the back arrow closes the searchbar
                        if (showAutocompleteResults.value) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Search location",
                                tint = Color.White.copy(alpha = ContentAlpha.medium),
                                modifier = Modifier
                                    .clickable(
                                        interactionSource = interactionSource,
                                        indication = null
                                    ) {
                                        showAutocompleteResults.value = false
                                        focusManager.clearFocus()
                                        keyboardController?.hide()
                                    }
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search location",
                                tint = Color.White.copy(alpha = ContentAlpha.medium))
                        }
                    },
                    trailingIcon = {
                        if (searchQuery.value.isNotEmpty()) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear search",
                                tint = Color.White.copy(alpha = ContentAlpha.medium),
                                modifier = Modifier
                                    .clickable(
                                        interactionSource = interactionSource,
                                        indication = null
                                    ) {
                                        searchQuery.value = ""
                                    }
                            )
                        }
                    },
                    placeholder = { Text("Search location") },
                    content = {
                        PlaceAutocomplete(
                            query = searchQuery.value,
                            context = context,
                            onItemClick = { place ->
                                place.latLng?.let { latLng ->
                                    onLocationSelected(latLng)
                                    coroutineScope.launch {
                                        Log.d(
                                            "LocationPicker",
                                            "Animating camera position to $latLng"
                                        )
                                        cameraPositionState.animate(
                                            CameraUpdateFactory.newLatLngZoom(
                                                latLng,
                                                15f
                                            )
                                        )
                                    }
                                    locationDisplay.value =
                                        place.address ?: "${latLng.latitude}, ${latLng.longitude}"
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
                )
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
                FetchPlaceRequest.newInstance(
                    placeId,
                    listOf(Place.Field.LAT_LNG, Place.Field.ADDRESS)
                )
            ).addOnSuccessListener { placeResponse ->
                placeResponse.place.latLng?.let { latLng ->
                    Log.d("searchLocation", "Fetched place details: $latLng")
                    coroutineScope.launch {
                        cameraUpdate.value = CameraUpdateFactory.newLatLngZoom(latLng, 15f)
                    }
                    onLocationSelected(latLng)
                    onLocationNameSet(
                        placeResponse.place.address ?: "${latLng.latitude}, ${latLng.longitude}"
                    )
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
        LazyColumn {
            items(predictions.value) { prediction ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 16.dp, start = 10.dp, end = 10.dp)
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
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Place,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .background(Color.Gray, shape = CircleShape)
                            .padding(7.dp)
                            .size(24.dp)
                    )

                    Text(
                        text = prediction.getFullText(null).toString(),
                        color = Color.White,
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                }

                Divider(
                    color = Color.Gray,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp)
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

