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
import com.example.new_app.common.util.Resource
import com.example.new_app.model.service.AccountService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val accountService: AccountService,
    private val snackbarManager: SnackbarManager
) : ViewModel() {

    var uiState = mutableStateOf(LoginUiState())
        private set

    private val email
        get() = uiState.value.email
    private val password
        get() = uiState.value.password

    private val _authenticationState = MutableStateFlow<Resource<Unit>>(Resource.Empty())
    val authenticationState: StateFlow<Resource<Unit>> get() = _authenticationState

    fun onEmailChange(newValue: String) {
        uiState.value = uiState.value.copy(email = newValue)
    }

    fun onPasswordChange(newValue: String) {
        uiState.value = uiState.value.copy(password = newValue)
    }

    fun onSignInClick() {
        viewModelScope.launch {
            _authenticationState.value = Resource.Loading()
            when (val result = accountService.authenticate(email, password)) {
                is Resource.Success -> {
                    _authenticationState.value = result.data?.let { Resource.Success(it) }!!
                    // TODO - Handle successful sign-in
//                    openAndPopUp(TASK_LIST_SCREEN, LOGIN_SCREEN)
                }
                is Resource.Error -> {
                    if (password.isEmpty() && email.isEmpty()){
                        _authenticationState.value = Resource.Error("Email and password are empty")
                        snackbarManager.showSnackbarMessage(
                            SnackbarMessage.Text("Email and password are empty")
                        )


                    } else if(password.isEmpty()) {
                        _authenticationState.value = Resource.Error("Password is empty")
                        snackbarManager.showSnackbarMessage(
                            SnackbarMessage.Text("Password is empty")
                        )
                    }
                    else if (email.isEmpty()){
                        _authenticationState.value = Resource.Error("Email is empty")
                        snackbarManager.showSnackbarMessage(
                            SnackbarMessage.Text("Email is empty")
                        )
                    } else {
                        _authenticationState.value = Resource.Error(result.message)
                        snackbarManager.showSnackbarMessage(
                            SnackbarMessage.Text(result.message ?: "Unknown error")
                        )
                    }
                }
                else -> {
                    _authenticationState.value = Resource.Empty()
                }
            }
        }
    }

    fun onForgetPasswordClick() {
        viewModelScope.launch {
            _authenticationState.value = Resource.Loading()
            if (!email.isValidEmail()) {
                _authenticationState.value = Resource.Error("Invalid email")
                snackbarManager.showSnackbarMessage(SnackbarMessage.Text("Invalid email"))
                return@launch
            }


            try {
                accountService.sendRecoveryEmail(email)
                _authenticationState.value = Resource.Success(Unit)
                snackbarManager.showSnackbarMessage(SnackbarMessage.Text("Password reset email sent"))
            } catch (e: Exception) {
                _authenticationState.value = Resource.Error(e.message ?: "Unknown error")
                snackbarManager.showSnackbarMessage(
                    SnackbarMessage.Text(
                        e.message ?: "Unknown error"
                    )
                )
            } finally {
                _authenticationState.value = Resource.Empty()
            }
        }
    }

    fun resetSuccessState() {
        _authenticationState.value = Resource.Empty()
    }
}


