package com.kiteme.app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kiteme.app.MainActivity
import com.kiteme.app.R
import com.kiteme.app.api.ApiClient
import com.kiteme.app.api.Classified
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
        val categories = listOf(
            null to getString(R.string.classifieds_all),
            "kites" to getString(R.string.classifieds_kites),
            "pranchas" to getString(R.string.classifieds_boards),
            "trapezios" to getString(R.string.classifieds_harnesses),
            "acessorios" to getString(R.string.classifieds_accessories),
            "roupas" to getString(R.string.classifieds_clothes),
            "aulas" to getString(R.string.classifieds_lessons)
        )
        
        categories.forEachIndexed { index, (key, label) ->
            val chip = createChip(label, key)
            if (index == 0) chip.isSelected = true
            chipGroup.addView(chip)
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
                Toast.makeText(context, getString(R.string.classifieds_error), Toast.LENGTH_SHORT).show()
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
        val context = holder.itemView.context
        
        holder.title.text = item.title
        holder.price.text = item.price?.let { currencyFormat.format(it) } ?: context.getString(R.string.classifieds_price_consult)
        holder.category.text = getCategoryEmoji(context, item.category)
        holder.condition.text = getConditionLabel(context, item.condition)
        holder.location.text = "📍 ${item.location ?: "Brasil"}"
        
        holder.itemView.setOnClickListener { onClick(item) }
    }
    
    private fun getCategoryEmoji(context: android.content.Context, cat: String): String {
        return when (cat) {
            "kites" -> "🪁 Kite"
            "pranchas" -> "🏄 ${context.getString(R.string.classifieds_boards).replace("🏄 ", "")}"
            "trapezios" -> "🎽 ${context.getString(R.string.classifieds_harnesses).replace("🎽 ", "")}"
            "acessorios" -> "🔧 ${context.getString(R.string.classifieds_accessories).replace("🔧 ", "")}"
            "roupas" -> "👕 ${context.getString(R.string.classifieds_clothes).replace("👕 ", "")}"
            "aulas" -> "📚 ${context.getString(R.string.classifieds_lessons).replace("📚 ", "")}"
            else -> cat
        }
    }
    
    private fun getConditionLabel(context: android.content.Context, cond: String?): String {
        return when (cond?.lowercase()) {
            "novo", "new" -> context.getString(R.string.condition_new)
            "seminovo", "like new" -> context.getString(R.string.condition_like_new)
            "usado", "used" -> context.getString(R.string.condition_used)
            else -> cond ?: ""
        }
    }
    
    override fun getItemCount() = classifieds.size
}
