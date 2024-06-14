package com.mints.projectgamma.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET


object ApiClient {
    private const val BASE_URL = "https://nycpokemap.com"

    val retrofit = Retrofit.Builder()
        .baseUrl("https://nycpokemap.com") // Exclude trailing slash
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    interface PokeMapApi {
        @GET("/pokestop.php")
        suspend fun getInvasions(): List<Invasion>
    }


    val api: PokeMapApi = retrofit.create(PokeMapApi::class.java)
}