package com.example.e_commerce_app.ui.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.e_commerce_app.ui.activities.BaseActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.e_commerce_app.databinding.ActivityOrdersBinding
import com.example.e_commerce_app.data.model.Order
import com.example.e_commerce_app.ui.adapters.OrdersAdapter
import com.example.e_commerce_app.utils.CurrencyConverter
import com.example.e_commerce_app.utils.FirebaseManager
import com.example.e_commerce_app.utils.LocaleHelper
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class OrdersActivity : BaseActivity() {
    
    private lateinit var binding: ActivityOrdersBinding
    private lateinit var ordersAdapter: OrdersAdapter
    private val orders = mutableListOf<Order>()
    private val db = FirebaseFirestore.getInstance()
    

    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrdersBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupRecyclerView()
        loadOrders()
    }
    
    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }
    
    private fun setupRecyclerView() {
        ordersAdapter = OrdersAdapter(orders) { order ->
            shareOrderDetails(order)
        }
        binding.rvOrders.layoutManager = LinearLayoutManager(this)
        binding.rvOrders.adapter = ordersAdapter
    }
    
    private fun loadOrders() {
        val userId = FirebaseManager.currentUser?.uid
        if (userId == null) {
            binding.tvEmptyOrders.visibility = View.VISIBLE
            binding.rvOrders.visibility = View.GONE
            return
        }
        
        binding.progressBar.visibility = View.VISIBLE
        
        lifecycleScope.launch {
            try {
                android.util.Log.d("OrdersActivity", "Loading orders for userId: $userId")
                val snapshot = db.collection("CompletedOrders")
                    .whereEqualTo("userId", userId)
                    .get()
                    .await()
                
                android.util.Log.d("OrdersActivity", "Found ${snapshot.size()} orders")
                
                orders.clear()
                snapshot.documents.forEach { doc ->
                    android.util.Log.d("OrdersActivity", "Order doc: ${doc.id}")
                    doc.toObject(Order::class.java)?.let { order ->
                        orders.add(order)
                        android.util.Log.d("OrdersActivity", "Added order: ${order.id}")
                    }
                }
                
                // Sort by date in code instead of Firestore
                orders.sortByDescending { it.orderDate }
                
                ordersAdapter.notifyDataSetChanged()
                updateEmptyState()
                
            } catch (e: Exception) {
                android.util.Log.e("OrdersActivity", "Error loading orders", e)
                e.printStackTrace()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }
    
    private fun updateEmptyState() {
        if (orders.isEmpty()) {
            binding.tvEmptyOrders.visibility = View.VISIBLE
            binding.rvOrders.visibility = View.GONE
        } else {
            binding.tvEmptyOrders.visibility = View.GONE
            binding.rvOrders.visibility = View.VISIBLE
        }
    }
    
    /**
     * Share order details via implicit intent (ACTION_SEND)
     */
    private fun shareOrderDetails(order: Order) {
        val shareText = buildString {
            append("📦 Order #${order.id.takeLast(8)}\n")
            append("📅 Date: ${order.getFormattedDate()}\n")
            append("💵 Total: ${CurrencyConverter.formatPrice(order.total, "USD")}\n")
            append("📊 Status: ${order.status}\n")
            append("🛍️ Items: ${order.items.size}\n\n")
            append("Items:\n")
            order.items.forEach { item ->
                append("- ${item.productName} (x${item.quantity})\n")
            }
        }
        
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
            putExtra(Intent.EXTRA_SUBJECT, "My Order #${order.id.takeLast(8)}")
        }
        
        startActivity(Intent.createChooser(shareIntent, "Share order via"))
    }
}
