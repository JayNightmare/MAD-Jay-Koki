package com.example.staysafe.ui.components

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.staysafe.viewModel.MapViewModel

@Composable
fun StepCounterScreen(viewModel: MapViewModel = viewModel()) {
    val context = LocalContext.current

    val stepCount by viewModel.stepCount.collectAsState()

    DisposableEffect(Unit) {
        viewModel.startStepCounting(context)
        viewModel.startAccelerometerCounting(context)

        onDispose {
            viewModel.clearUpdateResult()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Current steps",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "$stepCount Steps",
                style = MaterialTheme.typography.displayLarge
            )
            Spacer(modifier = Modifier.height(32.dp))

            Button(onClick = { viewModel.resetStepCount() }) {
                Text("Reset")
            }
        }
    }
}
