package com.example.new_app.model.service

import android.net.Uri
import android.util.Log
import com.example.new_app.common.util.Resource
import com.example.new_app.model.Task
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

    init {
        firebaseAuth.addAuthStateListener { firebaseAuth ->
            currentUser.value = firebaseAuth.currentUser
        }
    }

    val tasks: Flow<List<Task>> = currentUser.flatMapLatest { user ->
        user?.let {
            currentCollection(user.uid).snapshots().map { snapshot -> snapshot.toObjects() }
        } ?: flowOf(emptyList())
    }

    suspend fun getTask(taskId: String): Task? =
        firebaseAuth.currentUser?.uid?.let { uid ->
            currentCollection(uid).document(taskId).get().await().toObject()
        }

    suspend fun updateTaskStatus(taskId: String, status: TaskStatus) {
        currentUser.value?.uid?.let { uid ->
            currentCollection(uid).document(taskId).update("status", status)
        }
    }

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

    suspend fun update(task: Task) {
        firebaseAuth.currentUser?.uid?.let { uid ->
            currentCollection(uid).document(task.id).set(task).await()
        }
    }

//    suspend fun delete(taskId: String) {
//        firebaseAuth.currentUser?.uid?.let { uid ->
//            // Gets the task before deleting it
//            val task = currentCollection(uid).document(taskId).get().await().toObject<Task>()
//
//            // Deletes the task from Firestore
//            currentCollection(uid).document(taskId).delete().await()
//
//            // If the task exists, proceed with image deletion
//            if (task != null) {
//                // Deletes the image from Firebase Storage if there's one associated with the task
//                task.imageUri?.let { imageUrl ->
//                    val path = imageUrl
//                        .substringAfter("example-f27a3.appspot.com/o/")
//                        .substringBefore("?")
//                        .replace("%2F", "/")
//
//                    Log.d("FirebaseService", "imageUrl: $imageUrl")
//                    Log.d("FirebaseService", "path: $path")
//
//                    firebaseStorage.reference.child(path).delete().await()
//                }
//            }
//        }
//    }

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


    suspend fun deleteAllForUser(userId: String) {
        val matchingTasks = currentCollection(userId).get().await()
        matchingTasks.map { it.reference.delete().asDeferred() }.awaitAll()
    }

    private fun currentCollection(uid: String): CollectionReference =
        firebaseFirestore.collection(USER_COLLECTION).document(uid).collection(TASK_COLLECTION)

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

    suspend fun deleteTasks(tasks: List<Task>): Resource<Unit> {
        return try {
            firebaseAuth.currentUser?.uid?.let { uid ->
                tasks.forEach { task ->
                    // Delete the task from Firestore
                    currentCollection(uid).document(task.id).delete().await()

                    // Delete the image from Firebase Storage if there's one associated with the task
                    task.imageUri?.let { imageUrl ->
                        val path = imageUrl
                            .substringAfter("example-f27a3.appspot.com/o/")
                            .substringBefore("?")
                            .replace("%2F", "/")

                        Log.d("FirebaseService", "imageUrl: $imageUrl")
                        Log.d("FirebaseService", "path: $path")

                        firebaseStorage.reference.child(path).delete().await()
                    }
                }
            }
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error")
        }
    }

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

    suspend fun uploadImage(uri: Uri, path: String): String {
        val storageRef = firebaseStorage.reference.child(path)
        storageRef.putFile(uri).await()
        return storageRef.downloadUrl.await().toString()
    }

    suspend fun deleteImage(path: String) {
        val storageRef = firebaseStorage.reference.child(path)
        storageRef.delete().await()
    }

    companion object {
        private const val USER_COLLECTION = "users"
        private const val TASK_COLLECTION = "tasks"
    }
}
