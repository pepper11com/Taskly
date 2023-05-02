package com.example.new_app.screens.screen_switcher

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.new_app.SharedViewModel
import com.example.new_app.common.composables.BottomNavigationBar
import com.example.new_app.screens.calender_screens.CalenderScreens
import com.example.new_app.screens.login.UserData
import com.example.new_app.screens.task.create_edit_tasks.TaskEditCreateViewModel
import com.example.new_app.screens.task.tasklist.TaskListScreen
import com.example.new_app.screens.task.tasklist.TaskListViewModel

@Composable
fun NavigatorScreen(
    openScreen: (String) -> Unit,
    mainViewModel: SharedViewModel,
    userData: UserData?,
    taskEditCreateViewModel: TaskEditCreateViewModel,
) {
    val selectedIndex = remember { mutableStateOf(0) }
    val viewModel: TaskListViewModel = hiltViewModel()

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                selectedIndex = selectedIndex,
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
                        taskEditCreateViewModel = taskEditCreateViewModel
                    )
                }

                1 -> {
                    CalenderScreens(
                        openScreen = openScreen,
                        userData = userData,
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}
