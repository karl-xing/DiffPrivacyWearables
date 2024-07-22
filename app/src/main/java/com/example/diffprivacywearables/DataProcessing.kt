package com.example.diffprivacywearables

import kotlin.math.exp
import kotlin.random.Random

object DataProcessing {
    fun applyLaplaceMechanism(value: Double, epsilon: Double): Double {
        val scale = 1.0 / epsilon
        return value + laplaceNoise(scale)
    }

    fun applyExponentialMechanism(value: Double, epsilon: Double): Double {
        return expNoise(value, epsilon)
    }

    private fun laplaceNoise(scale: Double): Double {
        val u = Random.nextDouble() - 0.5
        return -scale * kotlin.math.sign(u) * kotlin.math.ln(1 - 2 * kotlin.math.abs(u))
    }

    private fun expNoise(value: Double, epsilon: Double): Double {
        // Simplified exponential mechanism for demonstration
        val noise = Random.nextDouble()
        return value * exp(epsilon * noise)
    }
}
