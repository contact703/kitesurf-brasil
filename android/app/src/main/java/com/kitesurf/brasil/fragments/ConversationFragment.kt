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
import com.kitesurf.brasil.api.Message
import com.kitesurf.brasil.api.SendMessageRequest
import kotlinx.coroutines.*

class ConversationFragment : Fragment() {
    
    private var otherUserId: Int = 0
    private var otherUserName: String = ""
    private var conversationId: Int? = null
    
    private lateinit var recyclerView: RecyclerView
    private lateinit var inputMessage: EditText
    private lateinit var btnSend: ImageButton
    private lateinit var txtTitle: TextView
    private val messages = mutableListOf<Message>()
    private lateinit var adapter: DirectMessagesAdapter
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    
    companion object {
        fun newInstance(userId: Int, userName: String): ConversationFragment {
            return ConversationFragment().apply {
                arguments = Bundle().apply {
                    putInt("userId", userId)
                    putString("userName", userName)
                }
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        otherUserId = arguments?.getInt("userId") ?: 0
        otherUserName = arguments?.getString("userName") ?: ""
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_conversation, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        recyclerView = view.findViewById(R.id.recycler_messages)
        inputMessage = view.findViewById(R.id.input_message)
        btnSend = view.findViewById(R.id.btn_send)
        txtTitle = view.findViewById(R.id.txt_title)
        
        txtTitle.text = otherUserName
        
        view.findViewById<ImageButton>(R.id.btn_back).setOnClickListener {
            (activity as? MainActivity)?.navigateToMessages()
        }
        
        adapter = DirectMessagesAdapter(messages, MainActivity.currentUserId)
        recyclerView.layoutManager = LinearLayoutManager(context).apply {
            stackFromEnd = true
        }
        recyclerView.adapter = adapter
        
        btnSend.setOnClickListener { sendMessage() }
        
        // Load existing messages if conversation exists
        loadMessages()
    }
    
    private fun loadMessages() {
        // For new conversations, we need to first check if one exists
        // This is simplified - in production, you'd need better handling
    }
    
    private fun sendMessage() {
        val text = inputMessage.text.toString().trim()
        if (text.isEmpty()) return
        
        inputMessage.text.clear()
        btnSend.isEnabled = false
        
        scope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    ApiClient.api.sendMessage(SendMessageRequest(
                        sender_id = MainActivity.currentUserId,
                        recipient_id = otherUserId,
                        content = text
                    ))
                }
                if (response.isSuccessful && response.body() != null) {
                    // Add message locally
                    val newMessage = Message(
                        id = response.body()!!.id ?: 0,
                        conversation_id = response.body()!!.id ?: 0,
                        sender_id = MainActivity.currentUserId,
                        content = text,
                        created_at = "",
                        sender_name = "Você",
                        sender_username = MainActivity.currentUsername,
                        sender_avatar = null
                    )
                    messages.add(newMessage)
                    adapter.notifyItemInserted(messages.size - 1)
                    recyclerView.scrollToPosition(messages.size - 1)
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Erro ao enviar mensagem", Toast.LENGTH_SHORT).show()
            } finally {
                btnSend.isEnabled = true
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        scope.cancel()
    }
}

class DirectMessagesAdapter(
    private val messages: List<Message>,
    private val currentUserId: Int
) : RecyclerView.Adapter<DirectMessagesAdapter.ViewHolder>() {
    
    class ViewHolder(view: View, val isMe: Boolean) : RecyclerView.ViewHolder(view) {
        val content: TextView = view.findViewById(R.id.txt_message)
    }
    
    override fun getItemViewType(position: Int): Int {
        return if (messages[position].sender_id == currentUserId) 1 else 0
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layout = if (viewType == 1) R.layout.item_message_user else R.layout.item_message_bot
        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return ViewHolder(view, viewType == 1)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.content.text = messages[position].content
    }
    
    override fun getItemCount() = messages.size
}
