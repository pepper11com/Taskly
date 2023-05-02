package com.example.new_app.common.composables

import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.ContentAlpha
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun customTextFieldColors(): TextFieldColors {
    return OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Color.White.copy(alpha = 0.7f),
        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = ContentAlpha.disabled),
        focusedLabelColor = Color.White.copy(alpha = 0.7f),
        cursorColor = Color.White.copy(alpha = 0.7f),
        selectionColors = TextSelectionColors(
            Color(0xFF444444),
            Color(0xFF444444).copy(alpha = 0.5f),
        )
    )
}
