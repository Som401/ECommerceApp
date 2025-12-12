package com.example.e_commerce_app.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.e_commerce_app.databinding.ActivityAboutBinding

/**
 * About/README screen showing project information
 * Fulfills the project requirement for a README screen in the app
 */
class AboutActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityAboutBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupUI()
    }
    
    private fun setupUI() {
        binding.btnBack.setOnClickListener {
            finish()
        }
    }
}
