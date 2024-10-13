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
fun CompletedTasksScreen(firestoreUtils: FirestoreUtils, paddingValues: PaddingValues) {
    val scope = rememberCoroutineScope()
    var tasks by remember { mutableStateOf(listOf<Task>()) }

    // Load completed tasks
    LaunchedEffect(Unit) {
        scope.launch {
            tasks = firestoreUtils.getTasks(isCompleted = true)
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
        Text("Completed Tasks", style = MaterialTheme.typography.displayLarge)
        tasks.forEach { task ->
            TaskItem(task = task, onTaskCheckedChange = {}, onTaskClick = {})
        }
    }
}
