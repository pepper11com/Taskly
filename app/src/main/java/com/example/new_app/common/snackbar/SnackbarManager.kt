package com.example.new_app.common.snackbar

import androidx.annotation.StringRes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object SnackbarManager {
    val messages: MutableStateFlow<SnackbarMessage?> = MutableStateFlow(null)
    val snackbarMessages: StateFlow<SnackbarMessage?>
        get() = messages.asStateFlow()

    fun showMessage(message: String) {
        messages.value = SnackbarMessage.StringSnackbar(message)
    }

    fun showMessage(@StringRes messageId: Int) {
        messages.value = SnackbarMessage.ResourceSnackbar(messageId)
    }

    fun showMessage(snackbarMessage: SnackbarMessage) {
        messages.value = snackbarMessage
    }
}


