package com.example.new_app.screens.tasklist

import android.annotation.SuppressLint
import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.new_app.CREATE_TASK_SCREEN
import com.example.new_app.LOGIN_SCREEN
import com.example.new_app.common.composables.LoadingIndicator
import com.example.new_app.model.Task
import com.example.new_app.util.Resource
import java.lang.StringBuilder

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun TaskListScreen(
    openScreen: (String) -> Unit
) {
    val viewModel: TaskListViewModel = viewModel()
    val uiState by viewModel.taskListUiState.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.onAddClick(openScreen) },
                backgroundColor = MaterialTheme.colors.primary,
                contentColor = MaterialTheme.colors.onPrimary,
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(Icons.Filled.Add, "Add")
            }
        },
        topBar = {
            ActionToolbar(
                title = "Task List",
//                modifier = Modifier.wrapContentSize(Alignment.TopEnd),
                endActionIcon = Icons.Filled.Settings,
                endAction = {
                    //todo: open settings screen
//                    viewModel.onSettingsClick(openScreen)
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
fun TaskListItem(
    task: Task,
    onClick: () -> Unit
) {
    Card(
        backgroundColor = MaterialTheme.colors.background,
        modifier = Modifier.padding(8.dp, 8.dp, 8.dp, 8.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .clickable(onClick = onClick),
        ) {
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = null,
                modifier = Modifier.padding(8.dp, 0.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(text = task.title, style = MaterialTheme.typography.subtitle2)
                CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                    Text(text = getDueDateAndTime(task), fontSize = 12.sp)
                }
            }

            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                Text(
                    text = if (task.isCompleted) "Completed" else "Pending",
                    color = if (task.isCompleted) MaterialTheme.colors.primary else MaterialTheme.colors.error
                )
            }
        }
    }
}

@Composable
fun ActionToolbar(
    title: String,
    modifier: Modifier = Modifier,
    endActionIcon: ImageVector,
    endAction: () -> Unit
) {
    TopAppBar(
        title = { Text(title) },
        actions = {
            IconButton(onClick = endAction) {
                Icon(endActionIcon, contentDescription = "Settings")
            }
        },
        modifier = modifier
    )
}

private fun getDueDateAndTime(task: Task): String {
    val stringBuilder = StringBuilder("")

    if (task.dueDate.orEmpty().isNotBlank()) {
        stringBuilder.append(task.dueDate)
        stringBuilder.append(" ")
    }

    if (task.dueTime.orEmpty().isNotBlank()) {
        stringBuilder.append("at ")
        stringBuilder.append(task.dueTime)
    }

    return stringBuilder.toString()
}




