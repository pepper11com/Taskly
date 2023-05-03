package com.example.new_app

import android.Manifest
import android.annotation.SuppressLint
import android.content.res.Resources
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.new_app.common.composables.PermissionDialog
import com.example.new_app.common.composables.RationaleSnackbar
import com.example.new_app.common.snackbar.SnackbarManager
import com.example.new_app.model.service.GoogleAuth
import com.example.new_app.screens.authentication.AuthenticationScreen
import com.example.new_app.screens.calender.MonthlyCalendarScreen
import com.example.new_app.screens.calender.WeeklyCalendarViewScreen
import com.example.new_app.screens.calender_screens.CalenderScreens
import com.example.new_app.screens.google_maps.LocationPickerScreen
import com.example.new_app.screens.task.create_edit_tasks.createtask.CreateTaskScreen
import com.example.new_app.screens.login.LoginScreen
import com.example.new_app.screens.screen_switcher.NavigatorScreen
import com.example.new_app.screens.settings.SettingsScreen
import com.example.new_app.screens.signup.SignupScreen
import com.example.new_app.screens.splashscreen.SplashScreen
import com.example.new_app.screens.task.create_edit_tasks.TaskEditCreateViewModel
import com.example.new_app.screens.task.create_edit_tasks.edit_task.EditTaskScreen
import com.example.new_app.screens.task.tasklist.TaskListScreen
import com.example.new_app.screens.task.tasklist.TaskListViewModel
import com.example.new_app.theme.*
import com.example.new_app.theme.New_AppTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import kotlinx.coroutines.CoroutineScope

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun TaskApp(
    mainViewModel: SharedViewModel = hiltViewModel(),
    taskEditCreateViewModel: TaskEditCreateViewModel = hiltViewModel(),
    taskListViewModel: TaskListViewModel = hiltViewModel(),
    googleAuthUiClient: GoogleAuth
) {
    New_AppTheme {

        val appState = rememberAppState()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            RequestNotificationPermissionDialog(
                appState = appState
            )
        }

        Surface(color = MaterialTheme.colorScheme.background) {

            Scaffold(
                snackbarHost = {
                    SnackbarHost(
                        hostState = appState.snackbarState,
                        modifier = Modifier.padding(8.dp),
                        snackbar = { snackbarData ->
                            Snackbar(
                                snackbarData,
                                containerColor = DarkGrey,
                                contentColor = Color.White,
                                actionColor = DarkOrange,
                                dismissActionContentColor = Color.White,
                                modifier = Modifier.padding(8.dp),
                                actionOnNewLine = true
                            )
                        }
                    )
                }
            ) {
                NavHost(
                    navController = appState.navController,
                    startDestination = SPLASH_SCREEN,
                ) {
                    taskAppGraph(
                        appState,
                        mainViewModel,
                        taskEditCreateViewModel,
                        googleAuthUiClient,
                        taskListViewModel
                    )
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RequestNotificationPermissionDialog(
    appState: TaskAppState
) {
    val permissionState = rememberPermissionState(permission = Manifest.permission.POST_NOTIFICATIONS)

    if (!permissionState.status.isGranted) {
        if (permissionState.status.shouldShowRationale) {
            RationaleSnackbar(snackbarHostState = appState.snackbarState, onRequestPermission = { permissionState.launchPermissionRequest() })
        } else {
            PermissionDialog { permissionState.launchPermissionRequest() }
        }
    }
}

@Composable
fun rememberAppState(
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    navController: NavHostController = rememberNavController(),
    snackbarManager: SnackbarManager = SnackbarManager,
    resources: Resources = resources(),
    coroutineScope: CoroutineScope = rememberCoroutineScope()
) =
    remember(snackbarHostState, navController, snackbarManager, resources, coroutineScope) {
        TaskAppState(snackbarHostState, navController, snackbarManager, resources, coroutineScope)
    }

@Composable
@ReadOnlyComposable
fun resources(): Resources {
    LocalConfiguration.current
    return LocalContext.current.resources
}

fun NavGraphBuilder.taskAppGraph(
    appState: TaskAppState,
    mainViewModel: SharedViewModel,
    taskEditCreateViewModel: TaskEditCreateViewModel,
    googleAuthUiClient: GoogleAuth,
    taskListViewModel: TaskListViewModel
) {

    composable(SPLASH_SCREEN) {
        SplashScreen(
            openAndPopUp = { route, popUp -> appState.navigateAndPopUp(route, popUp) },
            clearBackstack = { appState.clearBackstack() }
        )
    }

    composable(SETTINGS_SCREEN){
        SettingsScreen(
            openAndPopUp = { route, popUp -> appState.navigateAndPopUp(route, popUp) },
            navigateToMainScreen = { route -> appState.clearAndNavigate(route) },
            clearAndNavigateAndPopUp = { route, popUp -> appState.clearAndNavigateAndPopUp(route, popUp) },
            clearAndPopUpMultiple = { route, popUpScreens -> appState.clearAndPopUpMultiple(route, popUpScreens) },
            openScreen = { route -> appState.navigate(route) },
            clearBackstack = { appState.clearBackstack() },
            navigateToLogin = { route -> appState.clearAndNavigate(route) }
        )
    }

    composable(AUTHENTICATION_SCREEN) {
        AuthenticationScreen(
            openAndPopUp = { route, popUp -> appState.navigateAndPopUp(route, popUp) },
            navigateToMainScreen = { route -> appState.clearAndNavigate(route) },
            googleAuthUiClient = googleAuthUiClient
        )
    }

    composable(LOGIN_SCREEN) {
        LoginScreen(
            openAndPopUp = { route, popUp -> appState.navigateAndPopUp(route, popUp) },
            navigateToMainScreen = { route -> appState.clearAndNavigate(route) },
            googleAuthUiClient = googleAuthUiClient
        )
    }

    composable(SIGN_UP_SCREEN) {
        SignupScreen(
            openAndPopUp = { route, popUp -> appState.navigateAndPopUp(route, popUp) }
        )
    }

    composable(TASK_LIST_SCREEN) {
        TaskListScreen(
            openScreen = { route -> appState.navigate(route) },
            mainViewModel = mainViewModel,
            userData = googleAuthUiClient.getSignedInUser(),
            taskEditCreateViewModel = taskEditCreateViewModel
        )
    }

    composable(NAVIGATOR_SCREEN){
        NavigatorScreen(
            openScreen = { route -> appState.navigate(route) },
            mainViewModel = mainViewModel,
            userData = googleAuthUiClient.getSignedInUser(),
            taskEditCreateViewModel = taskEditCreateViewModel,
            viewModel = taskListViewModel
        )
    }

    composable(CALENDAR_SCREENS){
        CalenderScreens(
            openScreen = { route -> appState.navigate(route) },
            userData = googleAuthUiClient.getSignedInUser(),
            viewModel = taskListViewModel,
            taskEditCreateViewModel = taskEditCreateViewModel,
        )
    }

//    composable(MONTHLY_CALENDAR_SCREEN){
//        MonthlyCalendarScreen(
//            openScreen = { route -> appState.navigate(route) },
//            viewModel = taskListViewModel
//        )
//    }
//
//    composable(WEEKLY_CALENDAR_VIEW_SCREEN){
//        WeeklyCalendarViewScreen(
//            openScreen = { route -> appState.navigate(route) },
//            viewModel = taskListViewModel
//        )
//    }

    composable(TASK_MAP_SCREEN){
        LocationPickerScreen(
            openAndPopUp = { route, popUp -> appState.navigateAndPopUp(route, popUp) },
            openScreen = { route -> appState.navigate(route) },
            viewModel = taskEditCreateViewModel,
            popUpScreen = { appState.popUp() }
        )
    }

    composable(
        route = "$EDIT_TASK_SCREEN$TASK_ID_KEY",
        arguments = listOf(navArgument(TASK_ID) { defaultValue = TASK_DEFAULT_ID })
    ) {
        EditTaskScreen(
            popUpScreen = { appState.popUp() },
            taskId = it.arguments?.getString(TASK_ID) ?: TASK_DEFAULT_ID,
            userId = it.arguments?.getString("userId") ?: "",
            mainViewModel = mainViewModel,
            openScreen = { route -> appState.navigate(route) },
            viewModel = taskEditCreateViewModel
        )
    }

    composable(
        route = "$CREATE_TASK_SCREEN$TASK_ID_KEY",
        arguments = listOf(navArgument(TASK_ID) { defaultValue = TASK_DEFAULT_ID })
    ) {
        CreateTaskScreen(
            popUpScreen = { appState.popUp() },
            userId = it.arguments?.getString("userId") ?: "",
            mainViewModel = mainViewModel,
            openScreen = { route -> appState.navigate(route) },
            viewModel = taskEditCreateViewModel
        )
    }
}
