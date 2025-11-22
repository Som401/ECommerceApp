package com.example.e_commerce_app.utils

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

object CurrencyConverter {
    private const val TAG = "CurrencyConverter"
    private const val API_URL = "https://api.ratesexchange.eu/client/latest?apikey=c4441d26-c2db-4aec-9add-5bea2c7bafc2&base_currency=USD&currencies=EUR"
    
    private var usdToEurRate: Double = 0.87 // Default fallback rate
    private var lastFetchTime: Long = 0
    private const val CACHE_DURATION = 3600000L // 1 hour in milliseconds
    
    /**
     * Fetch latest exchange rate from API (Coroutine)
     */
    suspend fun fetchExchangeRate(): Double = withContext(Dispatchers.IO) {
        try {
            // Check if we have a recent cached rate
            if (System.currentTimeMillis() - lastFetchTime < CACHE_DURATION) {
                Log.d(TAG, "Using cached exchange rate: $usdToEurRate")
                return@withContext usdToEurRate
            }
            
            Log.d(TAG, "Fetching exchange rate from API...")
            val response = URL(API_URL).readText()
            val json = JSONObject(response)
            
            val rates = json.getJSONObject("rates")
            usdToEurRate = rates.getDouble("EUR")
            lastFetchTime = System.currentTimeMillis()
            
            Log.d(TAG, "Exchange rate updated: 1 USD = $usdToEurRate EUR")
            usdToEurRate
            
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching exchange rate: ${e.message}", e)
            usdToEurRate // Return cached/default rate on error
        }
    }
    
    /**
     * Convert USD to EUR
     */
    fun convertToEuro(usdAmount: Double): Double {
        return usdAmount * usdToEurRate
    }
    
    /**
     * Convert EUR to USD
     */
    fun convertToDollar(eurAmount: Double): Double {
        return if (usdToEurRate > 0) eurAmount / usdToEurRate else eurAmount
    }
    
    /**
     * Format price with currency symbol
     */
    fun formatPrice(amount: Double, currency: String): String {
        return when (currency) {
            "EUR" -> "€${"%.2f".format(amount)}"
            "USD" -> "$${"%.2f".format(amount)}"
            else -> "$${"%.2f".format(amount)}"
        }
    }
    
    /**
     * Convert and format price based on selected currency
     */
    fun convertAndFormat(usdAmount: Double, targetCurrency: String): String {
        val amount = if (targetCurrency == "EUR") {
            convertToEuro(usdAmount)
        } else {
            usdAmount
        }
        return formatPrice(amount, targetCurrency)
    }
}
