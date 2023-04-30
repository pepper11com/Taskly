package com.example.new_app


import android.os.Build
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.new_app.BuildConfig.MAPS_API_KEY
import com.example.new_app.screens.splashscreen.SplashScreenViewModel
import com.google.android.gms.maps.MapsInitializer
import com.google.android.libraries.places.api.Places
import dagger.hilt.android.AndroidEntryPoint
import android.Manifest
import com.example.new_app.model.service.AccountService
import com.example.new_app.model.service.GoogleAuth
import com.google.android.gms.auth.api.identity.Identity

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val googleAuthUiClient by lazy {
        GoogleAuth(
            context = applicationContext,
            oneTapClient = Identity.getSignInClient(applicationContext),
        )
    }

    private val viewModel: SplashScreenViewModel by viewModels()
    @RequiresApi(Build.VERSION_CODES.O)
    @OptIn(ExperimentalMaterialApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //todo - settings screen top bar
        //todo - edit task screen - location
        //todo - add a search bar to search for tasks
        //todo - when swiping to delete or to complete a task, show a icon with color under the task

        //todo - automatically set task to delete if it is overdue

        //todo - we got an navigation bug againnnnn when logging out :))))))))))

        //todo - test image deletion again!

        Places.initialize(applicationContext, MAPS_API_KEY)
        MapsInitializer.initialize(applicationContext)


        installSplashScreen().apply {
            setKeepOnScreenCondition() {
                viewModel.isLoading.value
            }
        }

        setContent {
            TaskApp(
                googleAuthUiClient = googleAuthUiClient,
            )
        }
    }
}





