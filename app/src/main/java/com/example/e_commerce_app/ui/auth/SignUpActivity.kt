package com.example.e_commerce_app.ui.auth

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.e_commerce_app.MainActivity
import com.example.e_commerce_app.data.model.User
import com.example.e_commerce_app.databinding.ActivitySignUpBinding
import com.example.e_commerce_app.utils.Extensions.showToast
import com.example.e_commerce_app.utils.FirebaseManager
import com.example.e_commerce_app.utils.LocaleHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private val userCollection = FirebaseManager.firestore.collection("Users")



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupTextWatchers()
        setupClickListeners()
    }

    private fun setupTextWatchers() {
        // Name validation
        binding.etSignUpName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val name = s.toString().trim()
                if (name.isNotEmpty() && name.length < 3) {
                    binding.tvSignUpError.text = "Name must be at least 3 characters"
                    binding.tvSignUpError.visibility = View.VISIBLE
                } else {
                    binding.tvSignUpError.visibility = View.GONE
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Email validation
        binding.etSignUpEmail.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val email = s.toString().trim()
                if (email.isNotEmpty() && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    binding.tvSignUpError.text = "Please enter a valid email address"
                    binding.tvSignUpError.visibility = View.VISIBLE
                } else {
                    binding.tvSignUpError.visibility = View.GONE
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Password validation
        binding.etSignUpPassword.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val password = s.toString()
                if (password.isNotEmpty() && password.length < 6) {
                    binding.tvSignUpError.text = "Password must be at least 6 characters"
                    binding.tvSignUpError.visibility = View.VISIBLE
                } else {
                    binding.tvSignUpError.visibility = View.GONE
                }
                checkPasswordMatch()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Confirm password validation
        binding.etConfirmPassword.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                checkPasswordMatch()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun checkPasswordMatch() {
        val password = binding.etSignUpPassword.text.toString()
        val confirmPassword = binding.etConfirmPassword.text.toString()
        
        if (confirmPassword.isNotEmpty() && password != confirmPassword) {
            binding.tvSignUpError.text = "Passwords do not match"
            binding.tvSignUpError.visibility = View.VISIBLE
        } else if (password == confirmPassword && confirmPassword.isNotEmpty()) {
            binding.tvSignUpError.visibility = View.GONE
        }
    }

    private fun setupClickListeners() {
        binding.btnSignUp.setOnClickListener {
            performSignUp()
        }

        binding.tvLogin.setOnClickListener {
            finish() // Go back to login
        }
    }

    private fun performSignUp() {
        val name = binding.etSignUpName.text.toString().trim()
        val email = binding.etSignUpEmail.text.toString().trim()
        val password = binding.etSignUpPassword.text.toString()
        val confirmPassword = binding.etConfirmPassword.text.toString()

        // Validate all fields
        if (name.isEmpty()) {
            binding.tvSignUpError.text = "Please enter your full name"
            binding.tvSignUpError.visibility = View.VISIBLE
            return
        }

        if (name.length < 3) {
            binding.tvSignUpError.text = "Name must be at least 3 characters"
            binding.tvSignUpError.visibility = View.VISIBLE
            return
        }

        if (email.isEmpty()) {
            binding.tvSignUpError.text = "Please enter your email"
            binding.tvSignUpError.visibility = View.VISIBLE
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tvSignUpError.text = "Please enter a valid email address"
            binding.tvSignUpError.visibility = View.VISIBLE
            return
        }

        if (password.isEmpty()) {
            binding.tvSignUpError.text = "Please enter a password"
            binding.tvSignUpError.visibility = View.VISIBLE
            return
        }

        if (password.length < 6) {
            binding.tvSignUpError.text = "Password must be at least 6 characters"
            binding.tvSignUpError.visibility = View.VISIBLE
            return
        }

        if (password != confirmPassword) {
            binding.tvSignUpError.text = "Passwords do not match"
            binding.tvSignUpError.visibility = View.VISIBLE
            return
        }

        // Show loading
        binding.progressBar.visibility = View.VISIBLE
        binding.btnSignUp.isEnabled = false
        binding.tvSignUpError.visibility = View.GONE

        // Create account with Firebase
        FirebaseManager.auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val userId = authResult.user?.uid ?: ""
                val user = User(
                    fullName = name,
                    email = email,
                    userId = userId,
                    profileImage = "",
                    phoneNumber = "",
                    address = ""
                )
                
                // Save user data to Firestore
                saveUserData(user)
            }
            .addOnFailureListener { exception ->
                binding.progressBar.visibility = View.GONE
                binding.btnSignUp.isEnabled = true
                
                val errorMessage = when {
                    exception.message?.contains("already in use") == true -> "This email is already registered"
                    exception.message?.contains("network") == true -> "Network error. Please check your connection"
                    else -> "Registration failed. Please try again"
                }
                
                binding.tvSignUpError.text = errorMessage
                binding.tvSignUpError.visibility = View.VISIBLE
            }
    }

    private fun saveUserData(user: User) = CoroutineScope(Dispatchers.IO).launch {
        try {
            userCollection.document(user.userId).set(user).await()
            
            withContext(Dispatchers.Main) {
                binding.progressBar.visibility = View.GONE
                showToast("Account created successfully!")
                
                // Navigate to main activity
                startActivity(Intent(this@SignUpActivity, MainActivity::class.java))
                finish()
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                binding.progressBar.visibility = View.GONE
                binding.btnSignUp.isEnabled = true
                
                binding.tvSignUpError.text = "Failed to save user data: ${e.message}"
                binding.tvSignUpError.visibility = View.VISIBLE
            }
        }
    }
}
