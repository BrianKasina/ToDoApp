package com.example.programmingassignment.ui.body

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.example.programmingassignment.data.Task
import com.example.programmingassignment.ui.tasks.TaskItem
import com.example.programmingassignment.util.FirestoreUtils
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch


@Composable
fun CompletedTasksScreen(firestoreUtils: FirestoreUtils, paddingValues: PaddingValues) {
    val scope = rememberCoroutineScope()
    val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email ?: ""
    var tasks by remember { mutableStateOf(listOf<Task>()) }

    // Load completed tasks
    LaunchedEffect(Unit) {
        scope.launch {
            tasks = firestoreUtils.getTasks(currentUserEmail, isCompleted = true)
        }
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)) {
        Text(
            text = "Completed Tasks",
            style = MaterialTheme.typography.displayMedium.copy(
                color = MaterialTheme.colorScheme.surfaceVariant, // Text color
                fontWeight = FontWeight.Bold // Bold text
            )
        )

        // Use LazyColumn for the list of completed tasks to enable scrolling
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(tasks) { task ->
                TaskItem(
                    task = task,
                    onTaskCheckedChange = {},  // No need to change the completion status of completed tasks
                    onTaskClick = {}           // Add functionality here if you want to handle task clicks
                )
            }
        }
    }
}
