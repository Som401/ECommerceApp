package com.example.e_commerce_app.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.e_commerce_app.data.cache.WishlistCache
import com.example.e_commerce_app.data.model.Product
import com.example.e_commerce_app.databinding.FragmentFavoritesBinding
import com.example.e_commerce_app.ui.adapters.ProductGridAdapter
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class FavoritesFragment : Fragment() {
    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!
    private val auth = FirebaseAuth.getInstance()
    private lateinit var adapter: ProductGridAdapter
    private val wishlistProducts = mutableListOf<Product>()
    private var isViewCreated = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        isViewCreated = true
        // Don't load here - let onResume handle it
    }

    private fun setupRecyclerView() {
        adapter = ProductGridAdapter(
            products = wishlistProducts,
            onProductClick = { product ->
                val intent = Intent(requireContext(), com.example.e_commerce_app.ui.activities.ProductDetailsActivity::class.java)
                intent.putExtra("PRODUCT_ID", product.id)
                startActivity(intent)
            }
        )
        binding.rvWishlist.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvWishlist.adapter = adapter
    }

    private fun loadWishlist() {
        val userId = auth.currentUser?.uid
        android.util.Log.d("WishlistDebug", "FavoritesFragment: Loading wishlist from cache for userId=$userId")
        
        if (userId == null) {
            android.util.Log.e("WishlistDebug", "FavoritesFragment: No user logged in!")
            return
        }
        
        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                // Use WishlistCache - fetches only once, then uses cached data
                val products = WishlistCache.getWishlistProducts()
                android.util.Log.d("WishlistDebug", "FavoritesFragment: Received ${products.size} products from cache")
                
                wishlistProducts.clear()
                wishlistProducts.addAll(products)
                
                adapter.notifyDataSetChanged()
                updateEmptyState()
                android.util.Log.d("WishlistDebug", "FavoritesFragment: UI updated with ${wishlistProducts.size} products")
            } catch (e: Exception) {
                android.util.Log.e("WishlistDebug", "FavoritesFragment: Error loading wishlist: ${e.message}", e)
                e.printStackTrace()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun removeFromWishlist(product: Product) {
        lifecycleScope.launch {
            try {
                // Remove via WishlistCache - updates both local cache and Firebase
                WishlistCache.removeFromWishlist(product.id)
                wishlistProducts.remove(product)
                adapter.notifyDataSetChanged()
                updateEmptyState()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun updateEmptyState() {
        if (wishlistProducts.isEmpty()) {
            binding.tvEmptyWishlist.visibility = View.VISIBLE
            binding.rvWishlist.visibility = View.GONE
        } else {
            binding.tvEmptyWishlist.visibility = View.GONE
            binding.rvWishlist.visibility = View.VISIBLE
        }
    }

    override fun onResume() {
        super.onResume()
        // Only load if view has been created
        if (isViewCreated) {
            loadWishlist()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        isViewCreated = false
        _binding = null
    }
}
