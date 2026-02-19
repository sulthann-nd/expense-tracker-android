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
        @Query("access_key") accessKey: String = "7701f38b4ec45c08154e713cf66e45e2",
        @Query("base") base: String = "EUR"
    ): Response<ExchangeRateResponse>

    @GET("symbols")
    suspend fun getSymbols(@Query("access_key") accessKey: String = "7701f38b4ec45c08154e713cf66e45e2"): Response<SymbolsResponse>

    @GET("{date}")
    suspend fun getHistoricalRates(
        @Path("date") date: String,
        @Query("access_key") accessKey: String = "7701f38b4ec45c08154e713cf66e45e2"
    ): Response<ExchangeRateResponse>
}