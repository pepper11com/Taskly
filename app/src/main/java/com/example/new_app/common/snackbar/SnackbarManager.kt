package com.example.new_app.common.snackbar

import android.content.res.Resources
import androidx.annotation.StringRes
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object SnackbarManager {
    private val _snackbarMessages = MutableSharedFlow<SnackbarMessage?>()
    val snackbarMessages: SharedFlow<SnackbarMessage?> get() = _snackbarMessages.asSharedFlow()

    suspend fun showSnackbarMessage(message: SnackbarMessage) {
        _snackbarMessages.emit(message)
    }

    fun showMessage(message: SnackbarMessage) {
        _snackbarMessages.tryEmit(message)
    }

    suspend fun clearSnackbarMessages() {
        _snackbarMessages.emit(null)
    }
}


