package com.example.new_app.common.composables

import androidx.annotation.StringRes
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ContentAlpha
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    @StringRes label: Int,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    singleLine: Boolean = true,
    isError: Boolean = false,
    errorMessage: String? = null,
    colors: TextFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = MaterialTheme.colorScheme.secondary,
        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = ContentAlpha.disabled),
        focusedLabelColor = MaterialTheme.colorScheme.secondary,
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White,
    )
) {
    val isErrorState = remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(stringResource(label)) },
            modifier = Modifier
                .fillMaxWidth(),
            isError = isError && isErrorState.value,
            singleLine = singleLine,
            keyboardOptions = keyboardOptions,
            colors = colors
        )

        if (isError && isErrorState.value && !errorMessage.isNullOrEmpty()) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 16.dp)
            )
        }
    }

    LaunchedEffect(value) {
        isErrorState.value = value.isNotEmpty()
    }
}

@Composable
fun CustomPasswordTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    errorMessage: String? = null
) {
    val isErrorState = remember { mutableStateOf(false) }
    val passwordVisibility = remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            modifier = Modifier
                .fillMaxWidth(),
            isError = isError && isErrorState.value,
            singleLine = true,
            visualTransformation = if (passwordVisibility.value) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                IconButton(onClick = { passwordVisibility.value = !passwordVisibility.value }) {
                    Icon(
                        imageVector = if (passwordVisibility.value) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = if (passwordVisibility.value) "Hide password" else "Show password"
                    )
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.secondary,
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = ContentAlpha.disabled),
                focusedLabelColor = MaterialTheme.colorScheme.secondary,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
            )
        )

        if (isError && isErrorState.value && !errorMessage.isNullOrEmpty()) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 16.dp)
            )
        }
    }

    LaunchedEffect(value) {
        isErrorState.value = value.isNotEmpty()
    }
}

@Composable
fun CustomMultiLineTextfield(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    @StringRes hintText: Int,
    textStyle: TextStyle = MaterialTheme.typography.bodySmall,
    maxLines: Int = 4,
    colors: TextFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = MaterialTheme.colorScheme.secondary,
        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = ContentAlpha.disabled),
        focusedLabelColor = MaterialTheme.colorScheme.secondary,
    )
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(stringResource(hintText)) },
        textStyle = textStyle,
        maxLines = maxLines,
        modifier = modifier.fillMaxWidth(),
        colors = colors,
        singleLine = false,
        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
    )
}

@Composable
fun CustomCopyTrueTextField(
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Center,
    fontSize: TextUnit = 12.sp,
    lineHeight: TextUnit = 16.sp,
    color: Color = Color.White,
    value: String,
    maxLines: Int = 2,
){
    BasicTextField(
        value = value,
        onValueChange = {},
        modifier = modifier
            .padding(horizontal = 2.dp, vertical = 0.dp)
            .verticalScroll(rememberScrollState()),
        textStyle = TextStyle(
            textAlign = textAlign,
            fontSize = fontSize,
            lineHeight = lineHeight,
            color = color
        ),
        maxLines = maxLines,
        readOnly = true,
        interactionSource = remember { MutableInteractionSource() },
        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
    )
}

