package com.example.e_commerce_app.utils

/**
 * Global currency state manager with observer pattern
 */
object GlobalCurrency {
    private val listeners = mutableListOf<(String) -> Unit>()
    
    var currentCurrency: String = "USD"
        private set
    
    fun setCurrency(currency: String) {
        if (currentCurrency != currency) {
            currentCurrency = currency
            notifyListeners()
        }
    }
    
    fun addListener(listener: (String) -> Unit) {
        listeners.add(listener)
    }
    
    fun removeListener(listener: (String) -> Unit) {
        listeners.remove(listener)
    }
    
    private fun notifyListeners() {
        listeners.forEach { it(currentCurrency) }
    }
}
