package com.superbgoal.caritasrig.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.superbgoal.caritasrig.data.model.User

class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get the current user UID from FirebaseAuth
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid

        if (userId == null) {
            // If user is not logged in, redirect to LoginActivity
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            return
        }

        setContent {
            HomeScreen(userId, onLogout = {
                // Sign out and navigate back to LoginActivity
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            })
        }
    }
}

@Composable
fun HomeScreen(userId: String, onLogout: () -> Unit) {
    var user by remember { mutableStateOf<User?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Firebase database initialization
    val databaseUrl = "https://caritas-rig-default-rtdb.asia-southeast1.firebasedatabase.app"
    val database = FirebaseDatabase.getInstance(databaseUrl).reference

    LaunchedEffect(userId) {
        database.child("users").child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("HomeScreenData", "Data snapshot: $snapshot")
                if (snapshot.exists()) {
                    user = snapshot.getValue(User::class.java)
                } else {
                    errorMessage = "User not found"
                    Log.e("HomeScreen", "User not found")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                errorMessage = "Failed to load data: ${error.message}"
                Log.e("HomeScreen", "Failed to read user data: ${error.message}")
            }
        })
    }

    // UI
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (user != null) {
                    UserProfile(user!!)
                } else if (errorMessage != null) {
                    ErrorMessage(errorMessage!!)
                } else {
                    LoadingScreen()
                }
            }

            // Logout Button
            Button(
                onClick = { onLogout() },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                Text(text = "Logout")
            }
        }
    }
}

@Composable
fun UserProfile(user: User) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Welcome, ${user.firstName}",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "${user.firstName} ${user.lastName}")
        Text(text = "Username: ${user.username}")
        Text(text = "Date of Birth: ${user.dateOfBirth}")
    }
}

@Composable
fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun ErrorMessage(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = message, color = MaterialTheme.colorScheme.error)
    }
}
