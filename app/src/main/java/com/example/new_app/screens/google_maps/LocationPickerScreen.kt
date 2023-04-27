package com.example.new_app.screens.google_maps

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.new_app.CREATE_TASK_SCREEN
import com.example.new_app.TASK_MAP_SCREEN
import com.example.new_app.screens.map.LocationPicker
import com.example.new_app.screens.task.create_edit_tasks.TaskEditCreateViewModel
import com.google.android.gms.maps.model.LatLng

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationPickerScreen(
    openAndPopUp: (String, String) -> Unit,
    openScreen: (String) -> Unit,
    viewModel: TaskEditCreateViewModel
) {
    val searchInput = remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black,
                ),
                title = { Text("Task location") },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            openAndPopUp(CREATE_TASK_SCREEN, TASK_MAP_SCREEN)
                            viewModel.onLocationReset()
                            viewModel.locationDisplay.value = ""
                        }
                    ) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            openAndPopUp(CREATE_TASK_SCREEN, TASK_MAP_SCREEN)
                        },
                        enabled = searchInput.value.isNotEmpty()
                    ) {
                        Icon(
                            Icons.Filled.Done,
                            contentDescription = "Done",
                            tint = if (searchInput.value.isEmpty()) Color.White.copy(alpha = 0.5f) else MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        LocationPicker(
            modifier = Modifier.padding(innerPadding),
            onLocationSelected = viewModel::onLocationChange,
            onLocationNameSet = { locationName ->
                viewModel.onLocationNameChange(locationName)
                viewModel.locationDisplay.value = locationName
            },
            locationDisplay = viewModel.locationDisplay,
            searchInput = searchInput
        )
    }
}
