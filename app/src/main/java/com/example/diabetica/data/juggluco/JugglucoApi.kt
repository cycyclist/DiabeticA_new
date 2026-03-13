package com.example.diabetica.data.juggluco

import retrofit2.http.GET
import retrofit2.http.Query

interface JugglucoApi {

    // xDrip формат - возвращает массив с данными
    @GET("sgv.json")
    suspend fun getGlucoseXDrip(
        @Query("count") count: Int = 1,
        @Query("brief_mode") briefMode: String = "Y"
    ): List<RealJugglucoData>

    // Получить последнее показание
    @GET("sgv.json?count=1")
    suspend fun getLatestXDrip(): List<RealJugglucoData>

    // Получить статус сенсора
    @GET("sgv.json?sensor=Y&brief_mode=Y")
    suspend fun getSensorStatus(): List<RealJugglucoData>

    // Nightscout формат
    @GET("api/v1/entries.json")
    suspend fun getNightscoutEntries(
        @Query("count") count: Int = 1
    ): List<RealJugglucoData>

    // Экспортный формат Juggluco
    @GET("x/stream")
    suspend fun getStream(
        @Query("count") count: Int = 10,
        @Query("header") header: Boolean = false
    ): String
}