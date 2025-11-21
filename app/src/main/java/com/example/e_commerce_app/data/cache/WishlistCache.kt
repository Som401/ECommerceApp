package com.example.e_commerce_app.data.cache

import android.util.Log
import com.example.e_commerce_app.data.model.Product
import com.example.e_commerce_app.data.model.WishlistItem
import com.example.e_commerce_app.utils.FirebaseManager
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Singleton cache for wishlist items
 * Fetches once from Firestore, then maintains local array
 * All changes update both local cache and Firebase
 */
object WishlistCache {
    private const val TAG = "WishlistCache"
    private val db = FirebaseFirestore.getInstance()
    private val wishlistRef = db.collection("Wishlist")
    
    private var cachedProductIds: MutableSet<String>? = null
    private var cachedProducts: MutableList<Product>? = null
    private var isFetching = false
    
    /**
     * Get all wishlist product IDs - fetches from Firestore only on first call
     */
    suspend fun getWishlistProductIds(forceRefresh: Boolean = false): Set<String> {
        val userId = FirebaseManager.currentUser?.uid
        if (userId == null) {
            Log.e(TAG, "User not authenticated")
            return emptySet()
        }
        
        // Return cached data if available
        if (!forceRefresh && cachedProductIds != null) {
            Log.d(TAG, "Returning ${cachedProductIds!!.size} cached wishlist IDs")
            return cachedProductIds!!.toSet()
        }
        
        // Prevent multiple simultaneous fetches
        if (isFetching) {
            Log.d(TAG, "Already fetching wishlist, waiting...")
            while (isFetching) {
                kotlinx.coroutines.delay(100)
            }
            return cachedProductIds?.toSet() ?: emptySet()
        }
        
        return try {
            isFetching = true
            Log.d(TAG, "Fetching wishlist items from Firestore for userId=$userId")
            
            val snapshot = wishlistRef.whereEqualTo("userId", userId).get().await()
            Log.d(TAG, "Query returned ${snapshot.documents.size} wishlist documents")
            
            val productIds = snapshot.documents.mapNotNull { doc ->
                try {
                    doc.toObject(WishlistItem::class.java)?.productId?.also {
                        Log.d(TAG, "Found wishlist item: $it")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing wishlist item ${doc.id}: ${e.message}")
                    null
                }
            }.toMutableSet()
            
            cachedProductIds = productIds
            Log.d(TAG, "Successfully cached ${productIds.size} wishlist product IDs")
            productIds.toSet()
            
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching wishlist items: ${e.message}", e)
            cachedProductIds?.toSet() ?: emptySet()
        } finally {
            isFetching = false
        }
    }
    
    /**
     * Get all wishlist products (full Product objects)
     */
    suspend fun getWishlistProducts(forceRefresh: Boolean = false): List<Product> {
        if (!forceRefresh && cachedProducts != null) {
            Log.d(TAG, "Returning ${cachedProducts!!.size} cached wishlist products")
            return cachedProducts!!.toList()
        }
        
        return try {
            val productIds = getWishlistProductIds(forceRefresh)
            Log.d(TAG, "Fetching ${productIds.size} products for wishlist")
            
            val products = productIds.mapNotNull { productId ->
                ProductCache.getProductById(productId)?.also {
                    Log.d(TAG, "Loaded product: ${it.name}")
                }
            }.toMutableList()
            
            cachedProducts = products
            Log.d(TAG, "Successfully cached ${products.size} wishlist products")
            products.toList()
            
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching wishlist products: ${e.message}", e)
            cachedProducts?.toList() ?: emptyList()
        }
    }
    
    /**
     * Add product to wishlist - updates both cache and Firebase
     */
    suspend fun addToWishlist(productId: String): Boolean {
        return try {
            val userId = FirebaseManager.currentUser?.uid
            if (userId == null) {
                Log.e(TAG, "User not authenticated")
                return false
            }
            
            Log.d(TAG, "Adding to wishlist: productId=$productId")
            
            val wishlistItem = WishlistItem(
                id = "${userId}_${productId}",
                productId = productId,
                userId = userId,
                addedAt = System.currentTimeMillis()
            )
            
            // Save to Firestore
            wishlistRef.document(wishlistItem.id).set(wishlistItem).await()
            Log.d(TAG, "Successfully saved to Firestore")
            
            // Update local cache
            if (cachedProductIds == null) {
                cachedProductIds = mutableSetOf()
            }
            cachedProductIds!!.add(productId)
            
            // Add to products cache if available
            if (cachedProducts != null) {
                val product = ProductCache.getProductById(productId)
                if (product != null && cachedProducts!!.none { it.id == productId }) {
                    cachedProducts!!.add(product)
                }
            }
            
            Log.d(TAG, "Added to wishlist cache, now has ${cachedProductIds!!.size} items")
            true
            
        } catch (e: Exception) {
            Log.e(TAG, "Error adding to wishlist: ${e.message}", e)
            false
        }
    }
    
    /**
     * Remove product from wishlist - updates both cache and Firebase
     */
    suspend fun removeFromWishlist(productId: String): Boolean {
        return try {
            val userId = FirebaseManager.currentUser?.uid
            if (userId == null) {
                Log.e(TAG, "User not authenticated, cannot remove from wishlist")
                return false
            }
            
            val documentId = "${userId}_${productId}"
            Log.d(TAG, "Starting removal - productId: $productId, documentId: $documentId, userId: $userId")
            
            // Delete from Firestore
            Log.d(TAG, "Attempting to delete document: $documentId from Firestore")
            wishlistRef.document(documentId).delete().await()
            Log.d(TAG, "Successfully deleted from Firestore")
            
            // Remove from cache
            val removedFromIds = cachedProductIds?.remove(productId)
            val removedFromProducts = cachedProducts?.removeIf { it.id == productId }
            
            Log.d(TAG, "Cache update - removed from IDs: $removedFromIds, removed from products: $removedFromProducts")
            Log.d(TAG, "Removed from cache, ${cachedProductIds?.size ?: 0} items remaining")
            true
            
        } catch (e: Exception) {
            Log.e(TAG, "Error removing from wishlist: ${e.message}", e)
            Log.e(TAG, "Stack trace:", e)
            false
        }
    }
    
    /**
     * Check if product is in wishlist
     */
    suspend fun isInWishlist(productId: String): Boolean {
        if (cachedProductIds == null) {
            getWishlistProductIds()
        }
        return cachedProductIds?.contains(productId) ?: false
    }
    
    /**
     * Get wishlist item count
     */
    fun getItemCount(): Int {
        return cachedProductIds?.size ?: 0
    }
    
    /**
     * Clear the cache (useful for logout)
     */
    fun clearCache() {
        Log.d(TAG, "Clearing wishlist cache")
        cachedProductIds = null
        cachedProducts = null
    }
}
