package com.example.programmingassignment.ui.tasks

import android.app.TimePickerDialog
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.platform.LocalContext // Add this line
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.programmingassignment.data.Task
import com.example.programmingassignment.ui.body.TaskDetailsScreen
import com.example.programmingassignment.util.FirestoreUtils
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

@Composable
fun TaskScreen(firestoreUtils: FirestoreUtils, paddingValues: PaddingValues) {
    val scope = rememberCoroutineScope()
    val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email ?: ""
    var tasks by remember { mutableStateOf(listOf<Task>()) }
    var selectedFilter by remember { mutableStateOf("Pending") }
    var showDialog by remember { mutableStateOf(false) }
    var newTaskTitle by remember { mutableStateOf("") }
    var newTaskDescription by remember { mutableStateOf("") }
    var newTaskDueDate by remember { mutableStateOf<Date?>(null) }
    var isImportant by remember { mutableStateOf(false) }
    var showDetails by remember { mutableStateOf(false) }
    var selectedTask by remember { mutableStateOf<Task?>(null) }

    // Load tasks based on filter
    LaunchedEffect(selectedFilter) {
        scope.launch {
            tasks = when (selectedFilter) {
                "pending" -> firestoreUtils.getTasks(currentUserEmail, isCompleted = false, isImportant = false)
                else -> firestoreUtils.getTasks(currentUserEmail,isCompleted = false, isImportant = false)
            }
        }
    }

    val context = LocalContext.current // Get the current context

    Box(modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = "View Tasks",
                style = MaterialTheme.typography.displayMedium.copy(
                    color = MaterialTheme.colorScheme.surfaceVariant, // Text color
                    fontWeight = FontWeight.Bold // Bold text
                )
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Use LazyColumn for the task list to enable scrolling
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(tasks) { task ->
                    TaskItem(task = task, onTaskCheckedChange = { isChecked ->
                        scope.launch {
                            // Update task completion status in Firestore
                            firestoreUtils.addOrUpdateTask(task.copy(completed = isChecked, id = task.id), currentUserEmail)
                            // Optionally refresh the tasks after updating
                            tasks = firestoreUtils.getTasks(currentUserEmail, isImportant = false, isCompleted = false)
                        }
                    }, onTaskClick = {
                        selectedTask = task
                        showDetails = true // Show task details
                    })
                }
            }

            // Task Details Dialog
            if (showDetails && selectedTask != null) {
                AlertDialog(
                    onDismissRequest = { showDetails = false },
                    title = { Text("Task Details") },
                    text = {
                        TaskDetailsScreen(
                            task = selectedTask!!,
                            firestoreUtils = firestoreUtils,
                            onDismiss = {
                                showDetails = false
                                scope.launch {
                                    // Optionally refresh the tasks after updating
                                    tasks = firestoreUtils.getTasks(currentUserEmail, isCompleted = false, isImportant = false)
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

            // Task Dialog for adding a new task
            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text("New Task") },
                    text = {
                        Column {
                            TextField(
                                value = newTaskTitle,
                                onValueChange = { newTaskTitle = it },
                                label = { Text("Title") }
                            )
                            TextField(
                                value = newTaskDescription,
                                onValueChange = { newTaskDescription = it },
                                label = { Text("Description") }
                            )

                            // Checkbox to mark the task as important
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = isImportant,
                                    onCheckedChange = { isImportant = it }
                                )
                                Text("Mark as Important")
                            }

                            // Due Date and Time Picker
                            Text("Due Date & Time: ${newTaskDueDate?.let { SimpleDateFormat("yyyy-MM-dd HH:mm").format(it) } ?: "Not set"}")
                            Button(onClick = {
                                showDateTimePickerDialog(context) { selectedDateTime ->
                                    newTaskDueDate = selectedDateTime
                                }
                            }) {
                                Text("Select Due Date & Time")
                            }
                        }
                    },
                    confirmButton = {
                        Button(onClick = {
                            // Add new task to Firestore with due date
                            scope.launch {
                                firestoreUtils.addOrUpdateTask(
                                    Task(
                                        title = newTaskTitle,
                                        description = newTaskDescription,
                                        dueDate = newTaskDueDate,
                                        important = isImportant
                                    ), currentUserEmail
                                )

                                // Optionally refresh the tasks after adding
                                tasks = firestoreUtils.getTasks(currentUserEmail, isImportant = false, isCompleted = false)

                                // Reset the input fields
                                newTaskTitle = ""
                                newTaskDescription = ""
                                newTaskDueDate = null
                                showDialog = false
                            }
                        }) {
                            Text("Add")
                        }
                    },
                    dismissButton = {
                        Button(onClick = { showDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
        // Floating Action Button to add a task
        FloatingActionButton(
            onClick = { showDialog = true },
            shape = CircleShape,
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.BottomEnd)
        ) {
            Icon(Icons.Filled.Add, "floating action button.")
        }
    }
}

fun showDateTimePickerDialog(context: Context, onDateTimeSelected: (Date) -> Unit) {
    val calendar = Calendar.getInstance()

    // Date Picker
    android.app.DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            calendar.set(year, month, dayOfMonth)

            // Time Picker
            TimePickerDialog(context, { _, hourOfDay, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)

                // Return the selected date and time
                onDateTimeSelected(calendar.time)
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()

        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).show()
}

@Composable
fun TaskItem(task: Task, onTaskCheckedChange: (Boolean) -> Unit, onTaskClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = onTaskClick)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Checkbox for important and pending tasks
        Checkbox(
            checked = task.completed,
            onCheckedChange = onTaskCheckedChange
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            // Task title
            Text(
                text = task.title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    textDecoration = if (task.completed) TextDecoration.LineThrough else TextDecoration.None
                ),
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            // Due Date and Time below the title
            task.dueDate?.let {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Due: ${SimpleDateFormat("yyyy-MM-dd HH:mm").format(it)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }

//        // Optional: Task description, depending on whether you want to display it here
//        Spacer(modifier = Modifier.width(16.dp))
//        Text(
//            text = task.description,
//            style = MaterialTheme.typography.bodyMedium,
//            color = MaterialTheme.colorScheme.onPrimaryContainer
//        )
    }
}
