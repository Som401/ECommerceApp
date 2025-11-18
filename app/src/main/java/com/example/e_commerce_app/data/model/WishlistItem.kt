package com.example.e_commerce_app.data.model

data class WishlistItem(
    val id: String = "",
    val productId: String = "",
    val userId: String = "",
    val addedAt: Long = System.currentTimeMillis()
)
