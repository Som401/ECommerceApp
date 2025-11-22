package com.example.e_commerce_app

import com.example.e_commerce_app.utils.CurrencyConverter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for Currency Converter
 */
class CurrencyConverterTest {
    
    @Test
    fun testConvertToEuro() {
        // Test USD to EUR conversion
        val usdAmount = 100.0
        val eurAmount = CurrencyConverter.convertToEuro(usdAmount)
        
        // EUR should be less than USD (rate is approximately 0.87)
        assertTrue("EUR amount should be less than USD", eurAmount < usdAmount)
        assertTrue("EUR amount should be positive", eurAmount > 0)
    }
    
    @Test
    fun testConvertToDollar() {
        // Test EUR to USD conversion
        val eurAmount = 87.0
        val usdAmount = CurrencyConverter.convertToDollar(eurAmount)
        
        // USD should be more than EUR
        assertTrue("USD amount should be more than EUR", usdAmount > eurAmount)
        assertTrue("USD amount should be positive", usdAmount > 0)
    }
    
    @Test
    fun testFormatPriceUSD() {
        // Test USD price formatting
        val amount = 123.45
        val formatted = CurrencyConverter.formatPrice(amount, "USD")
        
        assertEquals("Should format USD with $ symbol", "$123.45", formatted)
    }
    
    @Test
    fun testFormatPriceEUR() {
        // Test EUR price formatting
        val amount = 123.45
        val formatted = CurrencyConverter.formatPrice(amount, "EUR")
        
        assertEquals("Should format EUR with € symbol", "€123.45", formatted)
    }
    
    @Test
    fun testConvertAndFormatUSD() {
        // Test convert and format for USD (no conversion)
        val usdAmount = 50.0
        val result = CurrencyConverter.convertAndFormat(usdAmount, "USD")
        
        assertEquals("Should return USD formatted without conversion", "$50.00", result)
    }
    
    @Test
    fun testConvertAndFormatEUR() {
        // Test convert and format for EUR (with conversion)
        val usdAmount = 100.0
        val result = CurrencyConverter.convertAndFormat(usdAmount, "EUR")
        
        // Should start with € symbol
        assertTrue("Should start with € symbol", result.startsWith("€"))
        
        // Should contain a number less than 100
        val numberPart = result.substring(1).toDoubleOrNull()
        assertTrue("Should contain valid number", numberPart != null)
        assertTrue("Converted amount should be less than original", numberPart!! < usdAmount)
    }
}
