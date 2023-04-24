package com.example.new_app.screens.login

import androidx.compose.foundation.layout.*
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.new_app.LOGIN_SCREEN
import com.example.new_app.SIGN_UP_SCREEN
import com.example.new_app.TASK_LIST_SCREEN
import com.example.new_app.common.composables.CustomButton
import com.example.new_app.common.composables.CustomPasswordTextField
import com.example.new_app.common.composables.CustomTextField
import com.example.new_app.common.composables.LoadingIndicator
import com.example.new_app.common.util.Resource

@Composable
fun LoginScreen(
    openAndPopUp: (String, String) -> Unit,
    navigateToMainScreen: (String) -> Unit,
) {
    val viewModel: LoginViewModel = hiltViewModel()
    val uiState by viewModel.uiState
    val authenticationState by viewModel.authenticationState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

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

        CustomButton(
            text = "Login",
            onClick = { viewModel.onSignInClick() },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.background
            )
        )

        Spacer(modifier = Modifier.height(16.dp))


        TextButton(onClick = { openAndPopUp(SIGN_UP_SCREEN, LOGIN_SCREEN) }) {
            Text(
                "Don't have an account? Sign up",
                color = MaterialTheme.colorScheme.secondary
            )
        }

        TextButton(onClick = {
            viewModel.onForgetPasswordClick()
        }) {
            Text(
                "Forgot password? Click to reset password",
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }

    when (authenticationState) {
        is Resource.Loading -> {
            // Display a loading indicator
            LoadingIndicator()
        }

        is Resource.Success -> {
            // Handle successful sign-in
            navigateToMainScreen(TASK_LIST_SCREEN)
            viewModel.resetSuccessState()
        }

        is Resource.Error -> {
            // Handle error
        }

        else -> {
            // Handle empty state
        }
    }
}
