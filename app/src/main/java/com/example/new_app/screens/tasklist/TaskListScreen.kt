package com.example.new_app.screens.tasklist

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.MediaStore
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.new_app.R
import com.example.new_app.common.composables.LoadingIndicator
import com.example.new_app.model.Task
import com.example.new_app.model.service.AccountService
import com.example.new_app.screens.createtask.CreateTaskViewModel
import com.example.new_app.screens.createtask.centerCropToSquare
import com.example.new_app.screens.createtask.toCircularBitmap
import com.example.new_app.screens.createtask.toSoftwareBitmap
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.lang.StringBuilder


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
    val currentTaskBitmap = remember { mutableStateOf<Bitmap?>(null) }

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
            ActionToolbar(
                title = "Task List",
//                modifier = Modifier.wrapContentSize(Alignment.TopEnd),
                endActionIcon = Icons.Filled.Settings,
                endAction = {
                    //todo: open settings screen
//                    viewModel.onSettingsClick(openScreen)
                }
            )
        },
        content = {
            Box(modifier = Modifier.fillMaxSize()) {
                if (uiState.isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        LoadingIndicator()
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(uiState.tasks) { task ->
                            val taskBitmap = remember { mutableStateOf<Bitmap?>(null) }

                            TaskListItem(
                                context = context,
                                task = task,
                                onClick = { openScreen("detail") },
                                taskBitmap = taskBitmap,
                                onLongPress = {
                                    scope.launch {
                                        currentTask.value = task
                                        currentTaskBitmap.value = taskBitmap.value
                                        showDialog.value = true
                                    }
                                }
                            )


                            Divider()
                        }
                    }
                }
                if (showDialog.value && currentTask.value != null && currentTaskBitmap.value != null) {
                    ShowDialogWithTaskDetailsAndDelete(
                        taskBitmap = currentTaskBitmap.value!!,
                        context = context,
                        task = currentTask.value!!,
                        viewModel = viewModel,
                        onDismiss = { showDialog.value = false }
                    )
                }
            }
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TaskListItem(
    context: Context,
    task: Task,
    taskBitmap: MutableState<Bitmap?>, // Add this line
    onClick: () -> Unit,
    onLongPress: () -> Unit
) {

    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

    LaunchedEffect(task.id, task.imageUri) {
        if (task.imageUri != null && task.imageUri!!.isNotEmpty()) {
            val bitmap = try {
                val file = File(task.imageUri!!)
                BitmapFactory.decodeStream(FileInputStream(file))
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
                null
            }
            if (bitmap != null) {
                taskBitmap.value = bitmap
            } else {
                val drawable =
                    ContextCompat.getDrawable(context, R.drawable.baseline_account_box_24)
                drawable?.let { nonNullDrawable ->
                    val defaultBitmap = nonNullDrawable.toBitmap()
                    taskBitmap.value = defaultBitmap
                }
            }
        } else {
            val drawable = ContextCompat.getDrawable(context, R.drawable.baseline_account_box_24)
            drawable?.let { nonNullDrawable ->
                val defaultBitmap = nonNullDrawable.toBitmap()
                taskBitmap.value = defaultBitmap
            }
        }
    }

    Card(
        backgroundColor = MaterialTheme.colors.background,
        modifier = Modifier
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
//                .combinedClickable(
//                    onClick = onClick,
//                    onLongClick = { onLongPress() },
//                )
        ) {
            taskBitmap.value?.let { btm ->
                val squareBitmap = btm.centerCropToSquare()
                val softwareBitmap = squareBitmap.toSoftwareBitmap()
                val circularBitmap = softwareBitmap.toCircularBitmap()
                Image(
                    bitmap = circularBitmap.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .size(40.dp)
                        .padding(end = 8.dp)
                )
            }

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
fun ShowDialogWithTaskDetailsAndDelete(
    taskBitmap: Bitmap,
    context: Context,
    task: Task,
    viewModel: TaskListViewModel,
    onDismiss: () -> Unit
) {

    //todo delete only works when you click the outer box of the task item

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(text = task.title)
                val squareBitmap = taskBitmap.centerCropToSquare()
                val softwareBitmap = squareBitmap.toSoftwareBitmap()
                val circularBitmap = softwareBitmap.toCircularBitmap()
                Image(
                    bitmap = circularBitmap.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .size(60.dp)
                        .padding(end = 4.dp)
                )

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




