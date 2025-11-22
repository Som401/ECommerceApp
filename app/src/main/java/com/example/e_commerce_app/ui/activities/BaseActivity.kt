package com.example.e_commerce_app.ui.activities

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.e_commerce_app.R
import com.example.e_commerce_app.utils.NetworkUtils

open class BaseActivity : AppCompatActivity() {

    private var noInternetDialog: android.app.AlertDialog? = null
    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var networkCallback: ConnectivityManager.NetworkCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupNetworkMonitoring()
    }

    private fun setupNetworkMonitoring() {
        connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                runOnUiThread {
                    noInternetDialog?.dismiss()
                }
            }

            override fun onLost(network: Network) {
                runOnUiThread {
                    showNoInternetDialog()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (!NetworkUtils.isInternetAvailable(this)) {
            showNoInternetDialog()
        }
        try {
            connectivityManager.registerDefaultNetworkCallback(networkCallback)
        } catch (e: Exception) {
            // Handle potential errors
        }
    }

    override fun onPause() {
        super.onPause()
        try {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        } catch (e: Exception) {
            // Handle potential errors
        }
    }

    protected fun showNoInternetDialog() {
        if (noInternetDialog?.isShowing == true) return

        val dialogView = layoutInflater.inflate(R.layout.dialog_no_internet, null)
        val btnRetry = dialogView.findViewById<android.widget.Button>(R.id.btnRetry)

        val builder = android.app.AlertDialog.Builder(this)
        builder.setView(dialogView)
        builder.setCancelable(false)
        
        noInternetDialog = builder.create()
        noInternetDialog?.window?.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))
        
        btnRetry.setOnClickListener {
            if (NetworkUtils.isInternetAvailable(this)) {
                noInternetDialog?.dismiss()
            } else {
                android.widget.Toast.makeText(this, getString(R.string.no_internet), android.widget.Toast.LENGTH_SHORT).show()
            }
        }
        
        noInternetDialog?.show()
    }

    fun showSlowInternetDialog(onRetry: () -> Unit, onWait: () -> Unit) {
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.slow_internet))
        builder.setMessage(getString(R.string.slow_internet_message))
        builder.setCancelable(false)
        
        builder.setPositiveButton(getString(R.string.try_again)) { dialog, _ ->
            dialog.dismiss()
            onRetry()
        }
        
        builder.setNegativeButton(getString(R.string.wait)) { dialog, _ ->
            dialog.dismiss()
            onWait()
        }
        
        builder.show()
    }

    var isOperationInProgress: Boolean = false
}
