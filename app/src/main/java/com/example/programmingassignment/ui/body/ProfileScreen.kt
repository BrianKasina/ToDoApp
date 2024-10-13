package com.example.programmingassignment.ui.body

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.google.firebase.auth.FirebaseAuth

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
            .padding(paddingValues)
            .background(MaterialTheme.colorScheme.background), // Optional background color
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Profile Picture with a border
        Image(
            painter = painterResource(R.drawable.img), // Placeholder image
            contentDescription = "Profile Picture",
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape), // Adding a border to the image
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(16.dp))

        // User Info Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(8.dp)),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.Start // Aligning text to the left
            ) {
                Text(text = userName, style = MaterialTheme.typography.headlineMedium) // Larger font for name
                Text(text = userEmail, style = MaterialTheme.typography.bodyMedium)

                Spacer(modifier = Modifier.height(24.dp))

                // Additional Profile Details
                ProfileDetailRow(label = "Phone Number", value = currentUser?.phoneNumber ?: "No phone number")// Add more fields as necessary

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(onClick = {
                        AuthUtils(FirebaseAuth.getInstance()).signOut()
                    }) {
                        Text("Log Out")
                    }
                }
            }
        }
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
