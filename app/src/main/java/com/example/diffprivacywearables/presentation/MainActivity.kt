package com.example.diffprivacywearables.presentation

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.wear.compose.material.*
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import com.example.diffprivacywearables.data.HeartRateDataPoint
import com.example.diffprivacywearables.data.HeartRateManager
import com.example.diffprivacywearables.DataProcessing
import com.example.diffprivacywearables.Evaluation
import com.example.diffprivacywearables.SignInGoogle
import com.example.diffprivacywearables.presentation.theme.DiffPrivacyWearablesTheme
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var heartRateManager: HeartRateManager
    private lateinit var signInGoogle: SignInGoogle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        heartRateManager = HeartRateManager(this)

        setContent {
            val coroutineScope = rememberCoroutineScope()
            var googleIdTokenCredential by remember { mutableStateOf<GoogleIdTokenCredential?>(null) }

            signInGoogle = SignInGoogle(
                context = this,
                coroutineScope = coroutineScope,
                onGoogleIdTokenCredentialUpdated = { credential -> googleIdTokenCredential = credential }
            )

            DiffPrivacyWearablesTheme {
                Scaffold(
                    timeText = {
                        TimeText()
                    },
                    vignette = {
                        Vignette(vignettePosition = VignettePosition.TopAndBottom)
                    }
                ) {
                    MainScreen(heartRateManager, signInGoogle)
                }
            }
        }
    }

    @Composable
    fun MainScreen(heartRateManager: HeartRateManager, signInGoogle: SignInGoogle) {
        var heartRateData by remember { mutableStateOf<List<HeartRateDataPoint>>(emptyList()) }
        var fetchSuccess by remember { mutableStateOf(false) }
        var selectedAlgorithm by remember { mutableStateOf("Laplace") }
        val dataTypes = listOf("Heart Rate", "Step Count", "Acceleration")
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
                Column {
                    Text("Reading heart rate(1 hour ago)")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Fetch Success: $fetchSuccess")
                }
            }
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        lifecycleScope.launch {
                            heartRateData = heartRateManager.getHeartRateHistoryData()
                            fetchSuccess = heartRateData.isNotEmpty()
                            heartRateData.forEach {
                                Log.d("HeartRateData", "Timestamp: ${it.timestamp}, Heart Rate: ${it.heartRate} bpm")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Collect Data")
                }
            }
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        signInGoogle.signUpWithGoogle()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Google Sign-In")
                }
            }
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        val epsilon = 1.0 // Example epsilon value

                        val algorithm: (List<HeartRateDataPoint>, Double) -> List<HeartRateDataPoint> = if (selectedAlgorithm == "Laplace") {
                            DataProcessing::applyLaplaceMechanism
                        } else {
                            DataProcessing::applyExponentialMechanism
                        }

                        // Apply the selected differential privacy algorithm to heart rate data
                        val processedData = algorithm(heartRateData, epsilon)

                        // Evaluate the selected algorithm
                        val results = Evaluation.evaluateAlgorithm(algorithm, heartRateData, epsilon)
                        println("Evaluation Results: $results")

                        // Implement logic to process the selected data types and metrics
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Evaluate")
                }
            }
        }
    }
}
