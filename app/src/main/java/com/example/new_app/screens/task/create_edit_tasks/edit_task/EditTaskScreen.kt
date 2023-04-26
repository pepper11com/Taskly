package com.example.new_app.screens.task.create_edit_tasks.edit_task

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Timer
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.new_app.R
import com.example.new_app.common.composables.CustomTextField
import com.example.new_app.common.composables.RegularCardEditor
import com.example.new_app.model.Task
import com.example.new_app.screens.task.create_edit_tasks.TaskEditCreateViewModel
import com.example.new_app.screens.task.create_edit_tasks.createtask.AsyncImage
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.io.File
import java.io.FileInputStream


@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun EditTaskScreen(
    popUpScreen: () -> Unit,
    taskId: String,
    userId: String
) {
    val viewModel: TaskEditCreateViewModel = hiltViewModel()
    val task by viewModel.task

    LaunchedEffect(Unit) {
        viewModel.initialize(taskId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                ),
                title = { Text("Edit Task", color = MaterialTheme.colorScheme.background) },
                navigationIcon = {
                    IconButton(onClick = popUpScreen) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.background)
                    }
                },
                actions = {
                    IconButton(
                        enabled = task.title.isNotBlank() && task.description.isNotBlank(),

                        onClick = {
                            viewModel.onDoneClick(taskId, popUpScreen)
                        }
                    ) {
                        Icon(
                            Icons.Filled.Done,
                            contentDescription = "Done",
                            tint = if (task.title.isNotBlank() && task.description.isNotBlank())
                                Color.Black else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            PickImageFromGallery(
                LocalContext.current,
                viewModel,
                task,
                userId
            )

            CustomTextField(
                value = task.title,
                onValueChange = viewModel::onTitleChange,
                label = "Title",
                modifier = Modifier.fillMaxWidth()
            )

            CustomTextField(
                value = task.description,
                onValueChange = viewModel::onDescriptionChange,
                label = "Description",
                modifier = Modifier.fillMaxWidth(),
                singleLine = false
            )

            CardEditors(
                task,
                viewModel::onDateChange,
                viewModel::onTimeChange
            )

        }
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

    com.example.new_app.screens.task.create_edit_tasks.createtask.ShowDate(
        onDateChange = onDateChange,
        openDialog = showDatePicker
    )

    com.example.new_app.screens.task.create_edit_tasks.createtask.ShowTimePicker(
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
    userId: String
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
                    .clip(CircleShape)
                    .weight(1f, fill = false),
                contentDescription = "Task Image"
            )
        }

    }
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


