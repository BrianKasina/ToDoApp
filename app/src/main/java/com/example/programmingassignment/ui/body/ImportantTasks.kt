package com.example.programmingassignment.ui.body

import android.app.TimePickerDialog
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.programmingassignment.data.Task
import com.example.programmingassignment.ui.tasks.TaskItem
import com.example.programmingassignment.ui.tasks.showDateTimePickerDialog
import com.example.programmingassignment.util.FirestoreUtils
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

@Composable
fun ImportantTasksScreen(firestoreUtils: FirestoreUtils, paddingValues: PaddingValues) {
    val scope = rememberCoroutineScope()
    var tasks by remember { mutableStateOf(listOf<Task>()) }
    var showDetails by remember { mutableStateOf(false) }
    var selectedTask by remember { mutableStateOf<Task?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var newTaskTitle by remember { mutableStateOf("") }
    var newTaskDescription by remember { mutableStateOf("") }
    var newTaskDueDate by remember { mutableStateOf<Date?>(null) }
    var isImportant by remember { mutableStateOf(true) }

    // Load important tasks
    LaunchedEffect(Unit) {
        scope.launch {
            tasks = firestoreUtils.getTasks(isImportant = true, isCompleted = false)
        }
    }

    val context = LocalContext.current // Get the current context

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Text(
                text = "Important Tasks",
                style = MaterialTheme.typography.displayMedium.copy(
                    color = MaterialTheme.colorScheme.surfaceVariant, // Text color
                    fontWeight = FontWeight.Bold // Bold text
                )
            )

            // Use LazyColumn for the list of important tasks to enable scrolling
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(tasks) { task ->
                    TaskItem(
                        task = task,
                        onTaskCheckedChange = { isChecked ->
                            // Update task completion status
                            scope.launch {
                                firestoreUtils.addOrUpdateTask(task.copy(completed = isChecked))
                                // Optionally refresh the tasks after updating
                                tasks =
                                    firestoreUtils.getTasks(isImportant = true, isCompleted = false)
                            }
                        },
                        onTaskClick = {
                            selectedTask = task
                            showDetails = true // Show task details
                        }
                    )
                }
            }
        }

        // Display task details in an AlertDialog if a task is selected
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
                                tasks =
                                    firestoreUtils.getTasks(isImportant = true, isCompleted = false)
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
                                onCheckedChange = {}
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
                                )
                            )

                            // Optionally refresh the tasks after adding
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
}
