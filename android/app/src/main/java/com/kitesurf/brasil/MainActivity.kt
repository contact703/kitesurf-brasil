package com.kitesurf.brasil

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.kitesurf.brasil.adapter.ChatAdapter
import com.kitesurf.brasil.api.ApiClient
import com.kitesurf.brasil.databinding.ActivityMainBinding
import com.kitesurf.brasil.model.ChatMessage
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var chatAdapter: ChatAdapter
    private val messages = mutableListOf<ChatMessage>()
    private var sessionId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupChat()
        addWelcomeMessage()
    }

    private fun setupUI() {
        // Bottom navigation
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_chat -> showChat()
                R.id.nav_spots -> showSpots()
                R.id.nav_classifieds -> showClassifieds()
                R.id.nav_profile -> showProfile()
            }
            true
        }
    }

    private fun setupChat() {
        chatAdapter = ChatAdapter(messages)
        binding.chatRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity).apply {
                stackFromEnd = true
            }
            adapter = chatAdapter
        }

        // Send button
        binding.sendButton.setOnClickListener {
            sendMessage()
        }

        // Enter key
        binding.messageInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage()
                true
            } else false
        }
    }

    private fun addWelcomeMessage() {
        val welcome = ChatMessage(
            content = "E aí! 🏄‍♂️ Sou o KiteBot, seu parceiro no kite surf!\n\n" +
                    "Posso te ajudar com:\n" +
                    "• Dicas de equipamentos\n" +
                    "• Melhores spots e praias\n" +
                    "• Técnicas e segurança\n" +
                    "• Condições de vento\n" +
                    "• Pousadas e infraestrutura\n\n" +
                    "Manda sua pergunta! 🤙",
            isUser = false
        )
        messages.add(welcome)
        chatAdapter.notifyItemInserted(messages.size - 1)
    }

    private fun sendMessage() {
        val text = binding.messageInput.text.toString().trim()
        if (text.isEmpty()) return

        // Add user message
        val userMessage = ChatMessage(content = text, isUser = true)
        messages.add(userMessage)
        chatAdapter.notifyItemInserted(messages.size - 1)
        binding.chatRecyclerView.scrollToPosition(messages.size - 1)
        binding.messageInput.text?.clear()

        // Show loading
        binding.sendButton.isEnabled = false
        binding.progressBar.visibility = View.VISIBLE

        // Send to API
        lifecycleScope.launch {
            try {
                val response = ApiClient.api.sendMessage(
                    mapOf(
                        "message" to text,
                        "sessionId" to (sessionId ?: "")
                    )
                )

                sessionId = response.sessionId

                val botMessage = ChatMessage(
                    content = response.response,
                    isUser = false
                )
                messages.add(botMessage)
                chatAdapter.notifyItemInserted(messages.size - 1)
                binding.chatRecyclerView.scrollToPosition(messages.size - 1)

            } catch (e: Exception) {
                val errorMessage = ChatMessage(
                    content = "Ops, deu uma treta na conexão! 😅 Tenta de novo?",
                    isUser = false
                )
                messages.add(errorMessage)
                chatAdapter.notifyItemInserted(messages.size - 1)
            } finally {
                binding.sendButton.isEnabled = true
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun showChat() {
        binding.chatContainer.visibility = View.VISIBLE
        binding.spotsContainer.visibility = View.GONE
        binding.classifiedsContainer.visibility = View.GONE
        binding.profileContainer.visibility = View.GONE
    }

    private fun showSpots() {
        binding.chatContainer.visibility = View.GONE
        binding.spotsContainer.visibility = View.VISIBLE
        binding.classifiedsContainer.visibility = View.GONE
        binding.profileContainer.visibility = View.GONE
        Toast.makeText(this, "Spots em breve!", Toast.LENGTH_SHORT).show()
    }

    private fun showClassifieds() {
        binding.chatContainer.visibility = View.GONE
        binding.spotsContainer.visibility = View.GONE
        binding.classifiedsContainer.visibility = View.VISIBLE
        binding.profileContainer.visibility = View.GONE
        Toast.makeText(this, "Classificados em breve!", Toast.LENGTH_SHORT).show()
    }

    private fun showProfile() {
        binding.chatContainer.visibility = View.GONE
        binding.spotsContainer.visibility = View.GONE
        binding.classifiedsContainer.visibility = View.GONE
        binding.profileContainer.visibility = View.VISIBLE
        Toast.makeText(this, "Perfil em breve!", Toast.LENGTH_SHORT).show()
    }
}
