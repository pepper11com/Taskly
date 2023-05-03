package com.example.new_app.screens.signup

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.new_app.common.ext.isValidEmail
import com.example.new_app.common.ext.isValidPassword
import com.example.new_app.common.snackbar.SnackbarManager
import com.example.new_app.common.util.Resource
import com.example.new_app.model.service.AccountService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignupViewModel @Inject constructor(
    private val accountService: AccountService,
) : ViewModel() {

    var uiState = mutableStateOf(SignupUiState())
        private set

    private val _authenticationState = MutableStateFlow<Resource<Unit>>(Resource.Empty())
    val authenticationState: StateFlow<Resource<Unit>> get() = _authenticationState


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

    fun onSignUpClick() {
        viewModelScope.launch {
            _authenticationState.value = Resource.Loading()

            if (!email.isValidEmail()) {
                _authenticationState.value = Resource.Error("Invalid email")
                SnackbarManager.showMessage("Invalid email")
                return@launch
            }
            if (!password.isValidPassword()) {
                _authenticationState.value = Resource.Error("Invalid password")
                SnackbarManager.showMessage("Invalid password")
                return@launch
            }
            if (password != confirmPassword) {
                _authenticationState.value = Resource.Error("Passwords don't match")
                SnackbarManager.showMessage("Passwords don't match")
                return@launch
            }

            try {
                accountService.createAccount(email, password)
                _authenticationState.value = Resource.Success(Unit)
            } catch (e: Exception) {
                _authenticationState.value = Resource.Error(e.message ?: "Error signing up")
                SnackbarManager.showMessage(e.message ?: "Error signing up")
            } finally {
                _authenticationState.value = Resource.Empty()
            }
        }
    }

    fun resetSuccessState() {
        _authenticationState.value = Resource.Empty()
    }

}

