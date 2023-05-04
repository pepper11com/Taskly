package com.example.new_app.screens.calender_screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.new_app.R
import com.example.new_app.common.composables.CustomTabRow
import com.example.new_app.common.composables.CustomTopAppBarCalendar
import com.example.new_app.common.composables.HomeFloatingActionButton
import com.example.new_app.common.composables.isScrollingUp
import com.example.new_app.common.ext.padding16
import com.example.new_app.screens.calender.MonthlyCalendarScreen
import com.example.new_app.screens.calender.WeeklyCalendarViewScreen
import com.example.new_app.screens.login.UserData
import com.example.new_app.screens.task.create_edit_tasks.TaskEditCreateViewModel
import com.example.new_app.screens.task.tasklist.TaskListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalenderScreens(
    openScreen: (String) -> Unit,
    userData: UserData?,
    viewModel: TaskListViewModel,
    taskEditCreateViewModel: TaskEditCreateViewModel,
    listState: LazyListState
) {
    val selectedIndex = rememberSaveable { mutableStateOf(0) }
    val tabTitles = listOf("Week Calendar", "Month Calendar")
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    val userProfilePictureUrl = userData?.profilePictureUrl
    val userGoogleName = userData?.username

    val userId = viewModel.currentUserId

    Scaffold(
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {

                when (selectedIndex.value) {
                    0 -> {
                        WeeklyCalendarViewScreen(
                            viewModel = viewModel,
                            openScreen = openScreen,

                            paddingValues = paddingValues,
                            scrollBehavior = scrollBehavior,
                            listState = listState,
                        )
                    }

                    1 -> {
                        MonthlyCalendarScreen(
                            viewModel = viewModel,
                            openScreen = openScreen,

                            paddingValues = paddingValues,
                            scrollBehavior = scrollBehavior,
                            listState = listState,
                        )
                    }
                }
        }
    }
}
