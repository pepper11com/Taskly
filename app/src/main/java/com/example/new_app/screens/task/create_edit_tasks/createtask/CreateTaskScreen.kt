package com.example.new_app.screens.task.create_edit_tasks.createtask

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.ContentAlpha
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.rememberAsyncImagePainter
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.new_app.R
import com.example.new_app.SharedViewModel
import com.example.new_app.TASK_MAP_SCREEN
import com.example.new_app.common.composables.CustomCreateTaskAppBar
import com.example.new_app.common.composables.CustomMultiLineTextfield
import com.example.new_app.common.composables.CustomTextField
import com.example.new_app.common.composables.LoadingIndicator
import com.example.new_app.common.composables.RegularCardEditor
import com.example.new_app.common.util.Resource
import com.example.new_app.model.Task
import com.example.new_app.screens.task.create_edit_tasks.TaskEditCreateViewModel


@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun CreateTaskScreen(
    popUpScreen: () -> Unit,
    taskId: String,
    userId: String,
    mainViewModel: SharedViewModel,
    openAndPopUp: (String, String) -> Unit,
    openScreen: (String) -> Unit,
    viewModel: TaskEditCreateViewModel
) {

    val task by viewModel.task
    val context = LocalContext.current
    val newTaskState by viewModel.taskEditCreateState.collectAsState()

    val galleryLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri ->
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
                mainViewModel = mainViewModel,
                context = context
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
                SectionTitle("Image")
                PickImageFromGallery(
                    LocalContext.current,
                    viewModel,
                    task,
                    userId,
                    galleryLauncher
                )
            }

            item {
                Divider()
            }

            item {
                // Title Section
                SectionTitle("Title & Description")
                CustomTextField(
                    value = task.title,
                    onValueChange = viewModel::onTitleChange,
                    label = "Title",
                    modifier = Modifier.fillMaxWidth(),
                    colors= OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.White.copy(alpha = 0.7f),
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = ContentAlpha.disabled),
                        focusedLabelColor = Color.White.copy(alpha = 0.7f),
                        cursorColor = Color.White.copy(alpha = 0.7f),
                        selectionColors = TextSelectionColors(
                            Color(0xFF444444),
                            Color(0xFF444444).copy(alpha = 0.5f),
                        )
                    )
                )

                CustomMultiLineTextfield(
                    value = task.description,
                    onValueChange = viewModel::onDescriptionChange,
                    modifier = Modifier.fillMaxWidth(),
                    hintText = "Description",
                    textStyle = MaterialTheme.typography.bodyLarge,
                    maxLines = 4,
                    colors= OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.White.copy(alpha = 0.7f),
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = ContentAlpha.disabled),
                        focusedLabelColor = Color.White.copy(alpha = 0.7f),
                        cursorColor = Color.White.copy(alpha = 0.7f),
                        selectionColors = TextSelectionColors(
                            Color(0xFF444444),
                            Color(0xFF444444).copy(alpha = 0.5f),
                        )
                    )
                )
            }


            item {
                Divider(
                    modifier = Modifier
                        .padding(top = 18.dp,)
                )
            }

            item {
                // Date and Time Section
                SectionTitle("Location, Date & Time")
                ShowLocation(
                    viewModel.locationDisplay,
                    onEditClick = { openScreen(TASK_MAP_SCREEN) },
                    onLocationReset = viewModel::onLocationReset
                )

                CardEditors(
                    task,
                    viewModel::onDateChange,
                    viewModel::onTimeChange
                )
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
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 8.dp),
        color = Color.White
    )
}

@Composable
fun ShowLocation(
    locationDisplay: MutableState<String>,
    onEditClick: () -> Unit,
    onLocationReset: () -> Unit
) {
    val content = locationDisplay.value

    if (content.isNotBlank()) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ){
            RegularCardEditor(
                title = R.string.location,
                icon = Icons.Filled.EditLocation,
                content = content,
                modifier = Modifier
                    .padding(top = 16.dp)
                    .weight(1f),
                onEditClick = onEditClick,
                location = true
            )

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onLocationReset,
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(top = 16.dp)
                    .size(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF444444),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(4.dp),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 5.dp,
                    pressedElevation = 5.dp,
                    disabledElevation = 5.dp
                ),
                contentPadding = PaddingValues(0.dp),
            ) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete Location")
            }
        }

    } else {
        RegularCardEditor(
            title = R.string.location,
            icon = Icons.Filled.EditLocation,
            content = stringResource(R.string.no_location_selected),
            modifier = Modifier.padding(top = 16.dp),
            onEditClick = onEditClick
        )
    }
}


@Composable
private fun CardEditors(
    task: Task,
    onDateChange: (Long) -> Unit,
    onTimeChange: (Int, Int) -> Unit
) {
    val showDatePicker = remember { mutableStateOf(false) }
    val showTimePicker = remember { mutableStateOf(false) }

    RegularCardEditor(
        R.string.date,
        Icons.Filled.DateRange,
        task.dueDate,
        Modifier.padding(top = 16.dp)
    ) {
        showDatePicker.value = true
    }

    ShowDate(
        onDateChange = onDateChange,
        openDialog = showDatePicker
    )

    ShowTimePicker(
        onTimeChange = onTimeChange,
        openDialog = showTimePicker
    )

    RegularCardEditor(
        R.string.time,
        Icons.Filled.Timer,
        task.dueTime,
        Modifier.padding(top = 16.dp)
    ) {
        showTimePicker.value = true
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


@SuppressLint("UnrememberedMutableState")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowDate(
    onDateChange: (Long) -> Unit,
    openDialog: MutableState<Boolean>
) {
    if (openDialog.value) {
        val datePickerState = rememberDatePickerState()
        val confirmEnabled = derivedStateOf { datePickerState.selectedDateMillis != null }
        DatePickerDialog(
            onDismissRequest = {
                openDialog.value = false
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDateChange(datePickerState.selectedDateMillis!!)
                        openDialog.value = false
                    },
                    enabled = confirmEnabled.value
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        openDialog.value = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(
                state = datePickerState
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowTimePicker(
    onTimeChange: (Int, Int) -> Unit,
    openDialog: MutableState<Boolean>
) {
    val timePickerState = rememberTimePickerState()

    val content = @Composable {
        TimePicker(
            state = timePickerState,
            modifier = Modifier,
            colors = TimePickerDefaults.colors(
                clockDialSelectedContentColor = Color.White,
                clockDialUnselectedContentColor = Color.White,
                containerColor = Color.White,
                timeSelectorSelectedContainerColor = Color.White.copy(alpha = 0.5f),
                timeSelectorSelectedContentColor = Color.White,
            ),
            layoutType = TimePickerDefaults.layoutType()
        )
    }

    if (openDialog.value) {
        TimePickerDialog(
            onCancel = {
                openDialog.value = false
            },
            onConfirm = {
                val selectedHour = timePickerState.hour
                val selectedMinute = timePickerState.minute
                onTimeChange(selectedHour, selectedMinute)
                openDialog.value = false
            },
            content = content
        )
    }
}


@Composable
fun TimePickerDialog(
    title: String = "Select Time",
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
    toggle: @Composable () -> Unit = {},
    content: @Composable () -> Unit,
) {
    Dialog(
        onDismissRequest = onCancel,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        ),
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 6.dp,
            modifier = Modifier
                .width(IntrinsicSize.Min)
                .height(IntrinsicSize.Min)
                .background(
                    shape = MaterialTheme.shapes.extraLarge,
                    color = MaterialTheme.colorScheme.surface
                ),
        ) {
            toggle()
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    text = title,
                    style = MaterialTheme.typography.labelMedium
                )
                content()
                Row(
                    modifier = Modifier
                        .height(40.dp)
                        .fillMaxWidth()
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(
                        onClick = onCancel
                    ) { Text("Cancel") }
                    TextButton(
                        onClick = onConfirm
                    ) { Text("OK") }
                }
            }
        }
    }
}



