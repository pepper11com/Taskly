package com.example.new_app.model.service

import com.example.new_app.common.util.Resource
import com.example.new_app.model.User
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountService @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val googleAuth: GoogleAuth
) {

    val currentUserId: String
        get() = firebaseAuth.currentUser?.uid.orEmpty()

    val hasUser: Boolean
        get() = firebaseAuth.currentUser != null

    val currentUser: Flow<User>
        get() = callbackFlow {
            val listener =
                FirebaseAuth.AuthStateListener { auth ->
                    this.trySend(auth.currentUser?.let { User(it.uid) } ?: User())
                }
            firebaseAuth.addAuthStateListener(listener)
            awaitClose { firebaseAuth.removeAuthStateListener(listener) }
        }

    suspend fun authenticate(email: String, password: String): Resource<Unit> {
        return try {
            firebaseAuth.signInWithEmailAndPassword(email, password).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message)
        }
    }

    suspend fun sendRecoveryEmail(email: String) {
        firebaseAuth.sendPasswordResetEmail(email).await()
    }

    suspend fun createAccount(email: String, password: String) {
        firebaseAuth.createUserWithEmailAndPassword(email, password).await()
    }


    suspend fun deleteAccount() {
        firebaseAuth.currentUser!!.delete().await()
    }

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