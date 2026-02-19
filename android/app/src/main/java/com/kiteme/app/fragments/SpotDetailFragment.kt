package com.kiteme.app.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.kiteme.app.R
import com.kiteme.app.api.ApiClient
import com.kiteme.app.api.Spot
import kotlinx.coroutines.*
import org.json.JSONArray

class SpotDetailFragment : Fragment() {
    
    private var spotId: Int = 0
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    
    companion object {
        fun newInstance(spotId: Int): SpotDetailFragment {
            return SpotDetailFragment().apply {
                arguments = Bundle().apply {
                    putInt("spotId", spotId)
                }
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        spotId = arguments?.getInt("spotId") ?: 0
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_spot_detail, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        view.findViewById<ImageButton>(R.id.btn_back).setOnClickListener {
            activity?.onBackPressed()
        }
        
        loadSpot(view)
    }
    
    private fun loadSpot(view: View) {
        val progressBar = view.findViewById<ProgressBar>(R.id.progress_bar)
        progressBar.visibility = View.VISIBLE
        
        scope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    ApiClient.api.getSpot(spotId)
                }
                if (response.isSuccessful && response.body() != null) {
                    displaySpot(view, response.body()!!)
                }
            } catch (e: Exception) {
                Toast.makeText(context, getString(R.string.spots_error), Toast.LENGTH_SHORT).show()
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }
    
    private fun displaySpot(view: View, spot: Spot) {
        view.findViewById<TextView>(R.id.txt_name).text = spot.name
        view.findViewById<TextView>(R.id.txt_location).text = "📍 ${spot.location}"
        view.findViewById<TextView>(R.id.txt_description).text = spot.description ?: ""
        view.findViewById<TextView>(R.id.txt_wind).text = "${getString(R.string.spots_wind_direction)}: ${spot.wind_direction ?: getString(R.string.na)}"
        view.findViewById<TextView>(R.id.txt_months).text = "${getString(R.string.spots_best_months)}: ${spot.best_months ?: getString(R.string.spots_all_year)}"
        view.findViewById<TextView>(R.id.txt_difficulty).text = getDifficultyText(spot.difficulty)
        view.findViewById<TextView>(R.id.txt_rating).text = "⭐ ${spot.rating} (${spot.rating_count} ${getString(R.string.spots_reviews)})"
        
        // Amenities
        try {
            val amenitiesJson = JSONArray(spot.amenities ?: "[]")
            val amenitiesText = StringBuilder()
            for (i in 0 until amenitiesJson.length()) {
                if (i > 0) amenitiesText.append(" • ")
                amenitiesText.append(getAmenityEmoji(amenitiesJson.getString(i)))
            }
            view.findViewById<TextView>(R.id.txt_amenities).text = amenitiesText.toString()
        } catch (e: Exception) {
            view.findViewById<TextView>(R.id.txt_amenities).text = spot.amenities ?: ""
        }
        
        // Map button
        view.findViewById<Button>(R.id.btn_map).setOnClickListener {
            val uri = Uri.parse("geo:0,0?q=${spot.name}, ${spot.location}")
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        }
    }
    
    private fun getDifficultyText(diff: String?): String {
        return when (diff?.lowercase()) {
            "iniciante", "beginner" -> getString(R.string.level_beginner)
            "intermediário", "intermediario", "intermediate" -> getString(R.string.level_intermediate)
            "avançado", "avancado", "advanced" -> getString(R.string.level_advanced)
            "todos", "all" -> getString(R.string.level_all)
            else -> "${getString(R.string.spots_difficulty)}: ${diff ?: getString(R.string.na)}"
        }
    }
    
    private fun getAmenityEmoji(amenity: String): String {
        return when (amenity.lowercase()) {
            "escolas" -> "🎓 Escolas"
            "resgate" -> "🚤 Resgate"
            "restaurantes" -> "🍽️ Restaurantes"
            "hospedagem" -> "🏨 Hospedagem"
            "lojas" -> "🛒 Lojas"
            "lagoas" -> "💧 Lagoas"
            "pousadas" -> "🏠 Pousadas"
            "flat water" -> "🌊 Flat Water"
            "ondas" -> "🌊 Ondas"
            "downwind" -> "💨 Downwind"
            else -> amenity
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        scope.cancel()
    }
}
