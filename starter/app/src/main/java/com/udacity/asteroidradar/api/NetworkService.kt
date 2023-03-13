package com.udacity.asteroidradar.api

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.udacity.asteroidradar.domain.Asteroid
import retrofit2.Retrofit
import com.udacity.asteroidradar.Constants
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

enum class AsteroidApiFilter(val value: String) {
    SHOW_WEEK("week"), SHOW_TODAY("today"), SHOW_SAVED("saved")
}

//moshi
private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

//retrofit moshi
private val retrofit_moshi = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(Constants.BASE_URL)
    .build()

//retrofit
private val retrofit = Retrofit.Builder()
    .addConverterFactory(ScalarsConverterFactory.create())
    .baseUrl(Constants.BASE_URL)
    .build()


interface NetworkService {
    @GET("neo/rest/v1/feed")
    suspend fun getAsteroids(@Query("start_date") startDate: String?,
                             @Query("end_date") endDate: String?,
                             @Query("api_key") apiKey: String): String

    @GET("planetary/apod")
    suspend fun getPictureOfDay(@Query("api_key") apiKey: String): NetworkPictureOfDay
}

object  NasaApi {
    val retrofitScalarService : NetworkService by lazy {
        retrofit.create(NetworkService::class.java)
    }
    val retrofitMoshiService : NetworkService by lazy {
        retrofit_moshi.create(NetworkService::class.java)
    }
}
