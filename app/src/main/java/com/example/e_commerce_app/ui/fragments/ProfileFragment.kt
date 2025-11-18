package com.example.e_commerce_app.ui.fragments

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.e_commerce_app.databinding.FragmentProfileBinding
import com.example.e_commerce_app.ui.auth.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    
    private var currentPhotoUri: Uri? = null
    
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->
                currentPhotoUri = uri
                binding.ivProfilePhoto.setImageURI(uri)
                uploadPhotoToFirebase(uri)
            }
        }
    }
    
    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val imageBitmap = result.data?.extras?.get("data") as? android.graphics.Bitmap
            imageBitmap?.let { bitmap ->
                binding.ivProfilePhoto.setImageBitmap(bitmap)
                // Convert bitmap to URI and upload
                val uri = getImageUriFromBitmap(bitmap)
                uri?.let { uploadPhotoToFirebase(it) }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadUserInfo()
        setupClickListeners()
    }

    private fun loadUserInfo() {
        val userId = auth.currentUser?.uid ?: return
        val userEmail = auth.currentUser?.email

        lifecycleScope.launch {
            try {
                val userDoc = firestore.collection("Users").document(userId).get().await()
                val userName = userDoc.getString("name") ?: "User"
                val photoUrl = userDoc.getString("photoUrl")
                
                binding.tvUserName.text = userName
                binding.tvUserEmail.text = userEmail
                
                // Load profile photo if exists
                // TODO: Load photo using Glide or similar library
                
            } catch (e: Exception) {
                binding.tvUserName.text = "User"
                binding.tvUserEmail.text = userEmail
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnChangePhoto.setOnClickListener {
            showPhotoOptions()
        }

        binding.btnLogout.setOnClickListener {
            logout()
        }
    }
    
    private fun showPhotoOptions() {
        val options = arrayOf("Take Photo", "Choose from Gallery", "Cancel")
        val builder = android.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Select Photo")
        builder.setItems(options) { dialog, which ->
            when (which) {
                0 -> openCamera()
                1 -> openGallery()
                2 -> dialog.dismiss()
            }
        }
        builder.show()
    }
    
    private fun openCamera() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        takePictureLauncher.launch(takePictureIntent)
    }
    
    private fun openGallery() {
        val pickPhotoIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageLauncher.launch(pickPhotoIntent)
    }
    
    private fun getImageUriFromBitmap(bitmap: android.graphics.Bitmap): Uri? {
        return try {
            val bytes = java.io.ByteArrayOutputStream()
            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 100, bytes)
            val path = MediaStore.Images.Media.insertImage(
                requireContext().contentResolver,
                bitmap,
                "ProfilePhoto_${System.currentTimeMillis()}",
                null
            )
            Uri.parse(path)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    private fun uploadPhotoToFirebase(uri: Uri) {
        val userId = auth.currentUser?.uid ?: return
        binding.progressBar.visibility = View.VISIBLE
        
        lifecycleScope.launch {
            try {
                val storageRef = storage.reference.child("profile_photos/$userId.jpg")
                storageRef.putFile(uri).await()
                val downloadUrl = storageRef.downloadUrl.await()
                
                // Update Firestore with photo URL
                firestore.collection("Users")
                    .document(userId)
                    .update("photoUrl", downloadUrl.toString())
                    .await()
                
                Toast.makeText(requireContext(), "Profile photo updated!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Failed to upload photo: ${e.message}", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun logout() {
        auth.signOut()
        val intent = Intent(requireActivity(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
