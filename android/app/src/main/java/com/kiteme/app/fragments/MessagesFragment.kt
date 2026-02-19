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
import com.kiteme.app.api.Conversation
import kotlinx.coroutines.*

class MessagesFragment : Fragment() {
    
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyView: TextView
    private val conversations = mutableListOf<Conversation>()
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_messages, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        recyclerView = view.findViewById(R.id.recycler_conversations)
        progressBar = view.findViewById(R.id.progress_bar)
        emptyView = view.findViewById(R.id.txt_empty)
        
        val adapter = ConversationsAdapter(conversations) { conv ->
            (activity as? MainActivity)?.navigateToConversation(conv.other_user_id, conv.other_user_name)
        }
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
        
        loadConversations(adapter)
    }
    
    private fun loadConversations(adapter: ConversationsAdapter) {
        progressBar.visibility = View.VISIBLE
        scope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    ApiClient.api.getConversations(MainActivity.currentUserId)
                }
                if (response.isSuccessful && response.body() != null) {
                    conversations.clear()
                    conversations.addAll(response.body()!!)
                    adapter.notifyDataSetChanged()
                    
                    emptyView.visibility = if (conversations.isEmpty()) View.VISIBLE else View.GONE
                    emptyView.text = getString(R.string.messages_empty)
                }
            } catch (e: Exception) {
                emptyView.visibility = View.VISIBLE
                emptyView.text = getString(R.string.messages_error)
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

class ConversationsAdapter(
    private val conversations: List<Conversation>,
    private val onClick: (Conversation) -> Unit
) : RecyclerView.Adapter<ConversationsAdapter.ViewHolder>() {
    
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val avatar: ImageView = view.findViewById(R.id.img_avatar)
        val name: TextView = view.findViewById(R.id.txt_name)
        val lastMessage: TextView = view.findViewById(R.id.txt_last_message)
        val unreadBadge: TextView = view.findViewById(R.id.txt_unread)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_conversation, parent, false)
        return ViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val conv = conversations[position]
        
        holder.name.text = conv.other_user_name
        holder.lastMessage.text = conv.last_message ?: "Nova conversa"
        
        if (conv.unread_count > 0) {
            holder.unreadBadge.visibility = View.VISIBLE
            holder.unreadBadge.text = conv.unread_count.toString()
        } else {
            holder.unreadBadge.visibility = View.GONE
        }
        
        conv.other_user_avatar?.let {
            Glide.with(holder.itemView.context)
                .load(it)
                .circleCrop()
                .into(holder.avatar)
        }
        
        holder.itemView.setOnClickListener { onClick(conv) }
    }
    
    override fun getItemCount() = conversations.size
}
