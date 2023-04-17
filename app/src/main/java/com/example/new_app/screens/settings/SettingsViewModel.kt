package com.example.new_app.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.new_app.common.snackbar.SnackbarManager
import com.example.new_app.common.snackbar.SnackbarMessage
import com.example.new_app.common.util.Resource
import com.example.new_app.model.service.AccountService
import com.example.new_app.model.service.FirebaseService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val accountService: AccountService,
    private val firebaseService: FirebaseService,
    private val snackbarManager: SnackbarManager
) : ViewModel() {


    private val _settingsState = MutableStateFlow<Resource<Unit>>(Resource.Empty())
    val settingsState: StateFlow<Resource<Unit>> get() = _settingsState

    fun onSignOutClick() {
        viewModelScope.launch {
            _settingsState.value = Resource.Loading()
            when (val result = accountService.signOut()) {
                is Resource.Success -> {
                    _settingsState.value = Resource.Success(Unit)
                    snackbarManager.showSnackbarMessage(
                        SnackbarMessage.Text("Successfully logged out")
                    )
                }
                is Resource.Error -> {
                    _settingsState.value = Resource.Error(result.message)
                    snackbarManager.showSnackbarMessage(
                        SnackbarMessage.Text(result.message ?: "Unknown error")
                    )
                }
                else -> {
                    _settingsState.value = Resource.Empty()
                }
            }
        }
    }

    fun onDeleteAccountClick() {
        viewModelScope.launch {
            _settingsState.value = Resource.Loading()
            try {
                firebaseService.deleteAllForUser(accountService.currentUserId)
                accountService.deleteAccount()
                _settingsState.value = Resource.Success(Unit)

                snackbarManager.showSnackbarMessage(
                    SnackbarMessage.Text("Successfully deleted account")
                )
            } catch (e: Exception) {
                _settingsState.value = Resource.Error(e.message ?: "Unknown error")
                snackbarManager.showSnackbarMessage(
                    SnackbarMessage.Text(e.message ?: "Unknown error")
                )
            } finally {
                _settingsState.value = Resource.Empty()
            }
        }
    }

    fun resetSuccessState() {
        _settingsState.value = Resource.Empty()
    }
}