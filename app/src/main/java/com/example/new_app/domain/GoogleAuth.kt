package com.example.new_app.domain

import android.content.Context
import android.content.Intent
import android.content.IntentSender
import com.example.new_app.R
import com.example.new_app.screens.login.SignInResult
import com.example.new_app.screens.login.UserData
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import java.util.concurrent.CancellationException

class GoogleAuth(
    private val context: Context,
    private val oneTapClient: SignInClient
) {
    private val auth = Firebase.auth

    /**
     * Begins the sign in flow with Google One Tap.
     * Returns an IntentSender which can be used to start an activity for a result, which upon success, can be used to obtain a Google Sign In credential.
     */
    suspend fun signInWithGoogle(): IntentSender? {
        val result = try {
            oneTapClient.beginSignIn(
                buildSignInRequest()
            ).await()
        } catch (e: Exception) {
            e.printStackTrace()
            if (e is CancellationException) {
                throw e
            } else {
                null
            }
        }
        return result?.pendingIntent?.intentSender
    }

    /**
     * Signs in with a received Intent after a user completed the sign in flow.
     * The Intent should contain a Google Sign In credential.
     * Returns a SignInResult which can contain a UserData object for a successful sign in, or an error message in case of a failure.
     */
    suspend fun signInWithIntent(intent: Intent): SignInResult {
        val credential = oneTapClient.getSignInCredentialFromIntent(intent)
        val idToken = credential.googleIdToken
        val googleCredentials = GoogleAuthProvider.getCredential(idToken, null)

        return try {
            val user = auth.signInWithCredential(googleCredentials).await().user
            SignInResult(
                data = user?.run {
                    UserData(
                        userId = uid,
                        username = displayName,
                        profilePictureUrl = photoUrl?.toString()
                    )
                },
                errorMessage = null
            )
        } catch (e: Exception) {
            e.printStackTrace()
            if (e is CancellationException) {
                throw e
            } else {
                SignInResult(
                    data = null,
                    errorMessage = e.message
                )
            }
        }
    }

    /**
     * Signs out the user from Google One Tap and Firebase Auth.
     */
    suspend fun signOutFromGoogle() {
        try {
            oneTapClient.signOut().await()
            auth.signOut()
        } catch (e: Exception) {
            e.printStackTrace()
            if (e is CancellationException) {
                throw e
            }
        }
    }

    /**
     * Returns a UserData object for the currently signed in user, or null if no user is signed in.
     */
    fun getSignedInUser(): UserData? = auth.currentUser?.run {
        UserData(
            userId = uid,
            username = displayName,
            profilePictureUrl = photoUrl?.toString()
        )
    }

    /**
     * Builds a sign in request for Google One Tap.
     * Configures the request to ask for a Google ID Token, and enables auto selection of accounts.
     */
    private fun buildSignInRequest(): BeginSignInRequest {
        return BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(context.getString(R.string.default_web_client_id))
                    .build()
            )
            .setAutoSelectEnabled(true)
            .build()
    }
}