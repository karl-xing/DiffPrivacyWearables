package com.example.diffprivacywearables.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
//import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.*
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import com.example.diffprivacywearables.DataProcessing
import com.example.diffprivacywearables.Evaluation
import com.example.diffprivacywearables.presentation.theme.DiffPrivacyWearablesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DiffPrivacyWearablesTheme {
                Scaffold(
                    timeText = {
                        TimeText()
                    },
                    vignette = {
                        Vignette(vignettePosition = VignettePosition.TopAndBottom)
                    }
                ) {
                    MainScreen()
                }
            }
        }
    }
}

@Composable
fun MainScreen() {
    var selectedAlgorithm by remember { mutableStateOf("Laplace") }
    val dataTypes = listOf("Heart Rate", "Step Count", "Movement Acceleration")
    val evaluationMetrics = listOf("Computation Time", "Power Consumption", "Memory Usage", "CPU Usage")
    val selectedDataTypes = remember { mutableStateListOf<String>() }
    val selectedMetrics = remember { mutableStateListOf<String>() }

    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text("Select Algorithm")
        }
        item {
            Chip(
                onClick = { selectedAlgorithm = "Laplace" },
                label = { Text("Laplace Mechanism") },
                colors = ChipDefaults.primaryChipColors()
            )
        }
        item {
            Chip(
                onClick = { selectedAlgorithm = "Exponential" },
                label = { Text("Exponential Mechanism") },
                colors = ChipDefaults.primaryChipColors()
            )
        }
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Select Data Types")
        }
        items(dataTypes.size) { index ->
            val dataType = dataTypes[index]
            ToggleChip(
                checked = selectedDataTypes.contains(dataType),
                onCheckedChange = { checked ->
                    if (checked) {
                        selectedDataTypes.add(dataType)
                    } else {
                        selectedDataTypes.remove(dataType)
                    }
                },
                label = { Text(dataType) },
                toggleControl = {
                    Switch(checked = selectedDataTypes.contains(dataType), onCheckedChange = null)
                }
            )
        }
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Select Evaluation Metrics")
        }
        items(evaluationMetrics.size) { index ->
            val metric = evaluationMetrics[index]
            ToggleChip(
                checked = selectedMetrics.contains(metric),
                onCheckedChange = { checked ->
                    if (checked) {
                        selectedMetrics.add(metric)
                    } else {
                        selectedMetrics.remove(metric)
                    }
                },
                label = { Text(metric) },
                toggleControl = {
                    Switch(checked = selectedMetrics.contains(metric), onCheckedChange = null)
                }
            )
        }
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    val epsilon = 1.0 // Example epsilon value

                    val algorithm: (Double, Double) -> Double = if (selectedAlgorithm == "Laplace") {
                        DataProcessing::applyLaplaceMechanism
                    } else {
                        DataProcessing::applyExponentialMechanism
                    }

                    // Implement the logic to fetch data from sensors here

                    val heartRateValue = 75.0 // Placeholder for actual sensor data
                    val stepCountValue = 1000.0 // Placeholder for actual sensor data
                    val movementAccelerationValue = 1.5 // Placeholder for actual sensor data

                    val results = Evaluation.evaluateAlgorithm(algorithm, heartRateValue, epsilon)
                    println("Evaluation Results: $results")

                    // Implement logic to process the selected data types and metrics
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Process and Evaluate")
            }
        }
    }
}