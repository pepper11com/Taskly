package com.example.new_app.domain.repository

import com.example.new_app.common.util.Resource
import com.example.new_app.domain.GoogleAuth
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountService @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val googleAuth: GoogleAuth
) {
    /**
     * Returns the UID of the currently signed-in user.
     * If no user is signed in, returns an empty string.
     */
    val currentUserId: String
        get() = firebaseAuth.currentUser?.uid.orEmpty()


    /**
     * Returns a boolean indicating whether there is a currently signed-in user.
     */
    val hasUser: Boolean
        get() = firebaseAuth.currentUser != null

    /**
     * Authenticates a user using email and password.
     * Returns a Resource wrapping a Unit on success or an error message on failure.
     */
    suspend fun authenticate(email: String, password: String): Resource<Unit> {
        return try {
            firebaseAuth.signInWithEmailAndPassword(email, password).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message)
        }
    }

    /**
     * Sends a password recovery email to the specified email address.
     * Throws an exception if the process fails.
     */
    suspend fun sendRecoveryEmail(email: String) {
        firebaseAuth.sendPasswordResetEmail(email).await()
    }

    /**
     * Creates a new user account using email and password.
     * Throws an exception if the process fails.
     */
    suspend fun createAccount(email: String, password: String) {
        firebaseAuth.createUserWithEmailAndPassword(email, password).await()
    }

    /**
     * Deletes the currently signed-in user account.
     * Throws an exception if the process fails or there is no currently signed-in user.
     */
    suspend fun deleteAccount() {
        firebaseAuth.currentUser!!.delete().await()
    }

    /**
     * Signs out the currently signed-in user.
     * If the user was signed in using Google, also signs them out from Google.
     * If the user is anonymous, also deletes the user account.
     * Returns a Resource wrapping a Unit on success or an error message on failure.
     */
    suspend fun signOut(): Resource<Unit> {
        return try {
            val signedInUser = googleAuth.getSignedInUser()
            if (signedInUser != null) {
                // User signed in with Google
                googleAuth.signOutFromGoogle()
            } else {
                // User signed in with email and password
                if (firebaseAuth.currentUser!!.isAnonymous) {
                    firebaseAuth.currentUser!!.delete()
                }
                firebaseAuth.signOut()
            }
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message)
        }
    }
}