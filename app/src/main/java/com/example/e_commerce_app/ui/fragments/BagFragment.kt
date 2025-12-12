package com.example.e_commerce_app.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.e_commerce_app.data.cache.CartCache
import com.example.e_commerce_app.data.model.CartItem
import com.example.e_commerce_app.databinding.FragmentBagBinding
import com.example.e_commerce_app.ui.activities.CheckoutActivity
import com.example.e_commerce_app.ui.adapters.CartAdapter
import com.example.e_commerce_app.ui.viewmodel.BagViewModel
import com.example.e_commerce_app.utils.CurrencyConverter
import com.example.e_commerce_app.utils.Extensions.showToast
import com.example.e_commerce_app.utils.GlobalCurrency
import kotlinx.coroutines.launch

/**
 * BagFragment - Shopping cart screen
 * Uses BagViewModel for MVVM architecture
 */
class BagFragment : Fragment() {
    
    private var _binding: FragmentBagBinding? = null
    private val binding get() = _binding!!
    
    // ViewModel instance
    private val viewModel: BagViewModel by viewModels()
    
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
        setupObservers()
        isViewCreated = true
    }
    
    /**
     * Setup LiveData observers for ViewModel
     */
    private fun setupObservers() {
        // Observe cart items
        viewModel.cartItems.observe(viewLifecycleOwner) { items ->
            cartItems.clear()
            cartItems.addAll(items)
            cartAdapter.notifyDataSetChanged()
            
            // Toggle empty state
            binding.tvEmptyCart.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
            binding.rvCartItems.visibility = if (items.isEmpty()) View.GONE else View.VISIBLE
            binding.cardCheckout.visibility = if (items.isEmpty()) View.GONE else View.VISIBLE
        }
        
        // Observe price calculations
        viewModel.subtotal.observe(viewLifecycleOwner) { subtotal ->
            updatePriceDisplay()
        }
        
        viewModel.shipping.observe(viewLifecycleOwner) { shipping ->
            updatePriceDisplay()
        }
        
        viewModel.total.observe(viewLifecycleOwner) { total ->
            updatePriceDisplay()
        }
        
        // Observe currency
        viewModel.currentCurrency.observe(viewLifecycleOwner) { currency ->
            GlobalCurrency.setCurrency(currency)
            updatePriceDisplay()
        }
        
        // Observe loading state
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // Handle loading if needed
        }
    }
    
    /**
     * Update price display with current currency
     */
    private fun updatePriceDisplay() {
        val subtotal = viewModel.subtotal.value ?: 0.0
        val shipping = viewModel.shipping.value ?: 0.0
        val total = viewModel.total.value ?: 0.0
        val currency = viewModel.currentCurrency.value ?: "USD"
        
        val symbol = if (currency == "EUR") "€" else "$"
        val rate = if (currency == "EUR") 0.92 else 1.0
        
        binding.tvSubtotal.text = "$symbol${"%.2f".format(subtotal * rate)}"
        binding.tvShipping.text = "$symbol${"%.2f".format(shipping * rate)}"
        binding.tvTotal.text = "$symbol${"%.2f".format(total * rate)}"
    }
    
    private fun setupRecyclerView() {
        cartAdapter = CartAdapter(
            cartItems,
            onQuantityChanged = { item, newQuantity ->
                // Update via ViewModel
                viewModel.updateQuantity(item.id, newQuantity)
            },
            onRemoveClick = { item ->
                // Remove via ViewModel
                viewModel.removeItem(item.id)
            }
        )
        
        binding.rvCartItems.adapter = cartAdapter
    }
    
    private fun setupCheckout() {
        binding.btnCheckout.setOnClickListener {
            if (cartItems.isNotEmpty()) {
                val subtotal = viewModel.subtotal.value ?: 0.0
                val intent = Intent(requireContext(), CheckoutActivity::class.java)
                intent.putExtra("SUBTOTAL", subtotal)
                startActivity(intent)
            } else {
                requireContext().showToast("Cart is empty")
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        viewModel.loadCartItems()
        viewModel.updateCurrency()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
