package com.example.new_app.screens.screen_switcher

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import com.example.new_app.TaskViewModel
import com.example.new_app.common.composables.custom_composables.BottomNavigationBar
import com.example.new_app.screens.calender_screens.CalenderScreens
import com.example.new_app.screens.login.UserData
import com.example.new_app.screens.task.create_edit_tasks.TaskEditCreateViewModel
import com.example.new_app.screens.task.tasklist.TaskListScreen
import com.example.new_app.screens.task.tasklist.TaskListViewModel

@Composable
fun NavigatorScreen(
    openScreen: (String) -> Unit,
    mainViewModel: TaskViewModel,
    userData: UserData?,
    taskEditCreateViewModel: TaskEditCreateViewModel,
    viewModel: TaskListViewModel
) {
    val selectedIndex = rememberSaveable { mutableStateOf(0) }
    val listState = rememberLazyListState()

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                selectedIndex = selectedIndex,
                listState = listState
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (selectedIndex.value) {
                0 -> {
                    TaskListScreen(
                        openScreen = openScreen,
                        mainViewModel = mainViewModel,
                        userData = userData,
                        viewModel = viewModel,
                        taskEditCreateViewModel = taskEditCreateViewModel,
                        listState = listState
                    )
                }

                1 -> {
                    CalenderScreens(
                        openScreen = openScreen,
                        userData = userData,
                        viewModel = viewModel,
                        taskEditCreateViewModel = taskEditCreateViewModel,
                        listState = listState
                    )
                }
            }
        }
    }
}
