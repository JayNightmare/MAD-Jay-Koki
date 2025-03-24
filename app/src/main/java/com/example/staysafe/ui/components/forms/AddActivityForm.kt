package com.example.staysafe.ui.components.forms

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.staysafe.R
import com.example.staysafe.viewModel.MapViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AddActivityForm(viewModel: MapViewModel, onClose: () -> Unit) {
    var activityName by remember { mutableStateOf("") }
    var startPostCode by remember { mutableStateOf("") }
    var startAddressLine by remember { mutableStateOf("") }
    var destPostCode by remember { mutableStateOf("") }
    var destAddressLine by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    val travelMode by remember { mutableStateOf("DRIVE") }

    // **Date and Time Inputs**
    var fromDate by remember { mutableStateOf("") }
    var fromTime by remember { mutableStateOf("") }
    var toDate by remember { mutableStateOf("") }
    var toTime by remember { mutableStateOf("") }

    var isAdding by remember { mutableStateOf(false) }
    var routePoints by remember { mutableStateOf<List<LatLng>>(emptyList()) }
    val context = LocalContext.current

    val nightMapStyle = remember {
        context.resources.openRawResource(R.raw.map_style).bufferedReader().use { it.readText() }
    }

    // **Trigger route calculation when locations change**
    LaunchedEffect(startPostCode, startAddressLine, destPostCode, destAddressLine) {
        if (startPostCode.isNotBlank() && startAddressLine.isNotBlank() && destPostCode.isNotBlank() && destAddressLine.isNotBlank()) {
            viewModel.getRouteForLocation(
                startPostCode,
                startAddressLine,
                destPostCode,
                destAddressLine,
                travelMode
            ) { route ->
                routePoints = route
            }
        }
    }

    LaunchedEffect(isAdding) {
        if (isAdding) {
            kotlinx.coroutines.delay(1000)
            onClose()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Add Activity", style = MaterialTheme.typography.headlineLarge, color = Color.White)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = activityName,
            onValueChange = { activityName = it },
            label = { Text("Activity Name") },
            modifier = Modifier.fillMaxWidth(),
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

        Text("Starting Location", color = Color.White, style = MaterialTheme.typography.titleSmall)
        OutlinedTextField(
            value = startPostCode,
            onValueChange = { startPostCode = it },
            label = { Text("Post Code") },
            modifier = Modifier.fillMaxWidth(),
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
            value = startAddressLine,
            onValueChange = { startAddressLine = it },
            label = { Text("Address Line") },
            modifier = Modifier.fillMaxWidth(),
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

        Text("Destination Location", color = Color.White, style = MaterialTheme.typography.titleSmall)
        OutlinedTextField(
            value = destPostCode,
            onValueChange = { destPostCode = it },
            label = { Text("Post Code") },
            modifier = Modifier.fillMaxWidth(),
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
            value = destAddressLine,
            onValueChange = { destAddressLine = it },
            label = { Text("Address Line") },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedLabelColor = Color.White,
                unfocusedLabelColor = Color.Gray,
                unfocusedContainerColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedLabelColor = Color.White,
                unfocusedLabelColor = Color.Gray,
                unfocusedContainerColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row {
            DateTimePicker("From Date", fromDate) { fromDate = it }
            Spacer(modifier = Modifier.width(8.dp))
            DateTimePicker("From Time", fromTime, isTimePicker = true) { fromTime = it }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row {
            DateTimePicker("To Date", toDate) { toDate = it }
            Spacer(modifier = Modifier.width(8.dp))
            DateTimePicker("To Time", toTime, isTimePicker = true) { toTime = it }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (routePoints.isNotEmpty()) {
            Text("Route Preview", color = Color.White)
            Spacer(modifier = Modifier.height(16.dp))
            GoogleMap(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp),
                cameraPositionState = rememberCameraPositionState {
                    position = CameraPosition.fromLatLngZoom(routePoints.first(), 11f)
                },
                properties = MapProperties(
                    mapStyleOptions = MapStyleOptions(nightMapStyle),
                    mapType = MapType.NORMAL
                )
            ) {
                Polyline(points = routePoints, color = Color.Blue, width = 8f)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (!isValidDateTime(fromDate, fromTime, toDate, toTime)) {
                    Toast.makeText(context, "Invalid date/time. Please select future values.", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                if (activityName.length < 8) {
                    Toast.makeText(context, "Activity name must be at least 8 characters long", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                if (activityName.isNotBlank() && startPostCode.isNotBlank() && startAddressLine.isNotBlank() &&
                    destPostCode.isNotBlank() && destAddressLine.isNotBlank()
                ) {
                    isAdding = true
                    viewModel.addActivity(
                        activityName,
                        startAddressLine,
                        destAddressLine,
                        description,
                        convertToISO8601(fromDate, fromTime),
                        convertToISO8601(toDate, toTime),
                    )
                    Toast.makeText(context, "Adding Activity...", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                }
            },
            enabled = !isAdding,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Activity")
        }
    }
}

@Composable
fun DateTimePicker(
    label: String,
    value: String,
    isTimePicker: Boolean = false,
    onValueChange: (String) -> Unit
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    val datePicker = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth -> onValueChange("$year-${month + 1}-$dayOfMonth") },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    val timePicker = TimePickerDialog(
        context,
        { _, hour, minute -> onValueChange("$hour:$minute") },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        false
    )

    OutlinedButton(onClick = { if (isTimePicker) timePicker.show() else datePicker.show() }) {
        Text(if (value.isEmpty()) label else "$label: $value")
    }
}

fun isValidDateTime(fromDate: String, fromTime: String, toDate: String, toTime: String): Boolean {
    if (fromDate.isEmpty() || fromTime.isEmpty() || toDate.isEmpty() || toTime.isEmpty()) return false

    val format = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    val from = format.parse("$fromDate $fromTime") ?: return false
    val to = format.parse("$toDate $toTime") ?: return false

    return from.before(to) && from.after(Date()) // Ensure future time
}

fun convertToISO8601(date: String, time: String): String {
    val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    val outputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())
    val parsedDate = inputFormat.parse("$date $time") ?: return ""
    return outputFormat.format(parsedDate)
}