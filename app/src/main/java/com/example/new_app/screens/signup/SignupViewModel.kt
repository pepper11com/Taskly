package com.example.new_app.screens.signup

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.new_app.SETTINGS_SCREEN
import com.example.new_app.SIGN_UP_SCREEN
import com.example.new_app.TASK_LIST_SCREEN
import com.example.new_app.common.ext.isValidEmail
import com.example.new_app.common.ext.isValidPassword
import com.example.new_app.common.snackbar.SnackbarManager
import com.example.new_app.common.snackbar.SnackbarMessage
import com.example.new_app.model.service.AccountService
import com.example.new_app.model.service.FirebaseService
import kotlinx.coroutines.launch

class SignupViewModel : ViewModel() {
    private val accountService = AccountService()
    private val firebaseService = FirebaseService()

    var uiState = mutableStateOf(SignupUiState())
        private set

    private val email
        get() = uiState.value.email
    private val password
        get() = uiState.value.password
    private val confirmPassword
        get() = uiState.value.confirmPassword

    fun onEmailChange(newValue: String) {
        uiState.value = uiState.value.copy(email = newValue)
    }

    fun onPasswordChange(newValue: String) {
        uiState.value = uiState.value.copy(password = newValue)
    }

    fun onConfirmPasswordChange(newValue: String) {
        uiState.value = uiState.value.copy(confirmPassword = newValue)
    }

    fun onSignUpClick(openAndPopUp: (String, String) -> Unit) {
        viewModelScope.launch {
            if (!email.isValidEmail()) {
                SnackbarManager.showSnackbarMessage(SnackbarMessage.Text("Invalid email"))
                return@launch
            }

            if (!password.isValidPassword()) {
                SnackbarManager.showSnackbarMessage(SnackbarMessage.Text("Invalid password"))
                return@launch
            }

            if (password != confirmPassword) {
                SnackbarManager.showSnackbarMessage(SnackbarMessage.Text("Passwords don't match"))
                return@launch
            }

            uiState.value = uiState.value.copy(isLoading = true)
            try {
                accountService.createAccount(email, password)
                uiState.value = uiState.value.copy(isLoading = false, error = null)
                openAndPopUp(TASK_LIST_SCREEN, SIGN_UP_SCREEN)
            } catch (e: Exception) {
                uiState.value = uiState.value.copy(isLoading = false, error = e.message)
                SnackbarManager.showSnackbarMessage(SnackbarMessage.Text(e.message ?: "Error signing up"))
            }
        }
    }

}

