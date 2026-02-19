package com.kitesurf.brasil.fragments

import android.graphics.Color
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
import com.kitesurf.brasil.api.ForumCategory
import com.kitesurf.brasil.api.ForumTopic
import kotlinx.coroutines.*

class ForumFragment : Fragment() {
    
    private lateinit var recyclerCategories: RecyclerView
    private lateinit var recyclerTopics: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var txtSection: TextView
    private val categories = mutableListOf<ForumCategory>()
    private val topics = mutableListOf<ForumTopic>()
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_forum, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        recyclerCategories = view.findViewById(R.id.recycler_categories)
        recyclerTopics = view.findViewById(R.id.recycler_topics)
        progressBar = view.findViewById(R.id.progress_bar)
        txtSection = view.findViewById(R.id.txt_section)
        
        view.findViewById<ImageButton>(R.id.btn_back).setOnClickListener {
            activity?.onBackPressed()
        }
        
        val catAdapter = CategoriesAdapter(categories) { category ->
            loadCategoryTopics(category.id, category.name)
        }
        recyclerCategories.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        recyclerCategories.adapter = catAdapter
        
        val topicsAdapter = TopicsAdapter(topics) { topic ->
            (activity as? MainActivity)?.navigateToTopic(topic.id)
        }
        recyclerTopics.layoutManager = LinearLayoutManager(context)
        recyclerTopics.adapter = topicsAdapter
        
        loadCategories(catAdapter)
        loadRecentTopics(topicsAdapter)
    }
    
    private fun loadCategories(adapter: CategoriesAdapter) {
        scope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    ApiClient.api.getForumCategories()
                }
                if (response.isSuccessful && response.body() != null) {
                    categories.clear()
                    categories.addAll(response.body()!!)
                    adapter.notifyDataSetChanged()
                }
            } catch (e: Exception) {
                // Silent fail
            }
        }
    }
    
    private fun loadRecentTopics(adapter: TopicsAdapter) {
        progressBar.visibility = View.VISIBLE
        txtSection.text = "🔥 Discussões Recentes"
        
        scope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    ApiClient.api.getRecentTopics(20)
                }
                if (response.isSuccessful && response.body() != null) {
                    topics.clear()
                    topics.addAll(response.body()!!)
                    adapter.notifyDataSetChanged()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Erro ao carregar fórum", Toast.LENGTH_SHORT).show()
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }
    
    private fun loadCategoryTopics(categoryId: Int, categoryName: String) {
        progressBar.visibility = View.VISIBLE
        txtSection.text = categoryName
        
        scope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    ApiClient.api.getCategoryTopics(categoryId)
                }
                if (response.isSuccessful && response.body() != null) {
                    topics.clear()
                    topics.addAll(response.body()!!.topics)
                    recyclerTopics.adapter?.notifyDataSetChanged()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Erro ao carregar categoria", Toast.LENGTH_SHORT).show()
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

class CategoriesAdapter(
    private val categories: List<ForumCategory>,
    private val onClick: (ForumCategory) -> Unit
) : RecyclerView.Adapter<CategoriesAdapter.ViewHolder>() {
    
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: TextView = view.findViewById(R.id.txt_icon)
        val name: TextView = view.findViewById(R.id.txt_name)
        val card: View = view.findViewById(R.id.card_category)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_forum_category, parent, false)
        return ViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val cat = categories[position]
        
        holder.icon.text = cat.icon ?: "💬"
        holder.name.text = cat.name
        
        try {
            cat.color?.let {
                holder.card.setBackgroundColor(Color.parseColor(it))
            }
        } catch (e: Exception) {}
        
        holder.itemView.setOnClickListener { onClick(cat) }
    }
    
    override fun getItemCount() = categories.size
}

class TopicsAdapter(
    private val topics: List<ForumTopic>,
    private val onClick: (ForumTopic) -> Unit
) : RecyclerView.Adapter<TopicsAdapter.ViewHolder>() {
    
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.txt_title)
        val author: TextView = view.findViewById(R.id.txt_author)
        val stats: TextView = view.findViewById(R.id.txt_stats)
        val category: TextView = view.findViewById(R.id.txt_category)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_forum_topic, parent, false)
        return ViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val topic = topics[position]
        
        holder.title.text = topic.title
        holder.author.text = "por @${topic.author_username ?: "anônimo"}"
        holder.stats.text = "👁 ${topic.views_count}  💬 ${topic.replies_count}  ❤️ ${topic.likes_count}"
        holder.category.text = topic.category_name ?: ""
        
        holder.itemView.setOnClickListener { onClick(topic) }
    }
    
    override fun getItemCount() = topics.size
}
