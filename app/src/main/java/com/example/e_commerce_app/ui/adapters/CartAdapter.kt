package com.example.e_commerce_app.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.e_commerce_app.data.model.CartItem
import com.example.e_commerce_app.databinding.ItemCartBinding

class CartAdapter(
    private val items: List<CartItem>,
    private val onQuantityChanged: (CartItem, Int) -> Unit,
    private val onRemoveClick: (CartItem) -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    inner class CartViewHolder(private val binding: ItemCartBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CartItem) {
            binding.apply {
                tvProductName.text = item.productName
                tvProductDetails.text = "Size: ${item.selectedSize}, Color: ${item.selectedColor}"
                tvPrice.text = "$${item.price}"
                tvQuantity.text = item.quantity.toString()

                btnIncrease.setOnClickListener {
                    onQuantityChanged(item, item.quantity + 1)
                }

                btnDecrease.setOnClickListener {
                    if (item.quantity > 1) {
                        onQuantityChanged(item, item.quantity - 1)
                    }
                }

                btnRemove.setOnClickListener {
                    onRemoveClick(item)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = ItemCartBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CartViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size
}
