package com.example.programmingassignment.ui.body

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.programmingassignment.data.Task
import com.example.programmingassignment.util.FirestoreUtils
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat

@Composable
fun TaskDetailsScreen(
    task: Task,
    firestoreUtils: FirestoreUtils,
    paddingValues: PaddingValues,
    onDismiss: () -> Unit
) {
    // Create a coroutine scope
    val scope = rememberCoroutineScope() // Define the coroutine scope

    var title by remember { mutableStateOf(task.title) }
    var description by remember { mutableStateOf(task.description) }
    var isImportant by remember { mutableStateOf(task.important) }
    var dueDate by remember { mutableStateOf(task.dueDate) }

    Column(modifier = Modifier.padding(paddingValues)) {
        TextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Title") }
        )
        TextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") }
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = isImportant,
                onCheckedChange = { isImportant = it }
            )
            Text("Mark as Important")
        }
        Text("Due Date: ${dueDate?.let { SimpleDateFormat("yyyy-MM-dd HH:mm").format(it) } ?: "Not set"}")

        Button(onClick = {
            // Save changes
            scope.launch {
                firestoreUtils.addOrUpdateTask(
                    task.copy(title = title, description = description, important = isImportant, dueDate = dueDate)
                )
                onDismiss() // Optionally dismiss after saving
            }
        }) {
            Text("Save Changes")
        }

        Button(onClick = {
            // Delete task
            scope.launch {
                firestoreUtils.deleteTask(task.id)
                onDismiss() // Close the detail view after deletion
            }
        }) {
            Text("Delete Task", color = MaterialTheme.colorScheme.error)
        }
    }
}
