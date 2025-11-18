package com.example.e_commerce_app.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.e_commerce_app.data.model.CartItem
import com.example.e_commerce_app.data.repository.ProductRepository
import com.example.e_commerce_app.databinding.FragmentBagBinding
import com.example.e_commerce_app.ui.adapters.CartAdapter
import com.example.e_commerce_app.utils.Extensions.showToast
import kotlinx.coroutines.launch

class BagFragment : Fragment() {
    
    private var _binding: FragmentBagBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var cartAdapter: CartAdapter
    private val repository = ProductRepository()
    private var cartItems = mutableListOf<CartItem>()
    
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
        loadCartItems()
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
                requireContext().showToast("Proceeding to checkout...")
                // Navigate to checkout
            } else {
                requireContext().showToast("Cart is empty")
            }
        }
    }
    
    private fun loadCartItems() {
        lifecycleScope.launch {
            cartItems.clear()
            cartItems.addAll(repository.getCartItems())
            cartAdapter.notifyDataSetChanged()
            updateUI()
        }
    }
    
    private fun updateQuantity(item: CartItem, newQuantity: Int) {
        lifecycleScope.launch {
            val success = repository.updateCartItemQuantity(item.id, newQuantity)
            if (success) {
                loadCartItems()
            } else {
                requireContext().showToast("Failed to update quantity")
            }
        }
    }
    
    private fun removeItem(item: CartItem) {
        lifecycleScope.launch {
            val success = repository.removeFromCart(item.id)
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
            
            binding.tvSubtotal.text = "$${"%.2f".format(subtotal)}"
            binding.tvShipping.text = "$${"%.2f".format(shipping)}"
            binding.tvTotal.text = "$${"%.2f".format(total)}"
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
