package com.example.e_commerce_app.ui.activities

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.e_commerce_app.R
import com.example.e_commerce_app.data.cache.CartCache
import com.example.e_commerce_app.data.cache.ProductCache
import com.example.e_commerce_app.data.cache.WishlistCache
import com.example.e_commerce_app.data.model.CartItem
import com.example.e_commerce_app.data.model.Product
import com.example.e_commerce_app.databinding.ActivityProductDetailsBinding
import com.example.e_commerce_app.ui.adapters.ProductAdapter
import com.example.e_commerce_app.utils.FirebaseManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.util.UUID

class ProductDetailsActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityProductDetailsBinding
    private val auth = FirebaseAuth.getInstance()
    
    private var product: Product? = null
    private var selectedSize: String? = null
    private var selectedColor: String? = null
    private var isInWishlist = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        val productId = intent.getStringExtra("PRODUCT_ID") ?: ""
        if (productId.isEmpty()) {
            android.util.Log.w("ProductDebug", "Missing PRODUCT_ID extra; will display fallback extras only")
        }
        // Display fallback immediately to avoid mock placeholders
        displayFallbackIfPresent()
        
        loadProduct(productId)
        setupClickListeners()
    }
    
    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }
        
        binding.btnAddToCart.setOnClickListener {
            addToCartDirectly()
        }
        
        binding.ivWishlist.setOnClickListener {
            toggleWishlist()
        }
    }
    
    private fun loadProduct(productId: String) {
        binding.progressBar.visibility = View.VISIBLE
        
        lifecycleScope.launch {
            try {
                // Use ProductCache - fetches only once
                product = ProductCache.getProductById(productId)
                if (product != null) {
                    displayProduct(product!!)
                } else {
                    android.util.Log.w("ProductDebug", "Product not found for id=$productId")
                }
                checkWishlistStatus(productId)
            } catch (e: Exception) {
                android.util.Log.e("ProductDebug", "Error loading product: ${e.message}", e)
                Toast.makeText(this@ProductDetailsActivity, "Error loading product", Toast.LENGTH_SHORT).show()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }
    
    private fun displayProduct(product: Product) {
        binding.apply {
            // Load main image
            if (product.imageUrl.isNotEmpty()) {
                Glide.with(ivProductImage.context)
                    .load(product.imageUrl)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_report_image)
                    .into(ivProductImage)
            }
            tvProductName.text = product.name
            tvProductBrand.text = product.brand
            tvProductDescription.text = product.description
            
            if (product.discount > 0) {
                tvProductPrice.text = "$${product.getPriceAfterDiscount()}"
                tvOriginalPrice.text = "$${product.price}"
                tvOriginalPrice.visibility = View.VISIBLE
                tvDiscount.text = "${product.discount}% OFF"
                tvDiscount.visibility = View.VISIBLE
            } else {
                tvProductPrice.text = "$${product.price}"
                tvOriginalPrice.visibility = View.GONE
                tvDiscount.visibility = View.GONE
            }
            
            ratingBar.rating = product.rating
            tvRating.text = "(${product.rating})"
            
            // Setup size chips
            chipGroupSizes.removeAllViews()
            product.size.forEach { size ->
                val chip = Chip(this@ProductDetailsActivity)
                chip.text = size
                chip.isCheckable = true
                chip.setOnClickListener {
                    selectedSize = size
                    android.util.Log.d("ProductDebug", "Selected size: $size")
                }
                chipGroupSizes.addView(chip)
            }
            
            // Setup color chips
            chipGroupColors.removeAllViews()
            product.colors.forEach { color ->
                val chip = Chip(this@ProductDetailsActivity)
                chip.text = color
                chip.isCheckable = true
                chip.setOnClickListener {
                    selectedColor = color
                    android.util.Log.d("ProductDebug", "Selected color: $color")
                }
                chipGroupColors.addView(chip)
            }
            
            tvGender.text = "Gender: ${product.gender}"
            tvStock.text = if (product.inStock) "In Stock (${product.stock})" else "Out of Stock"
        }
    }

    private fun displayFallbackIfPresent() {
        val name = intent.getStringExtra("PRODUCT_NAME_FALLBACK")
        if (name != null) {
            binding.tvProductName.text = name
            binding.tvProductBrand.text = intent.getStringExtra("PRODUCT_BRAND_FALLBACK") ?: ""
            binding.tvProductDescription.text = intent.getStringExtra("PRODUCT_DESCRIPTION_FALLBACK") ?: ""
            val image = intent.getStringExtra("PRODUCT_IMAGE_FALLBACK")
            if (!image.isNullOrEmpty()) {
                Glide.with(binding.ivProductImage.context)
                    .load(image)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_report_image)
                    .into(binding.ivProductImage)
            }
            val price = intent.getDoubleExtra("PRODUCT_PRICE_FALLBACK", 0.0)
            val discount = intent.getIntExtra("PRODUCT_DISCOUNT_FALLBACK", 0)
            if (discount > 0) {
                binding.tvProductPrice.text = "$${price - (price * discount / 100)}"
                binding.tvOriginalPrice.text = "$${price}"
                binding.tvOriginalPrice.visibility = View.VISIBLE
                binding.tvDiscount.text = "${discount}% OFF"
                binding.tvDiscount.visibility = View.VISIBLE
            } else {
                binding.tvProductPrice.text = "$${price}"
                binding.tvOriginalPrice.visibility = View.GONE
                binding.tvDiscount.visibility = View.GONE
            }
            val stock = intent.getIntExtra("PRODUCT_STOCK_FALLBACK", 0)
            binding.tvStock.text = if (stock > 0) "In Stock (${stock})" else "Out of Stock"
            val gender = intent.getStringExtra("PRODUCT_GENDER_FALLBACK") ?: ""
            if (gender.isNotEmpty()) binding.tvGender.text = "Gender: ${gender}" else binding.tvGender.text = ""
        }
    }
    
    private fun checkWishlistStatus(productId: String) {
        lifecycleScope.launch {
            try {
                // Use WishlistCache to check status
                isInWishlist = WishlistCache.isInWishlist(productId)
                updateWishlistIcon()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    private fun updateWishlistIcon() {
        if (isInWishlist) {
            binding.ivWishlist.setImageResource(R.drawable.ic_favorite)
            ImageViewCompat.setImageTintList(
                binding.ivWishlist,
                ColorStateList.valueOf(ContextCompat.getColor(this, R.color.primary))
            )
        } else {
            binding.ivWishlist.setImageResource(R.drawable.ic_favorite_border)
            ImageViewCompat.setImageTintList(
                binding.ivWishlist,
                ColorStateList.valueOf(ContextCompat.getColor(this, R.color.grayText))
            )
        }
    }
    
    private fun toggleWishlist() {
        val prod = product ?: return
        
        android.util.Log.d("ProductDebug", "Toggle wishlist for product: ${prod.id}, current state: $isInWishlist")
        
        lifecycleScope.launch {
            // Optimistic UI
            val previous = isInWishlist
            isInWishlist = !previous
            updateWishlistIcon()
            
            try {
                val success = if (previous) {
                    android.util.Log.d("ProductDebug", "Removing from wishlist...")
                    // Use WishlistCache - updates both cache and Firebase
                    val result = WishlistCache.removeFromWishlist(prod.id)
                    android.util.Log.d("ProductDebug", "Remove from wishlist returned: $result")
                    result
                } else {
                    android.util.Log.d("ProductDebug", "Adding to wishlist...")
                    // Use WishlistCache - updates both cache and Firebase
                    val result = WishlistCache.addToWishlist(prod.id)
                    android.util.Log.d("ProductDebug", "Add to wishlist returned: $result")
                    result
                }
                
                if (success) {
                    if (previous) {
                        Toast.makeText(this@ProductDetailsActivity, "Removed from wishlist", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@ProductDetailsActivity, "Added to wishlist", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Revert on failure
                    android.util.Log.e("ProductDebug", "Wishlist operation failed, reverting UI")
                    isInWishlist = previous
                    updateWishlistIcon()
                    Toast.makeText(this@ProductDetailsActivity, "Failed to update wishlist", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                // Revert state
                android.util.Log.e("ProductDebug", "Exception in toggleWishlist: ${e.message}", e)
                isInWishlist = previous
                updateWishlistIcon()
                Toast.makeText(this@ProductDetailsActivity, "Error updating wishlist", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun addToCartDirectly() {
        val prod = product ?: run {
            Toast.makeText(this, "Product not loaded", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (selectedSize == null) {
            Toast.makeText(this, "Please select a size", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (selectedColor == null) {
            Toast.makeText(this, "Please select a color", Toast.LENGTH_SHORT).show()
            return
        }
        
        val userId = auth.currentUser?.uid
        android.util.Log.d("ProductDebug", "Current user ID: $userId")
        
        if (userId == null) {
            android.util.Log.e("ProductDebug", "User not logged in!")
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show()
            return
        }
        
        val quantity = 1
        val cartItem = CartItem(
            id = UUID.randomUUID().toString(),
            userId = userId,
            productId = prod.id,
            productName = prod.name,
            price = if (prod.discount > 0) prod.getPriceAfterDiscount() else prod.price,
            selectedSize = selectedSize!!,
            selectedColor = selectedColor!!,
            quantity = quantity,
            productImage = prod.imageUrl
        )
        
        android.util.Log.d("ProductDebug", "Cart item to add: $cartItem")
        
        lifecycleScope.launch {
            try {
                android.util.Log.d("ProductDebug", "Calling CartCache.addToCart()...")
                // Use CartCache - updates both cache and Firebase
                val success = CartCache.addToCart(cartItem)
                android.util.Log.d("ProductDebug", "addToCart returned: $success")
                
                if (success) {
                    Toast.makeText(this@ProductDetailsActivity, "Added to cart", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this@ProductDetailsActivity, "Failed to add to cart", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                android.util.Log.e("ProductDebug", "Error adding to cart: ${e.message}", e)
                Toast.makeText(this@ProductDetailsActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
