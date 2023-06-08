package com.example.new_app.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.new_app.SPLASH_SCREEN
import com.example.new_app.common.composables.custom_composables.CustomButton
import com.example.new_app.common.composables.top_app_bars.CustomTopAppBarSmall
import com.example.new_app.common.composables.LoadingIndicator
import com.example.new_app.common.util.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    openAndPopUp: (String, String) -> Unit,
    navigateToMainScreen: (String) -> Unit,
    clearAndNavigateAndPopUp: (String, String) -> Unit,
    clearAndPopUpMultiple: (String, List<String>) -> Unit,

    openScreen: (String) -> Unit,
    clearBackstack: () -> Unit,
    navigateToLogin: (String) -> Unit = {},
    popUpScreen: () -> Unit = {}
) {
    val viewModel: SettingsViewModel = hiltViewModel()
    val showDialog = remember { mutableStateOf(false) }
    val authenticationState by viewModel.settingsState.collectAsState()

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    val listState = rememberLazyListState()

    Scaffold(
        topBar = {
            CustomTopAppBarSmall(
                title = "Settings",
                scrollBehavior = scrollBehavior,
                popUpScreen = popUpScreen
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.padding(paddingValues).fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item{
                CustomButton(
                    onClick = { viewModel.onSignOutClick() },
                    text = "Logout",
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                CustomButton(
                    onClick = { showDialog.value = true },
                    text = "Delete Account",
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }



    if (showDialog.value) {
        AlertDialog(
            onDismissRequest = { showDialog.value = false },
            title = { Text(text = "Confirm Account Deletion") },
            text = { Text(text = "Are you sure you want to delete your account? All your data will be lost.") },
            confirmButton = {
                CustomButton(
                    onClick = {
                        viewModel.onDeleteAccountClick(clearBackstack, navigateToLogin)
                        showDialog.value = false
                    },
                    text = "Yes, Delete Account",
                    modifier = Modifier.padding(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                )
            },
            dismissButton = {
                CustomButton(
                    onClick = { showDialog.value = false },
                    text = "Cancel",
                    modifier = Modifier.padding(8.dp)
                )
            },
            modifier = Modifier.padding(16.dp)
        )
    }

    when (authenticationState) {
        is Resource.Loading -> {
            // Display a loading indicator
            LoadingIndicator()
        }

        is Resource.Success -> {
            LaunchedEffect(key1 = authenticationState) {
                viewModel.resetSuccessState()
                clearBackstack()
                navigateToLogin(SPLASH_SCREEN)
            }
        }

        is Resource.Error -> {
            // Handle error
        }

        else -> {
            // Handle empty state
        }
    }

}
