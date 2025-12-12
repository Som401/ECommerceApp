package com.example.e_commerce_app

import com.example.e_commerce_app.data.model.CartItem
import com.example.e_commerce_app.data.model.Order
import com.example.e_commerce_app.data.model.OrderItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for Order and Cart models
 */
class OrderAndCartTest {
    
    @Test
    fun testOrderItemTotalPrice() {
        // Test order item price calculation
        val orderItem = OrderItem(
            productId = "prod_2",
            productName = "Running Shoe",
            productImage = "",
            price = 75.50,
            selectedSize = "9",
            selectedColor = "Blue",
            quantity = 2
        )
        
        val totalPrice = orderItem.getTotalPrice()
        
        assertEquals("Total should be price * quantity", 151.0, totalPrice, 0.01)
    }
    
    @Test
    fun testOrderFormattedDate() {
        // Test order date formatting
        val timestamp = 1700000000000L // Some timestamp
        val order = Order(
            id = "order_1",
            userId = "user_1",
            orderDate = timestamp
        )
        
        val formattedDate = order.getFormattedDate()
        
        assertTrue("Formatted date should not be empty", formattedDate.isNotEmpty())
        assertTrue("Should contain month", formattedDate.matches(Regex(".*[A-Z][a-z]{2}.*")))
    }
    
    @Test
    fun testOrderDefaults() {
        // Test default order values
        val order = Order()
        
        assertEquals("ID should be empty", "", order.id)
        assertEquals("User ID should be empty", "", order.userId)
        assertTrue("Items list should be empty", order.items.isEmpty())
        assertEquals("Subtotal should be 0", 0.0, order.subtotal, 0.01)
        assertEquals("Shipping should be 0", 0.0, order.shippingCost, 0.01)
        assertEquals("Total should be 0", 0.0, order.total, 0.01)
        assertEquals("Status should be Pending", "Pending", order.status)
    }
    
    @Test
    fun testCartItemDefaults() {
        // Test default cart item values
        val cartItem = CartItem()
        
        assertEquals("ID should be empty", "", cartItem.id)
        assertEquals("Product ID should be empty", "", cartItem.productId)
        assertEquals("Price should be 0", 0.0, cartItem.price, 0.01)
        assertEquals("Quantity should be 1", 1, cartItem.quantity)
    }
}
