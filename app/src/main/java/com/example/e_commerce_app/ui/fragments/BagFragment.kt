package com.example.e_commerce_app.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.e_commerce_app.data.cache.CartCache
import com.example.e_commerce_app.data.model.CartItem
import com.example.e_commerce_app.databinding.FragmentBagBinding
import com.example.e_commerce_app.ui.activities.CheckoutActivity
import com.example.e_commerce_app.ui.adapters.CartAdapter
import com.example.e_commerce_app.utils.CurrencyConverter
import com.example.e_commerce_app.utils.Extensions.showToast
import com.example.e_commerce_app.utils.GlobalCurrency
import kotlinx.coroutines.launch

class BagFragment : Fragment() {
    
    private var _binding: FragmentBagBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var cartAdapter: CartAdapter
    private var cartItems = mutableListOf<CartItem>()
    private var isViewCreated = false
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBagBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupCheckout()
        isViewCreated = true
        // Don't load here - let onResume handle it
    }
    
    private fun setupRecyclerView() {
        cartAdapter = CartAdapter(
            cartItems,
            onQuantityChanged = { item, newQuantity ->
                updateQuantity(item, newQuantity)
            },
            onRemoveClick = { item ->
                removeItem(item)
            }
        )
        
        binding.rvCartItems.adapter = cartAdapter
    }
    
    private fun setupCheckout() {
        binding.btnCheckout.setOnClickListener {
            if (cartItems.isNotEmpty()) {
                val subtotal = cartItems.sumOf { it.getTotalPrice() }
                val intent = Intent(requireContext(), CheckoutActivity::class.java)
                intent.putExtra("SUBTOTAL", subtotal)
                startActivity(intent)
            } else {
                requireContext().showToast("Cart is empty")
            }
        }
    }
    
    private fun loadCartItems() {
        android.util.Log.d("CartDebug", "BagFragment: Loading cart items from cache...")
        lifecycleScope.launch {
            cartItems.clear()
            // Use CartCache - fetches only once, then uses cached data
            val items = CartCache.getCartItems()
            android.util.Log.d("CartDebug", "BagFragment: Received ${items.size} items from cache")
            cartItems.addAll(items)
            cartAdapter.notifyDataSetChanged()
            updateUI()
            android.util.Log.d("CartDebug", "BagFragment: UI updated with ${cartItems.size} items")
        }
    }
    
    private fun updateQuantity(item: CartItem, newQuantity: Int) {
        lifecycleScope.launch {
            // Update via CartCache - updates both local cache and Firebase
            val success = CartCache.updateQuantity(item.id, newQuantity)
            if (success) {
                loadCartItems()
            } else {
                requireContext().showToast("Failed to update quantity")
            }
        }
    }
    
    private fun removeItem(item: CartItem) {
        lifecycleScope.launch {
            // Remove via CartCache - updates both local cache and Firebase
            val success = CartCache.removeFromCart(item.id)
            if (success) {
                requireContext().showToast("Item removed")
                loadCartItems()
            } else {
                requireContext().showToast("Failed to remove item")
            }
        }
    }
    
    private fun updateUI() {
        if (cartItems.isEmpty()) {
            binding.tvEmptyCart.visibility = View.VISIBLE
            binding.rvCartItems.visibility = View.GONE
            binding.cardCheckout.visibility = View.GONE
        } else {
            binding.tvEmptyCart.visibility = View.GONE
            binding.rvCartItems.visibility = View.VISIBLE
            binding.cardCheckout.visibility = View.VISIBLE
            
            val subtotal = cartItems.sumOf { it.getTotalPrice() }
            val shipping = 10.0
            val total = subtotal + shipping
            
            val currency = GlobalCurrency.currentCurrency
            binding.tvSubtotal.text = when (currency) {
                "EUR" -> "€${String.format("%.2f", subtotal * 0.92)}"
                else -> "$${String.format("%.2f", subtotal)}"
            }
            binding.tvShipping.text = when (currency) {
                "EUR" -> "€${String.format("%.2f", shipping * 0.92)}"
                else -> "$${String.format("%.2f", shipping)}"
            }
            binding.tvTotal.text = when (currency) {
                "EUR" -> "€${String.format("%.2f", total * 0.92)}"
                else -> "$${String.format("%.2f", total)}"
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Only load if view has been created
        if (isViewCreated) {
            loadCartItems()
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        isViewCreated = false
        _binding = null
    }
}
