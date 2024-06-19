package com.mints.projectgamma.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET


object ApiClient {
    private const val BASE_URL_NYC = "https://nycpokemap.com"
    private const val BASE_URL_LONDON = "https://londonpogomap.com"
    private const val BASE_URL_SG = "https://sgpokemap.com/"
    private const val BASE_URL_VANCOUVER = "https://vanpokemap.com/"
    private const val BASE_URL_SYDNEY = "https://sydneypogomap.com/"

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

    interface PokeMapApi {
        @GET("/pokestop.php")
        suspend fun getInvasions(): List<Invasion>
    }

    val apiNYC: PokeMapApi = retrofitNYC.create(PokeMapApi::class.java)
    val apiLondon: PokeMapApi = retrofitLondon.create(PokeMapApi::class.java)
    val apiSingapore: PokeMapApi = retrofitSingapore.create(PokeMapApi::class.java)
    val apiVancouver: PokeMapApi = retrofitVancouver.create(PokeMapApi::class.java)
    val apiSydney: PokeMapApi = retrofitSydney.create(PokeMapApi::class.java)
}