package com.example.programmingassignment.ui.tasks

import android.app.TimePickerDialog
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.programmingassignment.data.Task
import com.example.programmingassignment.ui.body.TaskDetailsScreen
import com.example.programmingassignment.util.FirestoreUtils
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TaskScreen(firestoreUtils: FirestoreUtils, paddingValues: PaddingValues) {
    val scope = rememberCoroutineScope()
    val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email ?: ""
    var tasks by remember { mutableStateOf(listOf<Task>()) }
    var showDialog by remember { mutableStateOf(false) }
    var newTaskTitle by remember { mutableStateOf("") }
    var newTaskDescription by remember { mutableStateOf("") }
    var newTaskDueDate by remember { mutableStateOf<Date?>(null) }
    var isImportant by remember { mutableStateOf(false) }
    var showDetails by remember { mutableStateOf(false) }
    var selectedTask by remember { mutableStateOf<Task?>(null) }
    val today = Calendar.getInstance(TimeZone.getDefault()).apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.time
    var selectedRecurrence by remember { mutableStateOf("Once") }

    // Setup real-time task updates based on selected filter
    DisposableEffect(Unit) {
        loadTasks(firestoreUtils, currentUserEmail, today, selectedRecurrence) { updatedTasks ->
            tasks = updatedTasks
        }

        onDispose {
            firestoreUtils.removeListener() // Detach listener when composable is disposed
        }
    }

    // Update tasks when recurrence changes
    LaunchedEffect(selectedRecurrence) {
        loadTasks(firestoreUtils, currentUserEmail, today, selectedRecurrence) { updatedTasks ->
            tasks = updatedTasks
        }
    }

    val context = LocalContext.current

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
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    fontWeight = FontWeight.Bold
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
                            containerColor = if (selectedRecurrence == option) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
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

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(tasks) { task ->
                    TaskItem(task = task, onTaskCheckedChange = { isChecked ->
                        scope.launch {
                            if (isChecked) {
                                val updatedTask = task.copy(
                                    completed = true,
                                    dateCompleted = Date()
                                )
                                firestoreUtils.addOrUpdateTask(updatedTask, currentUserEmail)

                                if (task.recurrence != null && task.recurrence != "One-Time") {
                                    val nextDueDate = calculateNextDueDate(task)
                                    val recurringTask = updatedTask.copy(
                                        completed = false,
                                        dueDate = nextDueDate
                                    )
                                    firestoreUtils.addOrUpdateTask(recurringTask, currentUserEmail)
                                }
                            } else {
                                firestoreUtils.addOrUpdateTask(task.copy(completed = false, dateCompleted = null), currentUserEmail)
                            }
                        }
                    }, onTaskClick = {
                        selectedTask = task
                        showDetails = true
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
                            onDismiss = {
                                showDetails = false
                                scope.launch {
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

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = isImportant,
                                    onCheckedChange = { isImportant = it }
                                )
                                Text("Mark as Important")
                            }

                            Text("Due Date & Time: ${newTaskDueDate?.let { SimpleDateFormat("yyyy-MM-dd HH:mm").format(it) } ?: "Not set"}")
                            Button(onClick = {
                                showDateTimePickerDialog(context) { selectedDateTime ->
                                    newTaskDueDate = selectedDateTime
                                }
                            }) {
                                Text("Select Due Date & Time")
                            }

                            var expanded by remember { mutableStateOf(false) }
                            val recurrenceOptions = listOf("Once", "Daily", "Monthly", "Yearly")

                            Text("Recurrence")
                            Box {
                                Text(
                                    selectedRecurrence,
                                    modifier = Modifier
                                        .clickable { expanded = !expanded }
                                        .background(MaterialTheme.colorScheme.primaryContainer)
                                        .padding(8.dp)
                                )
                                DropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false }
                                ) {
                                    recurrenceOptions.forEach { option ->
                                        DropdownMenuItem(
                                            onClick = {
                                                selectedRecurrence = option
                                                expanded = false
                                            },
                                            text = { Text(option) }
                                        )
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(onClick = {

                            scope.launch {
                                firestoreUtils.addOrUpdateTask(
                                    Task(
                                        title = newTaskTitle,
                                        description = newTaskDescription,
                                        dueDate = newTaskDueDate,
                                        important = isImportant,
                                        dateCompleted = null,
                                        recurrence = if (selectedRecurrence == "Once") null else selectedRecurrence
                                    ),
                                    currentUserEmail
                                )
                                newTaskTitle = ""
                                newTaskDescription = ""
                                newTaskDueDate = null
                            }
                            showDialog=false
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

fun calculateNextDueDate(task: Task): Date? {
    val calendar = Calendar.getInstance()
    calendar.time = task.dueDate ?: return null

    when (task.recurrence) {
        "Daily" -> calendar.add(Calendar.DAY_OF_MONTH, 1)
        "Monthly" -> calendar.add(Calendar.MONTH, 1)
        "Yearly" -> calendar.add(Calendar.YEAR, 1)
    }
    return calendar.time
}

fun showDateTimePickerDialog(context: Context, onDateTimeSelected: (Date) -> Unit) {
    val calendar = Calendar.getInstance()
    android.app.DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            calendar.set(year, month, dayOfMonth)
            TimePickerDialog(context, { _, hourOfDay, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)
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
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .clickable(onClick = onTaskClick)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Checkbox with padding
        Checkbox(
            checked = task.completed,
            onCheckedChange = onTaskCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = MaterialTheme.colorScheme.primary,
                uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier.padding(end = 12.dp)
        )

        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Task Title
            Text(
                text = task.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textDecoration = if (task.completed) TextDecoration.LineThrough else null
                )
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Task Description
            Text(
                text = task.description,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                maxLines = 3,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Separator
            Divider(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Task Time Information
            Text(
                text = if (task.completed) "Completed on: ${SimpleDateFormat("yyyy-MM-dd HH:mm").format(task.dateCompleted ?: Date())}"
                else "Due: ${SimpleDateFormat("yyyy-MM-dd HH:mm").format(task.dueDate ?: Date())}",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}

// Function to load tasks based on recurrence
fun loadTasks(
    firestoreUtils: FirestoreUtils,
    currentUserEmail: String,
    today: Date,
    selectedRecurrence: String,
    onTasksUpdated: (List<Task>) -> Unit
) {
    firestoreUtils.getTasks(
        currentUserEmail = currentUserEmail,
        isCompleted = false,
        isImportant = false,
        dueDate = today
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
