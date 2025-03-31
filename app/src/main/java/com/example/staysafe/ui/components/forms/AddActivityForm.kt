package com.example.staysafe.ui.components.forms

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
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
    var fromName by remember { mutableStateOf("") }
    var fromPostCode by remember { mutableStateOf("") }
    var fromAddressLine by remember { mutableStateOf("") }
    var toName by remember { mutableStateOf("") }
    var toPostCode by remember { mutableStateOf("") }
    var toAddressLine by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    val travelMode by remember { mutableStateOf("DRIVE") }

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

    LaunchedEffect(fromPostCode, fromAddressLine, toPostCode, toAddressLine) {
        if (fromPostCode.isNotBlank() && fromAddressLine.isNotBlank() && toPostCode.isNotBlank() && toAddressLine.isNotBlank()) {
            viewModel.getRouteForLocation(
                fromPostCode,
                fromAddressLine,
                toPostCode,
                toAddressLine,
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
            label = { Text("Activity Name", color = Color.White) },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            colors = customTextFieldColors()
        )

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description", color = Color.White) },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            colors = customTextFieldColors()
        )

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 12.dp),
            color = Color(0xFF333333)
        )

        Text("Origin Location", color = Color.White, style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(
            value = fromName,
            onValueChange = { fromName = it },
            label = { Text("Starting Name", color = Color.White) },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            colors = customTextFieldColors()
        )
        OutlinedTextField(
            value = fromAddressLine,
            onValueChange = { fromAddressLine = it },
            label = { Text("Starting Address", color = Color.White) },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            colors = customTextFieldColors()
        )
        OutlinedTextField(
            value = fromPostCode,
            onValueChange = { fromPostCode = it },
            label = { Text("Starting Postcode", color = Color.White) },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            colors = customTextFieldColors()
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            DateTimePicker("From Date", fromDate) { fromDate = it }
            Spacer(modifier = Modifier.width(8.dp))
            DateTimePicker("From Time", fromTime, isTimePicker = true) { fromTime = it }
        }

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 12.dp),
            color = Color(0xFF333333)
        )

        Text("Destination Location", color = Color.White, style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(
            value = toName,
            onValueChange = { toName = it },
            label = { Text("Destination Name", color = Color.White) },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            colors = customTextFieldColors()
        )
        OutlinedTextField(
            value = toAddressLine,
            onValueChange = { toAddressLine = it },
            label = { Text("Destination Address", color = Color.White) },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            colors = customTextFieldColors()
        )
        OutlinedTextField(
            value = toPostCode,
            onValueChange = { toPostCode = it },
            label = { Text("Destination Postcode", color = Color.White) },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            colors = customTextFieldColors()
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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
                if (activityName.isNotBlank() && fromPostCode.isNotBlank() && fromAddressLine.isNotBlank() &&
                    toPostCode.isNotBlank() && toAddressLine.isNotBlank()
                ) {
                    isAdding = true
                    viewModel.addActivity(
                        name = activityName,
                        fromActivityName = fromName,
                        toActivityName = toName,
                        startAddressLine = fromAddressLine,
                        destAddressLine = toAddressLine,
                        description = description,
                        fromISOTime = convertToISO8601(fromDate, fromTime),
                        toisoTime = convertToISO8601(toDate, toTime),
                        fromPostcode = fromPostCode,
                        toPostcode = toPostCode
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
fun customTextFieldColors() = TextFieldDefaults.colors(
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedLabelColor = Color.White,
    unfocusedLabelColor = Color.Gray,
    unfocusedContainerColor = Color(0xFF1E1E1E),
    focusedContainerColor = Color(0xFF1E1E1E),
)

@Composable
fun DateTimePicker(label: String, value: String, isTimePicker: Boolean = false, onValueChange: (String) -> Unit) {
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

    OutlinedButton(
        onClick = { if (isTimePicker) timePicker.show() else datePicker.show() },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(value.ifEmpty { label })
    }
}

fun isValidDateTime(fromDate: String, fromTime: String, toDate: String, toTime: String): Boolean {
    if (fromDate.isEmpty() || fromTime.isEmpty() || toDate.isEmpty() || toTime.isEmpty()) return false

    val format = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    val from = format.parse("$fromDate $fromTime") ?: return false
    val to = format.parse("$toDate $toTime") ?: return false

    return from.before(to) && from.after(Date())
}

fun convertToISO8601(date: String, time: String): String {
    val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    val outputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())
    val parsedDate = inputFormat.parse("$date $time") ?: return ""
    return outputFormat.format(parsedDate)
}
