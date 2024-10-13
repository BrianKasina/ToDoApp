package com.example.programmingassignment.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.programmingassignment.util.AuthUtils
import com.example.programmingassignment.ui.body.ProfileScreen // Import your screens here
import com.example.programmingassignment.ui.dashboard.MainContent // Import your dashboard content
import com.example.programmingassignment.ui.auth.LoginRegisterScreen
import com.example.programmingassignment.ui.tasks.TaskScreen
import com.example.programmingassignment.util.FirestoreUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigator(navController: NavHostController, paddingValues: PaddingValues) {
    NavHost(navController = navController, startDestination = "dashboard") {
        composable("dashboard") {
            MainContent(paddingValues = paddingValues) // Main dashboard content
        }
        composable("profile") {
            ProfileScreen(authUtils = AuthUtils(FirebaseAuth.getInstance()), paddingValues = paddingValues) // Profile screen content
        }
        composable("dialysis") {
            // Dialysis screen content
        }
        composable("tasks") {
            TaskScreen(firestoreUtils = FirestoreUtils(FirebaseFirestore.getInstance()), paddingValues = paddingValues)
            // Nephrologist screen content
        }
        composable("appointment") {
            // Appointment screen content
        }
        // You can add more screens here
    }
}
