package com.example.e_commerce_app.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.e_commerce_app.data.cache.WishlistCache
import com.example.e_commerce_app.data.model.Product
import com.example.e_commerce_app.databinding.ItemProductBinding
import com.example.e_commerce_app.utils.GlobalCurrency
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ProductAdapter(
    private var products: MutableList<Product>,
    private val onProductClick: (Product) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    inner class ProductViewHolder(private val binding: ItemProductBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(product: Product) {
            binding.apply {
                tvProductName.text = product.name
                tvProductBrand.text = product.brand
                tvProductPrice.text = product.getFormattedPrice(GlobalCurrency.currentCurrency)
                
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
                GlobalScope.launch {
                    val isInWishlist = WishlistCache.isInWishlist(product.id)
                    root.post {
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
                }
                
                // Handle favorite click
                ivFavorite.setOnClickListener {
                    GlobalScope.launch {
                        val context = ivFavorite.context
                        val currentlyFav = WishlistCache.isInWishlist(product.id)
                        
                        if (currentlyFav) {
                            val success = WishlistCache.removeFromWishlist(product.id)
                            
                            root.post {
                                if (success) {
                                    ivFavorite.setImageResource(com.example.e_commerce_app.R.drawable.ic_favorite_border)
                                    ImageViewCompat.setImageTintList(
                                        ivFavorite,
                                        android.content.res.ColorStateList.valueOf(
                                            ContextCompat.getColor(context, com.example.e_commerce_app.R.color.grayText)
                                        )
                                    )
                                    Toast.makeText(context, "Removed from wishlist", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Error removing from wishlist", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            val success = WishlistCache.addToWishlist(product.id)
                            
                            root.post {
                                if (success) {
                                    ivFavorite.setImageResource(com.example.e_commerce_app.R.drawable.ic_favorite)
                                    ImageViewCompat.setImageTintList(
                                        ivFavorite,
                                        android.content.res.ColorStateList.valueOf(
                                            ContextCompat.getColor(context, com.example.e_commerce_app.R.color.primary)
                                        )
                                    )
                                    Toast.makeText(context, "Added to wishlist", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Error adding to wishlist", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                }
                
                // Handle item click
                root.setOnClickListener {
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
        products.clear()
        products.addAll(newList)
        notifyDataSetChanged()
    }
}
