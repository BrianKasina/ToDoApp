package com.example.programmingassignment.util

import android.util.Log
import com.example.programmingassignment.data.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.Date
import java.util.TimeZone

class FirestoreUtils(private val firestore: FirebaseFirestore) {

    private val taskCollection = firestore.collection("tasks")
    private var incompleteTasksListener: ListenerRegistration? = null
    private var completedTasksListener: ListenerRegistration? = null

    // Add or update a task
    suspend fun addOrUpdateTask(task: Task, currentUserEmail: String) {
        val taskWithUserEmail = task.copy(email = currentUserEmail) // Add current user's email to the task
        if (task.id.isNotEmpty()) {
            taskCollection.document(task.id).set(taskWithUserEmail).await()
        } else {
            taskCollection.add(taskWithUserEmail).await()
        }
    }

    // Get tasks by their status with real-time updates
    fun getTasks(
        currentUserEmail: String,
        isCompleted: Boolean? = null,
        isImportant: Boolean? = null,
        dueDate: Date? = null,
        onTasksUpdate: (List<Task>) -> Unit
    ) {
        var query: Query = taskCollection.whereEqualTo("email", currentUserEmail)

        if (isCompleted != null) {
            query = query.whereEqualTo("completed", isCompleted)
        }

        if (isImportant != null) {
            query = query.whereEqualTo("important", isImportant)
        }

        if (dueDate != null) {
            val calendar = Calendar.getInstance(TimeZone.getDefault())
            calendar.time = dueDate
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startOfDay = calendar.time

            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
            val endOfDay = calendar.time

            query = query.whereGreaterThanOrEqualTo("dueDate", startOfDay)
                .whereLessThanOrEqualTo("dueDate", endOfDay)
        }

        // Detach any previous listener
        incompleteTasksListener?.remove()

        // Attach a new snapshot listener
        incompleteTasksListener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                // Handle the error
                Log.e("Firestore", "Error fetching tasks: ${error.message}")
                return@addSnapshotListener
            }
            val tasks = snapshot?.documents?.mapNotNull { document ->
                document.toObject(Task::class.java)?.copy(id = document.id)
            }.orEmpty()

            // Pass the updated task list to the callback
            onTasksUpdate(tasks)
        }
    }

    // New function to get completed tasks based on the completion date
    fun getCompletedTasks(
        currentUserEmail: String,
        completionDate: Date? = null,
        onTasksUpdate: (List<Task>) -> Unit
    ) {
        var query: Query = taskCollection.whereEqualTo("email", currentUserEmail)
            .whereEqualTo("completed", true) // Only fetch completed tasks

        if (completionDate != null) {
            val calendar = Calendar.getInstance(TimeZone.getDefault())
            calendar.time = completionDate
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startOfDay = calendar.time

            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
            val endOfDay = calendar.time

            query = query.whereGreaterThanOrEqualTo("dateCompleted", startOfDay)
                .whereLessThanOrEqualTo("dateCompleted", endOfDay)
        }

        // Detach any previous listener
        completedTasksListener?.remove()

        // Attach a new snapshot listener
        completedTasksListener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                // Handle the error
                Log.e("Firestore", "Error fetching completed tasks: ${error.message}")
                return@addSnapshotListener
            }
            val tasks = snapshot?.documents?.mapNotNull { document ->
                document.toObject(Task::class.java)?.copy(id = document.id)
            }.orEmpty()

            // Pass the updated task list to the callback
            onTasksUpdate(tasks)
        }
    }

    // Call this to remove the listener when no longer needed (e.g., in onDestroy)
    fun removeListener() {
        incompleteTasksListener?.remove()
        completedTasksListener?.remove()
    }

    // Delete task
    suspend fun deleteTask(taskId: String) {
        taskCollection.document(taskId).delete().await()
    }
}
