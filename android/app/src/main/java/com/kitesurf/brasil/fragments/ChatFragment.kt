package com.kitesurf.brasil.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kitesurf.brasil.R
import com.kitesurf.brasil.api.ApiClient
import com.kitesurf.brasil.api.ChatRequest
import kotlinx.coroutines.*

class ChatFragment : Fragment() {
    
    private lateinit var recyclerView: RecyclerView
    private lateinit var inputMessage: EditText
    private lateinit var btnSend: ImageButton
    private lateinit var progressBar: ProgressBar
    private val messages = mutableListOf<ChatMessage>()
    private lateinit var adapter: ChatAdapter
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var sessionId: String? = null
    
    data class ChatMessage(val content: String, val isUser: Boolean)
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_chat, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        recyclerView = view.findViewById(R.id.recycler_chat)
        inputMessage = view.findViewById(R.id.input_message)
        btnSend = view.findViewById(R.id.btn_send)
        progressBar = view.findViewById(R.id.progress_bar)
        
        adapter = ChatAdapter(messages)
        recyclerView.layoutManager = LinearLayoutManager(context).apply { 
            stackFromEnd = true 
        }
        recyclerView.adapter = adapter
        
        // Welcome message
        messages.add(ChatMessage(
            "🏄 Olá! Eu sou o KiteBot, seu assistente de kitesurf!\n\n" +
            "Posso te ajudar com:\n" +
            "• Dicas de equipamentos\n" +
            "• Informações sobre spots\n" +
            "• Técnicas e manobras\n" +
            "• Condições de vento\n\n" +
            "O que você quer saber?", 
            false
        ))
        adapter.notifyDataSetChanged()
        
        btnSend.setOnClickListener { sendMessage() }
        
        inputMessage.setOnEditorActionListener { _, _, _ ->
            sendMessage()
            true
        }
    }
    
    private fun sendMessage() {
        val text = inputMessage.text.toString().trim()
        if (text.isEmpty()) return
        
        inputMessage.text.clear()
        messages.add(ChatMessage(text, true))
        adapter.notifyItemInserted(messages.size - 1)
        recyclerView.scrollToPosition(messages.size - 1)
        
        progressBar.visibility = View.VISIBLE
        btnSend.isEnabled = false
        
        scope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    ApiClient.api.sendChat(ChatRequest(text, sessionId))
                }
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    sessionId = body.session_id
                    messages.add(ChatMessage(body.response, false))
                    adapter.notifyItemInserted(messages.size - 1)
                    recyclerView.scrollToPosition(messages.size - 1)
                } else {
                    showError()
                }
            } catch (e: Exception) {
                showError()
            } finally {
                progressBar.visibility = View.GONE
                btnSend.isEnabled = true
            }
        }
    }
    
    private fun showError() {
        messages.add(ChatMessage(
            "Ops! Não consegui processar sua mensagem. Tente novamente! 🏄",
            false
        ))
        adapter.notifyItemInserted(messages.size - 1)
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        scope.cancel()
    }
}

class ChatAdapter(private val messages: List<ChatFragment.ChatMessage>) : 
    RecyclerView.Adapter<ChatAdapter.ViewHolder>() {
    
    class ViewHolder(view: View, val isUser: Boolean) : RecyclerView.ViewHolder(view) {
        val content: TextView = view.findViewById(R.id.txt_message)
    }
    
    override fun getItemViewType(position: Int) = if (messages[position].isUser) 1 else 0
    
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
