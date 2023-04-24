package com.example.new_app.common.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmptyTopBar(
    title: String,
    colors: TopAppBarColors = topAppBarColors(),
    textColor: Color = Color.White,
) {

    MediumTopAppBar(
        modifier = Modifier.fillMaxWidth(),
        colors = colors,
        title = {
            Text(
                text = title,
                color = textColor,
                style = MaterialTheme.typography.headlineLarge
            )
        }
    )


}