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
import java.text.SimpleDateFormat


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

    suspend fun getHeartRateData(
        days: Long = 30,
        bucketTimeMinutes: Int = 20,
        aggregate: Boolean = true,
        callback: (String) -> Unit = {}
    ): List<HeartRateDataPoint> {
//        val endTime = System.currentTimeMillis()
        val endTime = Calendar.getInstance().apply {
            set(Calendar.YEAR, 2024)
            set(Calendar.MONTH, Calendar.AUGUST)
            set(Calendar.DAY_OF_MONTH, 16)
            set(Calendar.HOUR_OF_DAY, 10)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val startTime = endTime - TimeUnit.DAYS.toMillis(days)

        val readRequestBuilder = DataReadRequest.Builder()
            .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)

        if (aggregate) {
            readRequestBuilder.aggregate(DataType.TYPE_HEART_RATE_BPM, DataType.AGGREGATE_HEART_RATE_SUMMARY)
                .bucketByTime(bucketTimeMinutes, TimeUnit.MINUTES)
        } else {
            readRequestBuilder.read(DataType.TYPE_HEART_RATE_BPM)
        }

        val readRequest = readRequestBuilder.build()

        return try {
            val response: DataReadResponse = Fitness.getHistoryClient(context, account).readData(readRequest).await()
            val heartRateData = processDataReadResult(response)
            val heartRateDataStr = parseHeartRateData(response)
            callback(heartRateDataStr)
            heartRateData
        } catch (e: Exception) {
            Log.e("HeartRateManager", "There was an error reading data", e)
            callback("Failed to read heart rate data")
            emptyList()
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
//        Log.d("HeartRateManager", "Processed HR Data: $heartRateData")
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