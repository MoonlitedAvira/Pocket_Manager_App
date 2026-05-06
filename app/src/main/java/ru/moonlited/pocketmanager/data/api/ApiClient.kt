// data/api/ApiClient.kt
package ru.moonlited.pocketmanager.data.api

import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import ru.moonlited.pocketmanager.utils.SessionManager

object ApiClient {
    private const val BASE_URL = "http://192.168.0.106:8000/"

    private val json = Json { ignoreUnknownKeys = true }

    fun create(sessionManager: SessionManager): ApiService {
        // Логирование запросов в Logcat (очень помогает при дебаге)
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        // Перехватчик для добавления токена
        val authInterceptor = Interceptor { chain ->
            val requestBuilder = chain.request().newBuilder()

            // Если токен есть, добавляем его в заголовок
            sessionManager.fetchAuthToken()?.let { token ->
                requestBuilder.addHeader("Authorization", "Bearer $token")
            }

            chain.proceed(requestBuilder.build())
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(ApiService::class.java)
    }
}