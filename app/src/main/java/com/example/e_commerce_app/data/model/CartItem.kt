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
    val userId: String = ""
) {
    fun getTotalPrice(): Double = price * quantity
}
