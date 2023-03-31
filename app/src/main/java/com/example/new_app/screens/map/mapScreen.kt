package com.example.new_app.screens.map

import android.content.ContentValues.TAG
import android.content.Context
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
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


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun LocationPicker(
    modifier: Modifier = Modifier,
    onLocationSelected: (LatLng) -> Unit,
    showMapAndSearch: MutableState<Boolean>,
    locationDisplay: MutableState<String>
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
                delay(300L) // Adjust this value to your preference
                if (searchQuery.value.isEmpty()) {
                    showAutocompleteResults.value = false
                }
            }
        }
    }

    Column {
        Box(modifier = Modifier.fillMaxSize()) {
            GoogleMapView(
                modifier = modifier,
                cameraPositionState = cameraPositionState,
                onMapLongClick = { latLng ->
                    val newMarker = MarkerOptions()
                        .position(latLng)
                        .icon(BitmapDescriptorFactory.defaultMarker())

                    marker.value = newMarker
                    onLocationSelected(latLng)
                    locationDisplay.value = "${latLng.latitude}, ${latLng.longitude}"
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
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = MaterialTheme.colors.secondary,
                        focusedLabelColor = MaterialTheme.colors.secondary,
                        unfocusedLabelColor = MaterialTheme.colors.secondary,
                        unfocusedBorderColor = MaterialTheme.colors.onSurface.copy(alpha = ContentAlpha.disabled),
                        textColor = Color.Black
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
                            cameraUpdate = cameraUpdate
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
                        elevation = 4.dp
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
    cameraUpdate: MutableState<CameraUpdate?>
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
                FetchPlaceRequest.newInstance(placeId, listOf(Place.Field.LAT_LNG))
            ).addOnSuccessListener { placeResponse ->
                placeResponse.place.latLng?.let { latLng ->
                    Log.d("searchLocation", "Fetched place details: $latLng")
                    coroutineScope.launch {
                        cameraUpdate.value = CameraUpdateFactory.newLatLngZoom(latLng, 15f)
                    }
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


private const val TAG = "BasicMapActivity"

val singapore = LatLng(1.35, 103.87)
val singapore2 = LatLng(1.40, 103.77)
val singapore3 = LatLng(1.45, 103.77)
val defaultCameraPosition = CameraPosition.fromLatLngZoom(singapore, 11f)

@Composable
fun GoogleMapView(
    modifier: Modifier = Modifier,
    cameraPositionState: CameraPositionState = rememberCameraPositionState(),
    onMapLoaded: () -> Unit = {},
    content: @Composable () -> Unit = {}
) {
    val singaporeState = rememberMarkerState(position = singapore)
    val singapore2State = rememberMarkerState(position = singapore2)
    val singapore3State = rememberMarkerState(position = singapore3)
    var circleCenter by remember { mutableStateOf(singapore) }
    if (singaporeState.dragState == DragState.END) {
        circleCenter = singaporeState.position
    }

    var uiSettings by remember { mutableStateOf(MapUiSettings(compassEnabled = false)) }
    var shouldAnimateZoom by remember { mutableStateOf(true) }
    var ticker by remember { mutableStateOf(0) }
    var mapProperties by remember {
        mutableStateOf(MapProperties(mapType = MapType.NORMAL))
    }
    var mapVisible by remember { mutableStateOf(true) }

    if (mapVisible) {
        GoogleMap(
            modifier = modifier,
            cameraPositionState = cameraPositionState,
            properties = mapProperties,
            uiSettings = uiSettings,
            onMapLoaded = onMapLoaded,
            onPOIClick = {
                Log.d(TAG, "POI clicked: ${it.name}")
            }
        ) {
            // Drawing on the map is accomplished with a child-based API
            val markerClick: (Marker) -> Boolean = {
                Log.d(TAG, "${it.title} was clicked")
                cameraPositionState.projection?.let { projection ->
                    Log.d(TAG, "The current projection is: $projection")
                }
                false
            }
            MarkerInfoWindowContent(
                state = singaporeState,
                title = "Zoom in has been tapped $ticker times.",
                onClick = markerClick,
                draggable = true,
            ) {
                Text(it.title ?: "Title", color = Color.Red)
            }
            MarkerInfoWindowContent(
                state = singapore2State,
                title = "Marker with custom info window.\nZoom in has been tapped $ticker times.",
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE),
                onClick = markerClick,
            ) {
                Text(it.title ?: "Title", color = Color.Blue)
            }
            Marker(
                state = singapore3State,
                title = "Marker in Singapore",
                onClick = markerClick
            )
            Circle(
                center = circleCenter,
                fillColor = MaterialTheme.colors.secondary,
                strokeColor = MaterialTheme.colors.secondaryVariant,
                radius = 1000.0,
            )
            content()
        }

    }
    Column {
        MapTypeControls(onMapTypeClick = {
            Log.d("GoogleMap", "Selected map type $it")
            mapProperties = mapProperties.copy(mapType = it)
        })
        Row {
            MapButton(
                text = "Reset Map",
                onClick = {
                    mapProperties = mapProperties.copy(mapType = MapType.NORMAL)
                    cameraPositionState.position = defaultCameraPosition
                    singaporeState.position = singapore
                    singaporeState.hideInfoWindow()
                }
            )
            MapButton(
                text = "Toggle Map",
                onClick = { mapVisible = !mapVisible },
                modifier = Modifier.testTag("toggleMapVisibility"),
            )
        }
        val coroutineScope = rememberCoroutineScope()
        ZoomControls(
            shouldAnimateZoom,
            uiSettings.zoomControlsEnabled,
            onZoomOut = {
                if (shouldAnimateZoom) {
                    coroutineScope.launch {
                        cameraPositionState.animate(CameraUpdateFactory.zoomOut())
                    }
                } else {
                    cameraPositionState.move(CameraUpdateFactory.zoomOut())
                }
            },
            onZoomIn = {
                if (shouldAnimateZoom) {
                    coroutineScope.launch {
                        cameraPositionState.animate(CameraUpdateFactory.zoomIn())
                    }
                } else {
                    cameraPositionState.move(CameraUpdateFactory.zoomIn())
                }
                ticker++
            },
            onCameraAnimationCheckedChange = {
                shouldAnimateZoom = it
            },
            onZoomControlsCheckedChange = {
                uiSettings = uiSettings.copy(zoomControlsEnabled = it)
            }
        )
        DebugView(cameraPositionState, singaporeState)
    }
}

@Composable
private fun MapTypeControls(
    onMapTypeClick: (MapType) -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .horizontalScroll(state = ScrollState(0)),
        horizontalArrangement = Arrangement.Center
    ) {
        MapType.values().forEach {
            MapTypeButton(type = it) { onMapTypeClick(it) }
        }
    }
}

@Composable
private fun MapTypeButton(type: MapType, onClick: () -> Unit) =
    MapButton(text = type.toString(), onClick = onClick)

@Composable
private fun ZoomControls(
    isCameraAnimationChecked: Boolean,
    isZoomControlsEnabledChecked: Boolean,
    onZoomOut: () -> Unit,
    onZoomIn: () -> Unit,
    onCameraAnimationCheckedChange: (Boolean) -> Unit,
    onZoomControlsCheckedChange: (Boolean) -> Unit,
) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
        MapButton("-", onClick = { onZoomOut() })
        MapButton("+", onClick = { onZoomIn() })
        Column(verticalArrangement = Arrangement.Center) {
            Text(text = "Camera Animations On?")
            Switch(
                isCameraAnimationChecked,
                onCheckedChange = onCameraAnimationCheckedChange,
                modifier = Modifier.testTag("cameraAnimations"),
            )
            Text(text = "Zoom Controls On?")
            Switch(
                isZoomControlsEnabledChecked,
                onCheckedChange = onZoomControlsCheckedChange
            )
        }
    }
}

@Composable
private fun MapButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        modifier = modifier.padding(4.dp),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = MaterialTheme.colors.onPrimary,
            contentColor = MaterialTheme.colors.primary
        ),
        onClick = onClick
    ) {
        Text(text = text, style = MaterialTheme.typography.body1)
    }
}

@Composable
private fun DebugView(
    cameraPositionState: CameraPositionState,
    markerState: MarkerState
) {
    Column(
        Modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.Center
    ) {
        val moving =
            if (cameraPositionState.isMoving) "moving" else "not moving"
        Text(text = "Camera is $moving")
        Text(text = "Camera position is ${cameraPositionState.position}")
        Spacer(modifier = Modifier.height(4.dp))
        val dragging =
            if (markerState.dragState == DragState.DRAG) "dragging" else "not dragging"
        Text(text = "Marker is $dragging")
        Text(text = "Marker position is ${markerState.position}")
    }
}


