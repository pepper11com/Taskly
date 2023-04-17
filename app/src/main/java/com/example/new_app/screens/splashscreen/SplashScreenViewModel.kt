package com.example.new_app.screens.splashscreen

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.example.new_app.LOGIN_SCREEN
import com.example.new_app.SPLASH_SCREEN
import com.example.new_app.TASK_LIST_SCREEN
import com.example.new_app.model.User
import com.example.new_app.model.service.AccountService
import com.example.new_app.screens.TaskAppViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashScreenViewModel @Inject constructor(
    private val accountService: AccountService

) : TaskAppViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    init {
        viewModelScope.launch {
            delay(1000)
            _isLoading.value = false
        }
    }


    fun onAppStart(openAndPopUp: (String, String) -> Unit) {
        if(accountService.hasUser) openAndPopUp(TASK_LIST_SCREEN, SPLASH_SCREEN)
        else openAndPopUp(LOGIN_SCREEN, SPLASH_SCREEN)
    }
}
