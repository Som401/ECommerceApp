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
import com.example.e_commerce_app.data.model.CartItem
import com.example.e_commerce_app.data.model.Product
import com.example.e_commerce_app.data.repository.ProductRepository
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
                product = repository.getProductById(productId)
                if (product != null) {
                    displayProduct(product!!)
                }
                checkWishlistStatus(productId)
            } catch (e: Exception) {
                Toast.makeText(this@ProductDetailsActivity, "Error loading product", Toast.LENGTH_SHORT).show()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }
    
    private fun loadRecommendedProducts() {
        lifecycleScope.launch {
            try {
                // For now, just fetch all products and take first 5 as recommended
                // In a real app, this would be a smarter query
                val allProducts = repository.getAllProducts()
                recommendedAdapter.updateProducts(allProducts.take(5))
            } catch (e: Exception) {
                e.printStackTrace()
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
                    } else if (selectedSize == size) {
                        selectedSize = null
                    }
                }
                chipGroupSizes.addView(chip)
            }
            
            // Setup color chips
            chipGroupColors.removeAllViews()
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
                    } else if (selectedColor == color) {
                        selectedColor = null
                    }
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
                binding.tvOriginalPrice.text = "$${price}"; binding.tvOriginalPrice.visibility = View.VISIBLE
                binding.tvDiscount.text = "${discount}% OFF"; binding.tvDiscount.visibility = View.VISIBLE
            } else {
                binding.tvProductPrice.text = "$${price}"; binding.tvOriginalPrice.visibility = View.GONE; binding.tvDiscount.visibility = View.GONE
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
                isInWishlist = repository.isInWishlist(productId)
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
    
    private fun showAddToCartBottomSheet() {
        val prod = product ?: return
        
        if (selectedSize == null) {
            Toast.makeText(this, "Please select a size", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (selectedColor == null) {
            Toast.makeText(this, "Please select a color", Toast.LENGTH_SHORT).show()
            return
        }
        
        val bottomSheetDialog = BottomSheetDialog(this)
        val bottomSheetView = LayoutInflater.from(this).inflate(
            R.layout.fragment_add_to_bag,
            null
        )
        
        val etQuantity = bottomSheetView.findViewById<EditText>(R.id.quantityEtBottom)
        var quantity = 1
        etQuantity.setText(quantity.toString())
        
        bottomSheetView.findViewById<View>(R.id.minusLayout).setOnClickListener {
            if (quantity > 1) {
                quantity--
                etQuantity.setText(quantity.toString())
            }
        }
        
        bottomSheetView.findViewById<View>(R.id.plusLayout).setOnClickListener {
            if (quantity < 10) { // Limit max quantity
                quantity++
                etQuantity.setText(quantity.toString())
            }
        }
        
        bottomSheetView.findViewById<View>(R.id.addToCart_BottomSheet).setOnClickListener {
            addToCart(quantity)
            bottomSheetDialog.dismiss()
        }
        
        bottomSheetDialog.setContentView(bottomSheetView)
        bottomSheetDialog.show()
    }
    
    private fun addToCart(quantity: Int) {
        val prod = product ?: return
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
            quantity = quantity,
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
