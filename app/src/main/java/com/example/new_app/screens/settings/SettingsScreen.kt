package com.example.new_app.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.AlertDialog
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.new_app.LOGIN_SCREEN
import com.example.new_app.SETTINGS_SCREEN
import com.example.new_app.SPLASH_SCREEN
import com.example.new_app.common.composables.CustomButton


@Composable
fun SettingsScreen(
    openAndPopUp: (String, String) -> Unit
) {
    val viewModel: SettingsViewModel = viewModel()
    val showDialog = remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CustomButton(
            onClick = { viewModel.onSignOutClick { route -> openAndPopUp(SPLASH_SCREEN, SETTINGS_SCREEN) } },
            text = "Logout",
            modifier = Modifier.padding(vertical = 8.dp)
        )

        CustomButton(
            onClick = { showDialog.value = true },
            text = "Delete Account",
            modifier = Modifier.padding(vertical = 8.dp)
        )
    }

    if (showDialog.value) {
        AlertDialog(
            onDismissRequest = { showDialog.value = false },
            title = { Text(text = "Confirm Account Deletion") },
            text = { Text(text = "Are you sure you want to delete your account? All your data will be lost.") },
            confirmButton = {
                CustomButton(
                    onClick = {
                        viewModel.onDeleteAccountClick { route -> openAndPopUp(SPLASH_SCREEN, SETTINGS_SCREEN) }
                        showDialog.value = false
                    },
                    text = "Yes, Delete Account",
                    modifier = Modifier.padding(8.dp),
                    colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.error)
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
}
