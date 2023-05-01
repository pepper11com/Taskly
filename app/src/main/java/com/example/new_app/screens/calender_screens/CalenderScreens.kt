package com.example.new_app.screens.calender_screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import com.example.new_app.common.composables.CustomTabRow
import com.example.new_app.common.composables.CustomTopAppBarCalendar
import com.example.new_app.screens.calender.CalendarViewScreen
import com.example.new_app.screens.calender.WeeklyCalendarViewScreen
import com.example.new_app.screens.login.UserData
import com.example.new_app.screens.task.tasklist.TaskListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalenderScreens(
    openScreen: (String) -> Unit,
    userData: UserData?,
    viewModel: TaskListViewModel,
) {

    val selectedIndex = remember { mutableStateOf(0) }
    val tabTitles = listOf("Week Calendar", "Month Calendar")
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    val userProfilePictureUrl = userData?.profilePictureUrl
    val userGoogleName = userData?.username
    val listState = rememberLazyListState()

    Scaffold(
        topBar = {
            Column {
                CustomTopAppBarCalendar(
                    title = if (selectedIndex.value == 0) "Week Calendar" else "Month Calendar",
                    scrollBehavior = scrollBehavior,
                    userProfilePictureUrl = userProfilePictureUrl,
                    openScreen = openScreen,
                    userGoogleName = userGoogleName,
                )
                CustomTabRow(
                    selectedIndex = selectedIndex,
                    tabTitles = tabTitles,
                    rowColor = MaterialTheme.colorScheme.primary
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .background(MaterialTheme.colorScheme.background),
            state = listState,
        ) {
            item {

                when (selectedIndex.value) {
                    0 -> {
                        WeeklyCalendarViewScreen(
                            viewModel = viewModel,
                            openScreen = openScreen,
                        )
                    }

                    1 -> {
                        CalendarViewScreen(
                            viewModel = viewModel,
                            openScreen = openScreen,
                        )
                    }
                }
            }
        }
    }
}
