package com.example.e_commerce_app.ui.activities

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.e_commerce_app.data.cache.CartCache
import com.example.e_commerce_app.data.model.Address
import com.example.e_commerce_app.data.model.Order
import com.example.e_commerce_app.data.model.OrderItem
import com.example.e_commerce_app.databinding.ActivityCheckoutBinding
import com.example.e_commerce_app.utils.FirebaseManager
import com.example.e_commerce_app.utils.GlobalCurrency
import com.example.e_commerce_app.utils.LocaleHelper
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class CheckoutActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityCheckoutBinding
    private val db = FirebaseFirestore.getInstance()
    private var subtotal = 0.0
    private val shippingCost = 10.0
    
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.applyLanguage(newBase))
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCheckoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        subtotal = intent.getDoubleExtra("SUBTOTAL", 0.0)
        
        setupToolbar()
        updatePriceSummary()
        setupButtons()
    }
    
    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }
    
    private fun updatePriceSummary() {
        val total = subtotal + shippingCost
        val currency = GlobalCurrency.currentCurrency
        val symbol = if (currency == "EUR") "€" else "$"
        val rate = if (currency == "EUR") 0.92 else 1.0
        
        binding.tvSubtotalAmount.text = "$symbol${"%.2f".format(subtotal * rate)}"
        binding.tvShippingAmount.text = "$symbol${"%.2f".format(shippingCost * rate)}"
        binding.tvTotalAmount.text = "$symbol${"%.2f".format(total * rate)}"
    }
    
    private fun setupButtons() {
        binding.btnPlaceOrder.setOnClickListener {
            if (validateInputs()) {
                placeOrder()
            }
        }
    }
    
    private fun validateInputs(): Boolean {
        binding.apply {
            if (etFullName.text.isNullOrBlank()) {
                etFullName.error = "Required"
                etFullName.requestFocus()
                return false
            }
            if (etPhoneNumber.text.isNullOrBlank()) {
                etPhoneNumber.error = "Required"
                etPhoneNumber.requestFocus()
                return false
            }
            if (etAddressLine1.text.isNullOrBlank()) {
                etAddressLine1.error = "Required"
                etAddressLine1.requestFocus()
                return false
            }
            if (etCity.text.isNullOrBlank()) {
                etCity.error = "Required"
                etCity.requestFocus()
                return false
            }
            if (etZipCode.text.isNullOrBlank()) {
                etZipCode.error = "Required"
                etZipCode.requestFocus()
                return false
            }
            if (etCardNumber.text.isNullOrBlank() || etCardNumber.text.toString().length < 16) {
                etCardNumber.error = "Invalid card number"
                etCardNumber.requestFocus()
                return false
            }
            if (etExpiryDate.text.isNullOrBlank()) {
                etExpiryDate.error = "Required"
                etExpiryDate.requestFocus()
                return false
            }
            if (etCvv.text.isNullOrBlank() || etCvv.text.toString().length < 3) {
                etCvv.error = "Invalid CVV"
                etCvv.requestFocus()
                return false
            }
            if (etCardholderName.text.isNullOrBlank()) {
                etCardholderName.error = "Required"
                etCardholderName.requestFocus()
                return false
            }
        }
        return true
    }
    
    private fun placeOrder() {
        val userId = FirebaseManager.currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "Please login to place order", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        binding.progressBar.visibility = View.VISIBLE
        binding.btnPlaceOrder.isEnabled = false
        
        lifecycleScope.launch {
            try {
                // Get cart items
                val cartItems = CartCache.getCartItems()
                
                if (cartItems.isEmpty()) {
                    Toast.makeText(this@CheckoutActivity, "Cart is empty", Toast.LENGTH_SHORT).show()
                    finish()
                    return@launch
                }
                
                // Create address
                val address = Address(
                    fullName = binding.etFullName.text.toString(),
                    phoneNumber = binding.etPhoneNumber.text.toString(),
                    addressLine1 = binding.etAddressLine1.text.toString(),
                    addressLine2 = binding.etAddressLine2.text.toString(),
                    city = binding.etCity.text.toString(),
                    zipCode = binding.etZipCode.text.toString()
                )
                
                // Convert cart items to order items
                val orderItems = cartItems.map { cartItem ->
                    OrderItem(
                        productId = cartItem.productId,
                        productName = cartItem.productName,
                        productImage = cartItem.productImage,
                        price = cartItem.price,
                        selectedSize = cartItem.selectedSize,
                        selectedColor = cartItem.selectedColor,
                        quantity = cartItem.quantity
                    )
                }
                
                // Get last 4 digits of card
                val cardNumber = binding.etCardNumber.text.toString()
                val lastFourDigits = cardNumber.takeLast(4)
                
                // Create order
                val orderId = "${userId}_${System.currentTimeMillis()}"
                val order = Order(
                    id = orderId,
                    userId = userId,
                    items = orderItems,
                    shippingAddress = address,
                    paymentMethod = "Credit Card",
                    cardLastFourDigits = lastFourDigits,
                    subtotal = subtotal,
                    shippingCost = shippingCost,
                    total = subtotal + shippingCost,
                    orderDate = System.currentTimeMillis(),
                    status = "Pending"
                )
                
                // Save order to Firebase (CompletedOrders collection)
                db.collection("CompletedOrders")
                    .document(orderId)
                    .set(order)
                    .await()
                
                // Clear cart after successful order
                CartCache.clearCart()
                
                Toast.makeText(this@CheckoutActivity, "Order placed successfully!", Toast.LENGTH_LONG).show()
                
                // Go back to main screen
                finish()
                
            } catch (e: Exception) {
                Toast.makeText(this@CheckoutActivity, "Error placing order: ${e.message}", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            } finally {
                binding.progressBar.visibility = View.GONE
                binding.btnPlaceOrder.isEnabled = true
            }
        }
    }
}
