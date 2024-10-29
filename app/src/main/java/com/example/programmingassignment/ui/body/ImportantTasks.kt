package com.example.programmingassignment.ui.body

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.example.programmingassignment.ui.tasks.calculateNextDueDate
import com.example.programmingassignment.util.FirestoreUtils
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.TimeZone

@Composable
fun ImportantTasksScreen(firestoreUtils: FirestoreUtils, paddingValues: PaddingValues) {
    val scope = rememberCoroutineScope()
    val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email ?: ""
    var tasks by remember { mutableStateOf(listOf<Task>()) }
    var showDetails by remember { mutableStateOf(false) }
    var selectedTask by remember { mutableStateOf<Task?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var newTaskTitle by remember { mutableStateOf("") }
    var newTaskDescription by remember { mutableStateOf("") }
    var newTaskDueDate by remember { mutableStateOf<Date?>(null) }
    val isImportant by remember { mutableStateOf(true) }
    val isCompleted by remember { mutableStateOf(false) }
    var isEditing by remember { mutableStateOf(false) }
    var selectedRecurrence by remember { mutableStateOf("Once") }
    val today = Calendar.getInstance( TimeZone.getDefault()).apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.time

    // Setup real-time task updates based on selected filter
    DisposableEffect(Unit) {
        loadImportantTasks(
            firestoreUtils,
            currentUserEmail,
            today,
            selectedRecurrence
        ) { updatedTasks ->
            tasks = updatedTasks
        }

        onDispose {
            firestoreUtils.removeListener() // Detach listener when composable is disposed
        }
    }

    // Update tasks when recurrence changes
    LaunchedEffect(selectedRecurrence) {
        loadImportantTasks(
            firestoreUtils,
            currentUserEmail,
            today,
            selectedRecurrence
        ) { updatedTasks ->
            tasks = updatedTasks
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


            // Use LazyColumn for the list of important tasks to enable scrolling
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(tasks) { task ->
                    TaskItem(
                        task = task,
                        onTaskCheckedChange = { isChecked ->
                            isEditing= true
                            // Update task completion status
                            scope.launch {
                                if (isChecked) {
                                    val updatedTask = task.copy(
                                        completed = true,
                                        dateCompleted = Date()
                                    )
                                    firestoreUtils.addOrUpdateTask(updatedTask, currentUserEmail)

                                    if (task.recurrence != null && task.recurrence != "Once") {
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
                            isEditing= false

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
                                    important = isImportant,
                                    dateCompleted = null,
                                    recurrence = if (selectedRecurrence == "Once") null else selectedRecurrence
                                ), currentUserEmail
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

// Function to load tasks based on recurrence
fun loadImportantTasks(
    firestoreUtils: FirestoreUtils,
    currentUserEmail: String,
    today: Date,
    selectedRecurrence: String,
    onTasksUpdated: (List<Task>) -> Unit
) {
    firestoreUtils.getTasks(
        currentUserEmail = currentUserEmail,
        isCompleted = false,
        isImportant = true,
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
