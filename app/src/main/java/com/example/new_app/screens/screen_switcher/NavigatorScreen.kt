package com.example.new_app.screens.screen_switcher

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.new_app.SharedViewModel
import com.example.new_app.common.composables.BottomNavigationBar
import com.example.new_app.common.composables.MediumAppBarWithTabs
import com.example.new_app.model.service.GoogleAuth
import com.example.new_app.screens.calender.CalendarViewScreen
import com.example.new_app.screens.login.LoginScreen
import com.example.new_app.screens.login.UserData
import com.example.new_app.screens.signup.SignupScreen
import com.example.new_app.screens.task.tasklist.TaskListScreen
import com.example.new_app.screens.task.tasklist.TaskListViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NavigatorScreen(
    openScreen: (String) -> Unit,
    mainViewModel: SharedViewModel,
    userData: UserData?,
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
                        viewModel = viewModel
                    )
                }

                1 -> {
                    CalendarViewScreen(
                        viewModel = viewModel,
                        openScreen = openScreen,
                        userData = userData,
                    )
                }
            }
        }
    }
}
