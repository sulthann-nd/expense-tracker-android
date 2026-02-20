package com.example.expensetracker.data.api

import com.example.expensetracker.data.model.ExchangeRateResponse
import com.example.expensetracker.data.model.SymbolsResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ExchangeRateApi {

    @GET("latest")
    suspend fun getLatestRates(
        @Query("access_key") accessKey: String = "3fbf307274f9f4213b74ae94a1d6ddee",
        @Query("base") base: String = "EUR"
    ): Response<ExchangeRateResponse>

    @GET("symbols")
    suspend fun getSymbols(@Query("access_key") accessKey: String = "3fbf307274f9f4213b74ae94a1d6ddee"): Response<SymbolsResponse>

    @GET("{date}")
    suspend fun getHistoricalRates(
        @Path("date") date: String,
        @Query("access_key") accessKey: String = "3fbf307274f9f4213b74ae94a1d6ddee"
    ): Response<ExchangeRateResponse>
}