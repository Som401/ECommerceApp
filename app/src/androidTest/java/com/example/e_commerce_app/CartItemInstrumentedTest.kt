package com.example.e_commerce_app

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.e_commerce_app.data.model.CartItem
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for CartItem model
 */
@RunWith(AndroidJUnit4::class)
class CartItemInstrumentedTest {

    private lateinit var testCartItem: CartItem

    @Before
    fun setup() {
        testCartItem = CartItem(
            id = "cart123",
            productId = "prod456",
            productName = "Running Shoes",
            productImage = "https://example.com/shoe.jpg",
            price = 50.0,
            selectedSize = "42",
            selectedColor = "Black",
            quantity = 2,
            userId = "user789"
        )
    }

    @Test
    fun testCartItemTotalPrice() {
        val expectedTotal = 100.0 // 50 * 2
        assertEquals(expectedTotal, testCartItem.getTotalPrice(), 0.01)
    }

    @Test
    fun testCartItemFormattedPriceUSD() {
        val formattedPrice = testCartItem.getFormattedPrice("USD")
        assertTrue(formattedPrice.startsWith("$"))
        assertTrue(formattedPrice.contains("50.00"))
    }

    @Test
    fun testCartItemFormattedPriceEUR() {
        val formattedPrice = testCartItem.getFormattedPrice("EUR")
        assertTrue(formattedPrice.startsWith("€"))
        // 50 * 0.92 = 46.00
        assertTrue(formattedPrice.contains("46.00"))
    }

    @Test
    fun testCartItemFormattedTotalUSD() {
        val formattedTotal = testCartItem.getFormattedTotal("USD")
        assertTrue(formattedTotal.startsWith("$"))
        assertTrue(formattedTotal.contains("100.00"))
    }

    @Test
    fun testCartItemFormattedTotalEUR() {
        val formattedTotal = testCartItem.getFormattedTotal("EUR")
        assertTrue(formattedTotal.startsWith("€"))
        // 100 * 0.92 = 92.00
        assertTrue(formattedTotal.contains("92.00"))
    }

    @Test
    fun testCartItemQuantityUpdate() {
        val updatedItem = testCartItem.copy(quantity = 5)
        assertEquals(5, updatedItem.quantity)
        assertEquals(250.0, updatedItem.getTotalPrice(), 0.01) // 50 * 5
    }

    @Test
    fun testCartItemPriceUpdate() {
        val updatedItem = testCartItem.copy(price = 75.0)
        assertEquals(75.0, updatedItem.price, 0.01)
        assertEquals(150.0, updatedItem.getTotalPrice(), 0.01) // 75 * 2
    }
}
