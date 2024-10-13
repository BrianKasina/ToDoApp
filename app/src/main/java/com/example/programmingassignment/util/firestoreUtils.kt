package com.example.programmingassignment.util

import com.example.programmingassignment.data.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class FirestoreUtils(private val firestore: FirebaseFirestore) {

    private val taskCollection = firestore.collection("tasks")

    // Add or update a task
    suspend fun addOrUpdateTask(task: Task) {
        if (task.id.isNotEmpty()) {
            taskCollection.document(task.id).set(task).await()
        } else {
            taskCollection.add(task).await()
        }
    }

    // Get tasks by their status
    suspend fun getTasks(isCompleted: Boolean? = null, isImportant: Boolean? = null): List<Task> {
        var query: Query = taskCollection

        if (isCompleted != null) {
            query = query.whereEqualTo("Completed", isCompleted)
        }

        if (isImportant != null) {
            query = query.whereEqualTo("Important", isImportant)
        }

        return query.get().await().toObjects(Task::class.java)
    }

    // Delete task
    suspend fun deleteTask(taskId: String) {
        taskCollection.document(taskId).delete().await()
    }
}
