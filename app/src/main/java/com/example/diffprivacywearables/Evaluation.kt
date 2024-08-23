package com.example.diffprivacywearables

import com.example.diffprivacywearables.data.FitnessDataPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.system.measureTimeMillis
//import kotlin.system.measureNanoTime

object Evaluation {

    data class EvaluationResults(
        val parameterValue: Double,
        val computationTime: Long,
        val memoryUsage: Double,
//        val cpuUsage: Double,
//        val powerConsumption: Double
    )

    fun evaluateAlgorithmWithParameters(
        algorithm: (List<FitnessDataPoint>, Double) -> List<FitnessDataPoint>,
        data: List<FitnessDataPoint>,
        parameterValues: List<Double> // List of k values or epsilon values
    ): Map<Double, List<EvaluationResults>> {
        val runtime = Runtime.getRuntime()
        val results = mutableMapOf<Double, MutableList<EvaluationResults>>()

        for (paramValue in parameterValues) {
            val paramResults = mutableListOf<EvaluationResults>()

            repeat(10) { // Run the evaluation 10 times for each parameter value
                System.gc() // Trigger garbage collection to reduce interference

                // Initial memory usage
                val initialMemory = runtime.totalMemory() - runtime.freeMemory()

                // Measure computation time
                val computationTime = measureTimeMillis {
                    algorithm(data, paramValue)
                }

                // Final memory usage
                val finalMemory = runtime.totalMemory() - runtime.freeMemory()

                // Memory usage in KB
                val memoryUsage = (finalMemory - initialMemory) / 1024.0

                // Store the result for this run
                paramResults.add(
                    EvaluationResults(
                        parameterValue = paramValue,
                        computationTime = computationTime,
                        memoryUsage = memoryUsage
                    )
                )

                runBlocking {
                    delay(100)
                }
            }

            // Store the results for this parameter value
            results[paramValue] = paramResults
        }
        return results
    }

    fun evaluateAlgorithm(
        algorithm: (List<FitnessDataPoint>, Double) -> List<FitnessDataPoint>,
        data: List<FitnessDataPoint>,
        epsilon: Double
    ): List<EvaluationResults> {
        val runtime = Runtime.getRuntime()
        val results = mutableListOf<EvaluationResults>()

        repeat(10) { // Run the evaluation 10 times
            System.gc() // Trigger garbage collection to reduce interference

            // Initial memory usage
            val initialMemory = runtime.totalMemory() - runtime.freeMemory()

            // Measure computation time
            val computationTime = measureTimeMillis {
                algorithm(data, epsilon)
            }

            // Final memory usage
            val finalMemory = runtime.totalMemory() - runtime.freeMemory()

            // Calculate memory usage in KB
            val memoryUsage = (finalMemory - initialMemory) / 1024.0

            // Store the result
            results.add(
                EvaluationResults(
                    parameterValue = epsilon,
                    computationTime = computationTime,
                    memoryUsage = memoryUsage
                )
            )
            // Short sleep to stabilize the system
            runBlocking {
                delay(100)
            }
        }
        return results
    }
}
