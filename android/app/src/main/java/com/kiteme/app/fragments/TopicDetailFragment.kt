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
import com.kiteme.app.api.*
import kotlinx.coroutines.*

class TopicDetailFragment : Fragment() {
    
    private var topicId: Int = 0
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private val replies = mutableListOf<ForumReply>()
    private lateinit var adapter: RepliesAdapter
    
    companion object {
        fun newInstance(topicId: Int): TopicDetailFragment {
            return TopicDetailFragment().apply {
                arguments = Bundle().apply {
                    putInt("topicId", topicId)
                }
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        topicId = arguments?.getInt("topicId") ?: 0
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_topic_detail, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        view.findViewById<ImageButton>(R.id.btn_back).setOnClickListener {
            activity?.onBackPressed()
        }
        
        adapter = RepliesAdapter(replies) { reply ->
            (activity as? MainActivity)?.navigateToProfile(reply.user_id)
        }
        view.findViewById<RecyclerView>(R.id.recycler_replies).apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@TopicDetailFragment.adapter
        }
        
        view.findViewById<Button>(R.id.btn_reply).setOnClickListener {
            showReplyDialog(view)
        }
        
        loadTopic(view)
    }
    
    private fun loadTopic(view: View) {
        val progressBar = view.findViewById<ProgressBar>(R.id.progress_bar)
        progressBar.visibility = View.VISIBLE
        
        scope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    ApiClient.api.getTopic(topicId)
                }
                if (response.isSuccessful && response.body() != null) {
                    displayTopic(view, response.body()!!)
                }
            } catch (e: Exception) {
                Toast.makeText(context, getString(R.string.forum_error), Toast.LENGTH_SHORT).show()
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }
    
    private fun displayTopic(view: View, topic: TopicDetail) {
        view.findViewById<TextView>(R.id.txt_title).text = topic.title
        view.findViewById<TextView>(R.id.txt_content).text = topic.content
        view.findViewById<TextView>(R.id.txt_author).text = "por @${topic.author_username}"
        view.findViewById<TextView>(R.id.txt_category).text = topic.category_name ?: ""
        view.findViewById<TextView>(R.id.txt_stats).text = "👁 ${topic.views_count}  💬 ${topic.replies_count}  ❤️ ${topic.likes_count}"
        
        topic.author_avatar?.let {
            Glide.with(this)
                .load(it)
                .circleCrop()
                .into(view.findViewById(R.id.img_avatar))
        }
        
        replies.clear()
        replies.addAll(topic.replies)
        adapter.notifyDataSetChanged()
        
        view.findViewById<TextView>(R.id.txt_replies_count).text = "${replies.size} ${getString(R.string.forum_replies)}"
    }
    
    private fun showReplyDialog(view: View) {
        val input = EditText(context).apply {
            hint = getString(R.string.chat_hint)
            minLines = 3
            setPadding(32, 32, 32, 32)
        }
        
        android.app.AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.profile_message))
            .setView(input)
            .setPositiveButton(getString(R.string.save)) { _, _ ->
                val content = input.text.toString().trim()
                if (content.isNotEmpty()) {
                    sendReply(content, view)
                }
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }
    
    private fun sendReply(content: String, view: View) {
        scope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    ApiClient.api.replyTopic(topicId, ReplyRequest(MainActivity.currentUserId, content))
                }
                if (response.isSuccessful) {
                    Toast.makeText(context, "✓", Toast.LENGTH_SHORT).show()
                    loadTopic(view)
                }
            } catch (e: Exception) {
                Toast.makeText(context, getString(R.string.forum_error), Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        scope.cancel()
    }
}

class RepliesAdapter(
    private val replies: List<ForumReply>,
    private val onAuthorClick: (ForumReply) -> Unit
) : RecyclerView.Adapter<RepliesAdapter.ViewHolder>() {
    
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val avatar: ImageView = view.findViewById(R.id.img_avatar)
        val author: TextView = view.findViewById(R.id.txt_author)
        val content: TextView = view.findViewById(R.id.txt_content)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_forum_reply, parent, false)
        return ViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val reply = replies[position]
        
        holder.author.text = "@${reply.author_username}"
        holder.content.text = reply.content
        
        reply.author_avatar?.let {
            Glide.with(holder.itemView.context)
                .load(it)
                .circleCrop()
                .into(holder.avatar)
        }
        
        holder.author.setOnClickListener { onAuthorClick(reply) }
        holder.avatar.setOnClickListener { onAuthorClick(reply) }
    }
    
    override fun getItemCount() = replies.size
}
