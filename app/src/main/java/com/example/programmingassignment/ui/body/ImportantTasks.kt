package com.example.programmingassignment.ui.body

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.programmingassignment.data.Task
import com.example.programmingassignment.ui.tasks.TaskItem
import com.example.programmingassignment.util.FirestoreUtils
import kotlinx.coroutines.launch

@Composable
fun ImportantTasksScreen(firestoreUtils: FirestoreUtils, paddingValues: PaddingValues) {
    val scope = rememberCoroutineScope()
    var tasks by remember { mutableStateOf(listOf<Task>()) }
    var showDetails by remember { mutableStateOf(false) }
    var selectedTask by remember { mutableStateOf<Task?>(null) }

    // Load important tasks
    LaunchedEffect(Unit) {
        scope.launch {
            tasks = firestoreUtils.getTasks(isImportant = true, isCompleted = false)
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
        Text("Important Tasks", style = MaterialTheme.typography.displayLarge)
        tasks.forEach { task ->
            TaskItem(task = task, onTaskCheckedChange = { isChecked ->
                // Update task completion status
                scope.launch {
                    firestoreUtils.addOrUpdateTask(task.copy(completed = isChecked))
                    // Optionally refresh the tasks after updating
                    tasks = firestoreUtils.getTasks(isImportant = true, isCompleted = false)
                }
            }, onTaskClick = {
                selectedTask = task
                showDetails = true // Show task details

            })
        }
    }

    if (showDetails && selectedTask != null) {
        AlertDialog(
            onDismissRequest = { showDetails = false },
            title = { Text("Task Details") },
            text = {
                TaskDetailsScreen(
                    task = selectedTask!!,
                    firestoreUtils = firestoreUtils,
                    onDismiss = { showDetails = false
                        scope.launch {
                            // Optionally refresh the tasks after updating
                            tasks = firestoreUtils.getTasks(isImportant = true, isCompleted = false)
                        }
                     },
                    paddingValues = paddingValues
                )
            },
            confirmButton = {
                Button(onClick = { showDetails = false }) {
                    Text("Close")
                }
            }
        )
    }
}

