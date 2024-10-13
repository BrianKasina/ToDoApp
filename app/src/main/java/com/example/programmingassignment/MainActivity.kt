package com.example.programmingassignment

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.example.programmingassignment.ui.auth.LoginRegisterScreen
import com.example.programmingassignment.ui.dashboard.DashboardComponent
import com.example.programmingassignment.util.AuthUtils
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    private lateinit var authUtils: AuthUtils

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase Auth
        val firebaseAuth = FirebaseAuth.getInstance()
        authUtils = AuthUtils(firebaseAuth)

        setContent {
            var isAuthenticated by remember { mutableStateOf(authUtils.isAuthenticated()) }

            LaunchedEffect(Unit) {
                authUtils.addAuthStateListener { user ->
                    isAuthenticated = user != null
                }
            }

            if (isAuthenticated) {
                DashboardComponent()
            } else {
                LoginRegisterScreen(authUtils) {
                    isAuthenticated = true
                }
            }
        }
    }
}