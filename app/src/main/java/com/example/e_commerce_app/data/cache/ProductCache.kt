package com.example.e_commerce_app.data.cache

import android.util.Log
import com.example.e_commerce_app.data.model.Product
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Singleton cache for products to avoid repeated Firestore fetches
 * Fetches products once and stores them in memory
 */
object ProductCache {
    private const val TAG = "ProductCache"
    private val db = FirebaseFirestore.getInstance()
    private val productsRef = db.collection("Products")
    
    private var cachedProducts: List<Product>? = null
    private var isFetching = false
    
    /**
     * Get all products - fetches from Firestore only on first call
     * Subsequent calls return cached data
     */
    suspend fun getProducts(forceRefresh: Boolean = false): List<Product> {
        // Return cached data if available and not forcing refresh
        if (!forceRefresh && cachedProducts != null) {
            Log.d(TAG, "Returning ${cachedProducts!!.size} cached products")
            return cachedProducts!!
        }
        
        // Prevent multiple simultaneous fetches
        if (isFetching) {
            Log.d(TAG, "Already fetching products, waiting...")
            while (isFetching) {
                kotlinx.coroutines.delay(100)
            }
            return cachedProducts ?: emptyList()
        }
        
        return try {
            isFetching = true
            Log.d(TAG, "Fetching products from Firestore...")
            
            val snapshot = productsRef.get().await()
            val products = snapshot.documents.mapNotNull { doc ->
                try {
                    doc.toObject(Product::class.java)?.copy(id = doc.id)
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing product ${doc.id}: ${e.message}")
                    null
                }
            }
            
            cachedProducts = products
            Log.d(TAG, "Successfully cached ${products.size} products")
            products
            
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching products: ${e.message}", e)
            cachedProducts ?: emptyList()
        } finally {
            isFetching = false
        }
    }
    
    /**
     * Get a specific product by ID from cache
     */
    suspend fun getProductById(productId: String): Product? {
        val products = getProducts()
        return products.find { it.id == productId }
    }
    
    /**
     * Clear the cache (useful for logout or force refresh)
     */
    fun clearCache() {
        Log.d(TAG, "Clearing product cache")
        cachedProducts = null
    }
}
