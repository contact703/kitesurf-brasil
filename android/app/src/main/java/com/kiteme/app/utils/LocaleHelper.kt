package com.kiteme.app.utils

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import java.util.Locale

object LocaleHelper {
    
    private const val PREFS_NAME = "kiteme_prefs"
    private const val LANGUAGE_KEY = "selected_language"
    private const val FIRST_LAUNCH_KEY = "first_launch"
    
    fun setLocale(context: Context, language: String): Context {
        persist(context, language)
        return updateResources(context, language)
    }
    
    fun getLanguage(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(LANGUAGE_KEY, "") ?: ""
    }
    
    fun isFirstLaunch(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(FIRST_LAUNCH_KEY, true)
    }
    
    fun setFirstLaunchDone(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(FIRST_LAUNCH_KEY, false).apply()
    }
    
    fun onAttach(context: Context): Context {
        val lang = getLanguage(context)
        return if (lang.isEmpty()) {
            // Use system language if not set
            context
        } else {
            setLocale(context, lang)
        }
    }
    
    private fun persist(context: Context, language: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(LANGUAGE_KEY, language).apply()
    }
    
    private fun updateResources(context: Context, language: String): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)
        
        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)
        
        return context.createConfigurationContext(configuration)
    }
    
    fun getSystemLanguage(): String {
        val systemLocale = Locale.getDefault()
        return when {
            systemLocale.language == "pt" -> "pt"
            else -> "en"
        }
    }
}
