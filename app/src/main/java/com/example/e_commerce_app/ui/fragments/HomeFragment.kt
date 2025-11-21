package com.example.e_commerce_app.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.e_commerce_app.data.model.Product
import com.example.e_commerce_app.data.repository.ProductRepository
import com.example.e_commerce_app.databinding.FragmentHomeBinding
import com.example.e_commerce_app.ui.activities.ProductDetailsActivity
import com.example.e_commerce_app.ui.adapters.ProductAdapter
import com.example.e_commerce_app.utils.FirebaseManager
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var newProductsAdapter: ProductAdapter
    private lateinit var featuredProductsAdapter: ProductAdapter
    
    private val repository = ProductRepository()
    private val newProducts = mutableListOf<Product>()
    private val featuredProducts = mutableListOf<Product>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupUI()
        setupRecyclerViews()
        loadSampleData()
    }
    
    private fun setupUI() {
        // Resolve and greet with the actual user name
        val firestore = FirebaseFirestore.getInstance()
        val firebaseUser = FirebaseManager.currentUser
        val userId = firebaseUser?.uid

        fun fallbackName(): String {
            val display = firebaseUser?.displayName?.takeIf { !it.isNullOrBlank() }
            if (!display.isNullOrBlank()) return display
            val emailName = firebaseUser?.email?.substringBefore('@')?.takeIf { !it.isNullOrBlank() }
            if (!emailName.isNullOrBlank()) return emailName
            return "Shopper"
        }

        if (userId != null) {
            lifecycleScope.launch {
                try {
                    val userDoc = firestore.collection("Users").document(userId).get().await()
                    val resolved = when {
                        userDoc.exists() -> {
                            // Prefer fullName (matches our User model), then common alternatives
                            userDoc.getString("fullName")
                                ?: userDoc.getString("name")
                                ?: userDoc.getString("username")
                                ?: fallbackName()
                        }
                        else -> fallbackName()
                    }
                    android.util.Log.d("UserDebug", "Resolved greeting name='$resolved' userId=$userId")
                    binding.tvUserName.text = "Hello, ${resolved}!"
                } catch (e: Exception) {
                    android.util.Log.w("UserDebug", "Failed to fetch username: ${e.message}")
                    val resolved = fallbackName()
                    binding.tvUserName.text = "Hello, ${resolved}!"
                }
            }
        } else {
            val resolved = "Shopper"
            android.util.Log.d("UserDebug", "No logged-in user; using default name='$resolved'")
            binding.tvUserName.text = "Hello, ${resolved}!"
        }

        binding.btnShopNow.setOnClickListener {
            // Navigate to shop fragment
            parentFragmentManager.beginTransaction()
                .replace(com.example.e_commerce_app.R.id.fragmentContainer, ShopFragment())
                .addToBackStack(null)
                .commit()
        }
    }
    
    private fun setupRecyclerViews() {
        // New Products RecyclerView
        newProductsAdapter = ProductAdapter(newProducts, lifecycleScope) { product ->
            openProductDetails(product)
        }
        binding.rvNewProducts.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = newProductsAdapter
        }
        
        // Featured Products RecyclerView
        featuredProductsAdapter = ProductAdapter(featuredProducts, lifecycleScope) { product ->
            openProductDetails(product)
        }
        binding.rvFeaturedProducts.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = featuredProductsAdapter
        }
    }
    
    private fun openProductDetails(product: Product) {
        android.util.Log.d("ProductDebug", "HomeFragment openProductDetails id='${product.id}' name='${product.name}'")
        val intent = Intent(requireContext(), ProductDetailsActivity::class.java)
        intent.putExtra("PRODUCT_ID", product.id)
        // Fallback extras in case product id fails; ProductDetails will prefer fetched data
        intent.putExtra("PRODUCT_NAME_FALLBACK", product.name)
        intent.putExtra("PRODUCT_BRAND_FALLBACK", product.brand)
        intent.putExtra("PRODUCT_IMAGE_FALLBACK", product.imageUrl)
        intent.putExtra("PRODUCT_PRICE_FALLBACK", product.price)
        intent.putExtra("PRODUCT_DISCOUNT_FALLBACK", product.discount)
        intent.putStringArrayListExtra("PRODUCT_SIZES_FALLBACK", ArrayList(product.size))
        intent.putStringArrayListExtra("PRODUCT_COLORS_FALLBACK", ArrayList(product.colors))
        intent.putExtra("PRODUCT_STOCK_FALLBACK", product.stock)
        intent.putExtra("PRODUCT_DESCRIPTION_FALLBACK", product.description)
        intent.putExtra("PRODUCT_GENDER_FALLBACK", product.gender)
        startActivity(intent)
    }
    
    private fun loadSampleData() {
        binding.progressBar?.visibility = View.VISIBLE
        
        lifecycleScope.launch {
            try {
                val allProducts = repository.getAllProducts()
                android.util.Log.d("ProductDebug", "console.log products = ${allProducts}")
                println("console.log products = ${allProducts}")
                
                // Split products into two halves deterministically
                val midpoint = allProducts.size / 2
                val firstHalf = allProducts.take(midpoint)
                val secondHalf = allProducts.drop(midpoint)
                android.util.Log.d("ProductDebug", "HomeFragment split firstHalf=${firstHalf.size} secondHalf=${secondHalf.size}")
                newProductsAdapter.updateProducts(firstHalf)
                featuredProductsAdapter.updateProducts(secondHalf)
                android.util.Log.d("ProductDebug", "Adapters updated new=${newProductsAdapter.itemCount} featured=${featuredProductsAdapter.itemCount}")
                    // Show on-screen confirmation for debugging
                    try {
                        val msg = if (allProducts.isEmpty()) "No products fetched" else "Fetched ${allProducts.size} products, first=${allProducts[0].name}"
                        android.util.Log.i("ProductDebug", msg)
                    } catch (t: Throwable) {
                        android.util.Log.w("ProductDebug", "Toast failed: ${t.message}")
                    }
            } catch (e: Exception) {
                e.printStackTrace()
                android.util.Log.e("ProductDebug", "HomeFragment load error: ${e.message}")
            } finally {
                binding.progressBar?.visibility = View.GONE
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // For debugging ensure products printed when returning to Home
        loadSampleData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
