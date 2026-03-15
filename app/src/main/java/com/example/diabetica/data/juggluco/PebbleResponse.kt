package com.example.diabetica.data.juggluco

import com.google.gson.annotations.SerializedName

data class PebbleResponse(
    @SerializedName("sgv") val sgv: Double? = null,
    @SerializedName("trend") val trend: Int? = null,
    @SerializedName("direction") val direction: String? = null,
    @SerializedName("datetime") val datetime: Long? = null,
    @SerializedName("bgdelta") val bgdelta: Double? = null,
    @SerializedName("iob") val iob: Double? = null,
    @SerializedName("now") val now: Long? = null
)