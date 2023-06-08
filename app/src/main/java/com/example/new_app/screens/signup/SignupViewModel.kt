package com.example.new_app.screens.signup

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.new_app.common.ext.isValidEmail
import com.example.new_app.common.ext.isValidPassword
import com.example.new_app.common.snackbar.SnackbarManager
import com.example.new_app.common.util.Resource
import com.example.new_app.domain.repository.AccountService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignupViewModel @Inject constructor(
    private val accountService: AccountService,
) : ViewModel() {

    /**
     * Represents the current state of the signup UI.
     */
    var uiState = mutableStateOf(SignupUiState())
        private set

    /**
     * Represents the current state of the authentication process.
     * This includes loading, success, error, and empty states.
     */
    private val _authenticationState = MutableStateFlow<Resource<Unit>>(Resource.Empty())
    val authenticationState: StateFlow<Resource<Unit>> get() = _authenticationState

    private val email
        get() = uiState.value.email
    private val password
        get() = uiState.value.password
    private val confirmPassword
        get() = uiState.value.confirmPassword

    /**
     * Updates the email value in the UI state when the email input field changes.
     */
    fun onEmailChange(newValue: String) {
        uiState.value = uiState.value.copy(email = newValue)
    }

    /**
     * Updates the password value in the UI state when the password input field changes.
     */
    fun onPasswordChange(newValue: String) {
        uiState.value = uiState.value.copy(password = newValue)
    }

    /**
     * Updates the confirmation password value in the UI state when the confirmation password input field changes.
     */
    fun onConfirmPasswordChange(newValue: String) {
        uiState.value = uiState.value.copy(confirmPassword = newValue)
    }

    /**
     * Handles the click event of the sign up button.
     * It performs a series of checks on the user's input.
     * If the input is valid, it attempts to create a new account with the provided email and password.
     * Upon completion, updates the authentication state and shows a snackbar with a relevant message.
     */
    fun onSignUpClick() {
        viewModelScope.launch {
            _authenticationState.value = Resource.Loading()

            if (!email.isValidEmail()) {
                _authenticationState.value = Resource.Error("Invalid email")
                SnackbarManager.showMessage("Invalid email")
            }
            else if (!password.isValidPassword()) {
                _authenticationState.value = Resource.Error("Invalid password")
                SnackbarManager.showMessage("Invalid password")
            }
            else if (password != confirmPassword) {
                _authenticationState.value = Resource.Error("Passwords don't match")
                SnackbarManager.showMessage("Passwords don't match")
            } else{
                try {
                    accountService.createAccount(email, password)
                    _authenticationState.value = Resource.Success(Unit)
                    SnackbarManager.showMessage("Account created successfully")
                } catch (e: Exception) {
                    _authenticationState.value = Resource.Error(e.message ?: "Error signing up")
                    SnackbarManager.showMessage(e.message ?: "Error signing up")
                }
            }
        }
    }

    /**
     * Resets the authentication state to its initial state (empty).
     * This function should be called when the signup page is left or when a successful operation is completed.
     */
    fun resetSuccessState() {
        _authenticationState.value = Resource.Empty()
    }
}