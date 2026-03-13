package com.example.diabetica.data.juggluco

import com.google.gson.annotations.SerializedName

data class XDripGlucose(
    @SerializedName("sgv") val sgv: Double? = null,
    @SerializedName("glucose") val glucose: Double? = null,
    @SerializedName("trend") val trend: Int? = null,
    @SerializedName("timestamp") val timestamp: Long? = null,
    @SerializedName("direction") val direction: String? = null,
    @SerializedName("datetime") val datetime: Long? = null
) {
    fun toJugglucoGlucose(): JugglucoGlucose? {
        val glucoseValue = sgv ?: glucose ?: return null
        val time = timestamp ?: datetime ?: System.currentTimeMillis()
        return JugglucoGlucose(
            timestamp = time,
            glucose = glucoseValue,
            trend = trend ?: 0,
            trendDescription = direction
        )
    }
}