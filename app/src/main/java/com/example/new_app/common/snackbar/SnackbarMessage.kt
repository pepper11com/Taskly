package com.example.new_app.common.snackbar

import android.content.res.Resources
import androidx.annotation.StringRes

sealed class SnackbarMessage {
    data class Text(val text: String) : SnackbarMessage()
    data class ResourceId(@StringRes val resourceId: Int) : SnackbarMessage()

    fun toMessage(resources: Resources): String = when (this) {
        is Text -> text
        is ResourceId -> resources.getString(resourceId)
    }

    companion object {
        fun Throwable.toSnackbarMessage(): SnackbarMessage {
            val message = this.message.orEmpty()
            return if (message.isNotBlank()) Text(message)
            else Text("Generic error")
        }
    }
}
