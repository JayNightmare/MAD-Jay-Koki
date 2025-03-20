package com.example.staysafe.view.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.staysafe.viewModel.MapViewModel

@Composable
fun LoginScreen(navController: NavController, viewModel: MapViewModel) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val users by viewModel.user.collectAsState(emptyList()) // Fetch users for dropdown
    val context = LocalContext.current

    // Fetch users when screen loads
    LaunchedEffect(Unit) {
        viewModel.fetchAllUsers()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.Black),
        verticalArrangement = Arrangement.Center,
    ) {
        Text("Login",
            style = MaterialTheme.typography.headlineLarge,
            color = Color.White,
            modifier = Modifier.padding(16.dp).align(Alignment.CenterHorizontally),
        )

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 10.dp, end = 10.dp, bottom = 5.dp),
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedLabelColor = Color.White,
                unfocusedLabelColor = Color.Gray,
                unfocusedContainerColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
            )
        )

//        Spacer(modifier = Modifier.height(5.dp))

        // Password Input
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 10.dp, end = 10.dp, bottom = 5.dp),
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedLabelColor = Color.White,
                unfocusedLabelColor = Color.Gray,
                unfocusedContainerColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Login Button
        Button(
            onClick = {
                val matchUser = users.find { it.userUsername == username }
                Log.d("Flow", "Match User: $matchUser")

                if (matchUser != null && viewModel.authenticateUser(matchUser, password)) {
                    Log.d("Flow", "User authenticated: $matchUser")
                    viewModel.setLoggedInUser(matchUser.userUsername)
                    navController.navigate("map")
                } else {
                    Toast.makeText(context, "Invalid username or password", Toast.LENGTH_SHORT)
                        .show()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("Login")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Register Button
        Button(
            onClick = {
                navController.navigate("register")
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        ) {
            Text(
                "Don't have an account?",
                style = MaterialTheme.typography.titleSmall,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
