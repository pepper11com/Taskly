package com.example.new_app.screens.task.create_edit_tasks.createtask

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.new_app.R
import com.example.new_app.SETTINGS_SCREEN
import com.example.new_app.SPLASH_SCREEN
import com.example.new_app.TASK_LIST_SCREEN
import com.example.new_app.common.composables.CustomCreateTaskAppBar
import com.example.new_app.common.composables.CustomMultiLineTextfield
import com.example.new_app.common.composables.CustomTextField
import com.example.new_app.common.composables.LoadingIndicator
import com.example.new_app.common.composables.RegularCardEditor
import com.example.new_app.common.util.Resource
import com.example.new_app.model.Task
import com.example.new_app.screens.map.LocationPicker
import com.example.new_app.screens.task.create_edit_tasks.TaskEditCreateViewModel
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat


@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun CreateTaskScreen(
    popUpScreen: () -> Unit,
    taskId: String,
    userId: String
) {
    val viewModel: TaskEditCreateViewModel = hiltViewModel()
    val task by viewModel.task

    val showMapAndSearch = remember { mutableStateOf(false) }
    val locationDisplay = remember { mutableStateOf("") }
    val newTaskState by viewModel.taskEditCreateState.collectAsState()

    val activity = LocalContext.current as AppCompatActivity
    val galleryLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            viewModel.onImageChange(it)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.initialize(null)
    }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        topBar = {
            CustomCreateTaskAppBar(
                task = task,
                viewModel = viewModel,
                popUpScreen = popUpScreen,
                scrollBehavior = scrollBehavior,
            )
        }
    ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(scrollBehavior.nestedScrollConnection)

                .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    PickImageFromGallery(
                        LocalContext.current,
                        viewModel,
                        task,
                        userId,
                        galleryLauncher
                    )
                }

                item {
                    Button(
                        onClick = { showMapAndSearch.value = !showMapAndSearch.value },
                        modifier = Modifier
                            .fillMaxWidth(),

                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                    ) {
                        Text("Add Location")
                    }
                }

                if (locationDisplay.value != "") {
                    item {
                        Button(
                            onClick = {
                                locationDisplay.value = ""
                                viewModel.onLocationReset()
                            },
                            modifier = Modifier
                                .fillMaxWidth(),

                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                        ) {
                            Text("Delete Location")
                        }
                    }
                }


                item {
                    TextField(
                        value = if (locationDisplay.value == "") "No Location Selected" else locationDisplay.value,
                        onValueChange = { },
                        modifier = Modifier
                            .fillMaxWidth(),

                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.secondary,
                            unfocusedContainerColor = MaterialTheme.colorScheme.secondary,
                            disabledContainerColor = MaterialTheme.colorScheme.secondary,
                            errorContainerColor = MaterialTheme.colorScheme.secondary,
                            focusedTextColor = MaterialTheme.colorScheme.onPrimary,
                            unfocusedTextColor = MaterialTheme.colorScheme.onPrimary,
                            disabledTextColor = MaterialTheme.colorScheme.onPrimary,
                            errorTextColor = MaterialTheme.colorScheme.onPrimary,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledLabelColor = MaterialTheme.colorScheme.onPrimary,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            focusedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                        shape = MaterialTheme.shapes.medium,
                        readOnly = true
                    )
                }


                item {
                    CustomTextField(
                        value = task.title,
                        onValueChange = viewModel::onTitleChange,
                        label = "Title",
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    CustomMultiLineTextfield(
                        value = task.description,
                        onValueChange = viewModel::onDescriptionChange,
                        modifier = Modifier.fillMaxWidth(),
                        hintText = "Description",
                        textStyle = MaterialTheme.typography.bodySmall,
                        maxLines = 4
                    )
                }

                item {
                    CardEditors(
                        task,
                        viewModel::onDateChange,
                        viewModel::onTimeChange
                    )
                }
            }

            if (showMapAndSearch.value) {
                Dialog(
                    onDismissRequest = { showMapAndSearch.value = false },
                    properties = DialogProperties(usePlatformDefaultWidth = false)
                ) {
                    Scaffold(
                        topBar = {
                            TopAppBar(
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                ),
                                title = { Text("Map and Search Bar") },
                                navigationIcon = {
                                    IconButton(onClick = { showMapAndSearch.value = false }) {
                                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                                    }
                                },
                                actions = {
                                    IconButton(
                                        onClick = {
                                            showMapAndSearch.value = false
                                        }
                                    ) {
                                        Icon(
                                            Icons.Filled.Done,
                                            contentDescription = "Done",
                                            tint = if (task.location != null)
                                                Color.Black else MaterialTheme.colorScheme.onSurface.copy(
                                                alpha = 0.5f
                                            )
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
                                locationDisplay.value = locationName
                            },
                            showMapAndSearch = showMapAndSearch,
                            locationDisplay = locationDisplay
                        )
                    }
                }
            }


        when (newTaskState) {
            is Resource.Loading -> {
                // Display a loading indicator
                LoadingIndicator()
            }

            is Resource.Success -> {

            }

            is Resource.Error -> {
                // Handle error
            }

            else -> {
                // Handle empty state
            }
        }
    }
}


@Composable
private fun CardEditors(
    task: Task,
    onDateChange: (Long) -> Unit,
    onTimeChange: (Int, Int) -> Unit
) {
    val activity = LocalContext.current as AppCompatActivity

    RegularCardEditor(
        R.string.date,
        Icons.Filled.DateRange,
        task.dueDate,
        Modifier.padding(top = 16.dp)
    ) {
        showDatePicker(activity, onDateChange)
    }

    RegularCardEditor(
        R.string.time,
        Icons.Filled.Timer,
        task.dueTime,
        Modifier.padding(top = 16.dp)
    ) {
        showTimePicker(activity, onTimeChange)
    }
}

@Composable
fun PickImageFromGallery(
    context: Context,
    viewModel: TaskEditCreateViewModel,
    task: Task,
    userId: String,
    galleryLauncher: ActivityResultLauncher<String>
) {
    val updatedTask by rememberUpdatedState(task)

    Row(
        modifier = Modifier.padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val imageUrl = viewModel.imageUri.value

        if (imageUrl != null) {
            AsyncImage(
                url = imageUrl,
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape),
                contentDescription = "Task Image"
            )
            Spacer(modifier = Modifier.width(16.dp))
            IconButton(onClick = viewModel::onDeleteImageClick) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete Image")
            }
        } else {
            IconButton(onClick = {
                galleryLauncher.launch("image/*")
            }) {
                Icon(Icons.Filled.AddAPhoto, contentDescription = "Pick Image from Gallery")
            }
        }
    }
}


@Composable
fun AsyncImage(url: String, modifier: Modifier = Modifier, contentDescription: String? = null) {

    val painter = rememberAsyncImagePainter(
        ImageRequest.Builder(LocalContext.current)
            .data(
                data = url
            )
            .apply(block = fun ImageRequest.Builder.() {
                memoryCachePolicy(CachePolicy.ENABLED)
            }).build()
    )

    Image(
        painter = painter,
        contentDescription = contentDescription,
        contentScale = ContentScale.Crop,
        modifier = modifier
    )
}



private fun showDatePicker(activity: AppCompatActivity, onDateChange: (Long) -> Unit) {
    val picker = MaterialDatePicker.Builder.datePicker()
        .setTheme(R.style.CustomDatePickerTheme) // set the custom theme here
        .build()

    activity.let {
        picker.show(it.supportFragmentManager, picker.toString())
        picker.addOnPositiveButtonClickListener { timeInMillis -> onDateChange(timeInMillis) }
    }
}

private fun showTimePicker(activity: AppCompatActivity, onTimeChange: (Int, Int) -> Unit) {
    val picker = MaterialTimePicker.Builder()
        .setTimeFormat(TimeFormat.CLOCK_24H)
        .setTheme(R.style.CustomTimePickerTheme) // set the custom theme here
        .build()

    activity.let {
        picker.show(it.supportFragmentManager, picker.toString())
        picker.addOnPositiveButtonClickListener { onTimeChange(picker.hour, picker.minute) }
    }
}




