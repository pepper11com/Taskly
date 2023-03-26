package com.example.new_app.screens.splashscreen

import androidx.compose.runtime.mutableStateOf
import com.example.new_app.LOGIN_SCREEN
import com.example.new_app.SPLASH_SCREEN
import com.example.new_app.TASK_LIST_SCREEN
import com.example.new_app.model.User
import com.example.new_app.model.service.AccountService
import com.example.new_app.screens.TaskAppViewModel
import kotlinx.coroutines.flow.first

class SplashScreenViewModel : TaskAppViewModel() {

    val error = mutableStateOf(false)
    private val accountService: AccountService = AccountService()

    suspend fun checkUserState(): User {
        return accountService.currentUser.first()
    }

    fun onAppStart(openAndPopUp: (String, String) -> Unit, user: User) {
        println("User-------:  ${user.isValid}")
        if (user.isValid) {
            openAndPopUp(TASK_LIST_SCREEN, SPLASH_SCREEN)
        } else {
            openAndPopUp(LOGIN_SCREEN, SPLASH_SCREEN)
        }
    }
}
