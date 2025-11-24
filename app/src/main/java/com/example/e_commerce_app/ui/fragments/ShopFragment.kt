package com.example.e_commerce_app.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.e_commerce_app.data.cache.ProductCache
import com.example.e_commerce_app.data.model.Product
import com.example.e_commerce_app.databinding.FragmentShopBinding
import com.example.e_commerce_app.ui.adapters.ProductGridAdapter
import com.example.e_commerce_app.utils.CurrencyConverter
import com.example.e_commerce_app.utils.CurrencyPreference
import com.example.e_commerce_app.utils.GlobalCurrency
import kotlinx.coroutines.launch

class ShopFragment : Fragment() {
    
    private var _binding: FragmentShopBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var productAdapter: ProductGridAdapter
    private var allProducts = listOf<Product>()
    private var currentCurrency = GlobalCurrency.currentCurrency
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShopBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize button text immediately
        binding.btnCurrencySwitch.text = currentCurrency
        
        setupRecyclerView()
        setupCategoryFilters()
        setupCurrencySwitch()
        loadUserCurrency()
        loadProducts()
    }
    
    private fun setupRecyclerView() {
        productAdapter = ProductGridAdapter(emptyList()) { product ->
            val intent = Intent(requireContext(), com.example.e_commerce_app.ui.activities.ProductDetailsActivity::class.java)
            intent.putExtra("PRODUCT_ID", product.id)
            startActivity(intent)
        }
        
        binding.rvProducts.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = productAdapter
        }
    }
    
    private fun setupCategoryFilters() {
        binding.chipGroupCategories.setOnCheckedChangeListener { group, checkedId ->
            val category = when (checkedId) {
                binding.chipAll.id -> "All"
                binding.chipRunning.id -> "Running"
                binding.chipSneakers.id -> "Sneakers"
                binding.chipSports.id -> "Sports"
                binding.chipCasual.id -> "Casual"
                else -> "All"
            }
            filterProducts(category)
        }
    }
    
    private fun setupCurrencySwitch() {
        binding.btnCurrencySwitch.setOnClickListener {
            lifecycleScope.launch {
                // Toggle currency
                currentCurrency = if (currentCurrency == "USD") "EUR" else "USD"
                binding.btnCurrencySwitch.text = currentCurrency
                
                // Update global currency
                GlobalCurrency.setCurrency(currentCurrency)
                
                // Save preference
                CurrencyPreference.saveUserCurrency(currentCurrency)
                
                // Refresh product display
                productAdapter.updateCurrency(currentCurrency)
            }
        }
    }
    
    private fun loadUserCurrency() {
        lifecycleScope.launch {
            // Fetch exchange rate in background
            CurrencyConverter.fetchExchangeRate()
            
            // Load user's preferred currency if not already set by GlobalCurrency
            // But GlobalCurrency is the source of truth for the session
            val prefCurrency = CurrencyPreference.getUserCurrency()
            
            // Only update if different and GlobalCurrency hasn't changed in the meantime
            if (prefCurrency != currentCurrency) {
                currentCurrency = prefCurrency
                binding.btnCurrencySwitch.text = currentCurrency
                GlobalCurrency.setCurrency(currentCurrency)
                // productAdapter.notifyDataSetChanged()
                productAdapter.updateCurrency(currentCurrency)
            }
        }
    }
    
    private fun loadProducts() {
        binding.progressBar.visibility = View.VISIBLE
        
        lifecycleScope.launch {
            // Use ProductCache - fetches only once
            allProducts = ProductCache.getProducts()
            
            binding.progressBar.visibility = View.GONE
            
            if (allProducts.isEmpty()) {
                binding.tvEmptyState.visibility = View.VISIBLE
                binding.rvProducts.visibility = View.GONE
            } else {
                binding.tvEmptyState.visibility = View.GONE
                binding.rvProducts.visibility = View.VISIBLE
                productAdapter.updateProducts(allProducts)
            }
        }
    }
    
    private fun filterProducts(category: String) {
        val filtered = if (category == "All") {
            allProducts
        } else {
            allProducts.filter { it.category == category }
        }
        productAdapter.updateProducts(filtered)
        
        if (filtered.isEmpty()) {
            binding.tvEmptyState.visibility = View.VISIBLE
            binding.rvProducts.visibility = View.GONE
        } else {
            binding.tvEmptyState.visibility = View.GONE
            binding.rvProducts.visibility = View.VISIBLE
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
