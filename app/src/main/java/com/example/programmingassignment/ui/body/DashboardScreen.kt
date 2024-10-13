package com.example.programmingassignment.ui.body


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import java.util.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.programmingassignment.util.FirestoreUtils
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Composable
fun DashboardScreen(firestoreUtils: FirestoreUtils, paddingValues: PaddingValues) {
    val scope = rememberCoroutineScope()
    var completedTasks by remember { mutableStateOf(0) }
    var activeTasks by remember { mutableStateOf(0) }
    var importantTasks by remember { mutableStateOf(0) }
    var tasksDueToday by remember { mutableStateOf(0) }
    val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email ?: ""

    // Load task data
    LaunchedEffect(Unit) {
        scope.launch {
            completedTasks = firestoreUtils.getTasks(currentUserEmail, isCompleted = true).size
            activeTasks = firestoreUtils.getTasks(currentUserEmail, isCompleted = false).size
            importantTasks = firestoreUtils.getTasks(currentUserEmail, isCompleted = false, isImportant = true).size
            tasksDueToday = firestoreUtils.getCountOfDatedTasks(dueDate = getTodayDate())
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
            text = "Dashboard",
            style = MaterialTheme.typography.displayMedium.copy(
                color = MaterialTheme.colorScheme.surfaceVariant, // Text color
                fontWeight = FontWeight.Bold // Bold text
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Display task statistics in cards with different colors
        TaskStatCard(label = "Completed Tasks", value = completedTasks, cardColor = Color(0xFF4CAF50)) // Green
        TaskStatCard(label = "Active Tasks", value = activeTasks, cardColor = Color(0xFF2196F3)) // Blue
        TaskStatCard(label = "Important Tasks", value = importantTasks, cardColor = Color(0xFFFFC107)) // Yellow
        TaskStatCard(label = "Tasks Due Today", value = tasksDueToday, cardColor = Color(0xFFF44336)) // Red
    }
}

@Composable
fun TaskStatCard(label: String, value: Int, cardColor: Color) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(value) {
        visible = true
    }

    AnimatedVisibility(visible) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .height(100.dp),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(containerColor = cardColor) // Set the card color here
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = label, style = MaterialTheme.typography.titleLarge)
                Text(text = value.toString(), style = MaterialTheme.typography.displayLarge)
            }
        }
    }
}

// Utility function to get today's date (modify based on your requirements)
fun getTodayDate(): Date {
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar.time
}
