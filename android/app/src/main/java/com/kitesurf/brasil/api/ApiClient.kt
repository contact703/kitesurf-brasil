package com.kitesurf.brasil.api

import com.kitesurf.brasil.BuildConfig
import com.kitesurf.brasil.model.ChatResponse
import com.kitesurf.brasil.model.Spot
import com.kitesurf.brasil.model.Classified
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

interface KiteSurfApi {
    
    @POST("api/chat")
    suspend fun sendMessage(@Body request: Map<String, String>): ChatResponse
    
    @GET("api/spots")
    suspend fun getSpots(
        @Query("difficulty") difficulty: String? = null,
        @Query("location") location: String? = null
    ): List<Spot>
    
    @GET("api/spots/{id}")
    suspend fun getSpot(@Path("id") id: Int): Spot
    
    @GET("api/classifieds")
    suspend fun getClassifieds(
        @Query("category") category: String? = null,
        @Query("location") location: String? = null
    ): List<Classified>
    
    @GET("api/classifieds/{id}")
    suspend fun getClassified(@Path("id") id: Int): Classified
    
    @GET("health")
    suspend fun healthCheck(): Map<String, Any>
}

object ApiClient {
    
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()
    
    private val retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.API_BASE_URL + "/")
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    val api: KiteSurfApi = retrofit.create(KiteSurfApi::class.java)
}
