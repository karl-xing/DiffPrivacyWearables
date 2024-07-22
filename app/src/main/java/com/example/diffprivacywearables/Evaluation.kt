package com.example.diffprivacywearables

import kotlin.system.measureNanoTime

object Evaluation {
    fun evaluateAlgorithm(algorithm: (Double, Double) -> Double, value: Double, epsilon: Double): Map<String, Any> {
        val computationTime = measureNanoTime {
            algorithm(value, epsilon)
        }

        // Placeholder for other metrics, these would require actual device-level measurements
        val powerConsumption = 0.0
        val memoryUsage = 0.0
        val cpuUsage = 0.0

        return mapOf(
            "computationTime" to computationTime,
            "powerConsumption" to powerConsumption,
            "memoryUsage" to memoryUsage,
            "cpuUsage" to cpuUsage
        )
    }
}
