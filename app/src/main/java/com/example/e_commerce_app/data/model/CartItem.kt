package com.example.e_commerce_app.data.model

data class CartItem(
    val id: String = "",
    val productId: String = "",
    val productName: String = "",
    val productImage: String = "",
    val price: Double = 0.0,
    val selectedSize: String = "",
    val selectedColor: String = "",
    val quantity: Int = 1,
    val userId: String = "",
    @com.google.firebase.firestore.Exclude
    var product: Product? = null  // Reference to full product, not stored in Firestore
) {
    fun getTotalPrice(): Double = price * quantity
    
    fun getFormattedPrice(currency: String = "USD"): String {
        return when (currency) {
            "EUR" -> {
                val euroPrice = price * 0.92
                "€${String.format("%.2f", euroPrice)}"
            }
            else -> "$${String.format("%.2f", price)}"
        }
    }
    
    fun getFormattedTotal(currency: String = "USD"): String {
        val total = getTotalPrice()
        return when (currency) {
            "EUR" -> {
                val euroTotal = total * 0.92
                "€${String.format("%.2f", euroTotal)}"
            }
            else -> "$${String.format("%.2f", total)}"
        }
    }
}
