package com.example.e_commerce_app.data.cache

import android.util.Log
import com.example.e_commerce_app.data.model.CartItem
import com.example.e_commerce_app.utils.FirebaseManager
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Singleton cache for cart items
 * Fetches once from Firestore, then maintains local array
 * All changes update both local cache and Firebase
 */
object CartCache {
    private const val TAG = "CartCache"
    private val db = FirebaseFirestore.getInstance()
    private val cartRef = db.collection("Cart")
    
    private var cachedCartItems: MutableList<CartItem>? = null
    private var isFetching = false
    
    /**
     * Get all cart items - fetches from Firestore only on first call
     */
    suspend fun getCartItems(forceRefresh: Boolean = false): List<CartItem> {
        val userId = FirebaseManager.currentUser?.uid
        if (userId == null) {
            Log.e(TAG, "User not authenticated")
            return emptyList()
        }
        
        // Return cached data if available
        if (!forceRefresh && cachedCartItems != null) {
            Log.d(TAG, "Returning ${cachedCartItems!!.size} cached cart items")
            return cachedCartItems!!.toList()
        }
        
        // Prevent multiple simultaneous fetches
        if (isFetching) {
            Log.d(TAG, "Already fetching cart, waiting...")
            while (isFetching) {
                kotlinx.coroutines.delay(100)
            }
            return cachedCartItems?.toList() ?: emptyList()
        }
        
        return try {
            isFetching = true
            Log.d(TAG, "Fetching cart items from Firestore for userId=$userId")
            
            val snapshot = cartRef.whereEqualTo("userId", userId).get().await()
            Log.d(TAG, "Query returned ${snapshot.documents.size} cart documents")
            
            val items = snapshot.documents.mapNotNull { doc ->
                try {
                    doc.toObject(CartItem::class.java)?.also {
                        Log.d(TAG, "Parsed cart item: ${it.productName}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing cart item ${doc.id}: ${e.message}")
                    null
                }
            }.toMutableList()
            
            cachedCartItems = items
            Log.d(TAG, "Successfully cached ${items.size} cart items")
            items.toList()
            
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching cart items: ${e.message}", e)
            cachedCartItems?.toList() ?: emptyList()
        } finally {
            isFetching = false
        }
    }
    
    /**
     * Add item to cart - updates both cache and Firebase
     */
    suspend fun addToCart(cartItem: CartItem): Boolean {
        return try {
            Log.d(TAG, "Adding to cart: ${cartItem.productName}")
            
            // Save to Firestore
            cartRef.document(cartItem.id).set(cartItem).await()
            Log.d(TAG, "Successfully saved to Firestore")
            
            // Update local cache
            if (cachedCartItems == null) {
                cachedCartItems = mutableListOf()
            }
            
            // Check if item already exists (same product, size, color)
            val existingIndex = cachedCartItems!!.indexOfFirst {
                it.productId == cartItem.productId && 
                it.selectedSize == cartItem.selectedSize && 
                it.selectedColor == cartItem.selectedColor
            }
            
            if (existingIndex >= 0) {
                // Update quantity
                val existing = cachedCartItems!![existingIndex]
                val updated = existing.copy(
                    quantity = existing.quantity + cartItem.quantity
                )
                cachedCartItems!![existingIndex] = updated
                // Update in Firestore too
                cartRef.document(updated.id).set(updated).await()
                Log.d(TAG, "Updated existing cart item quantity")
            } else {
                // Add new item
                cachedCartItems!!.add(cartItem)
                Log.d(TAG, "Added new cart item to cache")
            }
            
            Log.d(TAG, "Cart now has ${cachedCartItems!!.size} items")
            true
            
        } catch (e: Exception) {
            Log.e(TAG, "Error adding to cart: ${e.message}", e)
            false
        }
    }
    
    /**
     * Update cart item quantity - updates both cache and Firebase
     */
    suspend fun updateQuantity(cartItemId: String, newQuantity: Int): Boolean {
        return try {
            Log.d(TAG, "Updating quantity for item $cartItemId to $newQuantity")
            
            if (cachedCartItems == null) {
                getCartItems() // Load cache first
            }
            
            val index = cachedCartItems?.indexOfFirst { it.id == cartItemId } ?: -1
            if (index >= 0) {
                val item = cachedCartItems!![index]
                val updated = item.copy(
                    quantity = newQuantity
                )
                
                // Update Firestore
                cartRef.document(cartItemId).set(updated).await()
                
                // Update cache
                cachedCartItems!![index] = updated
                Log.d(TAG, "Successfully updated quantity")
                true
            } else {
                Log.e(TAG, "Cart item not found in cache")
                false
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error updating quantity: ${e.message}", e)
            false
        }
    }
    
    /**
     * Remove item from cart - updates both cache and Firebase
     */
    suspend fun removeFromCart(cartItemId: String): Boolean {
        return try {
            Log.d(TAG, "Starting cart item removal - cartItemId: $cartItemId")
            
            // Delete from Firestore
            Log.d(TAG, "Attempting to delete document: $cartItemId from Firestore")
            cartRef.document(cartItemId).delete().await()
            Log.d(TAG, "Successfully deleted from Firestore")
            
            // Remove from cache
            val removed = cachedCartItems?.removeIf { it.id == cartItemId }
            Log.d(TAG, "Cache update - removed: $removed, ${cachedCartItems?.size ?: 0} items remaining")
            
            true
            
        } catch (e: Exception) {
            Log.e(TAG, "Error removing from cart: ${e.message}", e)
            Log.e(TAG, "Stack trace:", e)
            false
        }
    }
    
    /**
     * Clear all cart items - updates both cache and Firebase
     */
    suspend fun clearCart(): Boolean {
        return try {
            val userId = FirebaseManager.currentUser?.uid ?: return false
            Log.d(TAG, "Clearing cart for userId=$userId")
            
            val snapshot = cartRef.whereEqualTo("userId", userId).get().await()
            snapshot.documents.forEach { doc ->
                doc.reference.delete().await()
            }
            
            cachedCartItems?.clear()
            Log.d(TAG, "Cart cleared successfully")
            true
            
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing cart: ${e.message}", e)
            false
        }
    }
    
    /**
     * Get total cart amount
     */
    fun getTotalAmount(): Double {
        return cachedCartItems?.sumOf { it.getTotalPrice() } ?: 0.0
    }
    
    /**
     * Get cart item count
     */
    fun getItemCount(): Int {
        return cachedCartItems?.size ?: 0
    }
    
    /**
     * Clear the cache (useful for logout)
     */
    fun clearCache() {
        Log.d(TAG, "Clearing cart cache")
        cachedCartItems = null
    }
}
