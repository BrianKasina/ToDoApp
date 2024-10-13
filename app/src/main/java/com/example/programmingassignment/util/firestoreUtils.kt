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
            query = query.whereEqualTo("completed", isCompleted)
        }

        if (isImportant != null) {
            query = query.whereEqualTo("important", isImportant)
        }

        return query.get().await().documents.map { document ->
            document.toObject(Task::class.java)?.copy(id = document.id) // Copy the document ID
        }.filterNotNull()
    }

    // Delete task
    suspend fun deleteTask(taskId: String) {
        taskCollection.document(taskId).delete().await()
    }
}
