package com.kitesurf.brasil.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.kitesurf.brasil.MainActivity
import com.kitesurf.brasil.R
import com.kitesurf.brasil.api.ApiClient
import com.kitesurf.brasil.api.Classified
import kotlinx.coroutines.*
import java.text.NumberFormat
import java.util.*

class ClassifiedDetailFragment : Fragment() {
    
    private var classifiedId: Int = 0
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
    
    companion object {
        fun newInstance(classifiedId: Int): ClassifiedDetailFragment {
            return ClassifiedDetailFragment().apply {
                arguments = Bundle().apply {
                    putInt("classifiedId", classifiedId)
                }
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        classifiedId = arguments?.getInt("classifiedId") ?: 0
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_classified_detail, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        view.findViewById<ImageButton>(R.id.btn_back).setOnClickListener {
            activity?.onBackPressed()
        }
        
        loadClassified(view)
    }
    
    private fun loadClassified(view: View) {
        val progressBar = view.findViewById<ProgressBar>(R.id.progress_bar)
        progressBar.visibility = View.VISIBLE
        
        scope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    ApiClient.api.getClassified(classifiedId)
                }
                if (response.isSuccessful && response.body() != null) {
                    displayClassified(view, response.body()!!)
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Erro ao carregar anúncio", Toast.LENGTH_SHORT).show()
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }
    
    private fun displayClassified(view: View, item: Classified) {
        view.findViewById<TextView>(R.id.txt_title).text = item.title
        view.findViewById<TextView>(R.id.txt_price).text = item.price?.let { currencyFormat.format(it) } ?: "Consultar"
        view.findViewById<TextView>(R.id.txt_description).text = item.description ?: ""
        view.findViewById<TextView>(R.id.txt_category).text = getCategoryLabel(item.category)
        view.findViewById<TextView>(R.id.txt_condition).text = getConditionLabel(item.condition)
        view.findViewById<TextView>(R.id.txt_location).text = "📍 ${item.location ?: "Brasil"}"
        view.findViewById<TextView>(R.id.txt_views).text = "👁 ${item.views_count} visualizações"
        
        // Brand and size
        val details = StringBuilder()
        item.brand?.let { details.append("Marca: $it\n") }
        item.size?.let { details.append("Tamanho: $it\n") }
        view.findViewById<TextView>(R.id.txt_details).text = details.toString()
        
        // Seller info
        item.name?.let { name ->
            view.findViewById<TextView>(R.id.txt_seller).text = "Vendedor: $name (@${item.username})"
            view.findViewById<LinearLayout>(R.id.layout_seller).setOnClickListener {
                (activity as? MainActivity)?.navigateToProfile(item.user_id)
            }
        }
        
        // Contact button
        view.findViewById<Button>(R.id.btn_contact).setOnClickListener {
            (activity as? MainActivity)?.navigateToConversation(item.user_id, item.name ?: "Vendedor")
        }
    }
    
    private fun getCategoryLabel(cat: String): String {
        return when (cat) {
            "kites" -> "🪁 Kite"
            "pranchas" -> "🏄 Prancha"
            "trapezios" -> "🎽 Trapézio"
            "acessorios" -> "🔧 Acessório"
            "roupas" -> "👕 Roupa"
            "aulas" -> "📚 Aula"
            else -> cat
        }
    }
    
    private fun getConditionLabel(cond: String?): String {
        return when (cond?.lowercase()) {
            "novo" -> "✨ Novo"
            "seminovo" -> "👍 Seminovo"
            "usado" -> "📦 Usado"
            else -> cond ?: ""
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        scope.cancel()
    }
}
