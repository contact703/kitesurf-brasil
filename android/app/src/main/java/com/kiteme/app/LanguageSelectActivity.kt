package com.kiteme.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import com.kiteme.app.utils.LocaleHelper

class LanguageSelectActivity : AppCompatActivity() {
    
    private lateinit var radioGroup: RadioGroup
    private lateinit var btnPortuguese: RadioButton
    private lateinit var btnEnglish: RadioButton
    private lateinit var btnContinue: Button
    
    override fun attachBaseContext(newBase: android.content.Context) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase))
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_language_select)
        
        radioGroup = findViewById(R.id.radio_group_language)
        btnPortuguese = findViewById(R.id.radio_portuguese)
        btnEnglish = findViewById(R.id.radio_english)
        btnContinue = findViewById(R.id.btn_continue)
        
        // Pre-select based on system language
        val systemLang = LocaleHelper.getSystemLanguage()
        if (systemLang == "pt") {
            btnPortuguese.isChecked = true
        } else {
            btnEnglish.isChecked = true
        }
        
        btnContinue.setOnClickListener {
            val selectedLanguage = when (radioGroup.checkedRadioButtonId) {
                R.id.radio_portuguese -> "pt"
                R.id.radio_english -> "en"
                else -> "pt"
            }
            
            LocaleHelper.setLocale(this, selectedLanguage)
            LocaleHelper.setFirstLaunchDone(this)
            
            // Go to main activity
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}
