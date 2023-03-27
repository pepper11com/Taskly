package com.example.new_app.screens.createtask

import android.annotation.SuppressLint
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Timer
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.new_app.R
import com.example.new_app.common.composables.CustomButton
import com.example.new_app.common.composables.CustomTextField
import com.example.new_app.common.composables.RegularCardEditor
import com.example.new_app.model.Task
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat


@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterialApi::class)
@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun CreateTaskScreen(
    popUpScreen: () -> Unit,
    taskId: String,
) {
    val viewModel: CreateTaskViewModel = viewModel()
    val task by viewModel.task

    LaunchedEffect(Unit){
        viewModel.initialize(taskId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Task") },
                navigationIcon = {
                    IconButton(onClick = popUpScreen) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

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

                CardEditors(task, viewModel::onDateChange, viewModel::onTimeChange)

                CustomButton(
                    onClick = {
                        viewModel.onDoneClick(popUpScreen)
                    },
                    text = "Create Task",
                    modifier = Modifier.fillMaxWidth(),
                    enabled = task.title.isNotBlank() && task.description.isNotBlank()
                )

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



private fun showDatePicker(activity: AppCompatActivity, onDateChange: (Long) -> Unit) {
    val picker = MaterialDatePicker.Builder.datePicker().build()

    activity.let {
        picker.show(it.supportFragmentManager, picker.toString())
        picker.addOnPositiveButtonClickListener { timeInMillis -> onDateChange(timeInMillis) }
    }
}

private fun showTimePicker(activity: AppCompatActivity, onTimeChange: (Int, Int) -> Unit) {
    val picker = MaterialTimePicker.Builder().setTimeFormat(TimeFormat.CLOCK_24H).build()

    activity.let {
        picker.show(it.supportFragmentManager, picker.toString())
        picker.addOnPositiveButtonClickListener { onTimeChange(picker.hour, picker.minute) }
    }
}

@Composable
fun Context.findActivity(): AppCompatActivity? {
    var context: Context? = this
    while (context is ContextWrapper) {
        if (context is AppCompatActivity) {
            return context
        }
        context = context.baseContext
    }
    return null
}


