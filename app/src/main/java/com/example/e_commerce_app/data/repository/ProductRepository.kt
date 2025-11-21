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
            snapshot.documents.mapNotNull { doc ->
                val prod = doc.toObject(Product::class.java)?.copy(id = doc.id)
                if (prod != null) {
                    android.util.Log.d("ProductDebug", "Fetched product docId=${doc.id} name=${prod.name} price=${prod.price} stock=${prod.stock}")
                } else {
                    android.util.Log.w("ProductDebug", "Null product for docId=${doc.id}")
                }
                prod
            }
        } catch (e: Exception) {
            android.util.Log.e("ProductDebug", "getAllProducts error: ${e.message}")
            emptyList()
        }
    }
    
    suspend fun getProductsByCategory(category: String): List<Product> {
        return try {
            val snapshot = productsRef
                .whereEqualTo("category", category)
                .get()
                .await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Product::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    suspend fun getProductById(id: String): Product? {
        return try {
            val snapshot = productsRef.document(id).get().await()
            if (snapshot.exists()) {
                val prod = snapshot.toObject(Product::class.java)?.copy(id = snapshot.id)
                android.util.Log.d("ProductDebug", "Loaded by docId id=${id} name=${prod?.name}")
                prod
            } else {
                // fallback: query by field id if documents stored with random IDs
                val query = productsRef.whereEqualTo("id", id).get().await()
                val first = query.documents.firstOrNull()
                val prod = first?.toObject(Product::class.java)?.copy(id = first.id)
                android.util.Log.d("ProductDebug", "Loaded by field id=${id} name=${prod?.name}")
                prod
            }
        } catch (e: Exception) {
            android.util.Log.e("ProductDebug", "getProductById error id=${id}: ${e.message}")
            null
        }
    }
    
    // Cart Operations
    suspend fun addToCart(cartItem: CartItem): Boolean {
        return try {
            val userId = FirebaseManager.currentUser?.uid
            android.util.Log.d("CartDebug", "addToCart called - userId=$userId, cartItem=$cartItem")
            
            if (userId == null) {
                android.util.Log.e("CartDebug", "No user logged in!")
                return false
            }
            
            val itemWithUser = cartItem.copy(userId = userId)
            android.util.Log.d("CartDebug", "Saving cart item to Firestore: ${itemWithUser}")
            
            cartRef.document(cartItem.id).set(itemWithUser).await()
            android.util.Log.d("CartDebug", "Successfully added to cart!")
            true
        } catch (e: Exception) {
            android.util.Log.e("CartDebug", "Error adding to cart: ${e.message}", e)
            e.printStackTrace()
            false
        }
    }
    
    suspend fun getCartItems(): List<CartItem> {
        return try {
            val userId = FirebaseManager.currentUser?.uid
            android.util.Log.d("CartDebug", "getCartItems - userId=$userId")
            
            if (userId == null) {
                android.util.Log.e("CartDebug", "getCartItems - No user logged in!")
                return emptyList()
            }
            
            android.util.Log.d("CartDebug", "Querying cart items for userId=$userId")
            val snapshot = cartRef
                .whereEqualTo("userId", userId)
                .get()
                .await()
            
            android.util.Log.d("CartDebug", "Query returned ${snapshot.documents.size} documents")
            val items = snapshot.documents.mapNotNull { 
                val item = it.toObject(CartItem::class.java)
                android.util.Log.d("CartDebug", "Parsed cart item: $item")
                item
            }
            android.util.Log.d("CartDebug", "Returning ${items.size} cart items")
            items
        } catch (e: Exception) {
            android.util.Log.e("CartDebug", "Error getting cart items: ${e.message}", e)
            e.printStackTrace()
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
            val userId = FirebaseManager.currentUser?.uid
            android.util.Log.d("WishlistDebug", "addToWishlist called - userId=$userId, productId=$productId")
            
            if (userId == null) {
                android.util.Log.e("WishlistDebug", "No user logged in!")
                return false
            }
            
            val wishlistItem = WishlistItem(
                id = "${userId}_$productId",
                productId = productId,
                userId = userId
            )
            android.util.Log.d("WishlistDebug", "Saving wishlist item: $wishlistItem")
            
            wishlistRef.document(wishlistItem.id).set(wishlistItem).await()
            android.util.Log.d("WishlistDebug", "Successfully added to wishlist!")
            true
        } catch (e: Exception) {
            android.util.Log.e("WishlistDebug", "Error adding to wishlist: ${e.message}", e)
            e.printStackTrace()
            false
        }
    }
    
    suspend fun removeFromWishlist(productId: String): Boolean {
        return try {
            val userId = FirebaseManager.currentUser?.uid
            android.util.Log.d("WishlistDebug", "removeFromWishlist called - userId=$userId, productId=$productId")
            
            if (userId == null) {
                android.util.Log.e("WishlistDebug", "No user logged in!")
                return false
            }
            
            val id = "${userId}_$productId"
            android.util.Log.d("WishlistDebug", "Deleting wishlist item with id: $id")
            
            wishlistRef.document(id).delete().await()
            android.util.Log.d("WishlistDebug", "Successfully removed from wishlist!")
            true
        } catch (e: Exception) {
            android.util.Log.e("WishlistDebug", "Error removing from wishlist: ${e.message}", e)
            e.printStackTrace()
            false
        }
    }
    
    suspend fun getWishlistItems(): List<String> {
        return try {
            val userId = FirebaseManager.currentUser?.uid
            android.util.Log.d("WishlistDebug", "getWishlistItems - userId=$userId")
            
            if (userId == null) {
                android.util.Log.e("WishlistDebug", "getWishlistItems - No user logged in!")
                return emptyList()
            }
            
            android.util.Log.d("WishlistDebug", "Querying wishlist items for userId=$userId")
            val snapshot = wishlistRef
                .whereEqualTo("userId", userId)
                .get()
                .await()
            
            android.util.Log.d("WishlistDebug", "Query returned ${snapshot.documents.size} documents")
            val productIds = snapshot.documents.mapNotNull { 
                val item = it.toObject(WishlistItem::class.java)
                android.util.Log.d("WishlistDebug", "Parsed wishlist item: $item")
                item?.productId
            }
            android.util.Log.d("WishlistDebug", "Returning ${productIds.size} product IDs")
            productIds
        } catch (e: Exception) {
            android.util.Log.e("WishlistDebug", "Error getting wishlist items: ${e.message}", e)
            e.printStackTrace()
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
