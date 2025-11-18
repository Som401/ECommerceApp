package com.example.e_commerce_app.utils

import android.widget.Toast
import android.content.Context

object Extensions {
    fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(this, message, duration).show()
    }
}
