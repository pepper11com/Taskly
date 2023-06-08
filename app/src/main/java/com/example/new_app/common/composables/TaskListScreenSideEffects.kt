package com.example.new_app.common.composables

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import com.example.new_app.TaskViewModel
import com.example.new_app.domain.model.Task
import kotlinx.coroutines.delay

@Composable
fun TaskListScreenSideEffects(
    mainViewModel: TaskViewModel,
    listState: LazyListState,
    lastAddedTaskId: String?,
    isScreenVisible: MutableState<Boolean>,
    filteredTasks: List<Task>,
) {
    LaunchedEffect(Unit) {
        mainViewModel.resetInitEdit()
    }
    DisposableEffect(Unit) {
        onDispose {
            isScreenVisible.value = false
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
}
