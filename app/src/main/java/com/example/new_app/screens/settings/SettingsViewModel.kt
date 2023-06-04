package com.example.new_app.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.new_app.AUTHENTICATION_SCREEN
import com.example.new_app.LOGIN_SCREEN
import com.example.new_app.R
import com.example.new_app.SETTINGS_SCREEN
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

    /**
     * Represents the current state of the settings page.
     * This includes loading, success, error, and empty states.
     */
    private val _settingsState = MutableStateFlow<Resource<Unit>>(Resource.Empty())
    val settingsState: StateFlow<Resource<Unit>> get() = _settingsState

    /**
     * Handles the click event of the sign out button.
     * Tries to sign out the currently signed-in user.
     * Upon completion, updates the settings state and shows a snackbar with a relevant message.
     */
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
    /**
     * Handles the click event of the delete account button.
     * Tries to delete all tasks associated with the current user and then delete the user account.
     * Upon completion, updates the settings state and shows a snackbar with a relevant message.
     */
    fun onDeleteAccountClick(clearBackStack: () -> Unit, navigateToLogin : (String) -> Unit) {
        viewModelScope.launch {
            _settingsState.value = Resource.Loading()
            try {
                firebaseService.deleteAllForUser(accountService.currentUserId)
                accountService.deleteAccount()
                _settingsState.value = Resource.Success(Unit)
                SnackbarManager.showMessage("Successfully deleted account")
                clearBackStack()
                navigateToLogin(AUTHENTICATION_SCREEN)
            } catch (e: Exception) {
                _settingsState.value = Resource.Error(e.message ?: "Unknown error")
                SnackbarManager.showMessage(e.message ?: "Unknown error")
            } finally {
                _settingsState.value = Resource.Empty()
            }
        }
    }

    /**
     * Resets the settings state to its initial state (empty).
     * This function should be called when the settings page is left or when a successful operation is completed.
     */
    fun resetSuccessState() {
        _settingsState.value = Resource.Empty()
    }
}