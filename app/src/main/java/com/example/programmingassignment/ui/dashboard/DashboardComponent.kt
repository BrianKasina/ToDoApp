package com.example.programmingassignment.ui.dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.programmingassignment.R
import kotlinx.coroutines.launch
import com.example.programmingassignment.util.AuthUtils
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.programmingassignment.navigation.AppNavigator
import com.example.programmingassignment.ui.body.DashboardScreen
import com.example.programmingassignment.util.FirestoreUtils
import com.google.firebase.firestore.FirebaseFirestore


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardComponent() {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var showMenu by remember { mutableStateOf(false) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                DrawerContent(drawerState, navController)
            }
        }
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    title = { Text("To Do App") },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch {
                                drawerState.open()
                            }
                        }) {
                            Icon(Icons.Filled.Menu, contentDescription = "Menu")
                        }
                    },
                    actions = {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Profile",
                            modifier = Modifier
                                .size(50.dp)
                                .padding(start = 8.dp, end = 8.dp)
                                .clickable (
                                    onClick = {
                                        navController.navigate("profile")
                                        scope.launch { drawerState.close() }
                                    }
                                )
                        )
                    }
                )
            },
            content = { paddingValues ->
                AppNavigator( navController, paddingValues= paddingValues)
            }
        )
    }
}

@Composable
fun DrawerContent(drawerState: DrawerState, navController: NavHostController) {
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header with Profile Info
        DrawerHeader()

        HorizontalDivider()

        // Drawer Items with Icons
        Spacer(modifier = Modifier.height(20.dp))
        DrawerItem(
            icon = Icons.Filled.Home,
            label = "Dashboard",
            onClick = {
                // Navigate to Profile Screen
                navController.navigate("dashboard")
                scope.launch { drawerState.close() }
            }
        )
        DrawerItem(
            icon = Icons.Filled.CheckCircle,
            label = "tasks",
            onClick = {
                navController.navigate("tasks")
                scope.launch { drawerState.close() }
            }
        )
        DrawerItem(
            icon = Icons.Filled.CheckCircle,
            label = "Important tasks",
            onClick = {
                navController.navigate("important")
                scope.launch { drawerState.close() }
            }
        )
        DrawerItem(
            icon = Icons.Filled.CheckCircle,
            label = "Completed tasks",
            onClick = {
                navController.navigate("completed")
                scope.launch { drawerState.close() }
            }
        )

        Spacer(modifier = Modifier.weight(1f))
        DrawerItem(
            icon = Icons.AutoMirrored.Filled.ExitToApp,
            label = "Logout",
            onClick = {
                // Sign out logic
                AuthUtils(FirebaseAuth.getInstance()).signOut()
                scope.launch { drawerState.close() }
            }
        )
    }
}

@Composable
fun DrawerHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Placeholder for profile picture (replace with actual image if available)
        Image(
            painter = painterResource(R.drawable.img), // Add a placeholder image in resources
            contentDescription = "Profile Picture",
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = "John Doe", style = MaterialTheme.typography.titleMedium)
        Text(text = "john.doe@example.com", style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun DrawerItem(icon: ImageVector, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = label, tint = Color.Gray)
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun MainContent(paddingValues: PaddingValues) {
    DashboardScreen(firestoreUtils = FirestoreUtils(FirebaseFirestore.getInstance()), paddingValues = paddingValues)
}

@Preview(showBackground = true)
@Composable
fun PreviewDashboardComponent() {
    DashboardComponent()
}