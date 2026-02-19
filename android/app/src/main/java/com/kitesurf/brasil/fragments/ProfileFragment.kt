package com.kitesurf.brasil.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.kitesurf.brasil.MainActivity
import com.kitesurf.brasil.R
import com.kitesurf.brasil.api.ApiClient
import com.kitesurf.brasil.api.Post
import com.kitesurf.brasil.api.User
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
                Toast.makeText(context, "Erro ao carregar perfil", Toast.LENGTH_SHORT).show()
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
        
        view.findViewById<TextView>(R.id.txt_followers).text = "${user.followers_count}\nSeguidores"
        view.findViewById<TextView>(R.id.txt_following).text = "${user.following_count}\nSeguindo"
        view.findViewById<TextView>(R.id.txt_posts_count).text = "${user.posts_count}\nPosts"
        
        view.findViewById<ImageView>(R.id.img_verified).visibility = 
            if (user.verified == 1) View.VISIBLE else View.GONE
        
        user.avatar_url?.let {
            Glide.with(this)
                .load(it)
                .circleCrop()
                .into(view.findViewById(R.id.img_avatar))
        }
        
        // Follow/Message buttons
        val btnFollow = view.findViewById<Button>(R.id.btn_follow)
        val btnMessage = view.findViewById<Button>(R.id.btn_message)
        
        if (userId == MainActivity.currentUserId) {
            btnFollow.visibility = View.GONE
            btnMessage.text = "Editar Perfil"
        } else {
            btnFollow.visibility = View.VISIBLE
            btnFollow.text = if (user.is_following) "Seguindo" else "Seguir"
            btnMessage.text = "Mensagem"
            
            btnFollow.setOnClickListener {
                toggleFollow(btnFollow, user)
            }
            
            btnMessage.setOnClickListener {
                (activity as? MainActivity)?.navigateToConversation(userId, user.name)
            }
        }
        
        // Instagram link
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
                    btn.text = if (response.body()!!.following) "Seguindo" else "Seguir"
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Erro ao seguir", Toast.LENGTH_SHORT).show()
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
            "iniciante" -> "🟢 Iniciante"
            "intermediário", "intermediario" -> "🟡 Intermediário"
            "avançado", "avancado" -> "🔴 Avançado"
            "profissional" -> "🏆 Profissional"
            else -> level
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        scope.cancel()
    }
}
