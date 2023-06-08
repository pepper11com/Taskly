package com.example.new_app.screens.task.create_edit_tasks.createtask

import android.graphics.*
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.new_app.TaskViewModel
import com.example.new_app.TASK_MAP_SCREEN
import com.example.new_app.common.composables.CardEditors
import com.example.new_app.common.composables.ColorPicker
import com.example.new_app.common.composables.top_app_bars.CustomCreateTaskAppBar
import com.example.new_app.common.composables.custom_composables.CustomMultiLineTextfield
import com.example.new_app.common.composables.custom_composables.CustomTextField
import com.example.new_app.common.composables.LoadingIndicator
import com.example.new_app.common.composables.PickImageFromGallery
import com.example.new_app.common.composables.custom_composables.SectionTitle
import com.example.new_app.common.composables.ShowLocation
import com.example.new_app.common.composables.customTextFieldColors
import com.example.new_app.common.ext.divider
import com.example.new_app.common.ext.lazyColumn
import com.example.new_app.common.util.Resource
import com.example.new_app.screens.task.create_edit_tasks.TaskEditCreateViewModel
import com.example.new_app.R.string as CreateTaskString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTaskScreen(
    popUpScreen: () -> Unit,
    userId: String,
    mainViewModel: TaskViewModel,
    openScreen: (String) -> Unit,
    viewModel: TaskEditCreateViewModel
) {

    val task by viewModel.task
    val context = LocalContext.current
    val newTaskState by viewModel.taskEditCreateState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val galleryLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                viewModel.onImageChange(it)
            }
        }

    LaunchedEffect(Unit) {
        viewModel.initialize(null)
    }

    Scaffold(
        topBar = {
            CustomCreateTaskAppBar(
                task = task,
                viewModel = viewModel,
                popUpScreen = popUpScreen,
                scrollBehavior = scrollBehavior,
                mainViewModel = mainViewModel,
                context = context
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .lazyColumn(innerPadding)
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            item {
                SectionTitle(CreateTaskString.image)
                PickImageFromGallery(
                    LocalContext.current,
                    viewModel,
                    task,
                    userId,
                    galleryLauncher
                )
            }

            item {
                Divider()
            }

            item {
                SectionTitle(CreateTaskString.color)
                task.color?.let {
                    ColorPicker(
                        it,
                        viewModel::onColorChange
                    )
                }
            }

            item {
                Divider()
            }

            item {
                SectionTitle(CreateTaskString.title_description)
                CustomTextField(
                    value = task.title,
                    onValueChange = viewModel::onTitleChange,
                    label = CreateTaskString.title,
                    modifier = Modifier.fillMaxWidth(),
                    colors = customTextFieldColors(),
                )

                CustomMultiLineTextfield(
                    value = task.description,
                    onValueChange = viewModel::onDescriptionChange,
                    modifier = Modifier.fillMaxWidth(),
                    hintText = CreateTaskString.description,
                    textStyle = MaterialTheme.typography.bodyLarge,
                    maxLines = 4,
                    colors = customTextFieldColors()
                )
            }

            item {
                Divider(Modifier.divider())
            }

            item {
                SectionTitle(CreateTaskString.location_date_time_notification)
                ShowLocation(
                    viewModel.locationDisplay,
                    onEditClick = { openScreen(TASK_MAP_SCREEN) },
                    onLocationReset = viewModel::onLocationReset
                )
                CardEditors(
                    task,
                    viewModel::onDateChange,
                    viewModel::onTimeChange,
                    viewModel = viewModel
                )
            }
        }

        when (newTaskState) {
            is Resource.Loading -> {
                LoadingIndicator()
            }
            else -> {}
        }
    }
}





