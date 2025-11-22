package com.example.e_commerce_app.utils

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object CurrencyPreference {
    private const val TAG = "CurrencyPreference"
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    private var cachedCurrency: String? = null
    
    /**
     * Get user's preferred currency
     */
    suspend fun getUserCurrency(): String {
        // Return cached value if available
        if (cachedCurrency != null) {
            return cachedCurrency!!
        }
        
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Log.d(TAG, "No user logged in, using USD")
            return "USD"
        }
        
        return try {
            val doc = db.collection("Users").document(userId).get().await()
            val currency = doc.getString("preferredCurrency") ?: "USD"
            cachedCurrency = currency
            Log.d(TAG, "User currency: $currency")
            currency
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user currency: ${e.message}")
            "USD"
        }
    }
    
    /**
     * Save user's preferred currency
     */
    suspend fun saveUserCurrency(currency: String): Boolean {
        val userId = auth.currentUser?.uid ?: return false
        
        return try {
            db.collection("Users")
                .document(userId)
                .update("preferredCurrency", currency)
                .await()
            
            cachedCurrency = currency
            Log.d(TAG, "Currency preference saved: $currency")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error saving currency preference: ${e.message}")
            false
        }
    }
    
    /**
     * Clear cached currency (useful on logout)
     */
    fun clearCache() {
        cachedCurrency = null
    }
}
