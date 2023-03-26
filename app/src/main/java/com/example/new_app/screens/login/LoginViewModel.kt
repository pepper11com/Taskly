package com.example.new_app.screens.login

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.new_app.LOGIN_SCREEN
import com.example.new_app.TASK_LIST_SCREEN
import com.example.new_app.common.ext.isValidEmail
import com.example.new_app.common.ext.isValidPassword
import com.example.new_app.common.snackbar.SnackbarManager
import com.example.new_app.common.snackbar.SnackbarMessage
import com.example.new_app.model.service.AccountService
import kotlinx.coroutines.launch

// Inside the login screen folder
class LoginViewModel : ViewModel() {
    private val accountService = AccountService()

    var uiState = mutableStateOf(LoginUiState())
        private set

    private val email
        get() = uiState.value.email
    private val password
        get() = uiState.value.password

    fun onEmailChange(newValue: String) {
        uiState.value = uiState.value.copy(email = newValue)
    }

    fun onPasswordChange(newValue: String) {
        uiState.value = uiState.value.copy(password = newValue)
    }

    fun onSignInClick(openAndPopUp: (String, String) -> Unit) {
        viewModelScope.launch {
            if (!email.isValidEmail()) {
                SnackbarManager.showSnackbarMessage(SnackbarMessage.Text("Invalid email"))
                return@launch
            }

            if (!password.isValidPassword()) {
                SnackbarManager.showSnackbarMessage(SnackbarMessage.Text("Invalid password"))
                return@launch
            }

            uiState.value = uiState.value.copy(isLoading = true)

            try {
                val user = accountService.authenticate(email, password)
                // Perform any action with the 'user' object if needed
                openAndPopUp(TASK_LIST_SCREEN, LOGIN_SCREEN)
            } catch (e: Exception) {
                SnackbarManager.showSnackbarMessage(SnackbarMessage.Text(e.message ?: "Unknown error"))
            } finally {
                uiState.value = uiState.value.copy(isLoading = false)
            }
        }
    }
}


