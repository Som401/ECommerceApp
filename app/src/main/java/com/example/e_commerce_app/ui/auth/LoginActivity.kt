package com.example.e_commerce_app.ui.auth

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.View
import com.example.e_commerce_app.MainActivity
import com.example.e_commerce_app.databinding.ActivityLoginBinding
import com.example.e_commerce_app.ui.activities.BaseActivity
import com.example.e_commerce_app.utils.Extensions.showToast
import com.example.e_commerce_app.utils.FirebaseManager
import com.example.e_commerce_app.utils.LocaleHelper

class LoginActivity : BaseActivity() {

    private lateinit var binding: ActivityLoginBinding
    private var isEmailValid = false
    private var isPasswordValid = false



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupTextWatchers()
        setupClickListeners()
    }

    private fun setupTextWatchers() {
        // Email validation
        binding.etLoginEmail.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val email = s.toString().trim()
                isEmailValid = when {
                    email.isEmpty() -> {
                        binding.tvEmailError.visibility = View.GONE
                        false
                    }
                    !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                        binding.tvEmailError.text = "Please enter a valid email"
                        binding.tvEmailError.visibility = View.VISIBLE
                        false
                    }
                    else -> {
                        binding.tvEmailError.visibility = View.GONE
                        true
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Password validation
        binding.etLoginPassword.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val password = s.toString()
                isPasswordValid = when {
                    password.isEmpty() -> {
                        binding.tvPasswordError.visibility = View.GONE
                        false
                    }
                    password.length < 6 -> {
                        binding.tvPasswordError.text = "Password must be at least 6 characters"
                        binding.tvPasswordError.visibility = View.VISIBLE
                        false
                    }
                    else -> {
                        binding.tvPasswordError.visibility = View.GONE
                        true
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            performLogin()
        }

        binding.tvSignUp.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        binding.tvForgotPassword.setOnClickListener {
            showToast("Password reset feature coming soon")
        }
    }

    private fun performLogin() {
        val email = binding.etLoginEmail.text.toString().trim()
        val password = binding.etLoginPassword.text.toString().trim()

        // Validate inputs
        if (email.isEmpty()) {
            binding.tvEmailError.text = "Email is required"
            binding.tvEmailError.visibility = View.VISIBLE
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tvEmailError.text = "Please enter a valid email"
            binding.tvEmailError.visibility = View.VISIBLE
            return
        }

        if (password.isEmpty()) {
            binding.tvPasswordError.text = "Password is required"
            binding.tvPasswordError.visibility = View.VISIBLE
            return
        }

        if (password.length < 6) {
            binding.tvPasswordError.text = "Password must be at least 6 characters"
            binding.tvPasswordError.visibility = View.VISIBLE
            return
        }

        // Show loading
        binding.progressBar.visibility = View.VISIBLE
        binding.btnLogin.isEnabled = false

        // Authenticate with Firebase
        FirebaseManager.auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                binding.progressBar.visibility = View.GONE
                showToast("Welcome back!")
                // Navigate to main activity
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
            .addOnFailureListener { exception ->
                binding.progressBar.visibility = View.GONE
                binding.btnLogin.isEnabled = true
                
                val errorMessage = when {
                    exception.message?.contains("password") == true -> "Incorrect password"
                    exception.message?.contains("user") == true -> "No account found with this email"
                    exception.message?.contains("network") == true -> "Network error. Please check your connection"
                    else -> "Login failed. Please try again"
                }
                showToast(errorMessage)
            }
    }

    override fun onStart() {
        super.onStart()
        // Check if user is already logged in
        if (FirebaseManager.isUserLoggedIn) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}
