package com.example.e_commerce_app.data.model

data class Address(
    val fullName: String = "",
    val phoneNumber: String = "",
    val addressLine1: String = "",
    val addressLine2: String = "",
    val city: String = "",
    val zipCode: String = ""
) {
    fun getFullAddress(): String {
        return buildString {
            append(addressLine1)
            if (addressLine2.isNotEmpty()) {
                append(", $addressLine2")
            }
            append(", $city $zipCode")
        }
    }
}
