package com.example.new_app.screens.task.tasklist


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.new_app.EDIT_TASK_SCREEN
import com.example.new_app.SharedViewModel
import com.example.new_app.TASK_ID
import com.example.new_app.TASK_ID_KEY
import com.example.new_app.common.composables.CustomTabRow
import com.example.new_app.common.composables.CustomTopAppBar
import com.example.new_app.common.composables.HomeFloatingActionButton
import com.example.new_app.common.composables.LoadingIndicator
import com.example.new_app.common.composables.TaskListScreenSideEffects
import com.example.new_app.common.composables.isScrollingUp
import com.example.new_app.common.ext.padding16
import com.example.new_app.common.sort.getFilteredTasks
import com.example.new_app.common.sort.sortTasks
import com.example.new_app.common.util.Resource
import com.example.new_app.model.Task
import com.example.new_app.screens.login.UserData
import com.example.new_app.screens.task.create_edit_tasks.TaskEditCreateViewModel
import kotlinx.coroutines.launch
import com.example.new_app.R.string as TaskString

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TaskListScreen(
    openScreen: (String) -> Unit,
    mainViewModel: SharedViewModel,
    userData: UserData?,
    taskEditCreateViewModel: TaskEditCreateViewModel,
    viewModel: TaskListViewModel = hiltViewModel(),
    listState: LazyListState,
) {
//    LaunchedEffect(Unit){
//        mainViewModel.resetInitEdit()
//    }
    val userId = viewModel.currentUserId
    //TODO TEST TEST TEST THIS was - .collectAsState()
    val uiState by viewModel.taskListUiState.collectAsStateWithLifecycle()
    val deleteTasksState by viewModel.deleteTasksState.collectAsState()
    val sortType by viewModel.sortType.collectAsState()
    val lastAddedTaskId by mainViewModel.lastAddedTaskId.observeAsState(null)

    val userProfilePictureUrl = userData?.profilePictureUrl
    val userGoogleName = userData?.username

    val selectedIndex = rememberSaveable { mutableStateOf(1) }
    val showDialog = remember { mutableStateOf(false) }
    val currentTask = remember { mutableStateOf<Task?>(null) }
    val selectedTasks = remember { mutableStateListOf<Task>() }
    val isScreenVisible = remember { mutableStateOf(true) }
    val taskSelectionStates = remember { mutableMapOf<String, MutableState<Boolean>>() }

    val scope = rememberCoroutineScope()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    val context = LocalContext.current

    val tabTitles = listOf(
        stringResource(TaskString.deleted_tasks),
        stringResource(TaskString.tasks),
        stringResource(TaskString.completed_tasks)
    )
    val filteredTasks = remember(uiState.tasks, selectedIndex.value, sortType) {
        val filtered = getFilteredTasks(uiState.tasks, TaskStatus.values()[selectedIndex.value], sortType)
        sortTasks(filtered, sortType)
    }
    //TODO TEST TEST TEST THIS
    TaskListScreenSideEffects(
        mainViewModel = mainViewModel,
        listState = listState,
        lastAddedTaskId = lastAddedTaskId,
        isScreenVisible = isScreenVisible,
        filteredTasks = filteredTasks,
    )

//    LaunchedEffect(lastAddedTaskId) {
//        if (lastAddedTaskId != null && isScreenVisible.value) {
//            val lastAddedTaskIndex = filteredTasks.indexOfFirst { it.id == lastAddedTaskId }
//            if (lastAddedTaskIndex != -1) {
//                delay(500)
//                listState.animateScrollToItem(lastAddedTaskIndex)
//                mainViewModel.updateLastAddedTaskId(null)
//            }
//        }
//    }
//    DisposableEffect(Unit) {
//        onDispose {
//            isScreenVisible.value = false
//        }
//    }
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            HomeFloatingActionButton(
                extended = listState.isScrollingUp()
            ) {
                taskEditCreateViewModel.resetTask()
                viewModel.onAddClick(openScreen, userId)
            }
        },
        topBar = {
            Column {
                CustomTopAppBar(
                    title = TaskString.new_task,
                    selectedIndex = selectedIndex,
                    uiState = uiState,
                    openScreen = openScreen,
                    viewModel = viewModel,
                    scrollBehavior = scrollBehavior,
                    mainViewModel = mainViewModel,
                    userProfilePictureUrl = userProfilePictureUrl,
                    userGoogleName = userGoogleName,
                )
                CustomTabRow(
                    selectedIndex = selectedIndex,
                    tabTitles = tabTitles,
                    selectedTasks = selectedTasks,
                    rowColor = MaterialTheme.colorScheme.primary,
                    mainViewModel = mainViewModel,
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
                LoadingIndicator()
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .nestedScroll(scrollBehavior.nestedScrollConnection)
                ) {
                    itemsIndexed(filteredTasks, key = { _, task -> task.id }) { _, task ->
                        Column(
                            modifier = Modifier.animateItemPlacement()
                        ) {
                            SwipeableTaskListItem(
                                task = task,
                                onClick = {
                                    openScreen(
                                        "$EDIT_TASK_SCREEN$TASK_ID_KEY".replace(
                                            "{$TASK_ID}", task.id
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
                                isSelected = taskSelectionStates.getOrPut(task.id) {
                                    mutableStateOf(
                                        task in selectedTasks
                                    )
                                },
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
//                            Divider()
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
                    LoadingIndicator()
                }
                is Resource.Success -> {
                    selectedTasks.clear()
                    viewModel.resetDeleteTasksState()
                }
                else -> {}
            }
        }
    }
}