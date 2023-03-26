package com.example.new_app

import android.content.res.Resources
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.new_app.common.snackbar.SnackbarManager
import com.example.new_app.screens.createtask.CreateTaskScreen
import com.example.new_app.screens.login.LoginScreen
import com.example.new_app.screens.signup.SignupScreen
import com.example.new_app.screens.splashscreen.SplashScreen
import com.example.new_app.screens.tasklist.TaskListScreen
import com.example.new_app.theme.New_AppTheme
import kotlinx.coroutines.CoroutineScope

@Composable
@ExperimentalMaterialApi
fun TaskApp() {
    New_AppTheme {
        Surface(color = MaterialTheme.colors.background) {
            val appState = rememberAppState()

            Scaffold(
                snackbarHost = {
                    SnackbarHost(
                        hostState = it,
                        modifier = Modifier.padding(8.dp),
                        snackbar = { snackbarData ->
                            Snackbar(snackbarData, contentColor = MaterialTheme.colors.onPrimary)
                        }
                    )
                },
                scaffoldState = appState.scaffoldState
            ) { innerPaddingModifier ->
                NavHost(
                    navController = appState.navController,
                    startDestination = SPLASH_SCREEN,
                    modifier = Modifier.padding(innerPaddingModifier)
                ) {
                    taskAppGraph(appState)
                }
            }
        }
    }
}

@Composable
fun rememberAppState(
    scaffoldState: ScaffoldState = rememberScaffoldState(),
    navController: NavHostController = rememberNavController(),
    snackbarManager: SnackbarManager = SnackbarManager,
    resources: Resources = resources(),
    coroutineScope: CoroutineScope = rememberCoroutineScope()
) =
    remember(scaffoldState, navController, snackbarManager, resources, coroutineScope) {
        TaskAppState(scaffoldState, navController, snackbarManager, resources, coroutineScope)
    }

@Composable
@ReadOnlyComposable
fun resources(): Resources {
    LocalConfiguration.current
    return LocalContext.current.resources
}

@ExperimentalMaterialApi
fun NavGraphBuilder.taskAppGraph(appState: TaskAppState) {

    composable(SPLASH_SCREEN) {
        SplashScreen(openAndPopUp = { route, popUp -> appState.navigateAndPopUp(route, popUp) })
    }

    composable(LOGIN_SCREEN) {
        LoginScreen(openAndPopUp = { route, popUp -> appState.navigateAndPopUp(route, popUp) })
    }

    composable(SIGN_UP_SCREEN) {
        SignupScreen(openAndPopUp = { route, popUp -> appState.navigateAndPopUp(route, popUp) })
    }

    composable(TASK_LIST_SCREEN) {
        TaskListScreen(openScreen = { route -> appState.navigate(route) })
    }

    composable(
        route = "$CREATE_TASK_SCREEN$TASK_ID_KEY",
        arguments = listOf(navArgument(TASK_ID) { defaultValue = TASK_DEFAULT_ID })
    ) {
        CreateTaskScreen(
            popUpScreen = { appState.popUp() },
            taskId = it.arguments?.getString(TASK_ID) ?: TASK_DEFAULT_ID
        )
    }
}
