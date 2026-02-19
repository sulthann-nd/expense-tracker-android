package com.example.expensetracker.data.repository

import com.example.expensetracker.data.api.ExchangeRateApi
import com.example.expensetracker.data.model.ExchangeRateResponse
import com.example.expensetracker.data.model.SymbolsResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.Response
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ExchangeRateRepository(private val api: ExchangeRateApi) {

    suspend fun getLatestRates(base: String = "EUR"): Flow<Result<ExchangeRateResponse>> = flow {
        try {
            val response = api.getLatestRates(base = base)
            if (response.isSuccessful) {
                response.body()?.let { emit(Result.success(it)) } ?: emit(Result.failure(Exception("Empty response")))
            } else {
                emit(Result.failure(Exception("API Error: ${response.message()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    suspend fun getSymbols(): Flow<Result<SymbolsResponse>> = flow {
        try {
            val response = api.getSymbols()
            if (response.isSuccessful) {
                response.body()?.let { emit(Result.success(it)) } ?: emit(Result.failure(Exception("Empty response")))
            } else {
                emit(Result.failure(Exception("API Error: ${response.message()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    suspend fun convertCurrency(from: String, to: String, amount: Double): Flow<Result<Double>> = flow {
        try {
            // Get latest rates (base is always EUR)
            val response = api.getLatestRates()
            if (response.isSuccessful) {
                response.body()?.let { data ->
                    if (data.success) {
                        // Convert to EUR first, then to target currency
                        val eurRate = data.rates[from] ?: 1.0
                        val targetRate = data.rates[to] ?: 1.0

                        // Convert amount to EUR, then to target
                        val amountInEur = amount / eurRate
                        val convertedAmount = amountInEur * targetRate

                        emit(Result.success(convertedAmount))
                    } else {
                        emit(Result.failure(Exception("API Error: ${data.error?.info ?: "Unknown error"}")))
                    }
                } ?: emit(Result.failure(Exception("Empty response")))
            } else {
                emit(Result.failure(Exception("API Error: ${response.message()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    suspend fun getHistoricalRates(date: LocalDate): Flow<Result<ExchangeRateResponse>> = flow {
        try {
            val dateString = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
            val response = api.getHistoricalRates(dateString)
            if (response.isSuccessful) {
                response.body()?.let { emit(Result.success(it)) } ?: emit(Result.failure(Exception("Empty response")))
            } else {
                emit(Result.failure(Exception("API Error: ${response.message()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}