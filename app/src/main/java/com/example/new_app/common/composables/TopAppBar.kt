package com.example.new_app.common.composables

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Deselect
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.example.new_app.SETTINGS_SCREEN
import com.example.new_app.SharedViewModel
import com.example.new_app.model.Task
import com.example.new_app.screens.task.create_edit_tasks.TaskEditCreateViewModel
import com.example.new_app.screens.task.tasklist.TaskListUiState
import com.example.new_app.screens.task.tasklist.TaskListViewModel
import com.example.new_app.screens.task.tasklist.onSelectAllTasks

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTopAppBar(
    title: String,
    selectedIndex: MutableState<Int>,
    selectedTasks: SnapshotStateList<Task>,
    uiState: TaskListUiState,
    openScreen: (String) -> Unit,
    viewModel: TaskListViewModel,
    scrollBehavior: TopAppBarScrollBehavior
) {
//    MediumTopAppBar(
    TopAppBar(
        modifier = Modifier.fillMaxWidth(),
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
        ),
        actions = {

            if (selectedIndex.value != 1) {
                if (selectedTasks.isEmpty()) {
                    IconButton(
                        onClick = {
                            onSelectAllTasks(
                                selectedIndex.value,
                                selectedTasks,
                                uiState.tasks
                            )
                        }
                    ) {
                        Icon(
                            Icons.Default.SelectAll,
                            contentDescription = "Select All",
                            tint = Color.White
                        )
                    }
                } else {
                    if (!selectedTasks.isEmpty()) {
                        IconButton(onClick = { selectedTasks.clear() }) {
                            Icon(
                                Icons.Default.Deselect,
                                contentDescription = "Select All",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
            if (selectedTasks.isEmpty()) {
                IconButton(onClick = { openScreen(SETTINGS_SCREEN) }) {
                    Icon(
                        Icons.Filled.Settings,
                        contentDescription = "Settings",
                        tint = Color.White
                    )
                }
            } else {
                DropdownContextMenu(
                    options = listOf("Delete All Selected"),
                    modifier = Modifier.padding(end = 8.dp),
                    onActionClick = { action ->
                        when (action) {
                            "Delete All Selected" -> {
                                viewModel.onDeleteSelectedTasks(selectedTasks)
                                selectedTasks.clear()
                            }
                        }
                    }
                )
            }
        },
        title = {
            Text(
                text = title,
                color = Color.White,
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomCreateTaskAppBar(
    task: Task,
    viewModel: TaskEditCreateViewModel,
    popUpScreen: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior,
    mainViewModel: SharedViewModel,
    context: Context
){
    Column {
        MediumTopAppBar(
            scrollBehavior = scrollBehavior,
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background,
            ),
            title = { Text("Create Task", color = Color.White) },
            navigationIcon = {
                IconButton(
                    onClick = {
                        popUpScreen()
                        viewModel.resetTask()
                    }
                ) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
            },
            actions = {
                IconButton(
                    enabled = task.title.isNotBlank() && task.description.isNotBlank(),

                    onClick = {
                        viewModel.onDoneClick(
                            context,
                            null,
                            popUpScreen,
                            onTaskCreated = { newTaskId -> mainViewModel.updateLastAddedTaskId(newTaskId) }
                        )
                    }
                ) {
                    Icon(
                        Icons.Filled.Done,
                        contentDescription = "Done",
                        tint = if (task.title.isNotBlank() && task.description.isNotBlank())
                            Color.White else Color.White.copy(alpha = 0.5f)
                    )
                }
            }
        )

        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            color = Color.White.copy(alpha = 0.5f),
            thickness = 1.dp
        )
    }
}