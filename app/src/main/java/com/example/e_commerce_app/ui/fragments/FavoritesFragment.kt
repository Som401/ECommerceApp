package com.example.e_commerce_app.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.e_commerce_app.data.model.Product
import com.example.e_commerce_app.data.repository.ProductRepository
import com.example.e_commerce_app.databinding.FragmentFavoritesBinding
import com.example.e_commerce_app.ui.adapters.ProductGridAdapter
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class FavoritesFragment : Fragment() {
    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!
    private val productRepository = ProductRepository()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var adapter: ProductGridAdapter
    private val wishlistProducts = mutableListOf<Product>()

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
        loadWishlist()
    }

    private fun setupRecyclerView() {
        adapter = ProductGridAdapter(
            products = wishlistProducts,
            lifecycleScope = lifecycleScope,
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
        val userId = auth.currentUser?.uid ?: return
        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val productIds = productRepository.getWishlistItems()
                wishlistProducts.clear()
                
                // Fetch full product details for each wishlist item
                for (productId in productIds) {
                    val product = productRepository.getProductById(productId)
                    product?.let { wishlistProducts.add(it) }
                }
                
                adapter.notifyDataSetChanged()
                updateEmptyState()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun removeFromWishlist(product: Product) {
        lifecycleScope.launch {
            try {
                productRepository.removeFromWishlist(product.id)
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
        loadWishlist()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
