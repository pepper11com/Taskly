package com.example.new_app.screens.createtask

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.new_app.common.composables.CustomButton
import com.example.new_app.common.composables.CustomTextField
import com.example.new_app.common.composables.LoadingIndicator
import com.example.new_app.util.Resource

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

