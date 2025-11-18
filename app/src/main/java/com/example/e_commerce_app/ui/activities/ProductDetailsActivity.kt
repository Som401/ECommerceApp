package com.example.e_commerce_app.ui.activities

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.e_commerce_app.data.model.CartItem
import com.example.e_commerce_app.data.model.Product
import com.example.e_commerce_app.data.repository.ProductRepository
import com.example.e_commerce_app.databinding.ActivityProductDetailsBinding
import com.google.android.material.chip.Chip
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.util.UUID

class ProductDetailsActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityProductDetailsBinding
    private val repository = ProductRepository()
    private val auth = FirebaseAuth.getInstance()
    
    private var product: Product? = null
    private var selectedSize: String? = null
    private var selectedColor: String? = null
    private var isInWishlist = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        val productId = intent.getStringExtra("PRODUCT_ID") ?: run {
            finish()
            return
        }
        
        loadProduct(productId)
        setupClickListeners()
    }
    
    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }
        
        binding.btnAddToCart.setOnClickListener {
            addToCart()
        }
        
        binding.ivWishlist.setOnClickListener {
            toggleWishlist()
        }
    }
    
    private fun loadProduct(productId: String) {
        binding.progressBar.visibility = View.VISIBLE
        
        lifecycleScope.launch {
            try {
                product = repository.getProductById(productId)
                product?.let { displayProduct(it) }
                checkWishlistStatus(productId)
            } catch (e: Exception) {
                Toast.makeText(this@ProductDetailsActivity, "Error loading product", Toast.LENGTH_SHORT).show()
                finish()
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
            tvRating.text = "${product.rating}"
            
            // Setup size chips
            product.size.forEach { size ->
                val chip = Chip(this@ProductDetailsActivity)
                chip.text = size
                chip.isCheckable = true
                chip.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        selectedSize = size
                        // Uncheck other chips
                        for (i in 0 until chipGroupSizes.childCount) {
                            val otherChip = chipGroupSizes.getChildAt(i) as? Chip
                            if (otherChip != chip) {
                                otherChip?.isChecked = false
                            }
                        }
                    }
                }
                chipGroupSizes.addView(chip)
            }
            
            // Setup color chips
            product.colors.forEach { color ->
                val chip = Chip(this@ProductDetailsActivity)
                chip.text = color
                chip.isCheckable = true
                chip.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        selectedColor = color
                        // Uncheck other chips
                        for (i in 0 until chipGroupColors.childCount) {
                            val otherChip = chipGroupColors.getChildAt(i) as? Chip
                            if (otherChip != chip) {
                                otherChip?.isChecked = false
                            }
                        }
                    }
                }
                chipGroupColors.addView(chip)
            }
            
            tvGender.text = "Gender: ${product.gender}"
            tvStock.text = if (product.inStock) "In Stock" else "Out of Stock"
        }
    }
    
    private fun checkWishlistStatus(productId: String) {
        lifecycleScope.launch {
            try {
                isInWishlist = repository.isInWishlist(productId)
                updateWishlistIcon()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    private fun updateWishlistIcon() {
        if (isInWishlist) {
            binding.ivWishlist.setImageResource(com.example.e_commerce_app.R.drawable.ic_favorite)
            ImageViewCompat.setImageTintList(
                binding.ivWishlist,
                android.content.res.ColorStateList.valueOf(
                    ContextCompat.getColor(this, com.example.e_commerce_app.R.color.primary)
                )
            )
        } else {
            binding.ivWishlist.setImageResource(com.example.e_commerce_app.R.drawable.ic_favorite_border)
            ImageViewCompat.setImageTintList(
                binding.ivWishlist,
                android.content.res.ColorStateList.valueOf(
                    ContextCompat.getColor(this, com.example.e_commerce_app.R.color.grayText)
                )
            )
        }
    }
    
    private fun toggleWishlist() {
        val prod = product ?: return
        
        lifecycleScope.launch {
            // Optimistic UI
            val previous = isInWishlist
            isInWishlist = !previous
            updateWishlistIcon()
            try {
                if (previous) {
                    repository.removeFromWishlist(prod.id)
                    Toast.makeText(this@ProductDetailsActivity, "Removed from wishlist", Toast.LENGTH_SHORT).show()
                } else {
                    repository.addToWishlist(prod.id)
                    Toast.makeText(this@ProductDetailsActivity, "Added to wishlist", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                // Revert state
                isInWishlist = previous
                updateWishlistIcon()
                Toast.makeText(this@ProductDetailsActivity, "Error updating wishlist", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun addToCart() {
        val prod = product ?: return
        
        if (selectedSize == null) {
            Toast.makeText(this, "Please select a size", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (selectedColor == null) {
            Toast.makeText(this, "Please select a color", Toast.LENGTH_SHORT).show()
            return
        }
        
        val userId = auth.currentUser?.uid ?: run {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show()
            return
        }
        
        val cartItem = CartItem(
            id = UUID.randomUUID().toString(),
            userId = userId,
            productId = prod.id,
            productName = prod.name,
            price = if (prod.discount > 0) prod.getPriceAfterDiscount() else prod.price,
            selectedSize = selectedSize!!,
            selectedColor = selectedColor!!,
            quantity = 1,
            productImage = prod.imageUrl
        )
        
        lifecycleScope.launch {
            try {
                val success = repository.addToCart(cartItem)
                if (success) {
                    Toast.makeText(this@ProductDetailsActivity, "Added to cart", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@ProductDetailsActivity, "Failed to add to cart", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@ProductDetailsActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
