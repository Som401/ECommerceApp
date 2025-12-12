package com.example.e_commerce_app.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * ViewModel for ProfileFragment
 * Manages user profile data and statistics
 * Follows MVVM architecture pattern
 */
class ProfileViewModel : ViewModel() {
    
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    
    // LiveData for user information
    private val _userName = MutableLiveData<String>()
    val userName: LiveData<String> = _userName
    
    private val _userEmail = MutableLiveData<String>()
    val userEmail: LiveData<String> = _userEmail
    
    private val _photoUrl = MutableLiveData<String?>()
    val photoUrl: LiveData<String?> = _photoUrl
    
    // LiveData for statistics
    private val _purchasesCount = MutableLiveData<Int>()
    val purchasesCount: LiveData<Int> = _purchasesCount
    
    private val _wishlistCount = MutableLiveData<Int>()
    val wishlistCount: LiveData<Int> = _wishlistCount
    
    private val _cartCount = MutableLiveData<Int>()
    val cartCount: LiveData<Int> = _cartCount
    
    // LiveData for loading state
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    init {
        // Don't auto-load in init, let Fragment control when to load
    }
    
    /**
     * Load user profile from Firestore
     */
    fun loadUserProfile() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val userId = auth.currentUser?.uid
                val userEmail = auth.currentUser?.email
                val displayName = auth.currentUser?.displayName
                
                _userEmail.value = userEmail ?: ""
                
                if (userId != null) {
                    val userDoc = firestore.collection("Users")
                        .document(userId)
                        .get()
                        .await()
                    
                    val userName = userDoc.getString("fullName")
                        ?: userDoc.getString("name")
                        ?: displayName
                        ?: userEmail?.substringBefore('@')
                        ?: "User"
                    
                    val photoUrl = userDoc.getString("photoUrl")
                    
                    _userName.value = userName
                    _photoUrl.value = photoUrl
                } else {
                    _userName.value = displayName ?: userEmail?.substringBefore('@') ?: "User"
                }
            } catch (e: Exception) {
                _userName.value = auth.currentUser?.displayName ?: "User"
                _userEmail.value = auth.currentUser?.email ?: ""
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Load user statistics (purchases, wishlist, cart counts)
     */
    fun loadUserStats() {
        val userId = auth.currentUser?.uid ?: return
        
        viewModelScope.launch {
            try {
                android.util.Log.d("ProfileViewModel", "Loading stats for userId: $userId")
                
                // Load purchases count
                val ordersSnapshot = firestore.collection("CompletedOrders")
                    .whereEqualTo("userId", userId)
                    .get()
                    .await()
                val ordersCount = ordersSnapshot.documents.size
                android.util.Log.d("ProfileViewModel", "Orders count: $ordersCount")
                _purchasesCount.value = ordersCount
                
                // Load wishlist count - stored in root Wishlist collection
                val wishlistSnapshot = firestore.collection("Wishlist")
                    .whereEqualTo("userId", userId)
                    .get()
                    .await()
                val wishlistCountValue = wishlistSnapshot.documents.size
                android.util.Log.d("ProfileViewModel", "Wishlist count: $wishlistCountValue")
                _wishlistCount.value = wishlistCountValue
                
                // Load cart count - stored in root Cart collection
                val cartSnapshot = firestore.collection("Cart")
                    .whereEqualTo("userId", userId)
                    .get()
                    .await()
                val cartCountValue = cartSnapshot.documents.size
                android.util.Log.d("ProfileViewModel", "Cart count: $cartCountValue")
                _cartCount.value = cartCountValue
                
            } catch (e: Exception) {
                android.util.Log.e("ProfileViewModel", "Error loading stats: ${e.message}", e)
                _purchasesCount.value = 0
                _wishlistCount.value = 0
                _cartCount.value = 0
            }
        }
    }
    
    /**
     * Update photo URL after upload
     */
    fun updatePhotoUrl(url: String) {
        _photoUrl.value = url
    }
}
