package com.example.new_app.screens.task.create_edit_tasks.createtask

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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.new_app.R
import com.example.new_app.common.composables.CustomTextField
import com.example.new_app.common.composables.RegularCardEditor
import com.example.new_app.model.Task
import com.example.new_app.screens.map.LocationPicker
import com.example.new_app.screens.task.create_edit_tasks.TaskEditCreateViewModel
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.lang.Integer.min


@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterialApi::class)
@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun CreateTaskScreen(
    popUpScreen: () -> Unit,
    taskId: String,
    userId: String
) {
    val viewModel: TaskEditCreateViewModel = viewModel()
    val task by viewModel.task

    val showMapAndSearch = remember { mutableStateOf(false) }
    val locationDisplay = remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.initialize(null)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                backgroundColor = MaterialTheme.colors.primary,
                title = { Text("Create Task") },
                navigationIcon = {
                    IconButton(onClick = popUpScreen) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        enabled = task.title.isNotBlank() && task.description.isNotBlank(),

                        onClick = {
                            viewModel.onDoneClick(null, popUpScreen)
                        }
                    ) {
                        Icon(
                            Icons.Filled.Done,
                            contentDescription = "Done",
                            tint = if (task.title.isNotBlank() && task.description.isNotBlank())
                                Color.Black else MaterialTheme.colors.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            )
        }
    ) {
        Box {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    PickImageFromGallery(
                        LocalContext.current,
                        viewModel,
                        task,
                        userId
                    )
                }

                item {
                    Button(
                        onClick = { showMapAndSearch.value = !showMapAndSearch.value },
                        modifier = Modifier
                            .fillMaxWidth(),

                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = MaterialTheme.colors.secondary,
                            contentColor = MaterialTheme.colors.onPrimary
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
                                backgroundColor = MaterialTheme.colors.error,
                                contentColor = MaterialTheme.colors.onPrimary
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

                        colors = TextFieldDefaults.textFieldColors(
                            backgroundColor = MaterialTheme.colors.secondary,
                            textColor = MaterialTheme.colors.onPrimary,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledLabelColor = MaterialTheme.colors.onPrimary,
                            unfocusedLabelColor = MaterialTheme.colors.onPrimary,
                            focusedLabelColor = MaterialTheme.colors.onPrimary,
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
                    CustomTextField(
                        value = task.description,
                        onValueChange = viewModel::onDescriptionChange,
                        label = "Description",
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = false
                    )
                }

                item {
                    CardEditors(task, viewModel::onDateChange, viewModel::onTimeChange)
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
                                backgroundColor = MaterialTheme.colors.primary,
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
                                                Color.Black else MaterialTheme.colors.onSurface.copy(
                                                alpha = 0.5f
                                            )
                                        )
                                    }
                                }
                            )
                        }
                    ) {
                        LocationPicker(
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
        }
    }
}

@ExperimentalMaterialApi
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
    userId: String
) {
    val updatedTask by rememberUpdatedState(task)

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                viewModel.onImageChange(uri.toString(), context, task.id, userId)
            }
        }
    }

    fun openImagePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            flags = Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION or
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        }
        launcher.launch(intent)
    }

    Row(
        modifier = Modifier.padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        viewModel.bitmap?.let { btm ->
            val squareBitmap = btm.centerCropToSquare()
            val softwareBitmap = squareBitmap.toSoftwareBitmap()
            val circularBitmap = softwareBitmap.toCircularBitmap()
            Image(
                bitmap = circularBitmap.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier
                    .size(100.dp)
                    .weight(1f, fill = false)
            )
        } ?: run {
            Image(
                painter = painterResource(id = R.drawable.baseline_account_box_24),
                contentDescription = "Logo",
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .weight(1f, fill = false)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(3f, fill = false)
        ) {
            Button(
                onClick = {
                    openImagePicker()
                },
                colors = ButtonDefaults.buttonColors(
                    contentColor = MaterialTheme.colors.onPrimary,
                    backgroundColor = MaterialTheme.colors.secondary
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Pick Image")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                enabled = updatedTask.imageUri != null && updatedTask.imageUri!!.isNotEmpty(),
                onClick = {
                    updatedTask.imageUri = ""
                    viewModel.bitmap = BitmapFactory.decodeResource(
                        context.resources,
                        R.drawable.baseline_account_box_24
                    )
                },
                colors = ButtonDefaults.buttonColors(
                    contentColor = MaterialTheme.colors.onPrimary,
                    backgroundColor = MaterialTheme.colors.secondary
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Delete Image")
            }
        }
    }
}


// Extension function to crop a Bitmap into a square
fun Bitmap.centerCropToSquare(): Bitmap {
    val dimension = min(width, height)
    val xOffset = (width - dimension) / 2
    val yOffset = (height - dimension) / 2
    return Bitmap.createBitmap(this, xOffset, yOffset, dimension, dimension)
}

// Extension function to crop a Bitmap into a circle
fun Bitmap.toCircularBitmap(): Bitmap {
    val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(output)

    val paint = Paint().apply {
        isAntiAlias = true
    }
    val rect = Rect(0, 0, width, height)
    val rectF = RectF(rect)

    canvas.drawOval(rectF, paint)
    paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
    canvas.drawBitmap(this, rect, rect, paint)

    return output
}

// Extension function to convert a hardware Bitmap to a software Bitmap
fun Bitmap.toSoftwareBitmap(): Bitmap {
    val config = if (isMutable) Bitmap.Config.ARGB_8888 else Bitmap.Config.RGB_565
    return copy(config, isMutable)
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




