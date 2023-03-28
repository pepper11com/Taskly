package com.example.new_app.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.new_app.LOGIN_SCREEN
import com.example.new_app.SPLASH_SCREEN
import com.example.new_app.model.service.AccountService
import com.example.new_app.model.service.FirebaseService
import kotlinx.coroutines.launch

class SettingsViewModel() : ViewModel() {
    private val accountService = AccountService()
    private val firebaseService = FirebaseService()

    fun onSignOutClick(restartApp: (String) -> Unit) {
        viewModelScope.launch {
            try {
                accountService.signOut()
                restartApp(SPLASH_SCREEN)
            } catch (e: Exception) {
                // Handle error
                println(e)
            }

        }
    }

    fun onDeleteAccountClick(restartApp: (String) -> Unit) {
        viewModelScope.launch {
            try {
                firebaseService.deleteAllForUser(accountService.currentUserId)

                accountService.deleteAccount()
                restartApp(SPLASH_SCREEN)
            } catch (e: Exception) {
                // Handle error
                println(e)
            }

        }
    }


}