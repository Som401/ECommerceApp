package com.example.e_commerce_app.data.model

data class Order(
    val id: String = "",
    val userId: String = "",
    val items: List<OrderItem> = emptyList(),
    val shippingAddress: Address = Address(),
    val paymentMethod: String = "", // "Credit Card", "Debit Card", etc.
    val cardLastFourDigits: String = "",
    val subtotal: Double = 0.0,
    val shippingCost: Double = 0.0,
    val total: Double = 0.0,
    val orderDate: Long = System.currentTimeMillis(),
    val status: String = "Pending" // Pending, Processing, Shipped, Delivered, Cancelled
) {
    fun getFormattedDate(): String {
        val sdf = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(orderDate))
    }
}

data class OrderItem(
    val productId: String = "",
    val productName: String = "",
    val productImage: String = "",
    val price: Double = 0.0,
    val selectedSize: String = "",
    val selectedColor: String = "",
    val quantity: Int = 1
) {
    fun getTotalPrice(): Double = price * quantity
}
