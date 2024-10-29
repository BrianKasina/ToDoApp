package com.example.programmingassignment.ui.body

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.programmingassignment.data.Task
import com.example.programmingassignment.ui.tasks.TaskItem
import com.example.programmingassignment.util.FirestoreUtils
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.util.Date


@Composable
fun CompletedTasksScreen(firestoreUtils: FirestoreUtils, paddingValues: PaddingValues) {
    val scope = rememberCoroutineScope()
    var isCompleted by remember { mutableStateOf(true) }
    var isImportant by remember { mutableStateOf(false) }
    val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email ?: ""
    var tasks by remember { mutableStateOf(listOf<Task>()) }
    var selectedRecurrence by remember { mutableStateOf("Once") }

    // Setup real-time task updates based on selected filter
    DisposableEffect(Unit) {
        loadCompletedTasks(firestoreUtils, currentUserEmail, selectedRecurrence) { updatedTasks ->
            tasks = updatedTasks
        }

        onDispose {
            firestoreUtils.removeListener() // Detach listener when composable is disposed
        }
    }

    // Update tasks when recurrence changes
    LaunchedEffect(selectedRecurrence) {
        loadCompletedTasks(firestoreUtils, currentUserEmail, selectedRecurrence) { updatedTasks ->
            tasks = updatedTasks
        }
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .background(MaterialTheme.colorScheme.background) // Set background to theme color
    ) {
        Text(
            text = "Completed Tasks",
            style = MaterialTheme.typography.displayMedium.copy(
                color = MaterialTheme.colorScheme.surfaceVariant, // Text color
                fontWeight = FontWeight.Bold // Bold text
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Horizontal buttons for recurrence options
        val recurrenceOptions = listOf("Daily", "Monthly", "Yearly", "Once")

        Row(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            recurrenceOptions.forEach { option ->
                Button(
                    onClick = { selectedRecurrence = option },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedRecurrence == option) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                        contentColor = if (selectedRecurrence == option) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondary // Text color based on button state
                    ),
                    modifier = Modifier
                        .padding(4.dp) // Spacing between buttons
                        .height(48.dp) // Set a fixed height for buttons
                ) {
                    Text(
                        text = option,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Use LazyColumn for the list of completed tasks to enable scrolling
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(tasks) { task ->
                TaskItem(
                    task = task,
                    onTaskCheckedChange = {},  // No need to change the completion status of completed tasks
                    onTaskClick = {}
                )
            }
        }
    }
}

// Function to load tasks based on recurrence
fun loadCompletedTasks(
    firestoreUtils: FirestoreUtils,
    currentUserEmail: String,
    selectedRecurrence: String,
    onTasksUpdated: (List<Task>) -> Unit
) {
    firestoreUtils.getTasks(
        currentUserEmail = currentUserEmail,
        isCompleted = true
    ) { updatedTasks ->
        onTasksUpdated(
            updatedTasks.filter { task ->
                when (selectedRecurrence) {
                    "Daily" -> task.recurrence == "Daily"
                    "Monthly" -> task.recurrence == "Monthly"
                    "Yearly" -> task.recurrence == "Yearly"
                    else -> task.recurrence == null // One-Time tasks
                }
            }
        )
    }
}
