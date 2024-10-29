package com.example.programmingassignment.ui.body

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.programmingassignment.data.Task
import com.example.programmingassignment.ui.tasks.TaskItem
import com.example.programmingassignment.util.FirestoreUtils
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.util.*

@Composable
fun DashboardScreen(firestoreUtils: FirestoreUtils, paddingValues: PaddingValues) {
    val scope = rememberCoroutineScope()
    val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email ?: ""
    var today = Calendar.getInstance(TimeZone.getDefault()).apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.time

    // State for tasks grouped by completion status
    var incompleteTasks by remember { mutableStateOf(listOf<Task>()) }
    var completedTasks by remember { mutableStateOf(listOf<Task>()) }

    // Expanded states for dropdown visibility
    var incompleteExpanded by remember { mutableStateOf(false) }
    var completedExpanded by remember { mutableStateOf(false) }

    // Load tasks with real-time updates
    // Separate DisposableEffect for incomplete tasks
    DisposableEffect(Unit) {
        firestoreUtils.getTasks(
            currentUserEmail = currentUserEmail,
            isCompleted = false,
            dueDate = today
        ) { tasks ->
            incompleteTasks = tasks
            Log.d("DashboardScreen", "Incomplete tasks fetched: ${tasks.size} tasks for $today")
        }
        firestoreUtils.getCompletedTasks(
            currentUserEmail = currentUserEmail,
            completionDate = today
        ) { tasks ->
            completedTasks = tasks
            Log.d("DashboardScreen", "Completed tasks fetched: ${tasks.size} tasks for $today")
        }

        onDispose {
            firestoreUtils.removeListener()
        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "My Day",
            style = MaterialTheme.typography.displayMedium.copy(
                color = MaterialTheme.colorScheme.surfaceVariant,
                fontWeight = FontWeight.Bold
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Incomplete Tasks for Today Dropdown
        DropdownCard(
            label = "Tasks for Today",
            tasks = incompleteTasks,
            expanded = incompleteExpanded,
            onExpandedChange = { incompleteExpanded = !incompleteExpanded },
            onTaskCheckedChange = { task ->
                scope.launch {
                    firestoreUtils.addOrUpdateTask(task.copy(completed = true), currentUserEmail)
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Completed Tasks for Today Dropdown
        DropdownCard(
            label = "Completed Tasks for Today",
            tasks = completedTasks,
            expanded = completedExpanded,
            onExpandedChange = { completedExpanded = !completedExpanded },
            onTaskCheckedChange = {}  // Completed tasks don't need to be marked again
        )
    }
}

@Composable
fun DropdownCard(
    label: String,
    tasks: List<Task>,
    expanded: Boolean,
    onExpandedChange: () -> Unit,
    onTaskCheckedChange: (Task) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onExpandedChange) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ArrowDropDown else Icons.Default.PlayArrow,
                        contentDescription = null
                    )
                }
            }
            AnimatedVisibility(visible = expanded) {
                LazyColumn {
                    items(tasks) { task ->
                        TaskItem(
                            task = task,
                            onTaskCheckedChange = { onTaskCheckedChange(task) },
                            onTaskClick = {} // Add additional click handling if needed
                        )
                    }
                }
            }
        }
    }
}
