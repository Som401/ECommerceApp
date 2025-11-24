package com.example.e_commerce_app.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.e_commerce_app.data.model.Order
import com.example.e_commerce_app.databinding.ItemOrderBinding
import com.example.e_commerce_app.utils.GlobalCurrency

class OrdersAdapter(
    private val orders: List<Order>,
    private val onShareClick: (Order) -> Unit
) : RecyclerView.Adapter<OrdersAdapter.OrderViewHolder>() {

    inner class OrderViewHolder(private val binding: ItemOrderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(order: Order) {
            binding.apply {
                tvOrderId.text = "Order #${order.id.takeLast(8)}"
                tvOrderDate.text = order.getFormattedDate()
                tvOrderStatus.text = order.status
                
                // Apply currency conversion
                tvOrderTotal.text = com.example.e_commerce_app.utils.CurrencyConverter.convertAndFormat(order.total, com.example.e_commerce_app.utils.GlobalCurrency.currentCurrency)
                tvItemCount.text = "${order.items.size} item${if (order.items.size > 1) "s" else ""}"
                
                // Set status color
                when (order.status) {
                    "Pending" -> tvOrderStatus.setTextColor(binding.root.context.getColor(android.R.color.holo_orange_dark))
                    "Processing" -> tvOrderStatus.setTextColor(binding.root.context.getColor(android.R.color.holo_blue_dark))
                    "Shipped" -> tvOrderStatus.setTextColor(binding.root.context.getColor(android.R.color.holo_purple))
                    "Delivered" -> tvOrderStatus.setTextColor(binding.root.context.getColor(android.R.color.holo_green_dark))
                    "Cancelled" -> tvOrderStatus.setTextColor(binding.root.context.getColor(android.R.color.holo_red_dark))
                }
                
                // Share button click
                root.setOnLongClickListener {
                    onShareClick(order)
                    true
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = ItemOrderBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return OrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.bind(orders[position])
    }

    override fun getItemCount() = orders.size
}
