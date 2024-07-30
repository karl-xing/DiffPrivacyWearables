@file:Suppress("DEPRECATION")

package com.example.diffprivacywearables.presentation

//import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.*
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import com.example.diffprivacywearables.DataProcessing
import com.example.diffprivacywearables.Evaluation
import com.example.diffprivacywearables.data.HeartRateDataPoint
import com.example.diffprivacywearables.data.HeartRateManager
import com.example.diffprivacywearables.presentation.theme.DiffPrivacyWearablesTheme
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.data.Field
import com.google.android.gms.fitness.request.DataReadRequest
import com.google.android.gms.fitness.result.DataReadResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    private val TAG = "GoogleFit"
    private lateinit var heartRateManager: HeartRateManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        heartRateManager = HeartRateManager(this)
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
        val evaluationMetrics = listOf("Computation Time", "Power Consumption", "Memory Usage", "CPU Usage")
        val selectedDataTypes = remember { mutableStateListOf<String>() }
        val selectedMetrics = remember { mutableStateListOf<String>() }
        val heartRateData by remember { mutableStateOf("No data") }

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

                        val algorithm: (List<HeartRateDataPoint>, Double) -> List<HeartRateDataPoint> = if (selectedAlgorithm == "Laplace") {
                            DataProcessing::applyLaplaceMechanism
                        } else {
                            DataProcessing::applyExponentialMechanism
                        }

                        // Fetch data using HeartRateManager
                        CoroutineScope(Dispatchers.IO).launch {
                            val heartRateDataPoints = heartRateManager.getHeartRateHistoryData()
                            val results = Evaluation.evaluateAlgorithm(algorithm, heartRateDataPoints, epsilon)
                            Log.d(TAG, "Evaluation Results: $results")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Evaluate")
                }
            }
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        val account = GoogleSignIn.getLastSignedInAccount(this@MainActivity)
                        if (account != null) {
                            accessGoogleFitData(account)
                        } else {
                            Log.e(TAG, "No Google account signed in")
                            Toast.makeText(this@MainActivity, "No Google account signed in", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Fetch Heart Rate Data")
                }
            }
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        val account = GoogleSignIn.getLastSignedInAccount(this@MainActivity)
                        if (account != null) {
                            accessHistoricalHeartRateData(account)
                        } else {
                            Log.e(TAG, "No Google account signed in")
                            Toast.makeText(this@MainActivity, "No Google account signed in", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Fetch Historical Heart Rate Data")
                }
            }
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = heartRateData)
            }
        }
    }

    private fun accessGoogleFitData(googleSignInAccount: GoogleSignInAccount) {
        val endTime = System.currentTimeMillis()
        val startTime = endTime - TimeUnit.HOURS.toMillis(1)

        val readRequest = DataReadRequest.Builder()
            .read(DataType.TYPE_HEART_RATE_BPM)
            .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
            .build()

        Fitness.getHistoryClient(this, googleSignInAccount)
            .readData(readRequest)
            .addOnSuccessListener { dataReadResponse ->
                val heartRateData = parseHeartRateData(dataReadResponse)
                Log.d(TAG, "Heart Rate Data: $heartRateData")
                runOnUiThread {
                    Toast.makeText(this, "Heart Rate Data: $heartRateData", Toast.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to read heart rate data", e)
                runOnUiThread {
                    Toast.makeText(this, "Failed to read heart rate data", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun accessHistoricalHeartRateData(googleSignInAccount: GoogleSignInAccount) {
        val endTime = System.currentTimeMillis()
        val startTime = endTime - TimeUnit.HOURS.toMillis(24)

        val readRequest = DataReadRequest.Builder()
            .read(DataType.TYPE_HEART_RATE_BPM)
            .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
            .build()

        Fitness.getHistoryClient(this, googleSignInAccount)
            .readData(readRequest)
            .addOnSuccessListener { dataReadResponse ->
                val heartRateData = parseHeartRateData(dataReadResponse)
                Log.d(TAG, "Historical Heart Rate Data: $heartRateData")
                runOnUiThread {
                    Toast.makeText(this, "Historical Heart Rate Data: $heartRateData", Toast.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to read historical heart rate data", e)
                runOnUiThread {
                    Toast.makeText(this, "Failed to read historical heart rate data", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun parseHeartRateData(dataReadResponse: DataReadResponse): String {
        val heartRateDataSet = dataReadResponse.getDataSet(DataType.TYPE_HEART_RATE_BPM)
        val dataPoints = heartRateDataSet.dataPoints

        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        return if (dataPoints.isNotEmpty()) {
            val sb = StringBuilder()
            for (dp in dataPoints) {
                val heartRate = dp.getValue(Field.FIELD_BPM).asFloat()
                val timestamp = dp.getTimestamp(TimeUnit.MILLISECONDS)
                val date = sdf.format(Date(timestamp))
                sb.append("Heart Rate: $heartRate BPM at $date\n")
            }
            sb.toString()
        } else {
            "No heart rate data available"
        }
    }
}
