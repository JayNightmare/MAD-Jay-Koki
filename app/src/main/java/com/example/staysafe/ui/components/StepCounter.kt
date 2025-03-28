package com.example.staysafe.ui.components

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.staysafe.viewModel.MapViewModel

@Composable
fun StepCounterScreen(viewModel: MapViewModel = viewModel()) {
    val context = LocalContext.current

    // trigger for alert box
    var showAlert by remember { mutableStateOf(false) }
    val stepCount by viewModel.stepCount.collectAsState()

    DisposableEffect(Unit) {
        viewModel.startStepCounting(context)
        viewModel.startAccelerometerCounting(context)

        onDispose {
            viewModel.clearUpdateResult()
        }
    }

    Surface(
        modifier = Modifier.height(55.dp),
        color = MaterialTheme.colorScheme.background,
        shape = MaterialTheme.shapes.small,
    ) {
        Row(
            modifier = Modifier.width(200.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Button(
                onClick = { showAlert = true },
                modifier = Modifier
                    .padding(8.dp)
                    .width(200.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Current steps: $stepCount Steps",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                )
            }
//            Spacer(modifier = Modifier.height(16.dp))
//            Spacer(modifier = Modifier.height(32.dp))
//
//            Button(onClick = { viewModel.resetStepCount() }) {
//                Text("Reset")
//            }
        }
    }

    if (showAlert) {
        AlertDialog(
            onDismissRequest = { showAlert = false },
            title = { Text("Step Count Alert") },
            text = { Text("Would you like to reset your steps?") },
            confirmButton = {
                Button(onClick = {
                    viewModel.resetStepCount()
                    showAlert = false
                }) {
                    Text("Reset")
                }
            }
        )
    }
}
