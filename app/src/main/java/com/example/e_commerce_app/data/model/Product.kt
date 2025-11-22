package com.example.e_commerce_app.data.model

data class Product(
    val id: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val description: String = "",
    val rating: Float = 0.0f,
    val discount: Int = 0,
    val brand: String = "",
    val imageUrl: String = "",
    val category: String = "", // Running, Casual, Formal, Sports, Sneakers
    val size: List<String> = emptyList(), // ["42", "43", "44", "45"]
    val colors: List<String> = emptyList(), // ["Black", "White", "Blue"]
    val stock: Int = 0, // numeric stock from backend
    val gender: String = "" // Men, Women, Unisex
) {
    // Original price is always in USD
    private val basePriceUSD: Double = price
    
    fun getPriceAfterDiscount(): Double =
        if (discount > 0) price - (price * discount / 100) else price
    
    val inStock: Boolean get() = stock > 0
    
    // Get formatted price with currency symbol
    fun getFormattedPrice(currency: String = "USD"): String {
        val finalPrice = getPriceAfterDiscount()
        return when (currency) {
            "EUR" -> {
                val euroPrice = finalPrice * 0.92 // Fixed rate: 1 USD = 0.92 EUR
                "€${String.format("%.2f", euroPrice)}"
            }
            else -> "$${String.format("%.2f", finalPrice)}"
        }
    }
}
