package com.example.new_app.screens.login

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.new_app.common.ext.isValidEmail
import com.example.new_app.common.snackbar.SnackbarManager
import com.example.new_app.common.util.Resource
import com.example.new_app.domain.repository.AccountService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val accountService: AccountService,
) : ViewModel() {

    var uiState = mutableStateOf(LoginUiState())
        private set

    private val email
        get() = uiState.value.email
    private val password
        get() = uiState.value.password

    private val _authenticationState = MutableStateFlow<Resource<Unit>>(Resource.Empty())
    val authenticationState: StateFlow<Resource<Unit>> get() = _authenticationState


    /**
     * Updates the email value in the UI state when the email input field changes
     */
    fun onEmailChange(newValue: String) {
        uiState.value = uiState.value.copy(email = newValue)
    }

    /**
     * Updates the password value in the UI state when the password input field changes
     */
    fun onPasswordChange(newValue: String) {
        uiState.value = uiState.value.copy(password = newValue)
    }

    /**
     * Handles the user clicking on the "Sign in with Google" button.
     * Updates the authentication state and shows a snackbar with the result message.
     */
    fun onGoogleSignInClick(result: SignInResult) {
        _authenticationState.value = Resource.Loading()
        if (result.data != null) {
            _authenticationState.value = Resource.Success(Unit)
            SnackbarManager.showMessage("Sign in successful with Google")
        } else {
            _authenticationState.value = Resource.Error(result.errorMessage ?: "Unknown error")
            SnackbarManager.showMessage(result.errorMessage ?: "Unknown error")
        }
    }

    /**
     * Handles the user clicking on the "Sign in" button.
     * Authenticates the user with the entered email and password.
     * Updates the authentication state and shows a snackbar with the result message.
     */
    fun onSignInClick() {
        viewModelScope.launch {
            _authenticationState.value = Resource.Loading()
            if (email.isEmpty() || password.isEmpty()) {
                val emptyFieldsMessage = when {
                    email.isEmpty() && password.isEmpty() -> "Email and password are empty"
                    email.isEmpty() -> "Email is empty"
                    else -> "Password is empty"
                }
                _authenticationState.value = Resource.Error(emptyFieldsMessage)
                SnackbarManager.showMessage(emptyFieldsMessage)
            } else {
                when (val result = accountService.authenticate(email, password)) {
                    is Resource.Success -> {
                        _authenticationState.value = Resource.Success(Unit)
                        SnackbarManager.showMessage("Sign in successful")
                    }

                    is Resource.Error -> {
                        _authenticationState.value = Resource.Error(result.message)
                        SnackbarManager.showMessage(result.message ?: "Unknown error")
                    }

                    else -> {
                        _authenticationState.value = Resource.Error("Unknown error")
                        SnackbarManager.showMessage("Unknown error, please try again")
                    }
                }
            }
        }
    }

    /**
     * Handles the user clicking on the "Forget password" button.
     * Sends a password recovery email to the entered email.
     * Updates the authentication state and shows a snackbar with the result message.
     */
    fun onForgetPasswordClick() {
        viewModelScope.launch {
            _authenticationState.value = Resource.Loading()
            if (!email.isValidEmail()) {
                _authenticationState.value = Resource.Error("Invalid email")
                SnackbarManager.showMessage("Invalid email")
                return@launch
            }

            try {
                accountService.sendRecoveryEmail(email)
                _authenticationState.value = Resource.Success(Unit)
                SnackbarManager.showMessage("Password reset email sent")
            } catch (e: Exception) {
                _authenticationState.value = Resource.Error(e.message ?: "Unknown error")
                SnackbarManager.showMessage(e.message ?: "Unknown error")
            } finally {
                _authenticationState.value = Resource.Empty()
            }
        }
    }


    /**
     * Resets the authentication state to avoid showing old success states when returning to the screen.
     */
    fun resetSuccessState() {
        _authenticationState.value = Resource.Empty()
    }
}


