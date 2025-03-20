import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import com.example.staysafe.viewModel.MapViewModel
import com.google.android.gms.location.LocationServices

@Composable
fun RegisterUserScreen(navController: NavController, viewModel: MapViewModel) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var userLatitude by remember { mutableStateOf<Double?>(null) }
    var userLongitude by remember { mutableStateOf<Double?>(null) }

    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    )
    { isGranted ->
        if (isGranted) {
            getCurrentLocation(context) { lat, lon ->
                userLatitude = lat
                userLongitude = lon
            }
        } else {
            Log.d("MapScreen", "Location permission denied")
        }
    }

    // * Fetch device location when screen loads
    LaunchedEffect(Unit) {
        permissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        permissionLauncher.launch(android.Manifest.permission.ACCESS_COARSE_LOCATION)
        permissionLauncher.launch(android.Manifest.permission.INTERNET)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.Black)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Register",
            style = MaterialTheme.typography.headlineLarge,
            color = Color.White,
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.CenterHorizontally),
        )

        Text(
            "Please fill in the form below to register",
            style = MaterialTheme.typography.titleSmall,
            color = Color.White,
            modifier = Modifier
                .padding(8.dp)
                .align(Alignment.CenterHorizontally),
        )

        OutlinedTextField(
            value = firstName,
            onValueChange = { firstName = it },
            label = { Text("First Name") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
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
        OutlinedTextField(
            value = lastName,
            onValueChange = { lastName = it },
            label = { Text("Last Name") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
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
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Phone Number") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedLabelColor = Color.White,
                unfocusedLabelColor = Color.Gray,
                unfocusedContainerColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
        )
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
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
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
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
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
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

        Button(
            onClick = {
                if (firstName.isBlank() || lastName.isBlank() || phone.isBlank() || username.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
                    Toast.makeText(context, "All fields are required!", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                if (password != confirmPassword) {
                    Toast.makeText(context, "Passwords do not match!", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                if (password.length < 8) {
                    Toast.makeText(context, "Password must be at least 8 characters long!", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                if (phone.startsWith("0")) {
                    // Remove the 0 and replace with country code
                    phone = "+44" + phone.substring(1)
                    Log.d("RegisterUserScreen", "Updated phone number: $phone")
                } else if (phone.length < 12) {
                    Toast.makeText(context, "Phone number must be 12 digits long!", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                if (userLatitude == null || userLongitude == null) {
                    Toast.makeText(
                        context,
                        "Location not available. Please enable GPS.",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@Button
                }

                val newUser = viewModel.createUser(
                    firstName = firstName,
                    lastName = lastName,
                    phone = phone,
                    username = username,
                    password = password,
                    userLatitude = userLatitude,
                    userLongitude = userLongitude
                )

                if (newUser != null) {
                    Toast.makeText(context, "Registration successful!", Toast.LENGTH_SHORT).show()
                    navController.navigate("login")
                } else {
                    Toast.makeText(context, "User already exists!", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("Register")
        }

        Spacer(modifier = Modifier.height(16.dp))


        Button(
            onClick = { navController.navigate("login") },
            modifier = Modifier
                .fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
        ) {
            Text(
                "Already have an account?",
                style = MaterialTheme.typography.titleSmall,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}


@SuppressLint("MissingPermission")
fun getCurrentLocation(context: Context, onLocationReceived: (Double, Double) -> Unit) {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
        if (location != null) {
            onLocationReceived(location.latitude, location.longitude)
        }
    }
}
