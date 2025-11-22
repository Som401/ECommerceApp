package com.example.e_commerce_app.utils

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Build
import java.util.*

object LocaleHelper {
    const val LANGUAGE_ENGLISH = "en"
    const val LANGUAGE_FRENCH = "fr"
    
    /**
     * Set the application language using AppCompatDelegate
     */
    fun setLanguage(context: Context, languageCode: String) {
        val appLocale = androidx.core.os.LocaleListCompat.forLanguageTags(languageCode)
        androidx.appcompat.app.AppCompatDelegate.setApplicationLocales(appLocale)
    }
    
    /**
     * Get the current application language
     */
    fun getLanguage(context: Context): String {
        val currentAppLocales = androidx.appcompat.app.AppCompatDelegate.getApplicationLocales()
        return if (!currentAppLocales.isEmpty) {
            currentAppLocales.get(0)?.language ?: LANGUAGE_ENGLISH
        } else {
            LANGUAGE_ENGLISH
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
