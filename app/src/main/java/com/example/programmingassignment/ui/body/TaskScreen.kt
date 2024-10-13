package com.example.programmingassignment.ui.tasks

import android.app.TimePickerDialog
import android.content.Context
import androidx.compose.ui.platform.LocalContext // Add this line
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.programmingassignment.data.Task
import com.example.programmingassignment.util.FirestoreUtils
import com.google.firebase.firestore.FirebaseFirestore
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

    // Load tasks based on filter
    LaunchedEffect(selectedFilter) {
        scope.launch {
            tasks = when (selectedFilter) {
                "Completed" -> firestoreUtils.getTasks(isCompleted = true)
                "Important" -> firestoreUtils.getTasks(isImportant = true)
                else -> firestoreUtils.getTasks(isCompleted = false)
            }
        }
    }

    val context = LocalContext.current // Get the current context

    Column(
        modifier = Modifier.fillMaxSize().padding(paddingValues)
    ) {
        // Filter Buttons
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Button(onClick = { selectedFilter = "Pending" }) { Text("Pending") }
            Button(onClick = { selectedFilter = "Completed" }) { Text("Completed") }
            Button(onClick = { selectedFilter = "Important" }) { Text("Important") }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Add Task Button
        Button(onClick = { showDialog = true }) {
            Text("Add Task")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Task List
        tasks.forEach { task ->
            TaskItem(task = task, onTaskCheckedChange = { isChecked ->
                scope.launch {
                    // Update task completion status in Firestore
                    firestoreUtils.addOrUpdateTask(task.copy(isCompleted = isChecked))
                }
            })
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
                                    dueDate = newTaskDueDate
                                )
                            )
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
fun TaskItem(task: Task, onTaskCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Checkbox for important and pending tasks
        if (!task.isCompleted) {
            Checkbox(
                checked = task.isCompleted,
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

        Text(text = task.title, style = MaterialTheme.typography.bodyLarge)

        // Display Due Date and Time
        task.dueDate?.let {
            Text(text = SimpleDateFormat("yyyy-MM-dd HH:mm").format(it), style = MaterialTheme.typography.bodyMedium)
        }
    }
}
