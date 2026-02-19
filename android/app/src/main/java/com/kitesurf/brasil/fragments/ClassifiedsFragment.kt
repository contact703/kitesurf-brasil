package com.kitesurf.brasil.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kitesurf.brasil.MainActivity
import com.kitesurf.brasil.R
import com.kitesurf.brasil.api.ApiClient
import com.kitesurf.brasil.api.Classified
import kotlinx.coroutines.*
import java.text.NumberFormat
import java.util.*

class ClassifiedsFragment : Fragment() {
    
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var chipGroup: LinearLayout
    private val classifieds = mutableListOf<Classified>()
    private var selectedCategory: String? = null
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private lateinit var adapter: ClassifiedsAdapter
    
    private val categories = listOf(
        "kites" to "🪁 Kites",
        "pranchas" to "🏄 Pranchas", 
        "trapezios" to "🎽 Trapézios",
        "acessorios" to "🔧 Acessórios",
        "roupas" to "👕 Roupas",
        "aulas" to "📚 Aulas"
    )
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_classifieds, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        recyclerView = view.findViewById(R.id.recycler_classifieds)
        progressBar = view.findViewById(R.id.progress_bar)
        chipGroup = view.findViewById(R.id.chip_group)
        
        setupCategoryChips()
        
        adapter = ClassifiedsAdapter(classifieds) { classified ->
            (activity as? MainActivity)?.navigateToClassifiedDetail(classified.id)
        }
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
        
        loadClassifieds()
    }
    
    private fun setupCategoryChips() {
        // Add "All" chip
        val allChip = createChip("Todos", null)
        allChip.isSelected = true
        chipGroup.addView(allChip)
        
        categories.forEach { (key, label) ->
            chipGroup.addView(createChip(label, key))
        }
    }
    
    private fun createChip(label: String, category: String?): TextView {
        return TextView(context).apply {
            text = label
            setPadding(32, 16, 32, 16)
            setBackgroundResource(R.drawable.chip_background)
            setTextColor(resources.getColor(R.color.white, null))
            setOnClickListener {
                selectedCategory = category
                // Update selection visual
                for (i in 0 until chipGroup.childCount) {
                    chipGroup.getChildAt(i).isSelected = false
                }
                isSelected = true
                loadClassifieds()
            }
        }
    }
    
    private fun loadClassifieds() {
        progressBar.visibility = View.VISIBLE
        scope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    ApiClient.api.getClassifieds(selectedCategory)
                }
                if (response.isSuccessful && response.body() != null) {
                    classifieds.clear()
                    classifieds.addAll(response.body()!!)
                    adapter.notifyDataSetChanged()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Erro ao carregar classificados", Toast.LENGTH_SHORT).show()
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

class ClassifiedsAdapter(
    private val classifieds: List<Classified>,
    private val onClick: (Classified) -> Unit
) : RecyclerView.Adapter<ClassifiedsAdapter.ViewHolder>() {
    
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
    
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.txt_title)
        val price: TextView = view.findViewById(R.id.txt_price)
        val category: TextView = view.findViewById(R.id.txt_category)
        val condition: TextView = view.findViewById(R.id.txt_condition)
        val location: TextView = view.findViewById(R.id.txt_location)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_classified, parent, false)
        return ViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = classifieds[position]
        
        holder.title.text = item.title
        holder.price.text = item.price?.let { currencyFormat.format(it) } ?: "Consultar"
        holder.category.text = getCategoryEmoji(item.category)
        holder.condition.text = getConditionLabel(item.condition)
        holder.location.text = "📍 ${item.location ?: "Brasil"}"
        
        holder.itemView.setOnClickListener { onClick(item) }
    }
    
    private fun getCategoryEmoji(cat: String): String {
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
    
    override fun getItemCount() = classifieds.size
}
