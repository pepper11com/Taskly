package com.example.new_app.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner

@Composable
fun <T : ViewModel> rememberViewModelProvider(
    viewModelClass: Class<T>,
    viewModelStoreOwner: ViewModelStoreOwner? = null,
    factory: ViewModelProvider.Factory? = null
): () -> T {
    val currentViewModelStoreOwner = viewModelStoreOwner ?: LocalViewModelStoreOwner.current
    val currentFactory = factory

    return remember(currentViewModelStoreOwner, currentFactory) {
        {
            ViewModelProvider(currentViewModelStoreOwner!!, currentFactory ?: ViewModelProvider.NewInstanceFactory()).get(viewModelClass)
        }
    }
}
