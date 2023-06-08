package com.example.new_app.di

import android.content.Context
import com.example.new_app.common.snackbar.SnackbarManager
import com.example.new_app.domain.GoogleAuth
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import com.google.android.gms.auth.api.identity.Identity
import dagger.hilt.android.qualifiers.ApplicationContext

@Module // Module means that this class will provide dependencies
@InstallIn(SingletonComponent::class) // Component that lives as long as the app does
object AppModule {

    @Provides // Provides means that this method will provide an object
    @Singleton // Singleton means that only one instance of this object will be created
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideSnackbarManager(): SnackbarManager = SnackbarManager

    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage = FirebaseStorage.getInstance()

    @Provides
    @Singleton
    fun provideSignInClient(@ApplicationContext context: Context): SignInClient {
        return Identity.getSignInClient(context)
    }

    @Provides
    @Singleton
    fun provideGoogleAuth(@ApplicationContext context: Context, signInClient: SignInClient): GoogleAuth {
        return GoogleAuth(context, signInClient)
    }

}