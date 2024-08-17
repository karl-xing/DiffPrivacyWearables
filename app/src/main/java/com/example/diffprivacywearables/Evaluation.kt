package com.example.diffprivacywearables

import com.example.diffprivacywearables.data.FitnessDataPoint
import kotlin.system.measureTimeMillis

object Evaluation {

    data class EvaluationResults(
        val computationTime: Long,
        val memoryUsage: Double, // Placeholder for actual memory usage calculation
//        val cpuUsage: Double // Placeholder for actual CPU usage calculation
//        val powerConsumption: Double, // Placeholder for actual power consumption calculation
    )

    fun evaluateAlgorithm(
        algorithm: (List<FitnessDataPoint>, Double) -> List<FitnessDataPoint>,
        data: List<FitnessDataPoint>,
        epsilon: Double
    ): EvaluationResults {
        val computationTime = measureTimeMillis {
            algorithm(data, epsilon)
        }

        val memoryUsage = measureMemoryUsage()
//        val cpuUsage = measureCPUUsage()
//        val powerConsumption = measurePowerConsumption()

        return EvaluationResults(
            computationTime = computationTime,
            memoryUsage = memoryUsage,
//            cpuUsage = cpuUsage,
//            powerConsumption = powerConsumption
        )
    }

    private fun measurePowerConsumption(): Double {
        // Placeholder for actual power consumption calculation
        return Math.random() * 10 // Random value for demonstration purposes
    }

    private fun measureMemoryUsage(): Double {
        // Placeholder for actual memory usage calculation
        return Math.random() * 100 // Random value for demonstration purposes
    }

    private fun measureCPUUsage(): Double {
        // Placeholder for actual CPU usage calculation
        return Math.random() * 50 // Random value for demonstration purposes
    }
}
