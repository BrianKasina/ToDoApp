package com.example.programmingassignment.util

import com.example.programmingassignment.data.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.Date

class FirestoreUtils(private val firestore: FirebaseFirestore) {

    private val taskCollection = firestore.collection("tasks")

    // Add or update a task
    suspend fun addOrUpdateTask(task: Task, currentUserEmail: String) {
        val taskWithUserEmail = task.copy(email = currentUserEmail) // Add current user's email to the task
        if (task.id.isNotEmpty()) {
            taskCollection.document(task.id).set(taskWithUserEmail).await()
        } else {
            taskCollection.add(taskWithUserEmail).await()
        }
    }

    // Get tasks by their status
    suspend fun getTasks(currentUserEmail: String, isCompleted: Boolean? = null, isImportant: Boolean? = null): List<Task> {
        var query: Query = taskCollection.whereEqualTo("email", currentUserEmail)

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

    suspend fun getCountOfDatedTasks(dueDate: Date? = null): Int {
        var query: Query = taskCollection

        dueDate?.let {
            // Calculate start and end of the day
            val startOfDay = Calendar.getInstance().apply {
                time = dueDate
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.time

            val endOfDay = Calendar.getInstance().apply {
                time = dueDate
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }.time

            // Filter tasks due today
            query = query.whereGreaterThanOrEqualTo("dueDate", startOfDay)
                .whereLessThanOrEqualTo("dueDate", endOfDay)
        }

        // Fetch the documents and return the count
        val documents = query.get().await().documents
        return documents.size // Return the count of tasks due today
    }

}
