package com.mints.projectgamma.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET


object ApiClient {
    private const val BASE_URL_NYC = "https://nycpokemap.com"
    private const val BASE_URL_LONDON = "https://londonpogomap.com"

    val retrofitNYC = Retrofit.Builder()
        .baseUrl(BASE_URL_NYC)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

     val retrofitLondon = Retrofit.Builder()
        .baseUrl(BASE_URL_LONDON)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    interface PokeMapApiNYC {
        @GET("/pokestop.php")
        suspend fun getInvasions(): List<Invasion>
    }

    interface PokeMapApiLondon {
        @GET("/pokestop.php")
        suspend fun getInvasions(): List<Invasion>
    }

    val apiNYC: PokeMapApiNYC = retrofitNYC.create(PokeMapApiNYC::class.java)
    val apiLondon: PokeMapApiLondon = retrofitLondon.create(PokeMapApiLondon::class.java)
}
