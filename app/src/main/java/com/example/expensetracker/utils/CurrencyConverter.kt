package com.example.expensetracker.utils

import com.example.expensetracker.data.model.ExchangeRateResponse

object CurrencyConverter {

    /**
     * Converts an amount from one currency to another using the provided exchange rates.
     * Uses EUR as the base currency for conversion.
     *
     * @param from The source currency code (e.g., "USD", "EUR", "INR")
     * @param to The target currency code (e.g., "USD", "EUR", "INR")
     * @param amount The amount to convert
     * @param rates The exchange rates response containing rates relative to EUR
     * @return The converted amount, or the original amount if conversion fails
     */
    fun convert(from: String, to: String, amount: Double, rates: ExchangeRateResponse?): Double {
        if (from == to || rates == null) return amount

        val exchangeRates = rates.rates

        // Get the rates for source and target currencies
        val fromRate = exchangeRates[from] ?: return amount
        val toRate = exchangeRates[to] ?: return amount

        // Convert: amount in 'from' currency -> EUR -> 'to' currency
        val amountInEur = amount / fromRate
        return amountInEur * toRate
    }

    /**
     * Converts an amount to INR using the provided exchange rates.
     *
     * @param from The source currency code
     * @param amount The amount to convert
     * @param rates The exchange rates response
     * @return The amount converted to INR, or the original amount if conversion fails
     */
    fun convertToINR(from: String, amount: Double, rates: ExchangeRateResponse?): Double {
        return convert(from, "INR", amount, rates)
    }
}