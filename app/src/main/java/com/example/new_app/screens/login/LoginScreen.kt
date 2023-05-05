package com.example.new_app.screens.login

import android.app.Activity.RESULT_OK
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.new_app.model.service.GoogleAuth
import kotlinx.coroutines.launch
import com.example.new_app.R.string as LoginString


@Composable
fun LoginScreen(
    openAndPopUp: (String, String) -> Unit,
    navigateToMainScreen: (String) -> Unit,
    googleAuthUiClient: GoogleAuth
) {
    val viewModel: LoginViewModel = hiltViewModel()
    val uiState by viewModel.uiState
    val authenticationState by viewModel.authenticationState.collectAsState()
    val scope = rememberCoroutineScope()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult(),
        onResult = { result ->
            if (result.resultCode == RESULT_OK) {
                scope.launch {
                    val signInResult = googleAuthUiClient.signInWithIntent(
                        intent = result.data ?: return@launch
                    )
                    viewModel.onGoogleSignInClick(signInResult)
                }
            }
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        CustomTextField(
            value = uiState.email,
            onValueChange = viewModel::onEmailChange,
            label = LoginString.email
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

        TextButton(
            onClick = {
                scope.launch {
                    val signInIntentSender = googleAuthUiClient.signInWithGoogle()
                    launcher.launch(
                        IntentSenderRequest.Builder(
                            signInIntentSender ?: return@launch
                        ).build()
                    )
                }
            }
        ) {
            Text(
                "Login with Google",
                color = MaterialTheme.colorScheme.secondary
            )
        }

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
            navigateToMainScreen(NAVIGATOR_SCREEN)
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
