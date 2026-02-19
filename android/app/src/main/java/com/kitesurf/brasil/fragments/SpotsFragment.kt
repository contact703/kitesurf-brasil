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
import com.kitesurf.brasil.api.Spot
import kotlinx.coroutines.*

class SpotsFragment : Fragment() {
    
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private val spots = mutableListOf<Spot>()
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_spots, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        recyclerView = view.findViewById(R.id.recycler_spots)
        progressBar = view.findViewById(R.id.progress_bar)
        
        val adapter = SpotsAdapter(spots) { spot ->
            (activity as? MainActivity)?.navigateToSpotDetail(spot.id)
        }
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
        
        loadSpots(adapter)
    }
    
    private fun loadSpots(adapter: SpotsAdapter) {
        progressBar.visibility = View.VISIBLE
        scope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    ApiClient.api.getSpots()
                }
                if (response.isSuccessful && response.body() != null) {
                    spots.clear()
                    spots.addAll(response.body()!!)
                    adapter.notifyDataSetChanged()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Erro ao carregar spots", Toast.LENGTH_SHORT).show()
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

class SpotsAdapter(
    private val spots: List<Spot>,
    private val onClick: (Spot) -> Unit
) : RecyclerView.Adapter<SpotsAdapter.ViewHolder>() {
    
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.txt_name)
        val location: TextView = view.findViewById(R.id.txt_location)
        val wind: TextView = view.findViewById(R.id.txt_wind)
        val months: TextView = view.findViewById(R.id.txt_months)
        val difficulty: TextView = view.findViewById(R.id.txt_difficulty)
        val rating: TextView = view.findViewById(R.id.txt_rating)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_spot, parent, false)
        return ViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val spot = spots[position]
        
        holder.name.text = spot.name
        holder.location.text = "📍 ${spot.location}"
        holder.wind.text = "💨 ${spot.wind_direction ?: "N/A"}"
        holder.months.text = "📅 ${spot.best_months ?: "Ano todo"}"
        holder.difficulty.text = getDifficultyEmoji(spot.difficulty)
        holder.rating.text = "⭐ ${spot.rating} (${spot.rating_count})"
        
        holder.itemView.setOnClickListener { onClick(spot) }
    }
    
    private fun getDifficultyEmoji(diff: String?): String {
        return when (diff?.lowercase()) {
            "iniciante" -> "🟢 Iniciante"
            "intermediário", "intermediario" -> "🟡 Intermediário"
            "avançado", "avancado" -> "🔴 Avançado"
            "todos" -> "🔵 Todos os níveis"
            else -> "⚪ ${diff ?: "N/A"}"
        }
    }
    
    override fun getItemCount() = spots.size
}
