package com.example.new_app.model.service

import android.view.ViewDebug.trace
import androidx.core.os.trace
import com.example.new_app.model.User
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.BuildConfig
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

//class ConfigService {
//
//    private val remoteConfig
//        get() = Firebase.remoteConfig
//
//    init {
//        if (BuildConfig.DEBUG) {
//            val configSettings = remoteConfigSettings { minimumFetchIntervalInSeconds = 0 }
//            remoteConfig.setConfigSettingsAsync(configSettings)
//        }
//
//        remoteConfig.setDefaultsAsync(AppConfig.remote_config_defaults)
//    }
//
//    suspend fun fetchConfiguration(): Boolean =
//        trace(FETCH_CONFIG_TRACE) { remoteConfig.fetchAndActivate().await() }
//
//    val isShowTaskEditButtonConfig: Boolean
//        get() = remoteConfig[SHOW_TASK_EDIT_BUTTON_KEY].asBoolean()
//
//
//
//}