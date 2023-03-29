package com.example.new_app.screens.tasklist

import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.new_app.EDIT_TASK_SCREEN
import com.example.new_app.R
import com.example.new_app.TASK_ID
import com.example.new_app.TASK_ID_KEY
import com.example.new_app.model.Task
import kotlin.math.roundToInt

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
    onSelectedTasksChange: (Task, Boolean) -> Unit,
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
            onSelectedTasksChange = onSelectedTasksChange,
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