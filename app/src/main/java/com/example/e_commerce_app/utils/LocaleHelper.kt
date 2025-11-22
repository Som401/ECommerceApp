package com.example.e_commerce_app.utils

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Build
import java.util.*

object LocaleHelper {
    private const val PREFS_NAME = "language_prefs"
    private const val KEY_LANGUAGE = "selected_language"
    
    const val LANGUAGE_ENGLISH = "en"
    const val LANGUAGE_FRENCH = "fr"
    
    /**
     * Save the selected language to SharedPreferences
     */
    fun setLanguage(context: Context, languageCode: String) {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LANGUAGE, languageCode).apply()
    }
    
    /**
     * Get the saved language from SharedPreferences
     */
    fun getLanguage(context: Context): String {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_LANGUAGE, LANGUAGE_ENGLISH) ?: LANGUAGE_ENGLISH
    }
    
    /**
     * Apply the saved language configuration
     */
    fun applyLanguage(context: Context): Context {
        val languageCode = getLanguage(context)
        return updateLocale(context, languageCode)
    }
    
    /**
     * Update the locale for the context
     */
    private fun updateLocale(context: Context, languageCode: String): Context {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        
        val resources = context.resources
        val configuration = Configuration(resources.configuration)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.setLocale(locale)
        } else {
            @Suppress("DEPRECATION")
            configuration.locale = locale
        }
        
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.createConfigurationContext(configuration)
        } else {
            @Suppress("DEPRECATION")
            resources.updateConfiguration(configuration, resources.displayMetrics)
            context
        }
    }
    
    /**
     * Get the display name for a language code
     */
    fun getLanguageName(context: Context, languageCode: String): String {
        return when (languageCode) {
            LANGUAGE_ENGLISH -> context.getString(com.example.e_commerce_app.R.string.english)
            LANGUAGE_FRENCH -> context.getString(com.example.e_commerce_app.R.string.french)
            else -> context.getString(com.example.e_commerce_app.R.string.english)
        }
    }
}
