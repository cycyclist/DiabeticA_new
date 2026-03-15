package com.example.diabetica.data.juggluco

import com.google.gson.annotations.SerializedName

data class RealJugglucoData(
    @SerializedName("_id") val id: String? = null,
    @SerializedName("device") val device: String? = null,
    @SerializedName("dateString") val dateString: String? = null,
    @SerializedName("sysTime") val sysTime: String? = null,
    @SerializedName("date") val date: Long? = null,
    @SerializedName("sgv") val sgv: Int? = null,        // Значение глюкозы
    @SerializedName("delta") val delta: Double? = null,  // Изменение
    @SerializedName("direction") val direction: String? = null, // Направление
    @SerializedName("noise") val noise: Int? = null,
    @SerializedName("filtered") val filtered: Int? = null,
    @SerializedName("unfiltered") val unfiltered: Int? = null,
    @SerializedName("rssi") val rssi: Int? = null,
    @SerializedName("type") val type: String? = null,
    @SerializedName("units_hint") val unitsHint: String? = null
) {
    fun toJugglucoGlucose(): JugglucoGlucose {
        return JugglucoGlucose(
            timestamp = date ?: System.currentTimeMillis(),
            glucose = sgv?.toDouble() ?: 0.0,
            trend = convertDirectionToTrend(direction),
            trendDescription = direction,
            delta = delta
        )
    }

    private fun convertDirectionToTrend(direction: String?): Int {
        return when (direction) {
            "DoubleUp" -> 7
            "SingleUp" -> 6
            "FortyFiveUp" -> 5
            "Flat" -> 4
            "FortyFiveDown" -> 3
            "SingleDown" -> 2
            "DoubleDown" -> 1
            else -> 0
        }
    }
}