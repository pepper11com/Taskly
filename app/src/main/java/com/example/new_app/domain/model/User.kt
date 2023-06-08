package com.example.new_app.domain.model

data class User (
    val id: String = "",
){
    val isValid: Boolean
        get() = id.isNotEmpty()
}
