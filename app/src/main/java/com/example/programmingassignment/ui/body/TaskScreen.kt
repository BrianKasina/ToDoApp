package com.example.programmingassignment.ui.tasks

import android.app.TimePickerDialog
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.ui.platform.LocalContext // Add this line
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.programmingassignment.data.Task
import com.example.programmingassignment.ui.body.TaskDetailsScreen
import com.example.programmingassignment.util.FirestoreUtils
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

@Composable
fun TaskScreen(firestoreUtils: FirestoreUtils, paddingValues: PaddingValues) {
    val scope = rememberCoroutineScope()
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
                "pending" -> firestoreUtils.getTasks(isCompleted = false, isImportant = false)
                else -> {firestoreUtils.getTasks(isCompleted = false)}
            }
        }
    }

    val context = LocalContext.current // Get the current context

    Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {


        Spacer(modifier = Modifier.height(16.dp))

        // Task List
        tasks.forEach { task ->
            TaskItem(task = task, onTaskCheckedChange = { isChecked ->
                scope.launch {
                    // Update task completion status in Firestore
                    firestoreUtils.addOrUpdateTask(task.copy(completed = isChecked, id = task.id))
                    // Optionally refresh the tasks after updating
                    tasks = firestoreUtils.getTasks(isImportant = false, isCompleted = false)
                }
            }, onTaskClick = {
                selectedTask = task
                showDetails = true // Show task details
            })
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Floating Action Button to add a task
        FloatingActionButton(
            onClick = { showDialog = true },
            shape = CircleShape,
            modifier = Modifier
                .padding(16.dp)
        ) {
            Icon(Icons.Filled.Add, "floating action button.")
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
                                tasks = firestoreUtils.getTasks(isCompleted = false, isImportant = false)
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

        // Task Dialog
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
                            // Open the date and time picker dialog
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
                                    important= isImportant
                                )
                            )

                            //optionally refresh the page after adding a task
                            tasks = firestoreUtils.getTasks(isImportant = false, isCompleted = false)

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
}}

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
            .clickable(
                    onClick = onTaskClick)
            ,
        verticalAlignment = Alignment.Top
    ) {
        // Checkbox for important and pending tasks
        if (!task.completed) {
            Checkbox(
                checked = task.completed,
                onCheckedChange = onTaskCheckedChange
            )
        } else {
            // Display a strikethrough text if the task is completed
            Text(
                text = task.title,
                style = MaterialTheme.typography.bodyLarge.copy(textDecoration = TextDecoration.LineThrough),
                modifier = Modifier.weight(1f)
            )
        }

        Text(text = task.description, style = MaterialTheme.typography.bodyMedium)

        // Display Due Date and Time
        task.dueDate?.let {
            Text(text = SimpleDateFormat("yyyy-MM-dd HH:mm").format(it), style = MaterialTheme.typography.bodyMedium)
        }
    }
}
