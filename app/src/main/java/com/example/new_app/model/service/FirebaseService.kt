package com.example.new_app.model.service

import com.example.new_app.model.Task
import com.example.new_app.model.User
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.snapshots
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.asDeferred
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.*

class FirebaseService {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val currentUser = MutableStateFlow(auth.currentUser)

    init {
        auth.addAuthStateListener { firebaseAuth ->
            currentUser.value = firebaseAuth.currentUser
        }
    }

    val tasks: Flow<List<Task>> = currentUser.flatMapLatest { user ->
        user?.let {
            currentCollection(user.uid).snapshots().map { snapshot -> snapshot.toObjects() }
        } ?: flowOf(emptyList())
    }

    suspend fun getTask(taskId: String): Task? =
        auth.currentUser?.uid?.let { uid ->
            currentCollection(uid).document(taskId).get().await().toObject()
        }


    suspend fun save(task: Task): String =
        auth.currentUser?.uid?.let { uid ->
            currentCollection(uid).add(task).await().id
        } ?: ""

    suspend fun update(task: Task) {
        auth.currentUser?.uid?.let { uid ->
            currentCollection(uid).document(task.id).set(task).await()
        }
    }

    suspend fun delete(taskId: String) {
        auth.currentUser?.uid?.let { uid ->
            currentCollection(uid).document(taskId).delete().await()
        }
    }

    suspend fun deleteAllForUser(userId: String) {
        val matchingTasks = currentCollection(userId).get().await()
        matchingTasks.map { it.reference.delete().asDeferred() }.awaitAll()
    }

    private fun currentCollection(uid: String): CollectionReference =
        firestore.collection(USER_COLLECTION).document(uid).collection(TASK_COLLECTION)

    companion object {
        private const val USER_COLLECTION = "users"
        private const val TASK_COLLECTION = "tasks"
    }
}
