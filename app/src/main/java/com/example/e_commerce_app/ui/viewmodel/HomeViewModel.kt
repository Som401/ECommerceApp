package com.example.e_commerce_app.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.e_commerce_app.data.cache.ProductCache
import com.example.e_commerce_app.data.model.Product
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * ViewModel for HomeFragment
 * Manages featured and new arrival products
 * Follows MVVM architecture pattern
 */
class HomeViewModel : ViewModel() {
    
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    
    // LiveData for user name
    private val _userName = MutableLiveData<String>()
    val userName: LiveData<String> = _userName
    
    // LiveData for new products
    private val _newProducts = MutableLiveData<List<Product>>()
    val newProducts: LiveData<List<Product>> = _newProducts
    
    // LiveData for featured products
    private val _featuredProducts = MutableLiveData<List<Product>>()
    val featuredProducts: LiveData<List<Product>> = _featuredProducts
    
    // LiveData for loading state
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    init {
        loadAllData()
    }
    
    /**
     * Load all data (user name and products)
     */
    fun loadAllData() {
        loadUserName()
        loadProducts()
    }
    
    /**
     * Load user name from Firestore
     */
    private fun loadUserName() {
        viewModelScope.launch {
            try {
                val userId = auth.currentUser?.uid
                val firebaseUser = auth.currentUser
                
                val fallbackName = firebaseUser?.displayName
                    ?: firebaseUser?.email?.substringBefore('@')
                    ?: "Shopper"
                
                if (userId != null) {
                    val userDoc = firestore.collection("Users")
                        .document(userId)
                        .get()
                        .await()
                    
                    val resolvedName = if (userDoc.exists()) {
                        userDoc.getString("fullName")
                            ?: userDoc.getString("name")
                            ?: userDoc.getString("username")
                            ?: fallbackName
                    } else {
                        fallbackName
                    }
                    
                    _userName.value = resolvedName
                } else {
                    _userName.value = fallbackName
                }
            } catch (e: Exception) {
                _userName.value = "Shopper"
            }
        }
    }
    
    /**
     * Load products and split into new arrivals and featured
     */
    fun loadProducts() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val allProducts = ProductCache.getProducts()
                
                // Take first 5 as new arrivals
                _newProducts.value = allProducts.take(5)
                
                // Take products with discount as featured
                _featuredProducts.value = allProducts.filter { it.discount > 0 }.take(5)
                
            } catch (e: Exception) {
                _newProducts.value = emptyList()
                _featuredProducts.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }
}
