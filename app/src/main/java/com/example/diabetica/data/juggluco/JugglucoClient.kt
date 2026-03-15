package com.example.diabetica.data.juggluco

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class JugglucoClient private constructor(
    private val context: Context,
    private val baseUrl: String,
    private val useHttps: Boolean = true
) {

    private val gson: Gson = GsonBuilder()
        .setLenient()
        .create()

    private val api: JugglucoApi

    init {
        val clientBuilder = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)

        val logging = HttpLoggingInterceptor { message ->
            Log.d("JugglucoClient", message)
        }.apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        clientBuilder.addInterceptor(logging)

        if (useHttps) {
            val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                override fun checkClientTrusted(
                    chain: Array<out X509Certificate>?,
                    authType: String?
                ) = Unit

                override fun checkServerTrusted(
                    chain: Array<out X509Certificate>?,
                    authType: String?
                ) = Unit

                override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
            })

            val sslContext = SSLContext.getInstance("TLS").apply {
                init(null, trustAllCerts, SecureRandom())
            }

            clientBuilder.sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
            clientBuilder.hostnameVerifier { _, _ -> true }
        }

        val client = clientBuilder.build()

        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        api = retrofit.create(JugglucoApi::class.java)
    }

    suspend fun isAvailable(): Boolean = try {
        val response = api.getLatestXDrip()
        response.isNotEmpty()
    } catch (e: Exception) {
        Log.e("JugglucoClient", "Ping failed", e)
        false
    }

    suspend fun getLatestGlucose(): JugglucoGlucose? = try {
        Log.d("JugglucoClient", "Пробуем получить данные...")

        val response = api.getLatestXDrip()
        Log.d("JugglucoClient", "Получен ответ: $response")

        if (response.isNotEmpty()) {
            val glucose = response.first().toJugglucoGlucose()
            Log.d("JugglucoClient", "Глюкоза: ${glucose.glucose}, тренд: ${glucose.trendDescription}")
            glucose
        } else {
            Log.d("JugglucoClient", "Нет данных в ответе")
            null
        }
    } catch (e: Exception) {
        Log.e("JugglucoClient", "Ошибка получения данных", e)
        null
    }

    suspend fun getGlucoseHistory(hours: Int): List<JugglucoGlucose> = try {
        val count = hours * 12 // примерно каждые 5 минут
        val response = api.getGlucoseXDrip(count = count, briefMode = "Y")
        response.map { it.toJugglucoGlucose() }
    } catch (e: Exception) {
        Log.e("JugglucoClient", "Failed to get history", e)
        emptyList()
    }

    fun observeGlucose(): Flow<JugglucoGlucose> = flow {
        while (true) {
            getLatestGlucose()?.let { emit(it) }
            kotlinx.coroutines.delay(60_000)
        }
    }

    companion object {
        private const val HTTP_PORT = 17580
        private const val HTTPS_PORT = 17581

        fun createLocal(context: Context, useHttps: Boolean = true): JugglucoClient {
            val protocol = if (useHttps) "https" else "http"
            val port = if (useHttps) HTTPS_PORT else HTTP_PORT
            val baseUrl = "$protocol://10.0.2.2:$port/"
            Log.d("JugglucoClient", "Base URL: $baseUrl")
            return JugglucoClient(context, baseUrl, useHttps)
        }

        fun createForDevice(context: Context, ipAddress: String, useHttps: Boolean = true): JugglucoClient {
            val protocol = if (useHttps) "https" else "http"
            val port = if (useHttps) HTTPS_PORT else HTTP_PORT
            val baseUrl = "$protocol://$ipAddress:$port/"
            Log.d("JugglucoClient", "Base URL for device: $baseUrl")
            return JugglucoClient(context, baseUrl, useHttps)
        }

        suspend fun createAuto(context: Context): JugglucoClient? {
            val httpsClient = createLocal(context, useHttps = true)
            if (httpsClient.isAvailable()) {
                Log.d("JugglucoClient", "HTTPS connection successful")
                return httpsClient
            }

            val httpClient = createLocal(context, useHttps = false)
            if (httpClient.isAvailable()) {
                Log.d("JugglucoClient", "HTTP connection successful")
                return httpClient
            }

            Log.e("JugglucoClient", "No connection to Juggluco")
            return null
        }
    }
}