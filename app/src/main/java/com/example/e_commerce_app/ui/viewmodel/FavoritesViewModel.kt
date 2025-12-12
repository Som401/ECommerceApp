package com.example.e_commerce_app.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.e_commerce_app.data.cache.WishlistCache
import com.example.e_commerce_app.data.model.Product
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

/**
 * ViewModel for FavoritesFragment (Wishlist)
 * Manages wishlist items and operations
 * Follows MVVM architecture pattern
 */
class FavoritesViewModel : ViewModel() {
    
    private val auth = FirebaseAuth.getInstance()
    
    // LiveData for wishlist items
    private val _wishlistItems = MutableLiveData<List<Product>>()
    val wishlistItems: LiveData<List<Product>> = _wishlistItems
    
    // LiveData for loading state
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    // LiveData for empty state
    private val _isEmpty = MutableLiveData<Boolean>()
    val isEmpty: LiveData<Boolean> = _isEmpty
    
    init {
        loadWishlistItems()
    }
    
    /**
     * Load wishlist items for current user
     */
    fun loadWishlistItems() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val items = WishlistCache.getWishlistProducts(forceRefresh = true)
                _wishlistItems.value = items
                _isEmpty.value = items.isEmpty()
            } catch (e: Exception) {
                _wishlistItems.value = emptyList()
                _isEmpty.value = true
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Remove item from wishlist
     */
    fun removeFromWishlist(productId: String) {
        viewModelScope.launch {
            try {
                WishlistCache.removeFromWishlist(productId)
                loadWishlistItems()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    /**
     * Add item to cart from wishlist
     */
    fun moveToCart(product: Product) {
        // This will be handled by the Fragment calling CartCache
        // ViewModel just triggers the reload after
        loadWishlistItems()
    }
}
