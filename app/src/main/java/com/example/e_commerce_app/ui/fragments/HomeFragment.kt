package com.example.e_commerce_app.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.e_commerce_app.data.model.Product
import com.example.e_commerce_app.databinding.FragmentHomeBinding
import com.example.e_commerce_app.ui.activities.ProductDetailsActivity
import com.example.e_commerce_app.ui.adapters.ProductAdapter
import com.example.e_commerce_app.ui.viewmodel.HomeViewModel

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()
    
    private lateinit var newProductsAdapter: ProductAdapter
    private lateinit var featuredProductsAdapter: ProductAdapter
    
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
        
        setupRecyclerViews()
        setupObservers()
        setupUI()
        viewModel.loadAllData()
    }
    
    private fun setupObservers() {
        // Observe user name
        viewModel.userName.observe(viewLifecycleOwner) { name ->
            binding.tvUserName.text = "Hello, $name!"
        }
        
        // Observe new products
        viewModel.newProducts.observe(viewLifecycleOwner) { products ->
            newProducts.clear()
            newProducts.addAll(products)
            newProductsAdapter.notifyDataSetChanged()
        }
        
        // Observe featured products
        viewModel.featuredProducts.observe(viewLifecycleOwner) { products ->
            featuredProducts.clear()
            featuredProducts.addAll(products)
            featuredProductsAdapter.notifyDataSetChanged()
        }
        
        // Observe loading state
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }
    
    private fun setupUI() {
        binding.btnShopNow.setOnClickListener {
            navigateToShop()
        }
        
        binding.tvViewAllNew.setOnClickListener {
            navigateToShop()
        }
        
        binding.tvViewAllFeatured.setOnClickListener {
            navigateToShop()
        }
    }
    
    private fun navigateToShop() {
        // Update bottom navigation to show Shop selected
        val mainActivity = activity as? com.example.e_commerce_app.MainActivity
        mainActivity?.findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(
            com.example.e_commerce_app.R.id.bottomNavigation
        )?.selectedItemId = com.example.e_commerce_app.R.id.nav_shop
    }
    
    private fun setupRecyclerViews() {
        // New Products RecyclerView
        newProductsAdapter = ProductAdapter(newProducts) { product ->
            openProductDetails(product)
        }
        binding.rvNewProducts.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = newProductsAdapter
        }
        
        // Featured Products RecyclerView
        featuredProductsAdapter = ProductAdapter(featuredProducts) { product ->
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

    override fun onResume() {
        super.onResume()
        
        // Update currency in case it changed while away
        if (::newProductsAdapter.isInitialized) {
            newProductsAdapter.updateCurrency(com.example.e_commerce_app.utils.GlobalCurrency.currentCurrency)
        }
        if (::featuredProductsAdapter.isInitialized) {
            featuredProductsAdapter.updateCurrency(com.example.e_commerce_app.utils.GlobalCurrency.currentCurrency)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
