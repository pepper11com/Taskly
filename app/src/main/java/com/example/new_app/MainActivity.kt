package com.example.new_app


import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.ExperimentalMaterialApi
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import com.example.new_app.BuildConfig.MAPS_API_KEY
import com.example.new_app.screens.splashscreen.SplashScreenViewModel
import com.google.android.gms.maps.MapsInitializer
import com.google.android.libraries.places.api.Places
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel: SplashScreenViewModel by viewModels()
    @RequiresApi(Build.VERSION_CODES.O)
    @OptIn(ExperimentalMaterialApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //https://console.firebase.google.com/u/0/project/example-f27a3/authentication/emails
        //https://console.firebase.google.com/u/0/project/example-f27a3/settings/general/android:com.example.new_app


        //todo - DockedSearchBar
        //todo - settings screen top bar
        //todo - edit task screen - location
        //todo - add a search bar to search for tasks
        //todo - notification when task is due
        //todo - when swiping to delete or to complete a task, show a icon with color under the task

        //todo - make google-maps a separate screen!!!!!!!!!!!!

        Places.initialize(applicationContext, MAPS_API_KEY)
        MapsInitializer.initialize(applicationContext)


        installSplashScreen().apply {
            setKeepOnScreenCondition() {
                viewModel.isLoading.value
            }
        }

//        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            TaskApp()
        }

    }
}





