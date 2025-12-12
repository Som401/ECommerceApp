package com.example.e_commerce_app.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.e_commerce_app.data.cache.CartCache
import com.example.e_commerce_app.data.model.CartItem
import com.example.e_commerce_app.utils.GlobalCurrency
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

/**
 * ViewModel for BagFragment (Shopping Cart)
 * Manages cart items, quantities, and price calculations
 * Follows MVVM architecture pattern
 */
class BagViewModel : ViewModel() {
    
    private val auth = FirebaseAuth.getInstance()
    
    // LiveData for cart items
    private val _cartItems = MutableLiveData<List<CartItem>>()
    val cartItems: LiveData<List<CartItem>> = _cartItems
    
    // LiveData for price calculations
    private val _subtotal = MutableLiveData<Double>()
    val subtotal: LiveData<Double> = _subtotal
    
    private val _shipping = MutableLiveData<Double>()
    val shipping: LiveData<Double> = _shipping
    
    private val _total = MutableLiveData<Double>()
    val total: LiveData<Double> = _total
    
    // LiveData for loading state
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    // LiveData for current currency
    private val _currentCurrency = MutableLiveData<String>()
    val currentCurrency: LiveData<String> = _currentCurrency
    
    init {
        _currentCurrency.value = GlobalCurrency.currentCurrency
        _shipping.value = 10.0
        loadCartItems()
    }
    
    /**
     * Load cart items for current user
     */
    fun loadCartItems() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val items = CartCache.getCartItems(forceRefresh = true)
                _cartItems.value = items
                calculateTotals()
            } catch (e: Exception) {
                _cartItems.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Update item quantity
     */
    fun updateQuantity(cartItemId: String, newQuantity: Int) {
        viewModelScope.launch {
            try {
                CartCache.updateQuantity(cartItemId, newQuantity)
                loadCartItems()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    /**
     * Remove item from cart
     */
    fun removeItem(cartItemId: String) {
        viewModelScope.launch {
            try {
                CartCache.removeFromCart(cartItemId)
                loadCartItems()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    /**
     * Calculate subtotal, shipping, and total
     */
    private fun calculateTotals() {
        val items = _cartItems.value ?: emptyList()
        val subtotalValue = items.sumOf { it.getTotalPrice() }
        val shippingValue = if (items.isEmpty()) 0.0 else 10.0
        val totalValue = subtotalValue + shippingValue
        
        _subtotal.value = subtotalValue
        _shipping.value = shippingValue
        _total.value = totalValue
    }
    
    /**
     * Update currency
     */
    fun updateCurrency() {
        _currentCurrency.value = GlobalCurrency.currentCurrency
    }
}
