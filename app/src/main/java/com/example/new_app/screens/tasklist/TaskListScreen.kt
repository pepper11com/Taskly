package com.example.new_app.screens.tasklist

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.new_app.CREATE_TASK_SCREEN
import com.example.new_app.LOGIN_SCREEN
import com.example.new_app.common.composables.LoadingIndicator
import com.example.new_app.model.Task
import com.example.new_app.util.Resource

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun TaskListScreen(
    openScreen: (String) -> Unit
) {
    val viewModel: TaskListViewModel = viewModel()
    val uiState by viewModel.taskListUiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Task List") },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.onAddClick(openScreen)
                        }
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "Add Task")
                    }
                }
            )
        }
    ) {
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                LoadingIndicator()
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(uiState.tasks) { task ->
                    TaskListItem(task = task, onClick = { openScreen("detail") })
                    Divider()
                }
            }
        }
    }
}

@Composable
fun TaskListItem(task: Task, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(task.title, style = MaterialTheme.typography.h6)
        Text(
            if (task.isCompleted) "Completed" else "Pending",
            color = if (task.isCompleted) MaterialTheme.colors.primary else MaterialTheme.colors.error
        )
    }
}


