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
        return com.example.e_commerce_app.utils.CurrencyConverter.convertAndFormat(price, currency)
    }
    
    fun getFormattedTotal(currency: String = "USD"): String {
        val total = getTotalPrice()
        return com.example.e_commerce_app.utils.CurrencyConverter.convertAndFormat(total, currency)
    }
}
