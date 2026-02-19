package com.example.expensetracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.data.api.NetworkModule
import com.example.expensetracker.data.model.ExchangeRateResponse
import com.example.expensetracker.data.model.SymbolsResponse
import com.example.expensetracker.data.repository.ExchangeRateRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

class ExchangeRateViewModel : ViewModel() {

    private val repository = ExchangeRateRepository(NetworkModule.exchangeRateApi)

    private val _latestRates = MutableStateFlow<Result<ExchangeRateResponse>?>(null)
    val latestRates: StateFlow<Result<ExchangeRateResponse>?> = _latestRates

    private val _symbols = MutableStateFlow<Result<SymbolsResponse>?>(null)
    val symbols: StateFlow<Result<SymbolsResponse>?> = _symbols

    private val _conversion = MutableStateFlow<Result<Double>?>(null)
    val conversion: StateFlow<Result<Double>?> = _conversion

    private val _historicalRates = MutableStateFlow<Result<ExchangeRateResponse>?>(null)
    val historicalRates: StateFlow<Result<ExchangeRateResponse>?> = _historicalRates

    fun fetchLatestRates(base: String = "EUR") {
        viewModelScope.launch {
            repository.getLatestRates(base).collect { result ->
                _latestRates.value = result
            }
        }
    }

    fun fetchSymbols() {
        viewModelScope.launch {
            repository.getSymbols().collect { result ->
                _symbols.value = result
            }
        }
    }

    fun convertCurrency(from: String, to: String, amount: Double) {
        viewModelScope.launch {
            repository.convertCurrency(from, to, amount).collect { result ->
                _conversion.value = result
            }
        }
    }

    fun fetchHistoricalRates(date: LocalDate) {
        viewModelScope.launch {
            repository.getHistoricalRates(date).collect { result ->
                _historicalRates.value = result
            }
        }
    }
}