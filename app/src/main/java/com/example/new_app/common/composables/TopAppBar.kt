package com.example.new_app.common.composables

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Deselect
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.NearMeDisabled
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter
import coil.request.ImageRequest
import coil.transform.CircleCropTransformation
import com.example.new_app.SETTINGS_SCREEN
import com.example.new_app.SharedViewModel
import com.example.new_app.model.Task
import com.example.new_app.screens.task.create_edit_tasks.TaskEditCreateViewModel
import com.example.new_app.screens.task.tasklist.TaskListUiState
import com.example.new_app.screens.task.tasklist.TaskListViewModel
import com.example.new_app.screens.task.tasklist.TaskSortType
import com.example.new_app.screens.task.tasklist.onSelectAllTasks

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTopAppBar(
    title: String,
    selectedIndex: MutableState<Int>,
    selectedTasks: SnapshotStateList<Task>,
    uiState: TaskListUiState,
    openScreen: (String) -> Unit,
    viewModel: TaskListViewModel,
    scrollBehavior: TopAppBarScrollBehavior,
    mainViewModel: SharedViewModel,
    mapsVisible: MutableState<Boolean>,
    userProfilePictureUrl: String? // Add this parameter
) {
    val selectedTaskIds by mainViewModel.selectedTaskIds.collectAsState()

    TopAppBar(
        modifier = Modifier.fillMaxWidth(),
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
        ),
        actions = {
            if (selectedIndex.value != 1) {
                if (selectedTaskIds.isEmpty()) {
                    IconButton(
                        onClick = {
                            onSelectAllTasks(
                                selectedIndex.value,
                                mainViewModel,
                                uiState.tasks
                            )
                        }
                    ) {
                        Icon(
                            Icons.Default.SelectAll,
                            contentDescription = "Select All",
                            tint = Color.White
                        )
                    }
                } else {
                    if (selectedTaskIds.isNotEmpty()) {
                        IconButton(onClick = { mainViewModel.clearSelectedTaskIds() }) {
                            Icon(
                                Icons.Default.Deselect,
                                contentDescription = "Deselect All",
                                tint = Color.White
                            )
                        }
                    }
                }
            }

            IconButton(
                onClick = {
                    mapsVisible.value = !mapsVisible.value
                }
            ) {
                Icon(
                    imageVector = if (mapsVisible.value) Icons.Default.LocationOn else Icons.Default.LocationOff,
                    contentDescription = "Toggle Map",
                    tint = Color.White
                )
            }

            DropdownContextMenu(
                options = listOf(
                    "Sort by Date Created (Asc)",
                    "Sort by Date Created (Desc)",
                    "Sort by Title (A-Z)",
                    "Sort by Title (Z-A)",
                    "Sort by Due Date (Asc)",
                    "Sort by Due Date (Desc)"
                ),
                modifier = Modifier.padding(end = 8.dp),
                onActionClick = { action ->
                    when (action) {
                        "Sort by Date Created (Asc)" -> viewModel.updateSortType(TaskSortType.DATE_CREATED_ASC)
                        "Sort by Date Created (Desc)" -> viewModel.updateSortType(TaskSortType.DATE_CREATED_DESC)
                        "Sort by Title (A-Z)" -> viewModel.updateSortType(TaskSortType.TITLE_ASC)
                        "Sort by Title (Z-A)" -> viewModel.updateSortType(TaskSortType.TITLE_DESC)
                        "Sort by Due Date (Asc)" -> viewModel.updateSortType(TaskSortType.DUE_DATE_ASC)
                        "Sort by Due Date (Desc)" -> viewModel.updateSortType(TaskSortType.DUE_DATE_DESC)
                    }
                },
                imageVector = Icons.Default.Tune,
                trailingIcon = Icons.Default.Done
            )
            if (selectedTaskIds.isEmpty()) {
                if (userProfilePictureUrl != null) {
                    UserImage(
                        userProfilePictureUrl = userProfilePictureUrl,
                        openScreen = openScreen
                    )
                } else {
                    IconButton(onClick = { openScreen(SETTINGS_SCREEN) }) {
                        Icon(
                            Icons.Filled.Settings,
                            contentDescription = "Settings",
                            tint = Color.White
                        )
                    }
                }
            } else {
                DropdownContextMenu(
                    options = listOf("Delete All Selected"),
                    modifier = Modifier.padding(end = 8.dp),
                    onActionClick = { action ->
                        when (action) {
                            "Delete All Selected" -> {
                                viewModel.onDeleteSelectedTasks(selectedTaskIds.toList())
                                mainViewModel.clearSelectedTaskIds()
                            }
                        }
                    }
                )
            }
        },
        title = {
            Text(
                text = title,
                color = Color.White,
            )
        }
    )
}

@Composable
fun UserImage(
    userProfilePictureUrl: String?,
    openScreen: (String) -> Unit,
) {
    userProfilePictureUrl?.let { url ->
        IconButton(
            onClick = {
                openScreen(SETTINGS_SCREEN)
            }
        ) {
            Image(
                painter = rememberAsyncImagePainter(
                    ImageRequest.Builder(LocalContext.current).data(data = url)
                        .apply<ImageRequest.Builder>(block = fun ImageRequest.Builder.() {
                            crossfade(true)
                            transformations(CircleCropTransformation())
                        }).build()
                ),
                contentDescription = "User Profile Picture",
                modifier = Modifier
                    .size(44.dp)
                    .padding(start = 8.dp, end = 8.dp)
                    .clip(CircleShape)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomCreateTaskAppBar(
    task: Task,
    viewModel: TaskEditCreateViewModel,
    popUpScreen: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior,
    mainViewModel: SharedViewModel,
    context: Context
) {
    val notificationResultLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                mainViewModel.onPermissionResult(
                    isGranted = isGranted,
                    permission = Manifest.permission.POST_NOTIFICATIONS
                )
            }
        }
    )

    Column {
        MediumTopAppBar(
            scrollBehavior = scrollBehavior,
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background,
            ),
            title = { Text("Create Task", color = Color.White) },
            navigationIcon = {
                IconButton(
                    onClick = {
                        popUpScreen()
                        viewModel.resetTask()
                    }
                ) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
            },
            actions = {
                IconButton(
                    enabled = task.title.isNotBlank() && task.description.isNotBlank(),

                    onClick = {

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            notificationResultLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }

                        viewModel.onDoneClick(
                            context,
                            null,
                            popUpScreen,
                            onTaskCreated = { newTaskId ->
                                mainViewModel.updateLastAddedTaskId(
                                    newTaskId
                                )
                            }
                        )
                    }
                ) {
                    Icon(
                        Icons.Filled.Done,
                        contentDescription = "Done",
                        tint = if (task.title.isNotBlank() && task.description.isNotBlank())
                            MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.5f)
                    )
                }
            }
        )

        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            color = Color.White.copy(alpha = 0.5f),
            thickness = 1.dp
        )
    }
}