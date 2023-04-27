package com.example.new_app.screens.task.tasklist

import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
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
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.new_app.R
import com.example.new_app.model.Task
import kotlin.math.roundToInt
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.SwipeableState
import androidx.compose.material.swipeable
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.draw.alpha
import kotlinx.coroutines.delay

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
    onTaskSwipedBackToActive: (Task) -> Unit,
    isFlashing: Boolean = false,

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
        if (swipeableState.targetValue == 0 && (status == TaskStatus.DELETED || status == TaskStatus.COMPLETED)) {
            onTaskSwipedBackToActive(task)
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
            isFlashing = isFlashing
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
    onSelectedTasksChange: (Task, Boolean) -> Unit,
    isFlashing: Boolean = false
) {
    val context = LocalContext.current
    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    val flashState = rememberSaveable { mutableStateOf(isFlashing) }

    val flashColor = animateColorAsState(
        targetValue = if (flashState.value) Color(0xFFCCCCCC) else Color(0xFF444444),
        animationSpec = tween(durationMillis = 5000)
    )

    LaunchedEffect(flashState.value) {
        if (flashState.value) {
            flashState.value = false
        }
    }

    SideEffect {
        flashState.value = isFlashing
    }

    val painter = rememberAsyncImagePainter(
        ImageRequest.Builder(LocalContext.current)
            .data(data = task.imageUri ?: R.drawable.baseline_account_box_24)
            .apply(block = fun ImageRequest.Builder.() {
                crossfade(true)
                error(R.drawable.baseline_account_box_24)
            }).build()
    )


    Card(
        colors = CardDefaults.cardColors(
            containerColor = flashColor.value
        ),
        modifier = Modifier
            .offset { IntOffset(offset.x.roundToInt(), offset.y.roundToInt()) }
            .padding(8.dp)
            .fillMaxWidth()
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
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Image(
                    painter = painter,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp)),
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp)
                ) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )
                    Text(
                        text = getDueDateAndTime(task),
                        fontSize = 12.sp,
                        color = Color.White
                    )
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
                        colors = CheckboxDefaults.colors(
                            checkmarkColor = Color(0xFFFF8C00),
                            disabledCheckedColor = MaterialTheme.colorScheme.background,
                            disabledIndeterminateColor = MaterialTheme.colorScheme.background,
                            uncheckedColor = MaterialTheme.colorScheme.background,
                            checkedColor = MaterialTheme.colorScheme.background,
                        )
                    )
                }
            }

            if (task.locationName != null && task.location != null) {
                val staticMapUrl = generateStaticMapUrl(task)
                StaticMap(
                    staticMapUrl = staticMapUrl,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .height(140.dp)
                        .clip(RoundedCornerShape(8.dp)),
                )
                Text(
                    modifier = Modifier.padding(start = 14.dp, bottom = 12.dp),
                    text = task.locationName.toString(),
                    fontSize = 9.sp,
                    color = Color.White,
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .height(140.dp)
                        .background(Color(0xFF666666), shape = RoundedCornerShape(8.dp))
                )
            }
        }
    }
}
