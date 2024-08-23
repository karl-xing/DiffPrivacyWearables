package com.example.diffprivacywearables.data

import android.util.Log
import kotlin.math.ln
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.sign

data class DataPoint(val attributes: List<Double>)

object PrivacyPreserving {

    fun applyKAnonymity(data: List<FitnessDataPoint>, k: Int): List<FitnessDataPoint> {
        // Convert FitnessDataPoints to DataPoints
        val dataPoints = data.map { DataPoint(listOf(it.value)) }

        // Apply personalized k-Anonymity
        val anonymizedDataPoints = personalizedKAnonymity(dataPoints, k)

        // Convert back to FitnessDataPoints
        return anonymizedDataPoints.mapIndexed { index, dataPoint ->
            FitnessDataPoint(
                timestamp = data[index].timestamp, // Preserve the original timestamp
                value = dataPoint.attributes[0],   // The anonymized value
                dataType = data[index].dataType    // Use the original dataType
            )
        }
    }

    fun personalizedKAnonymity(data: List<DataPoint>, k: Int): List<DataPoint> {
        // Step 1: Normalize attributes, data to [0, 1]
//        val normalizedData = normalizeData(data)
        val normalizedData = data
//        Log.d("personalizedKAnonymity", "Step 1 - Normalized DataPoints: $normalizedData")

        // Step 2: Calculate entropy and assign weights
        val entropies = calculateEntropies(normalizedData)
        val weights = assignWeights(entropies)
//        Log.d("personalizedKAnonymity", "Step 2 - Entropies: $entropies, Weights: $weights")

        // Step 3: Compute distance matrix
        val distanceMatrix = computeDistanceMatrix(normalizedData, weights)
//        Log.d("personalizedKAnonymity", "Step 3 - Distance Matrix: $distanceMatrix")

        // Step 4: Apply V-MDAV for k-anonymity grouping
        val anonymizedData = applyVMdav(distanceMatrix, normalizedData, k)
//        Log.d("personalizedKAnonymity", "Step 4 - Anonymized DataPoints: $anonymizedData")
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
        val attributeValues = data.map { it.attributes[0] }
        val numBins = 10                // 10 bins for entropy calculation
        return listOf(calculateEntropyForNumericData(attributeValues, numBins))
    }

    private fun calculateEntropyForNumericData(data: List<Double>, numBins: Int): Double {
        val minValue = data.minOrNull() ?: return 0.0
        val maxValue = data.maxOrNull() ?: return 0.0
        val binSize = (maxValue - minValue) / numBins
        val bins = MutableList(numBins) { 0 }

        for (value in data) {
            val binIndex = ((value - minValue) / binSize).toInt().coerceIn(0, numBins - 1)
            bins[binIndex]++
        }

        val total = data.size.toDouble()
        val probabilities = bins.map { it / total }

        return -probabilities.filter { it > 0 }.sumOf { p -> p * ln(p) } / ln(numBins.toDouble())
    }

    private fun assignWeights(entropies: List<Double>): List<Double> {
        val totalEntropy = entropies.sum()
        val q = entropies.size.toDouble()
        return entropies.map { (1 - it) / (q - totalEntropy) }
    }

    private fun computeDistanceMatrix(data: List<DataPoint>, weights: List<Double>): List<List<Double>> {
        return data.map { point1 ->
            data.map { point2 ->
                point1.attributes.zip(point2.attributes).mapIndexed { index, (attr1, attr2) ->
                    weights[index] * abs(attr1 - attr2)
                }.sum()
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

    //-----------------------------        Error Log         -----------------------------------//
    fun applyMechanismError(data: List<FitnessDataPoint>, epsilon: Double): List<FitnessDataPoint> {
        // Error Log
        Log.e("DataProcessing", "Algorithm Error")
        return data.map {
            val noise = laplace()
            FitnessDataPoint(it.timestamp, it.value + noise, it.dataType)
        }
    }

    private fun laplace(): Double  {
        return Math.random()
    }

    //-----------------------------Local Differential Privacy-----------------------------------//
    fun localDifferentialPrivacy(
        data: List<FitnessDataPoint>,
        epsilon: Double,
        alpha: Long  = 3L             // [1, 10] Control the threshold for keypoint identification
    ): List<FitnessDataPoint> {
        // Step 1: Identify salient points
//        Log.d("LDP", "data: $data")
        val salientPoints = identifySalientPoints(data, alpha)
//        val values = salientPoints.map { it.value }

        val values = data.map { it.value }
        val xmin = values.minOrNull() ?: return emptyList()
        val xmax = values.maxOrNull() ?: return emptyList()
        val xmean = values.average()

        return data.map { point ->
            val yi = normalizeDataPoint(point.value, xmin, xmax, xmean)

            // Apply RR Mechanism
            val ri = calculateAdaptiveRandomValue(yi, epsilon)
//            var noisyValue = addNoiseToData(point.value, ri, xmax - xmin, epsilon)
            // No RR Mechanism
            var noisyValue = addLaplaceNoise(point.value, xmax - xmin, epsilon)
//            Log.d("LDP", "noisyValue: $noisyValue")

            // Ensure the value remains within the valid range
            noisyValue = noisyValue.coerceIn(40.0, 200.0)
//            Log.d("LDP", "noisyValue after coerce: $noisyValue")

            FitnessDataPoint(
                timestamp = point.timestamp,
                value = noisyValue,
                dataType = point.dataType
            )
        }
    }

    // Identify salient points
    private fun identifySalientPoints(data: List<FitnessDataPoint>, alpha: Long): List<FitnessDataPoint> {
        val salientPoints = mutableListOf<FitnessDataPoint>()
        val derivatives = mutableListOf<Double>()

        for (i in 1 until data.size) {
            val di = (data[i].value - data[i - 1].value) / (data[i].timestamp - data[i - 1].timestamp)
            derivatives.add(di)
        }

        for (i in 1 until derivatives.size) {
            if (derivatives[i] != 0.0 && (i == 1 || derivatives[i].sign != derivatives[i - 1].sign)) {
                salientPoints.add(data[i])
            }
        }
        return salientPoints
    }

    private fun normalizeDataPoint(xi: Double, xmin: Double, xmax: Double, xmean: Double): Double {
        return (xi - xmean) / (xmax - xmin)
    }

    private fun calculateAdaptiveRandomValue(yi: Double, epsilon: Double): Double {
        val expEpsilon = exp(epsilon)
        return (expEpsilon - 1) / (2 * expEpsilon + 2) * yi + 0.5
    }

    private fun addNoiseToData(xi: Double, ri: Double, deltaS: Double, epsilon: Double): Double {
        val laplaceNoise = laplaceNoise(deltaS, epsilon)
        return xi + ri * laplaceNoise
    }

    private fun laplaceNoise(deltaS: Double, epsilon: Double): Double {
        val scale = deltaS / epsilon
        val uniform = Math.random() - 0.5
        return -scale * sign(uniform) * ln(1 - 2 * abs(uniform))
//        return -scale * Math.signum(uniform) * Math.log(1 - 2 * Math.abs(uniform))
    }

    private fun addLaplaceNoise(xi: Double, deltaS: Double, epsilon: Double): Double {
        val scale = deltaS / epsilon
        val uniform = Math.random() - 0.5
        val laplaceNoise = -scale * sign(uniform) * ln(1 - 2 * abs(uniform))
        return xi + laplaceNoise
    }
}
