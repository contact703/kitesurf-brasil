package com.kitesurf.brasil.fragments

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kitesurf.brasil.R
import com.kitesurf.brasil.api.ApiClient
import com.kitesurf.brasil.api.ChatRequest
import kotlinx.coroutines.*
import java.util.*

class ChatFragment : Fragment(), TextToSpeech.OnInitListener {
    
    private lateinit var recyclerView: RecyclerView
    private lateinit var inputMessage: EditText
    private lateinit var btnSend: ImageButton
    private lateinit var btnVoice: ImageButton
    private lateinit var btnSound: ImageButton
    private lateinit var progressBar: ProgressBar
    private lateinit var voiceIndicator: TextView
    
    private val messages = mutableListOf<ChatMessage>()
    private lateinit var adapter: ChatAdapter
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var sessionId: String? = null
    
    // Text-to-Speech
    private var tts: TextToSpeech? = null
    private var ttsEnabled = true
    
    // Speech-to-Text
    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening = false
    
    data class ChatMessage(val content: String, val isUser: Boolean)
    
    companion object {
        private const val PERMISSION_REQUEST_CODE = 100
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_chat, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        recyclerView = view.findViewById(R.id.recycler_chat)
        inputMessage = view.findViewById(R.id.input_message)
        btnSend = view.findViewById(R.id.btn_send)
        btnVoice = view.findViewById(R.id.btn_voice)
        btnSound = view.findViewById(R.id.btn_sound)
        progressBar = view.findViewById(R.id.progress_bar)
        voiceIndicator = view.findViewById(R.id.voice_indicator)
        
        // Initialize TTS
        tts = TextToSpeech(context, this)
        
        // Initialize Speech Recognizer
        if (SpeechRecognizer.isRecognitionAvailable(requireContext())) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(requireContext())
            setupSpeechRecognizer()
        }
        
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
            "Fale ou digite sua pergunta!", 
            false
        ))
        adapter.notifyDataSetChanged()
        
        btnSend.setOnClickListener { sendMessage() }
        
        btnVoice.setOnClickListener { toggleVoiceInput() }
        
        btnSound.setOnClickListener { toggleTTS() }
        
        inputMessage.setOnEditorActionListener { _, _, _ ->
            sendMessage()
            true
        }
        
        updateSoundButton()
    }
    
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale("pt", "BR")
        }
    }
    
    private fun setupSpeechRecognizer() {
        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                voiceIndicator.visibility = View.VISIBLE
                voiceIndicator.text = "🎤 Ouvindo..."
                btnVoice.setImageResource(R.drawable.ic_mic_on)
            }
            
            override fun onBeginningOfSpeech() {}
            
            override fun onRmsChanged(rmsdB: Float) {}
            
            override fun onBufferReceived(buffer: ByteArray?) {}
            
            override fun onEndOfSpeech() {
                voiceIndicator.text = "⏳ Processando..."
                isListening = false
                btnVoice.setImageResource(R.drawable.ic_mic)
            }
            
            override fun onError(error: Int) {
                voiceIndicator.visibility = View.GONE
                isListening = false
                btnVoice.setImageResource(R.drawable.ic_mic)
                
                val errorMsg = when (error) {
                    SpeechRecognizer.ERROR_NO_MATCH -> "Não entendi, tente novamente"
                    SpeechRecognizer.ERROR_NETWORK -> "Erro de conexão"
                    else -> "Erro ao reconhecer voz"
                }
                Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
            }
            
            override fun onResults(results: Bundle?) {
                voiceIndicator.visibility = View.GONE
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    inputMessage.setText(matches[0])
                    sendMessage()
                }
            }
            
            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    inputMessage.setText(matches[0])
                }
            }
            
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
    }
    
    private fun toggleVoiceInput() {
        if (isListening) {
            stopListening()
        } else {
            startListening()
        }
    }
    
    private fun startListening() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) 
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.RECORD_AUDIO),
                PERMISSION_REQUEST_CODE
            )
            return
        }
        
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "pt-BR")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        
        isListening = true
        speechRecognizer?.startListening(intent)
    }
    
    private fun stopListening() {
        isListening = false
        speechRecognizer?.stopListening()
        voiceIndicator.visibility = View.GONE
        btnVoice.setImageResource(R.drawable.ic_mic)
    }
    
    private fun toggleTTS() {
        ttsEnabled = !ttsEnabled
        updateSoundButton()
        
        val msg = if (ttsEnabled) "🔊 Som ativado" else "🔇 Som desativado"
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        
        if (!ttsEnabled) {
            tts?.stop()
        }
    }
    
    private fun updateSoundButton() {
        btnSound.setImageResource(
            if (ttsEnabled) R.drawable.ic_volume_on else R.drawable.ic_volume_off
        )
    }
    
    private fun speakText(text: String) {
        if (ttsEnabled && tts != null) {
            // Remove emojis for better TTS
            val cleanText = text.replace(Regex("[\\p{So}\\p{Cn}]"), "")
            tts?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, "response")
        }
    }
    
    private fun sendMessage() {
        val text = inputMessage.text.toString().trim()
        if (text.isEmpty()) return
        
        // Stop TTS if playing
        tts?.stop()
        
        inputMessage.text.clear()
        messages.add(ChatMessage(text, true))
        adapter.notifyItemInserted(messages.size - 1)
        recyclerView.scrollToPosition(messages.size - 1)
        
        progressBar.visibility = View.VISIBLE
        btnSend.isEnabled = false
        btnVoice.isEnabled = false
        
        scope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    ApiClient.api.sendChat(ChatRequest(text, sessionId))
                }
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    sessionId = body.session_id
                    val responseText = body.response
                    
                    messages.add(ChatMessage(responseText, false))
                    adapter.notifyItemInserted(messages.size - 1)
                    recyclerView.scrollToPosition(messages.size - 1)
                    
                    // Speak the response
                    speakText(responseText)
                } else {
                    showError()
                }
            } catch (e: Exception) {
                showError()
            } finally {
                progressBar.visibility = View.GONE
                btnSend.isEnabled = true
                btnVoice.isEnabled = true
            }
        }
    }
    
    private fun showError() {
        val errorMsg = "Ops! Não consegui processar sua mensagem. Tente novamente!"
        messages.add(ChatMessage(errorMsg, false))
        adapter.notifyItemInserted(messages.size - 1)
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        scope.cancel()
        tts?.shutdown()
        speechRecognizer?.destroy()
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
