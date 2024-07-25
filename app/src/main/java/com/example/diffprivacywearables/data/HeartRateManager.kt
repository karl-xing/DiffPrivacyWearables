package com.example.diffprivacywearables.data

import android.content.Context
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.data.Field
import com.google.android.gms.fitness.request.DataReadRequest
import com.google.android.gms.fitness.result.DataReadResponse
import kotlinx.coroutines.tasks.await
import java.util.*
import java.util.concurrent.TimeUnit

data class HeartRateDataPoint(
    val timestamp: Long, // Unix时间戳
    val heartRate: Double // 心率值，单位为bpm
)

class HeartRateManager(private val context: Context) {

    private val fitnessOptions = FitnessOptions.builder()
        .addDataType(DataType.TYPE_HEART_RATE_BPM, FitnessOptions.ACCESS_READ)
        .build()

    private val account = GoogleSignIn.getAccountForExtension(context, fitnessOptions)

    suspend fun getHeartRateHistoryData(): List<HeartRateDataPoint> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val endTime = calendar.timeInMillis
        val startTime = endTime - TimeUnit.HOURS.toMillis(1)

        val readRequest = DataReadRequest.Builder()
            .aggregate(DataType.TYPE_HEART_RATE_BPM, DataType.AGGREGATE_HEART_RATE_SUMMARY)
            .bucketByTime(1, TimeUnit.MINUTES)
            .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
            .build()

        return try {
            val response: DataReadResponse = Fitness.getHistoryClient(context, account).readData(readRequest).await()
            processDataReadResult(response)
        } catch (e: Exception) {
            Log.e("HeartRateManager", "There was an error reading data", e)
            emptyList()
        }
    }

    private fun processDataReadResult(dataReadResult: DataReadResponse): List<HeartRateDataPoint> {
        val heartRateData = mutableListOf<HeartRateDataPoint>()
        for (bucket in dataReadResult.buckets) {
            for (dataSet in bucket.dataSets) {
                for (dataPoint in dataSet.dataPoints) {
                    val heartRate = dataPoint.getValue(Field.FIELD_BPM).asFloat()
                    val timestamp = dataPoint.getTimestamp(TimeUnit.MILLISECONDS)
                    heartRateData.add(HeartRateDataPoint(timestamp, heartRate.toDouble()))
                }
            }
        }
        return heartRateData
    }
}
