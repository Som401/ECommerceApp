package com.example.e_commerce_app.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.e_commerce_app.data.cache.ProductCache
import com.example.e_commerce_app.data.model.Product
import com.example.e_commerce_app.utils.GlobalCurrency
import kotlinx.coroutines.launch

/**
 * ViewModel for ShopFragment
 * Handles product loading, filtering, and currency management
 * Follows MVVM architecture pattern
 */
class ShopViewModel : ViewModel() {
    
    // LiveData for products list
    private val _products = MutableLiveData<List<Product>>()
    val products: LiveData<List<Product>> = _products
    
    // LiveData for filtered products
    private val _filteredProducts = MutableLiveData<List<Product>>()
    val filteredProducts: LiveData<List<Product>> = _filteredProducts
    
    // LiveData for loading state
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    // LiveData for current currency
    private val _currentCurrency = MutableLiveData<String>()
    val currentCurrency: LiveData<String> = _currentCurrency
    
    // Current filter category
    private var currentCategory: String = "All"
    
    init {
        _currentCurrency.value = GlobalCurrency.currentCurrency
        loadProducts()
    }
    
    /**
     * Load products from cache
     */
    fun loadProducts() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val productList = ProductCache.getProducts()
                _products.value = productList
                applyFilter(currentCategory)
            } catch (e: Exception) {
                // Handle error
                _products.value = emptyList()
                _filteredProducts.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Filter products by category
     */
    fun applyFilter(category: String) {
        currentCategory = category
        val allProducts = _products.value ?: emptyList()
        
        _filteredProducts.value = if (category == "All") {
            allProducts
        } else {
            allProducts.filter { it.category.equals(category, ignoreCase = true) }
        }
    }
    
    /**
     * Toggle currency between USD and EUR
     */
    fun toggleCurrency() {
        val newCurrency = if (GlobalCurrency.currentCurrency == "USD") "EUR" else "USD"
        GlobalCurrency.setCurrency(newCurrency)
        _currentCurrency.value = newCurrency
    }
    
    /**
     * Get current currency value
     */
    fun getCurrency(): String = GlobalCurrency.currentCurrency
}
