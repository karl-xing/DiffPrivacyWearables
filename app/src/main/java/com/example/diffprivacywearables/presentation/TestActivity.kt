package com.example.diffprivacywearables.presentation

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.example.diffprivacywearables.data.FitnessDataManager
import com.example.diffprivacywearables.Evaluation
import com.example.diffprivacywearables.R
import com.example.diffprivacywearables.data.PrivacyPreserving
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TestActivity : ComponentActivity() {

    private lateinit var fitnessDataManager: FitnessDataManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_test) // Set your layout if you need one
        fitnessDataManager = FitnessDataManager(this)

        lifecycleScope.launch(Dispatchers.IO) {
            testPrivacyPreservingAlgorithms()
            finish() // Finish the activity after task completion
        }
    }

    private fun testPrivacyPreservingAlgorithms() {
        // Load fitness data from JSON
//        val fitnessRequestId = R.raw.fitness_data2    // 450 records Heart Rate
        val fitnessRequestId = R.raw.fitness_data6      // 1200 records Heart Rate
//        val fitnessRequestId = R.raw.fitness_data11   // 5751 records ACC

        val fitnessDataPoints = fitnessDataManager.loadFitnessDataFromJson(this, fitnessRequestId)

        if (fitnessDataPoints != null) {
            val kValues = listOf(2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0)
            val epsilonValues = listOf(0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9)

            // Test LDP with different epsilon values
            val ldpResults = Evaluation.evaluateAlgorithmWithParameters(
                algorithm = { data, epsilon ->
                    PrivacyPreserving.localDifferentialPrivacy(data, epsilon)
                },
                data = fitnessDataPoints,
                parameterValues = epsilonValues
            )

            // Log the LDP results
            Log.d("TestActivity", "LDP Results:")
            ldpResults.forEach { (epsilon, results) ->
                results.forEach { result ->
                    Log.d("TestActivity", "epsilon = $epsilon, Computation Time: ${result.computationTime}, Memory Usage: ${result.memoryUsage}")
                }
            }

            val kAnonymityResults = Evaluation.evaluateAlgorithmWithParameters(
                algorithm = { data, k ->
                    PrivacyPreserving.applyKAnonymity(data, k.toInt())
                },
                data = fitnessDataPoints,
                parameterValues = kValues
            )

            // Log the k-Anonymity results
            Log.d("TestActivity", "k-Anonymity Results:")
            kAnonymityResults.forEach { (k, results) ->
                results.forEach { result ->
                    Log.d("TestActivity", "k = $k, Computation Time: ${result.computationTime}, Memory Usage: ${result.memoryUsage}")
                }
            }
        } else {
            Log.e("TestActivity", "Failed to load fitness data from JSON")
        }
    }
}
