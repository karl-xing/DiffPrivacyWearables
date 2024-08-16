@file:Suppress("DEPRECATION")

package com.example.diffprivacywearables.presentation

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.wear.compose.material.*
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import com.example.diffprivacywearables.data.DataProcessing
import com.example.diffprivacywearables.Evaluation
import com.example.diffprivacywearables.data.HeartRateDataPoint
import com.example.diffprivacywearables.data.HeartRateManager
import com.example.diffprivacywearables.data.FitnessDataManager
import com.example.diffprivacywearables.data.DataPoint
import com.example.diffprivacywearables.presentation.theme.DiffPrivacyWearablesTheme
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.data.DataType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {
    private val TAG = "GoogleFit"
    private lateinit var heartRateManager: HeartRateManager
    private lateinit var fitnessDataManager: FitnessDataManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        heartRateManager = HeartRateManager(this)
        fitnessDataManager = FitnessDataManager(this)
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

    @Composable
    fun MainScreen() {
        var selectedAlgorithm by remember { mutableStateOf("Laplace") }
        val dataTypes = listOf("Heart Rate", "Step Count", "Acceleration")
        val evaluationMetrics =
            listOf("Computation Time", "Power Consumption", "Memory Usage", "CPU Usage")
        val selectedDataTypes = remember { mutableStateListOf<String>() }
        val selectedMetrics = remember { mutableStateListOf<String>() }

        ScalingLazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        val account = GoogleSignIn.getLastSignedInAccount(this@MainActivity)
                        if (account != null) {
                            lifecycleScope.launch {
                                heartRateManager.getHeartRateData(1, 20, false) { heartRateData ->
                                    Log.d(TAG, "1 Day HR: $heartRateData")
                                    runOnUiThread {
                                        Toast.makeText(
                                            this@MainActivity,
                                            "Heart Rate Data: $heartRateData",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }
                            }
                        } else {
                            Log.e(TAG, "No Google account signed in")
                            Toast.makeText(
                                this@MainActivity,
                                "No Google account signed in",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Fetch 1Day HR")
                }
            }
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        val account = GoogleSignIn.getLastSignedInAccount(this@MainActivity)
                        if (account != null) {
                            lifecycleScope.launch {
                                heartRateManager.getHeartRateData(30, 20, false) { heartRateData ->
                                    Log.d(TAG, "30 Day HR: $heartRateData")
                                    runOnUiThread {
                                        Toast.makeText(
                                            this@MainActivity,
                                            "Heart Rate Data: $heartRateData",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }
                            }
                        } else {
                            Log.e(TAG, "No Google account signed in")
                            Toast.makeText(
                                this@MainActivity,
                                "No Google account signed in",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Fetch Historical HR")
                }
            }
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        CoroutineScope(Dispatchers.IO).launch {
                            val accelerationDataPoints = fitnessDataManager.getFitnessData(DataType.TYPE_SPEED)
                            // Save acceleration data to JSON file
                            fitnessDataManager.saveFitnessData(accelerationDataPoints)
                            fitnessDataManager.exportFitnessDataToExternalStorage(accelerationDataPoints)
                        }
                        Toast.makeText(this@MainActivity, "Acceleration Data exported!", Toast.LENGTH_SHORT)
                            .show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Export Acceleration Data")
                }
            }
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        CoroutineScope(Dispatchers.IO).launch {
                            val heartRateDataPoints = heartRateManager.getHeartRateData()
                            // Save heart rate data to JSON file
                            heartRateManager.saveHeartRateData(heartRateDataPoints)
                            heartRateManager.exportHeartRateDataToExternalStorage(
                                heartRateDataPoints
                            )
                        }
                        Toast.makeText(this@MainActivity, "Data exported!", Toast.LENGTH_SHORT)
                            .show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Export Data")
                }
            }
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
//            item {
//                Chip(
//                    onClick = { selectedAlgorithm = "Exponential" },
//                    label = { Text("Exponential Mechanism") },
//                    colors = ChipDefaults.primaryChipColors()
//                )
//            }
            item {
                Chip(
                    onClick = { selectedAlgorithm = "k-Anonymity" },
                    label = { Text("Personalized k-Anonymity") },
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
                        Switch(
                            checked = selectedDataTypes.contains(dataType),
                            onCheckedChange = null
                        )
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
                        val k = 3 // Example k value for k-Anonymity

                        val algorithm: (List<HeartRateDataPoint>, Double) -> List<HeartRateDataPoint> =
                            when (selectedAlgorithm) {
                                "Laplace" -> DataProcessing::applyLaplaceMechanism
                                "Exponential" -> DataProcessing::applyExponentialMechanism
                                "k-Anonymity" -> { data, _ ->
                                    val dataPoints =
                                        data.map { DataPoint(listOf(it.heartRate)) }
                                    val anonymizedData =
                                        DataProcessing.personalizedKAnonymity(dataPoints, k)
                                    anonymizedData.map {
                                        HeartRateDataPoint(
                                            it.attributes[0].toLong(),
                                            it.attributes[0]
                                        )
                                    }
                                }

                                else -> DataProcessing::applyLaplaceMechanism
                            }

                        // Fetch data using HeartRateManager
                        CoroutineScope(Dispatchers.IO).launch {
                            val heartRateDataPoints = heartRateManager.getHeartRateData()
                            val results = Evaluation.evaluateAlgorithm(
                                algorithm,
                                heartRateDataPoints,
                                epsilon
                            )
                            Log.d(TAG, "Evaluation Results: $results")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Evaluate")
                }
            }
        }
    }
}