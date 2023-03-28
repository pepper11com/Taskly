package com.example.new_app.model.service

import android.content.Context
import android.net.Uri
import com.example.new_app.model.User
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.io.File
import java.io.FileOutputStream
import java.util.*

class AccountService {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    val currentUserId: String
        get() = auth.currentUser?.uid.orEmpty()

    val hasUser: Boolean
        get() = auth.currentUser != null

    val currentUser: Flow<User>
        get() = callbackFlow {
            val listener =
                FirebaseAuth.AuthStateListener { auth ->
                    this.trySend(auth.currentUser?.let { User(it.uid) } ?: User())
                }
            auth.addAuthStateListener(listener)
            awaitClose { auth.removeAuthStateListener(listener) }
        }

    suspend fun authenticate(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).await()
    }

    suspend fun sendRecoveryEmail(email: String) {
        auth.sendPasswordResetEmail(email).await()
    }

    suspend fun createAccount(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password).await()
    }


    suspend fun deleteAccount() {
        auth.currentUser!!.delete().await()
    }

    suspend fun saveImageToInternalStorage(context: Context, uri: Uri, userId: String, taskId: String): String {
        val inputStream = context.contentResolver.openInputStream(uri)

        // Create a directory to store your downloaded images
        val imagesDirectory = File(context.filesDir, "images")
        if (!imagesDirectory.exists()) {
            imagesDirectory.mkdir()
        }

        val imageFileName = "image_${UUID.randomUUID()}.jpg"
        val file = File(imagesDirectory, imageFileName)

        inputStream?.use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }

        return file.absolutePath
    }




    suspend fun signOut() {
        if (auth.currentUser!!.isAnonymous) {
            auth.currentUser!!.delete()
        }
        auth.signOut()

    }
}