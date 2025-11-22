package com.example.e_commerce_app.data.model

data class ExchangeRateResponse(
    val base: String = "",
    val date: String = "",
    val rates: Map<String, Double> = emptyMap()
)
