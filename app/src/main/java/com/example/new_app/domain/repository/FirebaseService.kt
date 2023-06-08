package com.example.new_app.domain.repository

import android.net.Uri
import android.util.Log
import com.example.new_app.common.util.Resource
import com.example.new_app.domain.model.Task
import com.example.new_app.screens.task.tasklist.TaskStatus
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.snapshots
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.asDeferred
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseService @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firebaseFirestore: FirebaseFirestore,
    private val firebaseStorage: FirebaseStorage
) {

    private val currentUser = MutableStateFlow(firebaseAuth.currentUser)

    /**
     * Provides real-time user authentication state updates.
     * Updates the currentUser MutableStateFlow object when the Firebase authentication state changes.
     */
    init {
        firebaseAuth.addAuthStateListener { firebaseAuth ->
            currentUser.value = firebaseAuth.currentUser
        }
    }

    /**
     * Returns a Flow of the current user's tasks from Firestore.
     * Automatically updates when the current user or their tasks change.
     */
    val tasks: Flow<List<Task>> = currentUser.flatMapLatest { user ->
        user?.let {
            currentCollection(user.uid).snapshots().map { snapshot -> snapshot.toObjects() }
        } ?: flowOf(emptyList())
    }

    /**
     * Retrieves a task with a specific ID for the current user from Firestore.
     * Returns null if the user is not authenticated or the task does not exist.
     */
    suspend fun getTask(taskId: String): Task? =
        firebaseAuth.currentUser?.uid?.let { uid ->
            currentCollection(uid).document(taskId).get().await().toObject()
        }

    /**
     * Updates the status of a task with a specific ID for the current user in Firestore.
     * Has no effect if the user is not authenticated or the task does not exist.
     */
    suspend fun updateTaskStatus(taskId: String, status: TaskStatus) {
        currentUser.value?.uid?.let { uid ->
            currentCollection(uid).document(taskId).update("status", status)
        }
    }

    /**
     * Saves a new task for the current user to Firestore and returns the ID of the saved task wrapped in a Resource.
     * Returns an error Resource if the user is not authenticated or there was an issue saving the task.
     */
    suspend fun save(task: Task): Resource<String> {
        return try {
            val taskId = firebaseAuth.currentUser?.uid?.let { uid ->
                currentCollection(uid).add(task).await().id
            } ?: ""
            Resource.Success(taskId)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error")
        }
    }

    /**
     * Deletes a task with a specific ID for the current user from Firestore and Firebase Storage (if it has an associated image).
     * Has no effect if the user is not authenticated, the task does not exist, or there was an issue deleting the task or its associated image.
     */
    suspend fun delete(taskId: String) {
        firebaseAuth.currentUser?.uid?.let { uid ->
            // Gets the task before deleting it
            val task = currentCollection(uid).document(taskId).get().await().toObject<Task>()

            // Deletes the task from Firestore
            currentCollection(uid).document(taskId).delete().await()

            // If the task exists, proceed with image deletion
            if (task != null) {
                // Deletes the image from Firebase Storage if there's one associated with the task
                task.imageUri?.let { imageUrl ->
                    val path = "task_images/$uid/$taskId"

                    Log.d("FirebaseService", "imageUrl: $imageUrl")
                    Log.d("FirebaseService", "path: $path")

                    if (doesImageExist(path)) {
                        firebaseStorage.reference.child(path).delete().await()
                    } else {
                        // Handle the case when the image does not exist
                        Log.d("FirebaseService", "Image not found at path: $path")
                    }
                }
            }
        }
    }

    /**
     * Deletes all tasks for a specific user from Firestore.
     * Has no effect if the user does not exist or there was an issue deleting the tasks.
     */
    suspend fun deleteAllForUser(userId: String) {
        val matchingTasks = currentCollection(userId).get().await()
        matchingTasks.map { it.reference.delete().asDeferred() }.awaitAll()
    }

    private fun currentCollection(uid: String): CollectionReference =
        firebaseFirestore.collection(USER_COLLECTION).document(uid).collection(TASK_COLLECTION)

    /**
     * Updates a task for the current user in Firestore and returns a success Resource.
     * Returns an error Resource if the user is not authenticated, the task does not exist, or there was an issue updating the task.
     */
    suspend fun updateTask(task: Task): Resource<Unit> {
        return try {
            firebaseAuth.currentUser?.uid?.let { uid ->
                currentCollection(uid).document(task.id).set(task).await()
            }
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error")
        }
    }

    /**
     * Checks if an image with a specific path exists in Firebase Storage and returns a boolean value.
     * Throws a StorageException if there was an issue accessing the storage.
     */
    suspend fun doesImageExist(path: String): Boolean {
        return try {
            val storageRef = firebaseStorage.reference.child(path)
            storageRef.metadata.await()
            true
        } catch (e: StorageException) {
            if (e.errorCode == StorageException.ERROR_OBJECT_NOT_FOUND) {
                false
            } else {
                throw e
            }
        }
    }

    /**
     * Uploads an image from a Uri to a specific path in Firebase Storage and returns the download URL as a string.
     * Throws a StorageException if there was an issue uploading the image.
     */
    suspend fun uploadImage(uri: Uri, path: String): String {
        val storageRef = firebaseStorage.reference.child(path)
        storageRef.putFile(uri).await()
        return storageRef.downloadUrl.await().toString()
    }

    /**
     * Deletes an image with a specific path from Firebase Storage.
     * Throws a StorageException if there was an issue deleting the image.
     */
    suspend fun deleteImage(path: String) {
        val storageRef = firebaseStorage.reference.child(path)
        storageRef.delete().await()
    }

    companion object {
        private const val USER_COLLECTION = "users"
        private const val TASK_COLLECTION = "tasks"
    }
}
