package com.example.piller.api


import com.example.piller.utilities.DbConstants
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ServiceBuilder {
    private var currentUrl = DbConstants.defaultStringValue
    private const val connectionTimeout: Long = DbConstants.connectionTimeout

    private var client = OkHttpClient.Builder()
        .connectTimeout(connectionTimeout, TimeUnit.SECONDS)
        .writeTimeout(connectionTimeout, TimeUnit.SECONDS)
        .readTimeout(connectionTimeout, TimeUnit.SECONDS)
        .build()

    private var gson: Gson = GsonBuilder()
        .setLenient()
        .create()

    private lateinit var retrofit: Retrofit

    fun updateRetrofit(url: String) {
        try {
            if (url.isNotEmpty() && url != currentUrl) {
                client = OkHttpClient.Builder()
                    .connectTimeout(connectionTimeout, TimeUnit.SECONDS)
                    .writeTimeout(connectionTimeout, TimeUnit.SECONDS)
                    .readTimeout(connectionTimeout, TimeUnit.SECONDS)
                    .build()
                retrofit = Retrofit.Builder()
                    .baseUrl(url)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(client)
                    .build()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun <T> buildService(service: Class<T>): T {
        return retrofit.create(service)
    }
}
