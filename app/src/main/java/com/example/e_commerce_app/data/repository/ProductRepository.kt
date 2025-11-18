package com.example.e_commerce_app.data.repository

import com.example.e_commerce_app.data.model.Product
import com.example.e_commerce_app.data.model.CartItem
import com.example.e_commerce_app.data.model.WishlistItem
import com.example.e_commerce_app.utils.FirebaseManager
import kotlinx.coroutines.tasks.await

class ProductRepository {
    
    private val productsRef = FirebaseManager.firestore.collection("Products")
    private val cartRef = FirebaseManager.firestore.collection("Cart")
    private val wishlistRef = FirebaseManager.firestore.collection("Wishlist")
    
    // Product Operations
    suspend fun getAllProducts(): List<Product> {
        return try {
            val snapshot = productsRef.get().await()
            snapshot.documents.mapNotNull { it.toObject(Product::class.java) }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    suspend fun getProductsByCategory(category: String): List<Product> {
        return try {
            val snapshot = productsRef
                .whereEqualTo("category", category)
                .get()
                .await()
            snapshot.documents.mapNotNull { it.toObject(Product::class.java) }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    suspend fun getProductById(id: String): Product? {
        return try {
            val snapshot = productsRef.document(id).get().await()
            snapshot.toObject(Product::class.java)
        } catch (e: Exception) {
            null
        }
    }
    
    // Cart Operations
    suspend fun addToCart(cartItem: CartItem): Boolean {
        return try {
            val userId = FirebaseManager.currentUser?.uid ?: return false
            val itemWithUser = cartItem.copy(userId = userId)
            cartRef.document(cartItem.id).set(itemWithUser).await()
            true
        } catch (e: Exception) {
            false
        }
    }
    
    suspend fun getCartItems(): List<CartItem> {
        return try {
            val userId = FirebaseManager.currentUser?.uid ?: return emptyList()
            val snapshot = cartRef
                .whereEqualTo("userId", userId)
                .get()
                .await()
            snapshot.documents.mapNotNull { it.toObject(CartItem::class.java) }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    suspend fun updateCartItemQuantity(itemId: String, quantity: Int): Boolean {
        return try {
            cartRef.document(itemId)
                .update("quantity", quantity)
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }
    
    suspend fun removeFromCart(itemId: String): Boolean {
        return try {
            cartRef.document(itemId).delete().await()
            true
        } catch (e: Exception) {
            false
        }
    }
    
    suspend fun clearCart(): Boolean {
        return try {
            val userId = FirebaseManager.currentUser?.uid ?: return false
            val snapshot = cartRef.whereEqualTo("userId", userId).get().await()
            snapshot.documents.forEach { it.reference.delete() }
            true
        } catch (e: Exception) {
            false
        }
    }
    
    // Wishlist Operations
    suspend fun addToWishlist(productId: String): Boolean {
        return try {
            val userId = FirebaseManager.currentUser?.uid ?: return false
            val wishlistItem = WishlistItem(
                id = "${userId}_$productId",
                productId = productId,
                userId = userId
            )
            wishlistRef.document(wishlistItem.id).set(wishlistItem).await()
            true
        } catch (e: Exception) {
            false
        }
    }
    
    suspend fun removeFromWishlist(productId: String): Boolean {
        return try {
            val userId = FirebaseManager.currentUser?.uid ?: return false
            val id = "${userId}_$productId"
            wishlistRef.document(id).delete().await()
            true
        } catch (e: Exception) {
            false
        }
    }
    
    suspend fun getWishlistItems(): List<String> {
        return try {
            val userId = FirebaseManager.currentUser?.uid ?: return emptyList()
            val snapshot = wishlistRef
                .whereEqualTo("userId", userId)
                .get()
                .await()
            snapshot.documents.mapNotNull { 
                it.toObject(WishlistItem::class.java)?.productId 
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    suspend fun isInWishlist(productId: String): Boolean {
        return try {
            val userId = FirebaseManager.currentUser?.uid ?: return false
            val id = "${userId}_$productId"
            val doc = wishlistRef.document(id).get().await()
            doc.exists()
        } catch (e: Exception) {
            false
        }
    }
}
