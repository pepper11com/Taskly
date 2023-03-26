package com.example.new_app.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.new_app.LOGIN_SCREEN
import kotlinx.coroutines.launch

// we need to use FirebaseService and AccountService
//class SettingsViewModel() : ViewModel() {
//    private val authRepository = AuthRepository()
//
//
//
//    fun onSignOutClick(restartApp: (String) -> Unit) {
//        viewModelScope.launch {
//            try {
//                authRepository.signOut()
//                restartApp(LOGIN_SCREEN)
//            } catch (e: Exception) {
//                // Handle error
//                println(e)
//            }
//
//        }
//    }
//
//    fun onAccountDeleteClick() {
//        viewModelScope.launch {
//            try {
//                authRepository.deleteAccount()
//                firebaseService.deleteAllForUser(accountService.currentUserId)
//            } catch (e: Exception) {
//                // Handle error
//                println(e)
//            }
//        }
//    }
//
//}