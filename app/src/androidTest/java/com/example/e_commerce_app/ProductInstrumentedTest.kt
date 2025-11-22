package com.example.e_commerce_app

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.e_commerce_app.data.model.Product
import com.example.e_commerce_app.utils.GlobalCurrency
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for Product model and currency formatting
 */
@RunWith(AndroidJUnit4::class)
class ProductInstrumentedTest {

    private lateinit var testProduct: Product

    @Before
    fun setup() {
        testProduct = Product(
            id = "test123",
            name = "Test Shoe",
            price = 100.0,
            brand = "Nike",
            rating = 4.5f,
            discount = 10,
            imageUrl = "https://example.com/image.jpg",
            category = "Running",
            stock = 50
        )
    }

    @Test
    fun testProductPriceAfterDiscount() {
        val expectedPrice = 90.0 // 100 - 10%
        assertEquals(expectedPrice, testProduct.getPriceAfterDiscount(), 0.01)
    }

    @Test
    fun testProductInStock() {
        assertTrue(testProduct.inStock)
        
        val outOfStockProduct = testProduct.copy(stock = 0)
        assertFalse(outOfStockProduct.inStock)
    }

    @Test
    fun testProductFormattedPriceUSD() {
        GlobalCurrency.setCurrency("USD")
        val formattedPrice = testProduct.getFormattedPrice("USD")
        
        // Should be $90.00 after discount
        assertTrue(formattedPrice.startsWith("$"))
        assertTrue(formattedPrice.contains("90.00"))
    }

    @Test
    fun testProductFormattedPriceEUR() {
        GlobalCurrency.setCurrency("EUR")
        val formattedPrice = testProduct.getFormattedPrice("EUR")
        
        // Should be €82.80 (90 * 0.92)
        assertTrue(formattedPrice.startsWith("€"))
        assertTrue(formattedPrice.contains("82.80"))
    }

    @Test
    fun testAppContextPackageName() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.example.e_commerce_app", appContext.packageName)
    }

    @Test
    fun testProductWithNoDiscount() {
        val noDiscountProduct = testProduct.copy(discount = 0)
        assertEquals(100.0, noDiscountProduct.getPriceAfterDiscount(), 0.01)
    }
}
