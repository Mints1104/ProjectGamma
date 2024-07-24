package com.mints.projectgamma.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

object ApiClient {
    private const val BASE_URL_NYC = "https://nycpokemap.com"
    private const val BASE_URL_LONDON = "https://londonpogomap.com"
    private const val BASE_URL_SG = "https://sgpokemap.com/"
    private const val BASE_URL_VANCOUVER = "https://vanpokemap.com/"
    private const val BASE_URL_SYDNEY = "https://sydneypogomap.com/"
    private const val BASE_URL_TIMEZONE = "https://api.timezonedb.com/v2.1/"

    val retrofitNYC: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL_NYC)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val retrofitLondon: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL_LONDON)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val retrofitSingapore: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL_SG)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val retrofitVancouver: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL_VANCOUVER)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val retrofitSydney: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL_SYDNEY)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    // Initialize Retrofit for TimeZoneDB
    private val retrofitTimeZone: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL_TIMEZONE)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // Create the TimeZoneDBService
    val timeZoneDBService: TimeZoneDBService by lazy {
        retrofitTimeZone.create(TimeZoneDBService::class.java)
    }

    // Define the TimeZoneDBService interface
    interface TimeZoneDBService {
        @GET("v2.1/get-time-zone")
        suspend fun getTimeZone(
            @Query("key") apiKey: String,
            @Query("format") format: String = "json",
            @Query("by") by: String = "position",
            @Query("lat") latitude: Double,
            @Query("lng") longitude: Double
        ): TimeZoneResponse
    }

    // Define the response data class
    data class TimeZoneResponse(
        val status: String,
        val message: String?,
        val countryCode: String,
        val countryName: String,
        val regionName: String,
        val zoneName: String,
        val gmtOffset: Int,
        val dst: Int,
        val dstOffset: Int,
        val timestamp: Long,
        val formatted: String
    )


    // Define your other API interfaces
    interface PokeMapApi {
        @GET("/pokestop.php")
        suspend fun getInvasions(): List<Invasion>
    }
}
