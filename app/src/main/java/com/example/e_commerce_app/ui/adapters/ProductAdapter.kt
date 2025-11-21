package com.example.e_commerce_app.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.e_commerce_app.data.model.Product
import com.example.e_commerce_app.data.repository.ProductRepository
import com.example.e_commerce_app.databinding.ItemProductBinding
import kotlinx.coroutines.launch

class ProductAdapter(
    private var products: MutableList<Product>,
    private val lifecycleScope: LifecycleCoroutineScope,
    private val onProductClick: (Product) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    private val repository = ProductRepository()

    inner class ProductViewHolder(private val binding: ItemProductBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(product: Product) {
            binding.apply {
                android.util.Log.d("ProductDebug", "Bind adapter product id='${product.id}' name='${product.name}' brand='${product.brand}'")
                tvProductName.text = product.name
                tvProductBrand.text = product.brand
                
                if (product.discount > 0) {
                    tvProductPrice.text = "$${product.getPriceAfterDiscount()}"
                } else {
                    tvProductPrice.text = "$${product.price}"
                }
                
                ratingBar.rating = product.rating
                
                // Load image with Glide
                if (product.imageUrl.isNotEmpty()) {
                    Glide.with(ivProduct.context)
                        .load(product.imageUrl)
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.ic_menu_report_image)
                        .into(ivProduct)
                }
                
                // Check if in wishlist and update icon
                lifecycleScope.launch {
                    val isInWishlist = repository.isInWishlist(product.id)
                    if (isInWishlist) {
                        ivFavorite.setImageResource(com.example.e_commerce_app.R.drawable.ic_favorite)
                        ImageViewCompat.setImageTintList(
                            ivFavorite,
                            android.content.res.ColorStateList.valueOf(
                                ContextCompat.getColor(ivFavorite.context, com.example.e_commerce_app.R.color.primary)
                            )
                        )
                    } else {
                        ivFavorite.setImageResource(com.example.e_commerce_app.R.drawable.ic_favorite_border)
                        ImageViewCompat.setImageTintList(
                            ivFavorite,
                            android.content.res.ColorStateList.valueOf(
                                ContextCompat.getColor(ivFavorite.context, com.example.e_commerce_app.R.color.grayText)
                            )
                        )
                    }
                }
                
                // Handle favorite click
                ivFavorite.setOnClickListener {
                    lifecycleScope.launch {
                        // Optimistic UI toggle
                        val currentlyFav = repository.isInWishlist(product.id)
                        if (currentlyFav) {
                            ivFavorite.setImageResource(com.example.e_commerce_app.R.drawable.ic_favorite_border)
                            ImageViewCompat.setImageTintList(
                                ivFavorite,
                                android.content.res.ColorStateList.valueOf(
                                    ContextCompat.getColor(ivFavorite.context, com.example.e_commerce_app.R.color.grayText)
                                )
                            )
                            try {
                                repository.removeFromWishlist(product.id)
                            } catch (_: Exception) {
                                // Revert on failure
                                ivFavorite.setImageResource(com.example.e_commerce_app.R.drawable.ic_favorite)
                                ImageViewCompat.setImageTintList(
                                    ivFavorite,
                                    android.content.res.ColorStateList.valueOf(
                                        ContextCompat.getColor(ivFavorite.context, com.example.e_commerce_app.R.color.primary)
                                    )
                                )
                            }
                        } else {
                            ivFavorite.setImageResource(com.example.e_commerce_app.R.drawable.ic_favorite)
                            ImageViewCompat.setImageTintList(
                                ivFavorite,
                                android.content.res.ColorStateList.valueOf(
                                    ContextCompat.getColor(ivFavorite.context, com.example.e_commerce_app.R.color.primary)
                                )
                            )
                            try {
                                repository.addToWishlist(product.id)
                            } catch (_: Exception) {
                                // Revert on failure
                                ivFavorite.setImageResource(com.example.e_commerce_app.R.drawable.ic_favorite_border)
                                ImageViewCompat.setImageTintList(
                                    ivFavorite,
                                    android.content.res.ColorStateList.valueOf(
                                        ContextCompat.getColor(ivFavorite.context, com.example.e_commerce_app.R.color.grayText)
                                    )
                                )
                            }
                        }
                    }
                }
                
                // Handle item click
                root.setOnClickListener {
                    android.util.Log.d("ProductDebug", "Card click product id='${product.id}' name='${product.name}'")
                    onProductClick(product)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(products[position])
    }

    override fun getItemCount() = products.size

    fun updateProducts(newList: List<Product>) {
        android.util.Log.d("ProductDebug", "ProductAdapter updateProducts size=${newList.size}")
        products.clear()
        products.addAll(newList)
        notifyDataSetChanged()
    }
}
