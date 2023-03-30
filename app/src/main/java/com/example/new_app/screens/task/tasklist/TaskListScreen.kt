package com.example.new_app.screens.task.tasklist


import android.annotation.SuppressLint
import android.graphics.Bitmap
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Deselect
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.new_app.EDIT_TASK_SCREEN
import com.example.new_app.SETTINGS_SCREEN
import com.example.new_app.TASK_ID
import com.example.new_app.TASK_ID_KEY
import com.example.new_app.common.composables.LoadingIndicator
import com.example.new_app.model.Task
import com.example.new_app.model.service.AccountService
import kotlinx.coroutines.launch
import com.example.new_app.common.composables.DropdownContextMenu

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun TaskListScreen(
    openScreen: (String) -> Unit,
) {
    val accountService = AccountService()
    val userId = accountService.currentUserId

    val context = LocalContext.current
    val viewModel: TaskListViewModel = viewModel()
    val uiState by viewModel.taskListUiState.collectAsState()

    val scope = rememberCoroutineScope()

    val showDialog = remember { mutableStateOf(false) }
    val currentTask = remember { mutableStateOf<Task?>(null) }

    val selectedTasks = remember { mutableStateListOf<Task>() }

    val tabTitles = listOf("Deleted Tasks", "Tasks", "Completed Tasks")
    val selectedIndex = remember { mutableStateOf(1) }

    val filteredTasks = remember(uiState.tasks, selectedIndex.value) {
        getFilteredTasks(uiState.tasks, TaskStatus.values()[selectedIndex.value])
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.onAddClick(openScreen, userId) },
                backgroundColor = MaterialTheme.colors.primary,
                contentColor = MaterialTheme.colors.onPrimary,
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(Icons.Filled.Add, "Add")
            }
        },
        topBar = {
            Column {
                TopAppBar(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = MaterialTheme.colors.primary,
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
                                    Icon(Icons.Default.SelectAll, contentDescription = "Select All")
                                }
                            } else {
                                if (!selectedTasks.isEmpty()) {
                                    IconButton(onClick = { selectedTasks.clear() }) {
                                        Icon(
                                            Icons.Default.Deselect,
                                            contentDescription = "Select All"
                                        )
                                    }
                                }
                            }
                        }
                        if (selectedTasks.isEmpty()) {
                            IconButton(onClick = { openScreen(SETTINGS_SCREEN) }) {
                                Icon(Icons.Filled.Settings, contentDescription = "Settings")
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
                            text = "Tasks",
                            color = MaterialTheme.colors.onPrimary,
                        )
                    }
                )
                TabRow(
                    selectedTabIndex = selectedIndex.value,
                    backgroundColor = MaterialTheme.colors.primarySurface,
                    contentColor = Color.White,
                ) {
                    tabTitles.forEachIndexed { index, title ->
                        Tab(
                            text = { Text(title) },
                            selected = index == selectedIndex.value,
                            onClick = { selectedIndex.value = index }
                        )
                    }
                }
            }
        },
        content = {
            Box(modifier = Modifier.fillMaxSize()) {
                if (uiState.isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        LoadingIndicator()
                    }
                } else {

                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        itemsIndexed(filteredTasks, key = { _, task -> task.id }) { index, task ->
                            val taskBitmap = remember { mutableStateOf<Bitmap?>(null) }
                            SwipeableTaskListItem(
                                context = context,
                                task = task,
                                onClick = {
                                    openScreen("$EDIT_TASK_SCREEN$TASK_ID_KEY".replace("{$TASK_ID}", task.id.toString()))
                                },
                                viewModel = viewModel,
                                onLongPress = {
                                    scope.launch {
                                        currentTask.value = task
                                        showDialog.value = true
                                    }
                                },
                                status = task.status,
                                taskBitmap = taskBitmap,
                                isSelected = task in selectedTasks,
                                selectedTasks = selectedTasks,
                                onSelectedTasksChange = { selectedTask, isChecked ->
                                    if (isChecked) {
                                        selectedTasks.add(selectedTask)
                                    } else {
                                        selectedTasks.remove(selectedTask)
                                    }
                                },
                            )
                            Divider()
                        }
                    }
                }
                if (showDialog.value && currentTask.value != null) {
                    ShowDialogWithTaskDetailsAndDelete(
                        context = context,
                        task = currentTask.value!!,
                        viewModel = viewModel,
                        onDismiss = { showDialog.value = false },
                    )
                }
            }
        }
    )
}











