package com.example.diabetica.data.juggluco

data class GlucoseData(
    val value: Double,
    val direction: String,
    val timestamp: Long,
    val delta: Double,
    val trend: Int,
    val trendSymbol: String,
    val formattedTime: String,
    val formattedDate: String
)

data class SensorInfo(
    val sensorConnected: Boolean,
    val sensorStartDate: Long,
    val sensorAgeDays: Double,
    val sensorDaysLeft: Int,
    val transmitterBattery: Int,
    val unit: String
)