package com.example.new_app.screens.authentication

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.example.new_app.common.composables.MediumAppBarWithTabs
import com.example.new_app.screens.login.LoginScreen
import com.example.new_app.screens.signup.SignupScreen

@Composable
fun AuthenticationScreen(
    openAndPopUp: (String, String) -> Unit,
    navigateToMainScreen: (String) -> Unit
) {
    val selectedIndex = remember { mutableStateOf(0) }
    val tabTitles = listOf("Login", "Register")

    Scaffold(
        topBar = {
            MediumAppBarWithTabs(
                selectedIndex = selectedIndex,
                tabTitles = tabTitles,
                title = if (selectedIndex.value == 0) "Login" else "Register"
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.padding(paddingValues)
        ) {
            item {
                when (selectedIndex.value) {
                    0 -> {
                        LoginScreen(
                            openAndPopUp = { _, _ -> selectedIndex.value = 1 },
                            navigateToMainScreen = navigateToMainScreen
                        )
                    }
                    1 -> {
                        SignupScreen(
                            openAndPopUp = { _, _ -> selectedIndex.value = 0 }
                        )
                    }
                }
            }
        }
    }
}


