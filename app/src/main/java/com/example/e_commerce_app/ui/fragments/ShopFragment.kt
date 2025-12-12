package com.example.e_commerce_app.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.e_commerce_app.data.cache.ProductCache
import com.example.e_commerce_app.data.model.Product
import com.example.e_commerce_app.databinding.FragmentShopBinding
import com.example.e_commerce_app.ui.adapters.ProductGridAdapter
import com.example.e_commerce_app.ui.viewmodel.ShopViewModel
import com.example.e_commerce_app.utils.CurrencyConverter
import com.example.e_commerce_app.utils.CurrencyPreference
import com.example.e_commerce_app.utils.GlobalCurrency
import kotlinx.coroutines.launch

/**
 * ShopFragment - Main shopping screen with product grid and filters
 * Uses ShopViewModel for MVVM architecture
 */
class ShopFragment : Fragment() {
    
    private var _binding: FragmentShopBinding? = null
    private val binding get() = _binding!!
    
    // ViewModel instance using Kotlin property delegation
    private val viewModel: ShopViewModel by viewModels()
    
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
        
        setupRecyclerView()
        setupCategoryFilters()
        setupCurrencySwitch()
        setupObservers()
        loadUserCurrency()
    }
    
    /**
     * Setup LiveData observers for ViewModel
     */
    private fun setupObservers() {
        // Observe filtered products
        viewModel.filteredProducts.observe(viewLifecycleOwner) { products ->
            productAdapter.updateProducts(products)
        }
        
        // Observe currency changes
        viewModel.currentCurrency.observe(viewLifecycleOwner) { currency ->
            currentCurrency = currency
            binding.btnCurrencySwitch.text = currency
            productAdapter.notifyDataSetChanged()
        }
        
        // Observe loading state
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
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
            // Use ViewModel to filter products
            viewModel.applyFilter(category)
        }
    }
    
    private fun setupCurrencySwitch() {
        binding.btnCurrencySwitch.setOnClickListener {
            // Toggle currency via ViewModel
            viewModel.toggleCurrency()
            
            // Save preference
            lifecycleScope.launch {
                CurrencyPreference.saveUserCurrency(viewModel.getCurrency())
            }
            
            // Refresh product display
            productAdapter.updateCurrency(viewModel.getCurrency())
        }
    }
    
    private fun loadUserCurrency() {
        lifecycleScope.launch {
            // Fetch exchange rate in background
            CurrencyConverter.fetchExchangeRate()
            
            // Load user's preferred currency
            val prefCurrency = CurrencyPreference.getUserCurrency()
            if (prefCurrency != currentCurrency) {
                GlobalCurrency.setCurrency(prefCurrency)
                productAdapter.updateCurrency(prefCurrency)
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
