package com.example.new_app.screens.task.tasklist


import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.new_app.EDIT_TASK_SCREEN
import com.example.new_app.SharedViewModel
import com.example.new_app.TASK_ID
import com.example.new_app.TASK_ID_KEY
import com.example.new_app.common.composables.CustomTabRow
import com.example.new_app.common.composables.CustomTopAppBar
import com.example.new_app.common.composables.LoadingIndicator
import com.example.new_app.common.util.Resource
import com.example.new_app.model.Task
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.ui.text.font.FontWeight

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun TaskListScreen(
    openScreen: (String) -> Unit,
    mainViewModel: SharedViewModel
) {
    //todo -add on click the id of the task to a list so we can use that to delete the tasks
    //todo -and use the old method of keeping the checkmark in place
    val viewModel: TaskListViewModel = hiltViewModel()

    val userId = viewModel.currentUserId

    val deleteTasksState by viewModel.deleteTasksState.collectAsState()

    val sortType by viewModel.sortType.collectAsState()

    val context = LocalContext.current
    val uiState by viewModel.taskListUiState.collectAsState()
    val scope = rememberCoroutineScope()

    val showDialog = remember { mutableStateOf(false) }
    val currentTask = remember { mutableStateOf<Task?>(null) }

    val selectedTasks = remember { mutableStateListOf<Task>() }

    val tabTitles = listOf("Deleted Tasks", "Tasks", "Completed Tasks")
    val selectedIndex = remember { mutableStateOf(1) }

//    val filteredTasks = remember(uiState.tasks, selectedIndex.value) {
//        getFilteredTasks(uiState.tasks, TaskStatus.values()[selectedIndex.value])
//    }

    val filteredTasks = remember(uiState.tasks, selectedIndex.value, sortType) {
        val filtered =
            getFilteredTasks(uiState.tasks, TaskStatus.values()[selectedIndex.value], sortType)
        sortTasks(filtered, sortType)
    }

    val taskSelectionStates = remember { mutableMapOf<String, MutableState<Boolean>>() }

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    val listState = rememberLazyListState()

    val lastAddedTaskId by mainViewModel.lastAddedTaskId.observeAsState(null)
    val isScreenVisible = remember { mutableStateOf(true) }

    val expandedFab by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex == 0
        }
    }
    LaunchedEffect(lastAddedTaskId) {
        if (lastAddedTaskId != null && isScreenVisible.value) {
            val lastAddedTaskIndex = filteredTasks.indexOfFirst { it.id == lastAddedTaskId }
            if (lastAddedTaskIndex != -1) {
                delay(500)
                listState.animateScrollToItem(lastAddedTaskIndex)
                mainViewModel.updateLastAddedTaskId(null)
            }
        }
    }
    DisposableEffect(Unit) {
        onDispose {
            isScreenVisible.value = false
        }
    }


    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                shape = RoundedCornerShape(16.dp),
                expanded = expandedFab,
                onClick = { viewModel.onAddClick(openScreen, userId) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.background,
                modifier = Modifier.padding(16.dp),
                icon = { Icon(Icons.Filled.Add, "Localized description") },
                text = {
                    Text(
                        text = "New Task",
                        fontWeight = FontWeight.SemiBold,
                    )
                },
            )
        },
        topBar = {
            Column {
                CustomTopAppBar(
                    title = "Tasks",
                    selectedIndex = selectedIndex,
                    selectedTasks = selectedTasks,
                    uiState = uiState,
                    openScreen = openScreen,
                    viewModel = viewModel,
                    scrollBehavior = scrollBehavior,
                    mainViewModel = mainViewModel,
                )

                CustomTabRow(
                    selectedIndex = selectedIndex,
                    tabTitles = tabTitles,
                    selectedTasks = selectedTasks,
                    rowColor = MaterialTheme.colorScheme.primary,
                )
            }
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                LoadingIndicator(
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .nestedScroll(scrollBehavior.nestedScrollConnection)
                ) {
                    itemsIndexed(filteredTasks, key = { _, task -> task.id }) { _, task ->
                        val taskBitmap = remember { mutableStateOf<Bitmap?>(null) }
                        Column(
                            modifier = Modifier.animateItemPlacement()
                        ) {
                            SwipeableTaskListItem(
                                context = context,
                                task = task,
                                onClick = {
                                    openScreen(
                                        "$EDIT_TASK_SCREEN$TASK_ID_KEY".replace(
                                            "{$TASK_ID}",
                                            task.id
                                        )
                                    )
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
                                isSelected = taskSelectionStates.getOrPut(task.id) { mutableStateOf(task in selectedTasks) },
                                selectedTasks = selectedTasks,
                                onSelectedTasksChange = { selectedTask, isChecked ->
                                    if (isChecked) {
                                        selectedTasks.add(selectedTask)
                                    } else {
                                        selectedTasks.remove(selectedTask)
                                    }
                                },
                                onTaskSwipedBackToActive = { task ->
                                    selectedTasks.remove(task)
                                },
                                isFlashing = task.id == lastAddedTaskId,
                                mainViewModel = mainViewModel,
                            )
                            Divider()
                        }
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

            when (deleteTasksState) {
                is Resource.Loading -> {
                    // Display a loading indicator
                    LoadingIndicator()
                }

                is Resource.Success -> {
                    // Handle successful deletion of tasks
                    selectedTasks.clear()
                    viewModel.resetDeleteTasksState()
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
}










