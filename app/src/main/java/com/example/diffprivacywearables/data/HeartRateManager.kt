@file:Suppress("DEPRECATION")

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
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.tasks.await
import java.util.*
import java.util.concurrent.TimeUnit
import java.io.File

data class HeartRateDataPoint(
    val timestamp: Long, // Unix timestamp in milliseconds
    val heartRate: Double // Heart Rate, unit: bpm
)

class HeartRateManager(private val context: Context) {

    private val fileName = "heart_rate_data.json"
    private val gson = Gson()
    private val fitnessOptions = FitnessOptions.builder()
        .addDataType(DataType.TYPE_HEART_RATE_BPM, FitnessOptions.ACCESS_READ)
        .build()

    private val account = GoogleSignIn.getAccountForExtension(context, fitnessOptions)

    suspend fun getHeartRateHistoryData(): List<HeartRateDataPoint> {
//        val calendar = Calendar.getInstance()
//        calendar.set(Calendar.MINUTE, 0)
//        calendar.set(Calendar.SECOND, 0)
//        calendar.set(Calendar.MILLISECOND, 0)
//        val endTime = calendar.timeInMillis
        val startTime = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(30)
        val endTime = System.currentTimeMillis()


        val readRequest = DataReadRequest.Builder()
            .aggregate(DataType.TYPE_HEART_RATE_BPM, DataType.AGGREGATE_HEART_RATE_SUMMARY)
            .bucketByTime(20, TimeUnit.MINUTES)     // Bucket by hour
//            .bucketByTime(1, TimeUnit.HOURS) // Bucket by hour
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
                    val average = dataPoint.getValue(Field.FIELD_AVERAGE).asFloat()
                    val timestamp = dataPoint.getStartTime(TimeUnit.MILLISECONDS)
                    heartRateData.add(HeartRateDataPoint(timestamp, average.toDouble()))
                }
            }
        }
//        Log.d("HeartRateManager", "Processed HR Data2: $heartRateData")
        return heartRateData
    }

    // Save heart rate data to JSON file
    fun saveHeartRateData(heartRateDataList: List<HeartRateDataPoint>) {
        val jsonString = gson.toJson(heartRateDataList)
        context.openFileOutput(fileName, Context.MODE_PRIVATE).use { outputStream ->
            outputStream.write(jsonString.toByteArray())
        }
    }

    // Load heart rate data from JSON file
    fun loadHeartRateData(): List<HeartRateDataPoint>? {
        return try {
            val file = File(context.filesDir, fileName)
            if (file.exists()) {
                val jsonString = file.readText()
                val type = object : TypeToken<List<HeartRateDataPoint>>() {}.type
                gson.fromJson(jsonString, type)
            } else {
                null // No data found
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Export heart rate data to external storage
    fun exportHeartRateDataToExternalStorage(heartRateDataList: List<HeartRateDataPoint>) {
        val jsonString = gson.toJson(heartRateDataList)
        val externalFile = File(context.getExternalFilesDir(null), fileName)
        externalFile.writeText(jsonString)
        Log.d("HeartRateManager", "Data exported to: ${externalFile.absolutePath}")
    }
}