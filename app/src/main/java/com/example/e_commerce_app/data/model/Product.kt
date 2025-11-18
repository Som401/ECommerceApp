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
    val size: List<String> = emptyList(), // ["7", "8", "9", "10", "11", "12"]
    val colors: List<String> = emptyList(), // ["Black", "White", "Blue"]
    val inStock: Boolean = true,
    val isFavorite: Boolean = false,
    val gender: String = "" // Men, Women, Unisex
) {
    fun getPriceAfterDiscount(): Double {
        return if (discount > 0) {
            price - (price * discount / 100)
        } else {
            price
        }
    }
}
