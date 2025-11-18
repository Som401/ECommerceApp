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
        // Get user name from Firebase
        val firestore = FirebaseFirestore.getInstance()
        val userId = FirebaseManager.currentUser?.uid
        
        if (userId != null) {
            lifecycleScope.launch {
                try {
                    val userDoc = firestore.collection("Users").document(userId).get().await()
                    val userName = userDoc.getString("name") ?: "Shopper"
                    binding.tvUserName.text = "Hello, $userName!"
                } catch (e: Exception) {
                    binding.tvUserName.text = "Hello, Shopper!"
                }
            }
        } else {
            binding.tvUserName.text = "Hello, Shopper!"
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
        val intent = Intent(requireContext(), ProductDetailsActivity::class.java)
        intent.putExtra("PRODUCT_ID", product.id)
        startActivity(intent)
    }
    
    private fun loadSampleData() {
        binding.progressBar?.visibility = View.VISIBLE
        
        lifecycleScope.launch {
            try {
                val allProducts = repository.getAllProducts()
                
                // Split products into new and featured
                newProducts.clear()
                featuredProducts.clear()
                
                if (allProducts.isNotEmpty()) {
                    // Take first half as new products
                    newProducts.addAll(allProducts.take(allProducts.size / 2 + 1))
                    // Take second half as featured products
                    featuredProducts.addAll(allProducts.drop(allProducts.size / 2 + 1))
                    
                    newProductsAdapter.notifyDataSetChanged()
                    featuredProductsAdapter.notifyDataSetChanged()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                binding.progressBar?.visibility = View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
