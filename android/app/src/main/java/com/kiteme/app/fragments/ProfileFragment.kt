package com.kiteme.app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.kiteme.app.MainActivity
import com.kiteme.app.R
import com.kiteme.app.api.ApiClient
import com.kiteme.app.api.Post
import com.kiteme.app.api.User
import kotlinx.coroutines.*

class ProfileFragment : Fragment() {
    
    private var userId: Int = 1
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private val posts = mutableListOf<Post>()
    
    companion object {
        fun newInstance(userId: Int): ProfileFragment {
            return ProfileFragment().apply {
                arguments = Bundle().apply {
                    putInt("userId", userId)
                }
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userId = arguments?.getInt("userId") ?: MainActivity.currentUserId
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadProfile(view)
        
        // Settings button
        view.findViewById<ImageButton>(R.id.btn_settings)?.setOnClickListener {
            (activity as? MainActivity)?.navigateToSettings()
        }
    }
    
    private fun loadProfile(view: View) {
        val progressBar = view.findViewById<ProgressBar>(R.id.progress_bar)
        progressBar.visibility = View.VISIBLE
        
        scope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    ApiClient.api.getUser(userId, MainActivity.currentUserId)
                }
                if (response.isSuccessful && response.body() != null) {
                    displayProfile(view, response.body()!!)
                    loadUserPosts(view)
                }
            } catch (e: Exception) {
                Toast.makeText(context, getString(R.string.profile_error), Toast.LENGTH_SHORT).show()
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }
    
    private fun displayProfile(view: View, user: User) {
        view.findViewById<TextView>(R.id.txt_name).text = user.name
        view.findViewById<TextView>(R.id.txt_username).text = "@${user.username}"
        view.findViewById<TextView>(R.id.txt_bio).text = user.bio ?: ""
        view.findViewById<TextView>(R.id.txt_location).text = user.location ?: ""
        view.findViewById<TextView>(R.id.txt_level).text = getLevelEmoji(user.level)
        
        view.findViewById<TextView>(R.id.txt_followers).text = "${user.followers_count}\n${getString(R.string.profile_followers)}"
        view.findViewById<TextView>(R.id.txt_following).text = "${user.following_count}\n${getString(R.string.profile_following)}"
        view.findViewById<TextView>(R.id.txt_posts_count).text = "${user.posts_count}\n${getString(R.string.profile_posts)}"
        
        view.findViewById<ImageView>(R.id.img_verified).visibility = 
            if (user.verified == 1) View.VISIBLE else View.GONE
        
        user.avatar_url?.let {
            Glide.with(this)
                .load(it)
                .circleCrop()
                .into(view.findViewById(R.id.img_avatar))
        }
        
        val btnFollow = view.findViewById<Button>(R.id.btn_follow)
        val btnMessage = view.findViewById<Button>(R.id.btn_message)
        
        if (userId == MainActivity.currentUserId) {
            btnFollow.visibility = View.GONE
            btnMessage.text = getString(R.string.profile_edit)
        } else {
            btnFollow.visibility = View.VISIBLE
            btnFollow.text = if (user.is_following) getString(R.string.profile_following_btn) else getString(R.string.profile_follow)
            btnMessage.text = getString(R.string.profile_message)
            
            btnFollow.setOnClickListener {
                toggleFollow(btnFollow, user)
            }
            
            btnMessage.setOnClickListener {
                (activity as? MainActivity)?.navigateToConversation(userId, user.name)
            }
        }
        
        user.instagram?.let { ig ->
            view.findViewById<TextView>(R.id.txt_instagram)?.apply {
                text = ig
                visibility = View.VISIBLE
            }
        }
    }
    
    private fun toggleFollow(btn: Button, user: User) {
        scope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    ApiClient.api.followUser(userId, mapOf("follower_id" to MainActivity.currentUserId))
                }
                if (response.isSuccessful && response.body() != null) {
                    btn.text = if (response.body()!!.following) getString(R.string.profile_following_btn) else getString(R.string.profile_follow)
                }
            } catch (e: Exception) {
                Toast.makeText(context, getString(R.string.profile_follow_error), Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun loadUserPosts(view: View) {
        scope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    ApiClient.api.getUserPosts(userId)
                }
                if (response.isSuccessful && response.body() != null) {
                    posts.clear()
                    posts.addAll(response.body()!!)
                    
                    val recycler = view.findViewById<RecyclerView>(R.id.recycler_posts)
                    recycler.layoutManager = LinearLayoutManager(context)
                    recycler.adapter = FeedAdapter(posts) { post ->
                        (activity as? MainActivity)?.navigateToProfile(post.user_id)
                    }
                }
            } catch (e: Exception) {
                // Silent fail for posts
            }
        }
    }
    
    private fun getLevelEmoji(level: String): String {
        return when (level.lowercase()) {
            "iniciante", "beginner" -> getString(R.string.level_beginner)
            "intermediário", "intermediario", "intermediate" -> getString(R.string.level_intermediate)
            "avançado", "avancado", "advanced" -> getString(R.string.level_advanced)
            "profissional", "professional" -> getString(R.string.level_professional)
            else -> level
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        scope.cancel()
    }
}
