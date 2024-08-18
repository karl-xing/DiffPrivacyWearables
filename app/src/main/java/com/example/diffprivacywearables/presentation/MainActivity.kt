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
import com.example.diffprivacywearables.R
import com.example.diffprivacywearables.data.FitnessDataManager
import com.example.diffprivacywearables.data.DataPoint
import com.example.diffprivacywearables.data.FitnessDataPoint
import com.example.diffprivacywearables.presentation.theme.DiffPrivacyWearablesTheme
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.data.DataType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {
    private val TAG = "GoogleFit"
    private val fitnessRequestId = R.raw.fitness_data2
    private lateinit var fitnessDataManager: FitnessDataManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        var selectedAlgorithm by remember { mutableStateOf("k-Anonymity") }
        val dataTypes = listOf("Heart Rate", "Step Count")
            // "Heart Rate", "Step Count", "Acceleration"
        val evaluationMetrics =
            listOf("Computation Time", "Memory Usage")
            // "Computation Time", "Power Consumption", "Memory Usage", "CPU Usage"
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
                        // Load fitness data from JSON
                        val fitnessDataPoints = fitnessDataManager.loadFitnessDataFromJson(this@MainActivity, fitnessRequestId)

                        if (fitnessDataPoints != null) {
                            val epsilon = 0.9 // [0.5, 1.0]
                            val k = 5         // [1, 5]

                            val algorithm: (List<FitnessDataPoint>, Double) -> List<FitnessDataPoint> =
                                when (selectedAlgorithm) {
                                    "LDP" -> { data, epsilonValue ->
                                        DataProcessing.applyLocalDifferentialPrivacy(data, epsilonValue)
                                    }
                                    "k-Anonymity" -> { data, _ ->
                                        // Step 1: Convert to DataPoint
                                        val dataPoints = data.map { DataPoint(listOf(it.value)) }
//                                        Log.d("MainActivity", "Step 1 - Original DataPoints: $dataPoints")

                                        // Step 2: Apply personalized K-Anonymity
                                        val anonymizedDataPoints = DataProcessing.personalizedKAnonymity(dataPoints, k)

                                        anonymizedDataPoints.mapIndexed { index, dataPoint ->
                                            FitnessDataPoint(
                                                timestamp = data[index].timestamp, // Preserve the original timestamp
                                                value = dataPoint.attributes[0],   // The anonymized value
                                                dataType = data[index].dataType    // Use the original dataType
                                            )
                                        }
                                    }
                                    else -> DataProcessing::applyMechanismError
                                }

                            CoroutineScope(Dispatchers.IO).launch {
                                val results = Evaluation.evaluateAlgorithm(
                                    algorithm,
                                    fitnessDataPoints,
                                    epsilon
                                )
                                Log.d("MainActivity", "Evaluation Results: $results")
                            }
                        } else {
                            Log.e("MainActivity", "Failed to load fitness data from JSON")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Load & Evaluate")
                }
            }
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        val account = GoogleSignIn.getLastSignedInAccount(this@MainActivity)
                        if (account != null) {
                            lifecycleScope.launch {
                                fitnessDataManager.getFitnessData(DataType.TYPE_HEART_RATE_BPM,
                                    1, 20, false) { fitnessData ->
                                    Log.d(TAG, "Fitness Data: $fitnessData")
                                    runOnUiThread {
                                        Toast.makeText(
                                            this@MainActivity,
                                            "Fitness Data: $fitnessData",
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
                    Text(text = "Fetch 1 Day HR")
                }
            }
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        val account = GoogleSignIn.getLastSignedInAccount(this@MainActivity)
                        if (account != null) {
                            lifecycleScope.launch {
                                fitnessDataManager.getFitnessData(DataType.TYPE_HEART_RATE_BPM,
                                    30, 20, false) { fitnessData ->
                                    Log.d(TAG, "30 Day HR: $fitnessData")
                                    runOnUiThread {
                                        Toast.makeText(
                                            this@MainActivity,
                                            "Heart Rate Data: $fitnessData",
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
                    Text(text = "Fetch 30 Days HR")
                }
            }
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        CoroutineScope(Dispatchers.IO).launch {
                            val fitnessRateDataPoints = fitnessDataManager.getFitnessData(DataType.TYPE_HEART_RATE_BPM)
                            fitnessDataManager.exportFitnessDataToExternalStorage(fitnessRateDataPoints)
                        }
                        Toast.makeText(this@MainActivity, "Heart Rate Data exported!", Toast.LENGTH_SHORT)
                            .show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Export HR Data")
                }
            }
            item {
                Text("Select Algorithm")
            }
            item {
                Chip(
                    onClick = { selectedAlgorithm = "LDP" },
                    label = { Text("LDP Mechanism") },
                    colors = ChipDefaults.primaryChipColors()
                )
            }
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
                        val epsilon = 0.8 // Example epsilon value
                        val k = 5 // Example k value for k-Anonymity

                        val algorithm: (List<FitnessDataPoint>, Double) -> List<FitnessDataPoint> =
                            when (selectedAlgorithm) {
                                "LDP" -> DataProcessing::applyLocalDifferentialPrivacy
                                "k-Anonymity" -> { data, _ ->
                                    val dataPoints =
                                        data.map { DataPoint(listOf(it.value)) }
                                    val anonymizedData =
                                        DataProcessing.personalizedKAnonymity(dataPoints, k)
                                    // Test k-anonymity, but below printing has only one list of values, like DataPoint(attributes=[0.13125])
                                    Log.d("MainActivity", "Anonymized Data:")
                                    anonymizedData.forEach { Log.d("MainActivity", it.toString()) }
                                    anonymizedData.map {
                                        FitnessDataPoint(
                                            it.attributes[0].toLong(),
                                            it.attributes[0],
                                            dataType = "HeartRate" // or the appropriate data type
                                        )
                                    }
                                }
                                else -> DataProcessing::applyMechanismError
                            }

                        // Fetch data using FitnessDataManager
                        CoroutineScope(Dispatchers.IO).launch {
                            val fitnessDataPoints = fitnessDataManager.getFitnessData(DataType.TYPE_HEART_RATE_BPM)
                            val results = Evaluation.evaluateAlgorithm(
                                algorithm,
                                fitnessDataPoints,
                                epsilon
                            )
                            Log.d(TAG, "Evaluation Results: $results")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Read & Evaluate")
                }
            }
        }
    }
}