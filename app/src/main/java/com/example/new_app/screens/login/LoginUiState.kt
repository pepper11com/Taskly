package com.example.new_app.screens.login

data class LoginUiState(
    val email: String = "",
    val password: String = "",
)

data class SignInResult(
    val data: UserData?,
    val errorMessage: String?
)

data class UserData(
    val userId: String,
    val username: String?,
    val profilePictureUrl: String?,
)
