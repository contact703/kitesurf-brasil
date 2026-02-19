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
import kotlinx.coroutines.*

class FeedFragment : Fragment() {
    
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var adapter: FeedAdapter
    private val posts = mutableListOf<Post>()
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_feed, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        recyclerView = view.findViewById(R.id.recycler_feed)
        progressBar = view.findViewById(R.id.progress_bar)
        
        // Quick access buttons
        view.findViewById<LinearLayout>(R.id.btn_messages)?.setOnClickListener {
            (activity as? MainActivity)?.navigateToMessages()
        }
        view.findViewById<LinearLayout>(R.id.btn_forum)?.setOnClickListener {
            (activity as? MainActivity)?.navigateToForum()
        }
        view.findViewById<LinearLayout>(R.id.btn_pousadas)?.setOnClickListener {
            (activity as? MainActivity)?.navigateToAccommodations()
        }
        
        adapter = FeedAdapter(posts) { post ->
            (activity as? MainActivity)?.navigateToProfile(post.user_id)
        }
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
        
        loadFeed()
    }
    
    private fun loadFeed() {
        progressBar.visibility = View.VISIBLE
        scope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    ApiClient.api.getFeed(MainActivity.currentUserId)
                }
                if (response.isSuccessful && response.body() != null) {
                    posts.clear()
                    posts.addAll(response.body()!!)
                    adapter.notifyDataSetChanged()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Erro ao carregar feed", Toast.LENGTH_SHORT).show()
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        scope.cancel()
    }
}

class FeedAdapter(
    private val posts: List<Post>,
    private val onProfileClick: (Post) -> Unit
) : RecyclerView.Adapter<FeedAdapter.ViewHolder>() {
    
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val avatar: ImageView = view.findViewById(R.id.img_avatar)
        val name: TextView = view.findViewById(R.id.txt_name)
        val username: TextView = view.findViewById(R.id.txt_username)
        val content: TextView = view.findViewById(R.id.txt_content)
        val spotName: TextView = view.findViewById(R.id.txt_spot)
        val likes: TextView = view.findViewById(R.id.txt_likes)
        val comments: TextView = view.findViewById(R.id.txt_comments)
        val verified: ImageView = view.findViewById(R.id.img_verified)
        val btnLike: ImageButton = view.findViewById(R.id.btn_like)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_post, parent, false)
        return ViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val post = posts[position]
        
        holder.name.text = post.name
        holder.username.text = "@${post.username}"
        holder.content.text = post.content ?: ""
        holder.likes.text = "${post.likes_count}"
        holder.comments.text = "${post.comments_count}"
        holder.verified.visibility = if (post.verified == 1) View.VISIBLE else View.GONE
        
        if (!post.spot_name.isNullOrEmpty()) {
            holder.spotName.text = "📍 ${post.spot_name}"
            holder.spotName.visibility = View.VISIBLE
        } else {
            holder.spotName.visibility = View.GONE
        }
        
        post.avatar_url?.let {
            Glide.with(holder.itemView.context)
                .load(it)
                .circleCrop()
                .into(holder.avatar)
        }
        
        holder.avatar.setOnClickListener { onProfileClick(post) }
        holder.name.setOnClickListener { onProfileClick(post) }
    }
    
    override fun getItemCount() = posts.size
}
