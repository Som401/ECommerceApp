package com.example.e_commerce_app

import android.app.Application
import android.content.Context
import com.example.e_commerce_app.utils.LocaleHelper

class ECommerceApplication : Application() {
    
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(LocaleHelper.applyLanguage(base))
    }
    
    override fun onCreate() {
        super.onCreate()
        // Apply language on app start
        LocaleHelper.applyLanguage(this)
    }
}
