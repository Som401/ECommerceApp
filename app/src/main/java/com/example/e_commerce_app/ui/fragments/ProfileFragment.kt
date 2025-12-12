package com.example.e_commerce_app.ui.fragments

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.e_commerce_app.R
import com.example.e_commerce_app.databinding.FragmentProfileBinding
import com.example.e_commerce_app.ui.activities.OrdersActivity
import com.example.e_commerce_app.ui.auth.LoginActivity
import com.example.e_commerce_app.ui.viewmodel.ProfileViewModel
import com.example.e_commerce_app.utils.LocaleHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProfileViewModel by viewModels()
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    
    private var currentPhotoUri: Uri? = null
    private var pendingPhotoAction: (() -> Unit)? = null
    
    // Permission launcher for camera
    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            pendingPhotoAction?.invoke()
        } else {
            // Check if user permanently denied permission
            if (!shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                // Permission permanently denied, show dialog to go to settings
                showPermissionSettingsDialog("Camera")
            } else {
                Toast.makeText(requireContext(), "Camera permission denied", Toast.LENGTH_SHORT).show()
            }
        }
        pendingPhotoAction = null
    }
    
    // Permission launcher for gallery (Android 13+)
    private val galleryPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            pendingPhotoAction?.invoke()
        } else {
            // Check if user permanently denied permission
            val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Manifest.permission.READ_MEDIA_IMAGES
            } else {
                Manifest.permission.READ_EXTERNAL_STORAGE
            }
            if (!shouldShowRequestPermissionRationale(permission)) {
                // Permission permanently denied, show dialog to go to settings
                showPermissionSettingsDialog("Gallery")
            } else {
                Toast.makeText(requireContext(), "Gallery permission denied", Toast.LENGTH_SHORT).show()
            }
        }
        pendingPhotoAction = null
    }
    
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
        setupObservers()
        setupClickListeners()
        viewModel.loadUserProfile()
        viewModel.loadUserStats()
    }

    private fun setupObservers() {
        // Observe user name
        viewModel.userName.observe(viewLifecycleOwner) { name ->
            binding.tvUserName.text = name
        }
        
        // Observe user email
        viewModel.userEmail.observe(viewLifecycleOwner) { email ->
            binding.tvUserEmail.text = email
        }
        
        // Observe photo URL
        viewModel.photoUrl.observe(viewLifecycleOwner) { url ->
            if (!url.isNullOrEmpty()) {
                loadProfilePhoto(url)
            }
        }
        
        // Observe statistics
        viewModel.purchasesCount.observe(viewLifecycleOwner) { count ->
            binding.tvOrdersCount.text = count.toString()
        }
        
        viewModel.wishlistCount.observe(viewLifecycleOwner) { count ->
            binding.tvWishlistCount.text = count.toString()
        }
        
        // Cart count is not displayed in the UI, but keeping the observer
        viewModel.cartCount.observe(viewLifecycleOwner) { count ->
            // Cart count can be used if needed in future
        }
    }

    private fun loadProfilePhoto(url: String) {
        try {
            val uri = Uri.parse(url)
            binding.ivProfilePhoto.setImageURI(uri)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setupClickListeners() {
        binding.btnChangePhoto.setOnClickListener {
            showPhotoOptions()
        }
        
        binding.btnMyOrders.setOnClickListener {
            val intent = Intent(requireContext(), OrdersActivity::class.java)
            startActivity(intent)
        }

        binding.btnSwitchLanguage.setOnClickListener {
            showLanguageDialog()
        }
        
        // About button opens the README/About screen
        binding.btnAbout.setOnClickListener {
            val intent = Intent(requireContext(), com.example.e_commerce_app.ui.activities.AboutActivity::class.java)
            startActivity(intent)
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
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                launchCamera()
            }
            else -> {
                pendingPhotoAction = { launchCamera() }
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }
    
    private fun launchCamera() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        takePictureLauncher.launch(takePictureIntent)
    }
    
    private fun openGallery() {
        // For Android 13+ (API 33+), we need READ_MEDIA_IMAGES permission
        // For older versions, READ_EXTERNAL_STORAGE
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                permission
            ) == PackageManager.PERMISSION_GRANTED -> {
                launchGallery()
            }
            else -> {
                pendingPhotoAction = { launchGallery() }
                galleryPermissionLauncher.launch(permission)
            }
        }
    }
    
    private fun launchGallery() {
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
                android.util.Log.d("ProfilePhoto", "Saving photo locally for user: $userId")
                
                // Save image to internal storage instead of Firebase Storage
                val inputStream = requireContext().contentResolver.openInputStream(uri)
                val filename = "profile_photo_$userId.jpg"
                val file = java.io.File(requireContext().filesDir, filename)
                
                inputStream?.use { input ->
                    file.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                
                val localPhotoPath = file.absolutePath
                android.util.Log.d("ProfilePhoto", "Photo saved locally: $localPhotoPath")
                
                // Update via ViewModel
                viewModel.updatePhotoUrl(localPhotoPath)
                
                android.util.Log.d("ProfilePhoto", "Photo URL updated")
                Toast.makeText(requireContext(), "Profile photo updated!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                android.util.Log.e("ProfilePhoto", "Save failed", e)
                Toast.makeText(requireContext(), "Failed to save photo: ${e.message}", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun showLanguageDialog() {
        val currentLanguage = LocaleHelper.getLanguage(requireContext())
        val languages = arrayOf(
            getString(R.string.english),
            getString(R.string.french)
        )
        val languageCodes = arrayOf(
            LocaleHelper.LANGUAGE_ENGLISH,
            LocaleHelper.LANGUAGE_FRENCH
        )
        
        val selectedIndex = languageCodes.indexOf(currentLanguage)
        
        val builder = android.app.AlertDialog.Builder(requireContext())
        builder.setTitle(getString(R.string.switch_language))
        builder.setSingleChoiceItems(languages, selectedIndex) { dialog, which ->
            val selectedLanguage = languageCodes[which]
            if (selectedLanguage != currentLanguage) {
                LocaleHelper.setLanguage(requireContext(), selectedLanguage)
                // Activity will be recreated automatically by AppCompatDelegate
            }
            dialog.dismiss()
        }
        builder.setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }
    
    private fun showPermissionSettingsDialog(permissionType: String) {
        val builder = android.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Permission Required")
        builder.setMessage("$permissionType permission is required to use this feature. Please enable it in the app settings.")
        builder.setPositiveButton("Go to Settings") { _, _ ->
            // Open app settings
            val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", requireContext().packageName, null)
            intent.data = uri
            startActivity(intent)
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
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
