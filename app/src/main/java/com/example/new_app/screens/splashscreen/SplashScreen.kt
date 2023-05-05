package com.example.new_app.screens.splashscreen

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.new_app.common.composables.LoadingIndicator
import com.example.new_app.common.util.Resource
import kotlinx.coroutines.delay


@Composable
fun SplashScreen(
    openAndPopUp: (String, String) -> Unit,
    clearBackstack: () -> Unit,
    modifier: Modifier = Modifier,
) {

    val viewModel: SplashScreenViewModel = hiltViewModel()
    val taskListUiState by viewModel.taskListResource.collectAsState()

    when(taskListUiState) {
        is Resource.Loading -> {
            Log.d("SplashScreen", "Loading")
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .background(color = MaterialTheme.colorScheme.background)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LoadingIndicator()
            }
        }
        else -> {}
    }

    LaunchedEffect(viewModel) {
        viewModel.onAppStart(openAndPopUp, clearBackstack)
    }
}
