package com.example.new_app.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.new_app.R
import com.example.new_app.common.snackbar.SnackbarManager
import com.example.new_app.common.snackbar.SnackbarMessage
import com.example.new_app.common.util.Resource
import com.example.new_app.model.service.AccountService
import com.example.new_app.model.service.FirebaseService
import com.example.new_app.model.service.GoogleAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val accountService: AccountService,
    private val firebaseService: FirebaseService,
) : ViewModel() {

    private val _settingsState = MutableStateFlow<Resource<Unit>>(Resource.Empty())
    val settingsState: StateFlow<Resource<Unit>> get() = _settingsState

    fun onSignOutClick() {
        viewModelScope.launch {
            _settingsState.value = Resource.Loading()
            when (val result = accountService.signOut()) {
                is Resource.Success -> {
                    _settingsState.value = Resource.Success(Unit)
                    SnackbarManager.showMessage("Successfully logged out")
                }
                is Resource.Error -> {
                    _settingsState.value = Resource.Error(result.message)
                    SnackbarManager.showMessage(result.message ?: "Unknown error")
                }
                else -> {
                    _settingsState.value = Resource.Empty()
                }
            }
        }
    }

    //TODO - Also delete the images from firebase storage
    fun onDeleteAccountClick() {
        viewModelScope.launch {
            _settingsState.value = Resource.Loading()
            try {
                firebaseService.deleteAllForUser(accountService.currentUserId)
                accountService.deleteAccount()
                _settingsState.value = Resource.Success(Unit)
                SnackbarManager.showMessage("Successfully deleted account")
            } catch (e: Exception) {
                _settingsState.value = Resource.Error(e.message ?: "Unknown error")
                SnackbarManager.showMessage(e.message ?: "Unknown error")
            } finally {
                _settingsState.value = Resource.Empty()
            }
        }
    }

    fun resetSuccessState() {
        _settingsState.value = Resource.Empty()
    }
}