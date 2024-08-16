package com.example.diffprivacywearables.data

import kotlin.math.ln
import kotlin.math.abs

data class DataPoint(val attributes: List<Double>)

object DataProcessing {

    fun personalizedKAnonymity(data: List<DataPoint>, k: Int): List<DataPoint> {
        // Step 1: Normalize attributes
        val normalizedData = normalizeData(data)

        // Step 2: Calculate entropy and assign weights
        val entropies = calculateEntropies(normalizedData)
        val weights = assignWeights(entropies)

        // Step 3: Compute distance matrix
        val distanceMatrix = computeDistanceMatrix(normalizedData, weights)

        // Step 4: Apply V-MDAV for k-anonymity grouping
        val anonymizedData = applyVMdav(distanceMatrix, normalizedData, k)

        return anonymizedData
    }

    private fun normalizeData(data: List<DataPoint>): List<DataPoint> {
        val numAttributes = data.first().attributes.size
        val minValues = DoubleArray(numAttributes) { Double.MAX_VALUE }
        val maxValues = DoubleArray(numAttributes) { Double.MIN_VALUE }

        // Find min and max for each attribute
        for (point in data) {
            for (i in point.attributes.indices) {
                minValues[i] = minOf(minValues[i], point.attributes[i])
                maxValues[i] = maxOf(maxValues[i], point.attributes[i])
            }
        }

        // Normalize the data points
        return data.map { point ->
            val normalizedAttributes = point.attributes.mapIndexed { index, value ->
                (value - minValues[index]) / (maxValues[index] - minValues[index])
            }
            DataPoint(normalizedAttributes)
        }
    }

    private fun calculateEntropies(data: List<DataPoint>): List<Double> {
        return data[0].attributes.indices.map { index ->
            val frequencies = data.groupingBy { it.attributes[index] }.eachCount()
            val total = frequencies.values.sum().toDouble()
            frequencies.values.sumOf { freq ->
                val p = freq / total
                -p * ln(p)
            } / ln(total)
        }
    }

    private fun assignWeights(entropies: List<Double>): List<Double> {
        val q = entropies.size.toDouble()
        return entropies.map { entropy ->
            (1 - entropy) / (q - entropies.sum())
        }
    }

    private fun computeDistanceMatrix(data: List<DataPoint>, weights: List<Double>): List<List<Double>> {
        return data.map { point1 ->
            data.map { point2 ->
                point1.attributes.zip(point2.attributes)
                    .mapIndexed { index, (attr1, attr2) ->
                        weights[index] * abs(attr1 - attr2)
                    }
                    .sum()
            }
        }
    }

    private fun applyVMdav(distanceMatrix: List<List<Double>>, data: List<DataPoint>, k: Int): List<DataPoint> {
        val assignedGroups = mutableListOf<List<DataPoint>>()
        val unassignedIndices = data.indices.toMutableList()

        while (unassignedIndices.size > k - 1) {
            val centerIndex = unassignedIndices.random()
            val distancesFromCenter = distanceMatrix[centerIndex]

            val closestIndices = unassignedIndices
                .sortedBy { distancesFromCenter[it] }
                .take(k)

            val group = closestIndices.map { data[it] }
            assignedGroups.add(group)
            unassignedIndices.removeAll(closestIndices)
        }

        assignedGroups.add(unassignedIndices.map { data[it] })
        return assignedGroups.flatten()
    }

    fun applyLaplaceMechanism(data: List<HeartRateDataPoint>, epsilon: Double): List<HeartRateDataPoint> {
        // Implement the Laplace mechanism here
        return data.map {
            val noise = laplaceNoise(epsilon)
            HeartRateDataPoint(it.timestamp, it.heartRate + noise)
        }
    }

    fun applyExponentialMechanism(data: List<HeartRateDataPoint>, epsilon: Double): List<HeartRateDataPoint> {
        // Implement the Exponential mechanism here
        return data.map {
            val noise = exponentialNoise(epsilon)
            HeartRateDataPoint(it.timestamp, it.heartRate + noise)
        }
    }

    private fun laplaceNoise(epsilon: Double): Double {
        // Generate Laplace noise based on epsilon
        return Math.random() // This is a placeholder
    }

    private fun exponentialNoise(epsilon: Double): Double {
        // Generate Exponential noise based on epsilon
        return Math.random() // This is a placeholder
    }
}
