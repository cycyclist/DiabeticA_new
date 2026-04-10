package com.example.diabetica.data.juggluco

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import java.io.InputStream
import java.util.concurrent.TimeUnit

class SimpleJugglucoClient {

    companion object {
        private const val TAG = "JugglucoClient"
        private const val BASE_URL = "http://192.168.0.245:17580"

        private val client = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .build()

        private const val MGDLTOMMOL = 18.018

        /**
         * Конвертирует значение из мг/дл в ммоль/л
         */
        fun mgdlToMmol(valueMgdl: Double): Double {
            return valueMgdl / MGDLTOMMOL
        }

        /**
         * Конвертирует значение из ммоль/л в мг/дл
         */
        fun mmolToMgdl(valueMmol: Double): Double {
            return valueMmol * MGDLTOMMOL
        }

        /**
         * Форматирует значение с единицами измерения
         */
        fun formatGlucoseValue(value: Double, useMmol: Boolean): String {
            return if (useMmol) {
                String.format("%.1f", mgdlToMmol(value))
            } else {
                String.format("%.0f", value)
            }
        }

        /**
         * Получает суффикс единиц измерения
         */
        fun getUnitSuffix(useMmol: Boolean): String {
            return if (useMmol) "ммоль/л" else "мг/дл"
        }}

    suspend fun getLatestGlucose(): GlucoseData? {
        return try {
            val url = "$BASE_URL/sgv.json?count=1"
            Log.d(TAG, "Запрос: $url")

            val request = Request.Builder()
                .url(url)
                .get()
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string()

            Log.d(TAG, "Ответ: $body")

            if (response.isSuccessful && !body.isNullOrEmpty()) {
                parseGlucoseResponse(body)
            } else {
                Log.e(TAG, "Ошибка: ${response.code}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка запроса", e)
            null
        }
    }

    suspend fun getGlucoseHistory(hours: Int): List<GlucoseData> {
        return try {
            val count = hours * 12
            val url = "$BASE_URL/sgv.json?count=$count"

            val request = Request.Builder()
                .url(url)
                .get()
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string()

            if (response.isSuccessful && !body.isNullOrEmpty()) {
                parseGlucoseHistoryResponse(body)
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка запроса истории", e)
            emptyList()
        }
    }

    /**
     * Получить график глюкозы в виде Bitmap
     * URL: http://192.168.0.245:17580/x/curve
     *
     * @param hours количество часов для отображения (по умолчанию 6)
     * @param width ширина графика в пикселях (по умолчанию 800)
     * @param height высота графика в пикселях (по умолчанию 400)
     * @param darkMode темный фон (true) или светлый (false)
     * @param useMmol показывать в ммоль/л (true) или мг/дл (false)
     */
    suspend fun getGlucoseCurve(
        hours: Int = 6,
        width: Int = 800,
        height: Int = 400,
        darkMode: Boolean = false,
        useMmol: Boolean = false
    ): Bitmap? {
        return try {
            // Собираем URL с параметрами
            val urlBuilder = StringBuilder("$BASE_URL/x/curve?")
            urlBuilder.append("duration=${hours * 3600}")  // часы в секунды
            urlBuilder.append("&width=$width")
            urlBuilder.append("&height=$height")

            if (darkMode) {
                urlBuilder.append("&darkmode")
            }

            if (useMmol) {
                urlBuilder.append("&mmol/L")
            } else {
                urlBuilder.append("&mg/dL")
            }

            // Добавляем stream и history для отображения всех данных
            urlBuilder.append("&stream&history&scans")

            val url = urlBuilder.toString()
            Log.d(TAG, "Запрос графика: $url")

            val request = Request.Builder()
                .url(url)
                .get()
                .build()

            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                val inputStream: InputStream? = response.body?.byteStream()
                val bitmap = BitmapFactory.decodeStream(inputStream)
                Log.d(TAG, "График загружен, размер: ${bitmap?.width}x${bitmap?.height}")
                bitmap
            } else {
                Log.e(TAG, "Ошибка загрузки графика: ${response.code}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при загрузке графика", e)
            null
        }
    }

    /**
     * Получить график с дополнительными опциями
     * @param startTime время начала в секундах (Unix timestamp)
     * @param endTime время окончания в секундах
     * @param showMeals показывать приёмы пищи
     * @param showAmounts показывать введённые значения
     */
    suspend fun getGlucoseCurveAdvanced(
        hours: Int = 6,
        width: Int = 800,
        height: Int = 400,
        darkMode: Boolean = false,
        useMmol: Boolean = false,
        startTime: Long? = null,
        endTime: Long? = null,
        showMeals: Boolean = false,
        showAmounts: Boolean = false
    ): Bitmap? {
        return try {
            val urlBuilder = StringBuilder("$BASE_URL/x/curve?")

            if (startTime != null && endTime != null) {
                urlBuilder.append("starttime=$startTime&endtime=$endTime")
            } else {
                urlBuilder.append("duration=${hours * 3600}")
            }

            urlBuilder.append("&width=$width")
            urlBuilder.append("&height=$height")

            if (darkMode) urlBuilder.append("&darkmode")
            if (useMmol) urlBuilder.append("&mmol/L") else urlBuilder.append("&mg/dL")
            if (showMeals) urlBuilder.append("&meals")
            if (showAmounts) urlBuilder.append("&amounts")

            urlBuilder.append("&stream&history&scans")

            val url = urlBuilder.toString()
            Log.d(TAG, "Запрос расширенного графика: $url")

            val request = Request.Builder()
                .url(url)
                .get()
                .build()

            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                val inputStream: InputStream? = response.body?.byteStream()
                BitmapFactory.decodeStream(inputStream)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при загрузке расширенного графика", e)
            null
        }
    }

    suspend fun isAvailable(): Boolean {
        return try {
            val url = "$BASE_URL/sgv.json?count=1"
            val request = Request.Builder()
                .url(url)
                .head()
                .build()

            val response = client.newCall(request).execute()
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getSensorInfo(): SensorInfo? {
        return try {
            val url = "$BASE_URL/sgv.json?sensor=Y&brief_mode=Y&count=1"
            val request = Request.Builder()
                .url(url)
                .get()
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string()

            if (response.isSuccessful && !body.isNullOrEmpty()) {
                parseSensorInfoResponse(body)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка запроса информации о сенсоре", e)
            null
        }
    }

    private fun parseGlucoseResponse(json: String): GlucoseData? {
        return try {
            val jsonArray = JSONArray(json)
            if (jsonArray.length() == 0) return null

            val item = jsonArray.getJSONObject(0)

            val sgv = item.optDouble("sgv", 0.0)
            val direction = item.optString("direction", "UNKNOWN")
            val date = item.optLong("date", System.currentTimeMillis())
            val delta = item.optDouble("delta", 0.0)
            val trend = item.optInt("trend", 0)

            GlucoseData(
                value = sgv,
                direction = direction,
                timestamp = date,
                delta = delta,
                trend = trend,
                trendSymbol = getTrendSymbol(direction),
                formattedTime = formatTime(date),
                formattedDate = formatDate(date)
            )
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка парсинга JSON", e)
            null
        }
    }

    private fun parseGlucoseHistoryResponse(json: String): List<GlucoseData> {
        val list = mutableListOf<GlucoseData>()
        try {
            val jsonArray = JSONArray(json)
            for (i in 0 until jsonArray.length()) {
                val item = jsonArray.getJSONObject(i)
                val sgv = item.optDouble("sgv", 0.0)
                val direction = item.optString("direction", "UNKNOWN")
                val date = item.optLong("date", System.currentTimeMillis())
                val delta = item.optDouble("delta", 0.0)
                val trend = item.optInt("trend", 0)

                list.add(GlucoseData(
                    value = sgv,
                    direction = direction,
                    timestamp = date,
                    delta = delta,
                    trend = trend,
                    trendSymbol = getTrendSymbol(direction),
                    formattedTime = formatTime(date),
                    formattedDate = formatDate(date)
                ))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка парсинга истории", e)
        }
        return list
    }

    private fun parseSensorInfoResponse(json: String): SensorInfo? {
        return try {
            val jsonArray = JSONArray(json)
            if (jsonArray.length() == 0) return null

            val item = jsonArray.getJSONObject(0)

            val sensorStart = item.optLong("sensor_start", 0)
            val sensorAge = item.optLong("sensor_age", 0)
            val sensorDaysLeft = ((14 * 24 * 60 * 60) - sensorAge) / (24 * 60 * 60)

            SensorInfo(
                sensorConnected = true,
                sensorStartDate = sensorStart,
                sensorAgeDays = sensorAge / (24 * 60 * 60).toDouble(),
                sensorDaysLeft = sensorDaysLeft.toInt(),
                transmitterBattery = item.optInt("transmitter_battery", 100),
                unit = item.optString("unit", "mg/dL")
            )
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка парсинга информации о сенсоре", e)
            null
        }
    }

    private fun getTrendSymbol(direction: String): String {
        return when (direction) {
            "DoubleUp" -> "⥣"
            "SingleUp" -> "↑"
            "FortyFiveUp" -> "↗"
            "Flat" -> "→"
            "FortyFiveDown" -> "↘"
            "SingleDown" -> "↓"
            "DoubleDown" -> "⥥"
            else -> "?"
        }
    }

    private fun formatTime(timestamp: Long): String {
        val date = java.util.Date(timestamp)
        val format = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
        return format.format(date)
    }

    private fun formatDate(timestamp: Long): String {
        val date = java.util.Date(timestamp)
        val format = java.text.SimpleDateFormat("dd.MM.yyyy HH:mm", java.util.Locale.getDefault())
        return format.format(date)
    }
}