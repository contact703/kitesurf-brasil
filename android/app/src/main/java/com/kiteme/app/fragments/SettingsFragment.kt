package com.kiteme.app.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.kiteme.app.LanguageSelectActivity
import com.kiteme.app.R
import com.kiteme.app.utils.LocaleHelper

class SettingsFragment : Fragment() {
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Language setting
        view.findViewById<LinearLayout>(R.id.setting_language)?.setOnClickListener {
            showLanguageDialog()
        }
        
        // Update language display
        val langText = view.findViewById<TextView>(R.id.txt_language_value)
        val currentLang = LocaleHelper.getLanguage(requireContext())
        langText?.text = when (currentLang) {
            "pt" -> "Português (BR)"
            "en" -> "English"
            else -> "Português (BR)"
        }
        
        // Privacy policy
        view.findViewById<LinearLayout>(R.id.setting_privacy)?.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/contact703/kitesurf-brasil/blob/main/PRIVACY_POLICY.md"))
            startActivity(intent)
        }
        
        // About
        view.findViewById<LinearLayout>(R.id.setting_about)?.setOnClickListener {
            showAboutDialog()
        }
        
        // Version
        val versionText = view.findViewById<TextView>(R.id.txt_version)
        try {
            val pInfo = requireContext().packageManager.getPackageInfo(requireContext().packageName, 0)
            versionText?.text = "v${pInfo.versionName}"
        } catch (e: Exception) {
            versionText?.text = "v3.0"
        }
    }
    
    private fun showLanguageDialog() {
        val languages = arrayOf("Português (BR)", "English")
        val langCodes = arrayOf("pt", "en")
        
        val currentLang = LocaleHelper.getLanguage(requireContext())
        val selectedIndex = langCodes.indexOf(currentLang).takeIf { it >= 0 } ?: 0
        
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.settings_language))
            .setSingleChoiceItems(languages, selectedIndex) { dialog, which ->
                val selectedLang = langCodes[which]
                LocaleHelper.setLocale(requireContext(), selectedLang)
                dialog.dismiss()
                
                // Restart activity to apply language
                val intent = Intent(context, LanguageSelectActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                // Skip language selection since already set
                LocaleHelper.setFirstLaunchDone(requireContext())
                activity?.startActivity(intent)
                activity?.finish()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }
    
    private fun showAboutDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Kite-me")
            .setMessage("${getString(R.string.settings_version)}: 3.0\n\n" +
                    "Kite-me is a social network for kitesurfing enthusiasts.\n\n" +
                    "Find spots, buy/sell equipment, chat with our AI assistant KiteBot, and connect with the community!\n\n" +
                    "© 2026 Titanio Films")
            .setPositiveButton("OK", null)
            .show()
    }
}
