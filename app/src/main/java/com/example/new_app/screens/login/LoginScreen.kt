package com.example.new_app.screens.login

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.new_app.LOGIN_SCREEN
import com.example.new_app.SIGN_UP_SCREEN
import com.example.new_app.common.composables.CustomPasswordTextField
import com.example.new_app.common.composables.CustomTextField

@Composable
fun LoginScreen(openAndPopUp: (String, String) -> Unit) {
    val viewModel: LoginViewModel = viewModel()
    val uiState by viewModel.uiState


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Login", style = MaterialTheme.typography.h4)

        Spacer(modifier = Modifier.height(16.dp))

        CustomTextField(
            value = uiState.email,
            onValueChange = viewModel::onEmailChange,
            label = "Email"
        )

        Spacer(modifier = Modifier.height(16.dp))

        CustomPasswordTextField(
            value = uiState.password,
            onValueChange = viewModel::onPasswordChange,
            label = "Password"
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { viewModel.onSignInClick { route, popUp -> openAndPopUp(route, popUp) } },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Login")
        }

        Spacer(modifier = Modifier.height(16.dp))


        TextButton(onClick = { openAndPopUp(SIGN_UP_SCREEN, LOGIN_SCREEN) }) {
            Text("Don't have an account? Sign up")
        }
    }
}
