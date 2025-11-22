package com.example.e_commerce_app

import com.example.e_commerce_app.data.model.Address
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for Address model
 */
class AddressTest {
    
    @Test
    fun testGetFullAddress() {
        // Test full address formatting
        val address = Address(
            fullName = "John Doe",
            phoneNumber = "1234567890",
            addressLine1 = "123 Main St",
            addressLine2 = "Apt 4B",
            city = "New York",
            zipCode = "10001"
        )
        
        val fullAddress = address.getFullAddress()
        
        assertTrue("Should contain address line 1", fullAddress.contains("123 Main St"))
        assertTrue("Should contain address line 2", fullAddress.contains("Apt 4B"))
        assertTrue("Should contain city", fullAddress.contains("New York"))
        assertTrue("Should contain zip code", fullAddress.contains("10001"))
    }
    
    @Test
    fun testGetFullAddressWithoutLine2() {
        // Test address without line 2
        val address = Address(
            fullName = "Jane Smith",
            phoneNumber = "9876543210",
            addressLine1 = "456 Oak Ave",
            addressLine2 = "",
            city = "Boston",
            zipCode = "02101"
        )
        
        val fullAddress = address.getFullAddress()
        
        assertTrue("Should contain address line 1", fullAddress.contains("456 Oak Ave"))
        assertTrue("Should contain city", fullAddress.contains("Boston"))
        assertTrue("Should contain zip code", fullAddress.contains("02101"))
        // Should not have extra commas
        assertTrue("Should not have double commas", !fullAddress.contains(",,"))
    }
    
    @Test
    fun testAddressDefaults() {
        // Test default empty address
        val address = Address()
        
        assertEquals("Full name should be empty", "", address.fullName)
        assertEquals("Phone should be empty", "", address.phoneNumber)
        assertEquals("Address line 1 should be empty", "", address.addressLine1)
        assertEquals("Address line 2 should be empty", "", address.addressLine2)
        assertEquals("City should be empty", "", address.city)
        assertEquals("Zip code should be empty", "", address.zipCode)
    }
}
