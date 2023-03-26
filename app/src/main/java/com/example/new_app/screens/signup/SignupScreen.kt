package com.example.new_app.screens.signup

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.new_app.LOGIN_SCREEN
import com.example.new_app.SIGN_UP_SCREEN
import com.example.new_app.TASK_LIST_SCREEN
import com.example.new_app.common.composables.CustomButton
import com.example.new_app.common.composables.CustomPasswordTextField
import com.example.new_app.common.composables.CustomTextField
import com.example.new_app.util.Resource

@Composable
fun SignupScreen(openAndPopUp: (String, String) -> Unit) {
    val viewModel: SignupViewModel = viewModel()
    val uiState by viewModel.uiState

    SignupContent(
        uiState = uiState,
        viewModel = viewModel,
        onSignupClick = { viewModel.onSignUpClick { route, popUp -> openAndPopUp(route, popUp) } },
        onBackToLoginClick = {
            openAndPopUp(LOGIN_SCREEN, SIGN_UP_SCREEN)
        }
    )
}

@Composable
fun SignupContent(
    uiState: SignupUiState,
    viewModel: SignupViewModel,
    onSignupClick: () -> Unit,
    onBackToLoginClick: () -> Unit
) {


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Sign Up",
            style = MaterialTheme.typography.h4,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        CustomTextField(
            value = uiState.email,
            onValueChange = viewModel::onEmailChange,
            label = "Email",
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
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = { onBackToLoginClick() }) {
            Text("Already have an account? Log in")
        }

        if (uiState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
        }

        if (uiState.error != null) {
            Text(
                text = uiState.error ?: "",
                color = MaterialTheme.colors.error,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}

