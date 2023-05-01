package com.example.new_app.screens.splashscreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.new_app.AUTHENTICATION_SCREEN
import com.example.new_app.NAVIGATOR_SCREEN
import com.example.new_app.SPLASH_SCREEN
import com.example.new_app.model.service.AccountService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashScreenViewModel @Inject constructor(
    private val accountService: AccountService

) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    init {
        viewModelScope.launch {
            delay(1000)
            _isLoading.value = false
        }
    }


    fun onAppStart(openAndPopUp: (String, String) -> Unit, clearBackstack: () -> Unit) {
        if (accountService.hasUser) {
            clearBackstack()
            openAndPopUp(NAVIGATOR_SCREEN, SPLASH_SCREEN)
        } else {
            clearBackstack()
            openAndPopUp(AUTHENTICATION_SCREEN, SPLASH_SCREEN)
        }
    }
}
