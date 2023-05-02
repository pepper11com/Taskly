package com.example.new_app.screens.task.create_edit_tasks.createtask

import android.annotation.SuppressLint
import android.graphics.*
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.new_app.SharedViewModel
import com.example.new_app.TASK_MAP_SCREEN
import com.example.new_app.common.composables.CardEditors
import com.example.new_app.common.composables.ColorPicker
import com.example.new_app.common.composables.CustomCreateTaskAppBar
import com.example.new_app.common.composables.CustomMultiLineTextfield
import com.example.new_app.common.composables.CustomTextField
import com.example.new_app.common.composables.LoadingIndicator
import com.example.new_app.common.composables.PickImageFromGallery
import com.example.new_app.common.composables.SectionTitle
import com.example.new_app.common.composables.ShowLocation
import com.example.new_app.common.composables.customTextFieldColors
import com.example.new_app.common.util.Resource
import com.example.new_app.screens.task.create_edit_tasks.TaskEditCreateViewModel
import com.example.new_app.theme.DarkGrey


@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun CreateTaskScreen(
    popUpScreen: () -> Unit,
    taskId: String,
    userId: String,
    mainViewModel: SharedViewModel,
    openAndPopUp: (String, String) -> Unit,
    openScreen: (String) -> Unit,
    viewModel: TaskEditCreateViewModel
) {

    val task by viewModel.task
    val context = LocalContext.current
    val newTaskState by viewModel.taskEditCreateState.collectAsState()

    val galleryLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                viewModel.onImageChange(it)
            }
        }

    LaunchedEffect(Unit) {
        viewModel.initialize(null)
    }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

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
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            item {
                SectionTitle("Image")
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
                SectionTitle("Color")
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
                // Title Section
                SectionTitle("Title & Description")
                CustomTextField(
                    value = task.title,
                    onValueChange = viewModel::onTitleChange,
                    label = "Title",
                    modifier = Modifier.fillMaxWidth(),
                    colors = customTextFieldColors(),
                )

                CustomMultiLineTextfield(
                    value = task.description,
                    onValueChange = viewModel::onDescriptionChange,
                    modifier = Modifier.fillMaxWidth(),
                    hintText = "Description",
                    textStyle = MaterialTheme.typography.bodyLarge,
                    maxLines = 4,
                    colors = customTextFieldColors()
                )
            }


            item {
                Divider(
                    modifier = Modifier
                        .padding(top = 18.dp)
                )
            }

            item {
                // Date and Time Section
                SectionTitle("Location, Date, Time & Notification")
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
                // Display a loading indicator
                LoadingIndicator()
            }

            is Resource.Success -> {

            }

            is Resource.Error -> {
                // Handle error
            }

            else -> {
                // Handle empty state
            }
        }
    }
}





