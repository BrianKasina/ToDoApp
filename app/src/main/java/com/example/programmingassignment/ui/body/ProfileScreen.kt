package com.example.programmingassignment.ui.body

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.programmingassignment.R
import com.example.programmingassignment.util.AuthUtils

@Composable
fun ProfileScreen(authUtils: AuthUtils, paddingValues: PaddingValues) {
    // Retrieve the current user
    val currentUser = authUtils.getCurrentUser()

    // User's information to be displayed
    val userName = currentUser?.displayName ?: "Unknown User"
    val userEmail = currentUser?.email ?: "No email available"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Profile Picture
        Image(
            painter = painterResource(R.drawable.img), // Placeholder image
            contentDescription = "Profile Picture",
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Display user's name and email from Firebase
        Text(text = userName, style = MaterialTheme.typography.titleLarge)
        Text(text = userEmail, style = MaterialTheme.typography.bodyMedium)

        Spacer(modifier = Modifier.height(32.dp))

        // Additional Profile Details (if available)
        ProfileDetailRow(label = "Phone Number", value = currentUser?.phoneNumber ?: "No phone number")
        // You can add more fields here like emergency contact if available in the user profile
    }
}

@Composable
fun ProfileDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
        Text(text = value, style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
    }
}
