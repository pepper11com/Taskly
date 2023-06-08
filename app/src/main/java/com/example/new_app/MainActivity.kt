package com.example.new_app


import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.new_app.BuildConfig.MAPS_API_KEY
import com.example.new_app.domain.GoogleAuth
import com.example.new_app.screens.splashscreen.SplashScreenViewModel
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.maps.MapsInitializer
import com.google.android.libraries.places.api.Places
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val googleAuthUiClient by lazy {
        GoogleAuth(
            context = applicationContext,
            oneTapClient = Identity.getSignInClient(applicationContext),
        )
    }

    private val viewModel: SplashScreenViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //todo - add a search bar to search for tasks

        //todo - bug with time picker if you don't set a time :)

        Places.initialize(applicationContext, MAPS_API_KEY)
        MapsInitializer.initialize(applicationContext)


        installSplashScreen().apply {
            setKeepOnScreenCondition {
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





