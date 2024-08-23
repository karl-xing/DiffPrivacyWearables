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
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import java.io.File
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Calendar


data class FitnessDataPoint(
    val timestamp: Long, // Unix timestamp in milliseconds
    val value: Double,   // Value of the data point (e.g., heart rate, acceleration)
    val dataType: String // Type of data ("HeartRate" or "Acceleration")
)

class FitnessDataManager(private val context: Context) {

    private val fileName = "fitness_data.json"
    private val gson = Gson()
    private val fitnessOptions = FitnessOptions.builder()
        .addDataType(DataType.TYPE_HEART_RATE_BPM, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.TYPE_LOCATION_SAMPLE , FitnessOptions.ACCESS_READ)
        .build()
    private val account = GoogleSignIn.getAccountForExtension(context, fitnessOptions)

    suspend fun getFitnessData(
        dataType: DataType,
        days: Long = 60,
        bucketTimeMinutes: Int = 10,
        aggregate: Boolean = true,
        callback: (String) -> Unit = {}
    ): List<FitnessDataPoint> {
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
            when (dataType) {
                DataType.TYPE_HEART_RATE_BPM -> {
                    readRequestBuilder.aggregate(dataType, DataType.AGGREGATE_HEART_RATE_SUMMARY)
                        .bucketByTime(bucketTimeMinutes, TimeUnit.MINUTES)
                }
                DataType.TYPE_STEP_COUNT_DELTA -> {
                    readRequestBuilder.aggregate(dataType, DataType.TYPE_STEP_COUNT_DELTA)
                        .bucketByTime(bucketTimeMinutes, TimeUnit.MINUTES)
                }
                else -> {
                    // Add more conditions here for other types of data as needed
                    readRequestBuilder.read(dataType)
                        .bucketByTime(bucketTimeMinutes, TimeUnit.MINUTES)
                }
            }
        } else {
            readRequestBuilder.read(dataType)
        }

        val readRequest = readRequestBuilder.build()

        return try {
            val response: DataReadResponse = Fitness.getHistoryClient(context, account).readData(readRequest).await()
            val fitnessData = processDataReadResult(response, dataType)
            val fitnessDataStr = parseFitnessData(response, dataType)
            callback(fitnessDataStr)
            fitnessData
        } catch (e: Exception) {
            Log.e("FitnessDataManager", "There was an error reading data", e)
            callback("Failed to read fitness data")
            emptyList()
        }
    }

    private fun parseFitnessData(dataReadResponse: DataReadResponse, dataType: DataType): String {
        val fitnessDataSet = dataReadResponse.getDataSet(dataType)
        val dataPoints = fitnessDataSet.dataPoints

        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        return if (dataPoints.isNotEmpty()) {
            val sb = StringBuilder()
            for (dp in dataPoints) {
                val value: String = when (dataType) {
                    DataType.TYPE_HEART_RATE_BPM -> {
                        dp.getValue(Field.FIELD_BPM).asFloat().toString()
                    }
                    DataType.TYPE_SPEED -> {
                        val speed = dp.getValue(Field.FIELD_SPEED).asFloat()
                        "$speed m/s"
                    }
                    DataType.TYPE_LOCATION_SAMPLE -> {
                        val latitude = dp.getValue(Field.FIELD_LATITUDE).asFloat()
                        val longitude = dp.getValue(Field.FIELD_LONGITUDE).asFloat()
                        "Lat: $latitude, Long: $longitude"
                    }
                    // Other DataType
                    else -> dp.getValue(Field.FIELD_AVERAGE).asFloat().toString()
                }
                val timestamp = dp.getTimestamp(TimeUnit.MILLISECONDS)
                val date = sdf.format(Date(timestamp))
                sb.append("${dataType.name} : $value at $date\n")
            }
            sb.toString()
        } else {
            "No data available for ${dataType.name}"
        }
    }

    private fun processDataReadResult(dataReadResult: DataReadResponse, dataType: DataType): List<FitnessDataPoint> {
        val fitnessData = mutableListOf<FitnessDataPoint>()
        for (bucket in dataReadResult.buckets) {
            for (dataSet in bucket.dataSets) {
                for (dataPoint in dataSet.dataPoints) {
                    val value = dataPoint.getValue(Field.FIELD_AVERAGE).asFloat()
                    val timestamp = dataPoint.getStartTime(TimeUnit.MILLISECONDS)
                    fitnessData.add(FitnessDataPoint(timestamp, value.toDouble(), dataType.name))
                }
            }
        }
        return fitnessData
    }

    // Load JSON file to fitness data
    fun loadFitnessDataFromJson(context: Context, resourceId: Int): List<FitnessDataPoint>? {
        return try {
            val inputStream = context.resources.openRawResource(resourceId)
            val reader = InputStreamReader(inputStream)
            val type = object : TypeToken<List<FitnessDataPoint>>() {}.type
            val heartRateData: List<FitnessDataPoint> = Gson().fromJson(reader, type)
            reader.close()
            heartRateData
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Save fitness data to JSON file
//    fun saveFitnessData(fitnessDataList: List<FitnessDataPoint>) {
//        val jsonString = gson.toJson(fitnessDataList)
//        context.openFileOutput(fileName, Context.MODE_PRIVATE).use { outputStream ->
//            outputStream.write(jsonString.toByteArray())
//        }
//    }

    // Export fitness data to external storage
    fun exportFitnessDataToExternalStorage(fitnessDataList: List<FitnessDataPoint>) {
        val jsonString = gson.toJson(fitnessDataList)
        val externalFile = File(context.getExternalFilesDir(null), fileName)
        externalFile.writeText(jsonString)
        Log.d("FitnessDataManager", "Data exported to: ${externalFile.absolutePath}")
    }
}
