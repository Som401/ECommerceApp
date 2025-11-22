package com.example.e_commerce_app

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.e_commerce_app.databinding.ActivityMainBinding
import com.example.e_commerce_app.ui.fragments.HomeFragment
import com.example.e_commerce_app.ui.fragments.ShopFragment
import com.example.e_commerce_app.ui.fragments.BagFragment
import com.example.e_commerce_app.ui.fragments.FavoritesFragment
import com.example.e_commerce_app.ui.fragments.ProfileFragment
import com.example.e_commerce_app.utils.LocaleHelper
import com.google.android.material.navigation.NavigationBarView

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up bottom navigation
        binding.bottomNavigation.setOnItemSelectedListener(navListener)
        
        // Load home fragment by default
        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
        }
    }

    private val navListener = NavigationBarView.OnItemSelectedListener { item ->
        val fragment: Fragment = when (item.itemId) {
            R.id.nav_home -> HomeFragment()
            R.id.nav_shop -> ShopFragment()
            R.id.nav_bag -> BagFragment()
            R.id.nav_favorites -> FavoritesFragment()
            R.id.nav_profile -> ProfileFragment()
            else -> HomeFragment()
        }
        loadFragment(fragment)
        true
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}