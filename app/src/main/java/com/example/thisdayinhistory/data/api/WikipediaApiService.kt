package com.example.thisdayinhistory.data.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

interface WikipediaApiService {

    @GET("feed/onthisday/{type}/{MM}/{DD}")
    suspend fun getOnThisDay(
        @Path("type") type: String,
        @Path("MM") month: String,
        @Path("DD") day: String
    ): OnThisDayResponse

    companion object {
        private const val BASE_URL = "https://en.wikipedia.org/api/rest_v1/"

        val instance: WikipediaApiService by lazy {
            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(WikipediaApiService::class.java)
        }
    }
}
