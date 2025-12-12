package com.example.e_commerce_app.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.example.e_commerce_app.data.model.Product
import com.example.e_commerce_app.databinding.FragmentFavoritesBinding
import com.example.e_commerce_app.ui.adapters.ProductGridAdapter
import com.example.e_commerce_app.ui.viewmodel.FavoritesViewModel

/**
 * Fragment displaying user's wishlist using MVVM architecture
 */
class FavoritesFragment : Fragment() {
    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!
    private val viewModel: FavoritesViewModel by viewModels()
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
        setupObservers()
        viewModel.loadWishlistItems()
    }

    /**
     * Setup LiveData observers for ViewModel
     */
    private fun setupObservers() {
        // Observe wishlist items
        viewModel.wishlistItems.observe(viewLifecycleOwner) { items ->
            wishlistProducts.clear()
            wishlistProducts.addAll(items)
            adapter.notifyDataSetChanged()
        }
        
        // Observe empty state
        viewModel.isEmpty.observe(viewLifecycleOwner) { isEmpty ->
            binding.tvEmptyWishlist.visibility = if (isEmpty) View.VISIBLE else View.GONE
            binding.rvWishlist.visibility = if (isEmpty) View.GONE else View.VISIBLE
        }
        
        // Observe loading state
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
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

    override fun onResume() {
        super.onResume()
        viewModel.loadWishlistItems()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
