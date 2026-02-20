package com.kiteme.app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.kiteme.app.fragments.*
import com.kiteme.app.utils.LocaleHelper

class MainActivity : AppCompatActivity() {
    
    private lateinit var bottomNav: BottomNavigationView
    
    companion object {
        var currentUserId: Int = 1
        var currentUsername: String = "pedrokiter"
    }
    
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase))
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        // Switch from splash theme to app theme
        setTheme(R.style.Theme_KiteMe)
        super.onCreate(savedInstanceState)
        
        // Check if first launch - show language selection
        if (LocaleHelper.isFirstLaunch(this)) {
            startActivity(Intent(this, LanguageSelectActivity::class.java))
            finish()
            return
        }
        
        setContentView(R.layout.activity_main)
        
        bottomNav = findViewById(R.id.bottom_navigation)
        
        // Start with Feed
        if (savedInstanceState == null) {
            loadFragment(FeedFragment())
        }
        
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_feed -> {
                    loadFragment(FeedFragment())
                    true
                }
                R.id.nav_spots -> {
                    loadFragment(SpotsFragment())
                    true
                }
                R.id.nav_classifieds -> {
                    loadFragment(ClassifiedsFragment())
                    true
                }
                R.id.nav_chat -> {
                    loadFragment(ChatFragment())
                    true
                }
                R.id.nav_profile -> {
                    loadFragment(ProfileFragment.newInstance(currentUserId))
                    true
                }
                else -> false
            }
        }
    }
    
    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
    
    fun navigateToProfile(userId: Int) {
        loadFragment(ProfileFragment.newInstance(userId))
        bottomNav.selectedItemId = R.id.nav_profile
    }
    
    fun navigateToMessages() {
        loadFragment(MessagesFragment())
    }
    
    fun navigateToForum() {
        loadFragment(ForumFragment())
    }
    
    fun navigateToAccommodations() {
        loadFragment(AccommodationsFragment())
    }
    
    fun navigateToConversation(userId: Int, userName: String) {
        loadFragment(ConversationFragment.newInstance(userId, userName))
    }
    
    fun navigateToTopic(topicId: Int) {
        loadFragment(TopicDetailFragment.newInstance(topicId))
    }
    
    fun navigateToSpotDetail(spotId: Int) {
        loadFragment(SpotDetailFragment.newInstance(spotId))
    }
    
    fun navigateToClassifiedDetail(classifiedId: Int) {
        loadFragment(ClassifiedDetailFragment.newInstance(classifiedId))
    }
    
    fun navigateToSettings() {
        loadFragment(SettingsFragment())
    }
}
