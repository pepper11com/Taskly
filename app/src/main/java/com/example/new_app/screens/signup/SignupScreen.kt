package com.example.new_app.screens.signup

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.new_app.LOGIN_SCREEN
import com.example.new_app.NAVIGATOR_SCREEN
import com.example.new_app.SIGN_UP_SCREEN
import com.example.new_app.common.composables.CustomButton
import com.example.new_app.common.composables.CustomPasswordTextField
import com.example.new_app.common.composables.CustomTextField
import com.example.new_app.common.composables.LoadingIndicator
import com.example.new_app.common.util.Resource
import com.example.new_app.R.string as SignupString


@Composable
fun SignupScreen(
    openAndPopUp: (String, String) -> Unit,
    viewModel: SignupViewModel = hiltViewModel()
) {

    val uiState by viewModel.uiState
    val authenticationState by viewModel.authenticationState.collectAsState()

    Column {
        SignupContent(
            uiState = uiState,
            viewModel = viewModel,
            onSignupClick = {
                viewModel.onSignUpClick()
            },
            onBackToLoginClick = {
                openAndPopUp(LOGIN_SCREEN, SIGN_UP_SCREEN)
            }
        )
        when (authenticationState) {
            is Resource.Success -> {
                openAndPopUp(NAVIGATOR_SCREEN, SIGN_UP_SCREEN)
                viewModel.resetSuccessState()
            }

            is Resource.Error -> {
                openAndPopUp(LOGIN_SCREEN, SIGN_UP_SCREEN)
            }

            is Resource.Loading -> {
                LoadingIndicator()
            }

            else -> {
            }
        }
    }

}

@Composable
fun SignupContent(
    modifier: Modifier = Modifier,
    uiState: SignupUiState,
    viewModel: SignupViewModel,
    onSignupClick: () -> Unit,
    onBackToLoginClick: () -> Unit
) {

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        CustomTextField(
            value = uiState.email,
            onValueChange = viewModel::onEmailChange,
            label = SignupString.email,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(16.dp))

        CustomPasswordTextField(
            value = uiState.password,
            onValueChange = viewModel::onPasswordChange,
            label = "Password",
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        CustomPasswordTextField(
            value = uiState.confirmPassword,
            onValueChange = viewModel::onConfirmPasswordChange,
            label = "Confirm Password",
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        CustomButton(
            text = "Sign Up",
            onClick = { onSignupClick() },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.background
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = { onBackToLoginClick() }) {
            Text("Already have an account? Log in", color = MaterialTheme.colorScheme.secondary)
        }

        if (uiState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
        }

        if (uiState.error != null) {
            Text(
                text = uiState.error,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}

