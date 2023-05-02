package com.example.new_app.common.composables

import android.annotation.SuppressLint
import android.content.Context
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EditLocation
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.new_app.R
import com.example.new_app.model.Task
import com.example.new_app.screens.task.create_edit_tasks.TaskEditCreateViewModel
import com.example.new_app.theme.DarkGrey
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat


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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowAlertTimePicker(
    showAlertTimePicker: MutableState<Boolean>,
    viewModel: TaskEditCreateViewModel
) {
    val radioOptions = listOf(
        "5 minutes in advance", "10 minutes in advance", "15 minutes in advance",
        "30 minutes in advance", "1 hour in advance", "2 hours in advance", "3 hours in advance",
        "1 day in advance"
    )

    val (selectedOption, onOptionSelected) = remember { mutableStateOf(-1) }

    LaunchedEffect(viewModel.alertTimeDisplay.value) {
        onOptionSelected(radioOptions.indexOf(viewModel.alertTimeDisplay.value))
    }

    if (showAlertTimePicker.value) {
        AlertDialog(
            modifier = Modifier
                .fillMaxWidth(),
            onDismissRequest = {
                showAlertTimePicker.value = false
            },
        ) {
            Surface(
                color = DarkGrey,
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    Modifier.selectableGroup()
                ) {
                    radioOptions.forEachIndexed { index, text ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = (selectedOption == index),
                                    onClick = {
                                        onOptionSelected(index)
                                        viewModel.onAlertOptionChange(text)
                                        showAlertTimePicker.value = false
                                    },
                                    role = Role.RadioButton
                                )
                                .padding(horizontal = 16.dp, vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (selectedOption == index),
                                onClick = null
                            )
                            Text(
                                text = text,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(start = 16.dp),
                                color = Color.White
                            )
                        }
                    }
                }
            }
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
            },
            colors = DatePickerDefaults.colors(
                containerColor = DarkGrey,
            )
        ) {
            DatePicker(
                state = datePickerState,
            )
        }
    }
}

@Composable
fun CardEditors(
    task: Task,
    onDateChange: (Long) -> Unit,
    onTimeChange: (Int, Int) -> Unit,
    viewModel: TaskEditCreateViewModel
) {
    val activity = LocalContext.current as AppCompatActivity

    val showDatePicker = remember { mutableStateOf(false) }
    val showAlertTimePicker = remember { mutableStateOf(false) }

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

    RegularCardEditor(
        R.string.time,
        Icons.Filled.Timer,
        task.dueTime,
        Modifier.padding(top = 16.dp)
    ) {
        showTimePicker(
            activity,
            onTimeChange
        )
    }

    RegularCardEditor(
        R.string.alert_time,
        Icons.Filled.Alarm,
        viewModel.alertTimeDisplay.value,
        Modifier.padding(top = 16.dp)
    ) {
        showAlertTimePicker.value = true
    }
    ShowAlertTimePicker(
        showAlertTimePicker,
        viewModel
    )
}

@Composable
fun ShowLocation(
    locationDisplay: MutableState<String>,
    onEditClick: () -> Unit,
    onLocationReset: () -> Unit,
) {
    val content = locationDisplay.value

    if (content.isNotBlank()) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
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
fun ColorPicker(
    selectedColor: Int,
    onColorSelected: (Int) -> Unit
) {
    val colors = listOf(
        Color(0xFFF8B195),
        Color(0xFFF67280),
        Color(0xFFC06C84),
        Color(0xFF6C5B7B),
        Color(0xFF355C7D),
        Color(0xFF99B898),
        Color(0xFFFECEAB),
        Color(0xFFFF847C),
        Color(0xFFE84A5F),
        Color(0xFF2A363B)
    )


    LazyRow {
        items(colors.size) { color ->
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .clip(CircleShape)
                    .background(color = colors[color])
                    .clickable {
                        onColorSelected(colors[color].toArgb())
                    }
                    .size(40.dp)
            ) {
                if (selectedColor == colors[color].toArgb()) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected color",
                        tint = Color.White,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}

fun showTimePicker(activity: AppCompatActivity?, onTimeChange: (Int, Int) -> Unit) {
    val picker = MaterialTimePicker.Builder()
        .setTimeFormat(TimeFormat.CLOCK_24H)
        .setTheme(R.style.CustomTimePickerTheme)
        .setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK)
        .build()

    activity?.let {
        picker.show(it.supportFragmentManager, picker.toString())
        picker.addOnPositiveButtonClickListener { onTimeChange(picker.hour, picker.minute) }
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