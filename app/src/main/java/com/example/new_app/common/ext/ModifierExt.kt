package com.example.new_app.common.ext

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


fun Modifier.divider(): Modifier {
  return this.padding(top = 16.dp)
}

fun Modifier.lazyColumn(innerPadding: PaddingValues): Modifier {
  return this.fillMaxSize().padding(innerPadding).padding(16.dp)
}

fun Modifier.padding16(): Modifier {
  return this.padding(16.dp)
}