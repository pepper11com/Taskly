package com.example.new_app.screens.tasklist


import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Deselect
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.Coil
import com.example.new_app.R
import com.example.new_app.SETTINGS_SCREEN
import com.example.new_app.common.composables.LoadingIndicator
import com.example.new_app.model.Task
import com.example.new_app.model.service.AccountService
import com.example.new_app.screens.createtask.centerCropToSquare
import com.example.new_app.screens.createtask.toCircularBitmap
import com.example.new_app.screens.createtask.toSoftwareBitmap
import kotlinx.coroutines.launch
import java.lang.StringBuilder
import kotlin.math.roundToInt
import coil.compose.AsyncImagePainter
import coil.compose.ImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.new_app.common.composables.DropdownContextMenu
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException


@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun TaskListScreen(
    openScreen: (String) -> Unit
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
                                IconButton(onClick = {
                                    onSelectAllTasks(
                                        selectedIndex.value,
                                        selectedTasks,
                                        uiState.tasks
                                    )
                                }) {
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
                                onClick = { openScreen("detail") },
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
                                onSelectedTasksChange = { selectedTask, isChecked -> // Pass onSelectedTasksChange
                                    if (isChecked) {
                                        selectedTasks.add(selectedTask)
                                    } else {
                                        selectedTasks.remove(selectedTask)
                                    }
                                }
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
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SwipeableTaskListItem(
    context: Context,
    task: Task,
    taskBitmap: MutableState<Bitmap?>,
    onClick: () -> Unit,
    viewModel: TaskListViewModel,
    onLongPress: () -> Unit,
    status: TaskStatus,
    isSelected: Boolean,
    selectedTasks: SnapshotStateList<Task>,
    onSelectedTasksChange: (Task, Boolean) -> Unit
) {
    //width of the swipeable item
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val swipeableState = remember(task.id) { SwipeableState(0) }
    val maxOffset = with(LocalDensity.current) { screenWidth.toPx() }
    val anchors = when (status) {
        TaskStatus.DELETED -> mapOf(0f to 0, maxOffset to 1)
        TaskStatus.COMPLETED -> mapOf(-maxOffset to -1, 0f to 0)
        TaskStatus.ACTIVE -> mapOf(-maxOffset to -1, 0f to 0, maxOffset to 1)
    }

    val offset by animateOffsetAsState(
        targetValue = Offset(swipeableState.offset.value, 0f),
    )

    LaunchedEffect(swipeableState.currentValue) {
        when (swipeableState.targetValue) {
            -1 -> {
                if (status != TaskStatus.COMPLETED) {
                    viewModel.onTaskSwipeDeleted(task)
                } else {
                    viewModel.onTaskSwipeActive(task)
                }
                swipeableState.snapTo(0)
            }
            1 -> {
                if (status != TaskStatus.DELETED) {
                    viewModel.onTaskSwipeCompleted(task)
                } else {
                    viewModel.onTaskSwipeActive(task)
                }
                swipeableState.snapTo(0)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .swipeable(
                state = swipeableState,
                anchors = anchors,
                thresholds = { _, _ -> FractionalThreshold(0.6f) },
                orientation = Orientation.Horizontal
            )
    ) {
        TaskListItem(
            task = task,
            taskBitmap = taskBitmap,
            onClick = onClick,
            onLongPress = onLongPress,
            offset = offset,
            isSelected = isSelected,
            selectedTasks = selectedTasks,
            onSelectedTasksChange = onSelectedTasksChange
        )
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TaskListItem(
    task: Task,
    taskBitmap: MutableState<Bitmap?>,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    offset: Offset = Offset.Zero,
    isSelected: Boolean,
    selectedTasks: SnapshotStateList<Task>,
    onSelectedTasksChange: (Task, Boolean) -> Unit
) {
    val context = LocalContext.current
    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

    val painter = rememberAsyncImagePainter(
        ImageRequest.Builder(LocalContext.current)
            .data(data = task.imageUri ?: R.drawable.baseline_account_box_24).apply(block = fun ImageRequest.Builder.() {
                crossfade(true)
                error(R.drawable.baseline_account_box_24)
            }).build()
    )


    Card(
        backgroundColor = MaterialTheme.colors.background,
        modifier = Modifier
            .offset { IntOffset(offset.x.roundToInt(), offset.y.roundToInt()) }
            .padding(8.dp, 8.dp, 8.dp, 8.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = {
                    onLongPress()

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(
                            VibrationEffect.createOneShot(
                                25,
                                VibrationEffect.DEFAULT_AMPLITUDE
                            )
                        )
                    } else {
                        vibrator.vibrate(25)
                    }
                },
            )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
            ) {
                Image(
                    painter = painter,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape),
                )
                when (painter.state) {
                    is AsyncImagePainter.State.Loading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    else -> {
                        // No action needed here
                    }
                }
            }

            if (TaskStatus.ACTIVE != task.status) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { isChecked ->
                        if (isChecked) {
                            selectedTasks.add(task)
                        } else {
                            selectedTasks.remove(task)
                        }
                    },
                    modifier = Modifier.padding(8.dp, 0.dp))
            } else {
                Spacer(modifier = Modifier.padding(8.dp, 0.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(text = task.title, style = MaterialTheme.typography.subtitle2)
                CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                    Text(text = getDueDateAndTime(task), fontSize = 12.sp)
                }
            }

            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                Text(
                    text = if (TaskStatus.ACTIVE == task.status) "Pending" else if (TaskStatus.DELETED == task.status) "Deleted" else "Completed",
                    color = if (TaskStatus.ACTIVE == task.status) Color(0xFFE53935) else if (TaskStatus.DELETED == task.status) Color(0xFFE53935) else Color(0xFF43A047),
                )
            }
        }
    }
}



fun getFilteredTasks(tasks: List<Task>, status: TaskStatus): List<Task> {
    val sortedTasks = tasks.sortedByDescending { task -> task.taskDate }
    return sortedTasks.filter { task ->
        when (status) {
            TaskStatus.DELETED -> task.status == TaskStatus.DELETED
            TaskStatus.ACTIVE -> task.status == TaskStatus.ACTIVE
            TaskStatus.COMPLETED -> task.status == TaskStatus.COMPLETED
            else -> false
        }
    }
}


@Composable
fun ShowDialogWithTaskDetailsAndDelete(
    context: Context,
    task: Task,
    viewModel: TaskListViewModel,
    onDismiss: () -> Unit
) {
    val painter = rememberAsyncImagePainter(
        ImageRequest.Builder(LocalContext.current)
            .data(data = task.imageUri ?: R.drawable.baseline_account_box_24).apply(block = fun ImageRequest.Builder.() {
                crossfade(true)
                error(R.drawable.baseline_account_box_24)
            }).build()
    )


    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(text = task.title)

                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .padding(end = 4.dp)
                ) {
                    Image(
                        painter = painter,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape),
                    )
                    when (painter.state) {
                        is AsyncImagePainter.State.Loading -> {
                            CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                        else -> {
                            // No action needed here
                        }
                    }
                }


            }
        },
        text = {
            Column {
                Text(text = task.description)
                Spacer(modifier = Modifier.height(18.dp))
                Text(text = getDueDateAndTime(task))
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    viewModel.onTaskDelete(task)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.error)
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            Button(
                colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.secondary),
                onClick = onDismiss,
            ) {
                Text("Cancel")
            }
        }
    )
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

fun onSelectAllTasks(selectedIndex: Int, selectedTasks: SnapshotStateList<Task>, taskList: List<Task>) {
    // Compute the TaskStatus based on selectedIndex
    val status = when (selectedIndex) {
        0 -> TaskStatus.DELETED
        2 -> TaskStatus.COMPLETED
        else -> TaskStatus.ACTIVE
    }

    // Get all tasks with the matching status
    val tasksToSelect = taskList.filter { it.status == status }

    // Add all tasks to the selectedTasks list if they're not already in it
    tasksToSelect.forEach { task ->
        if (task !in selectedTasks) {
            selectedTasks.add(task)
        }
    }
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




