package com.kitesurf.brasil.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.kitesurf.brasil.R
import com.kitesurf.brasil.api.ApiClient
import com.kitesurf.brasil.api.Spot
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
                Toast.makeText(context, "Erro ao carregar spot", Toast.LENGTH_SHORT).show()
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }
    
    private fun displaySpot(view: View, spot: Spot) {
        view.findViewById<TextView>(R.id.txt_name).text = spot.name
        view.findViewById<TextView>(R.id.txt_location).text = "📍 ${spot.location}"
        view.findViewById<TextView>(R.id.txt_description).text = spot.description ?: ""
        view.findViewById<TextView>(R.id.txt_wind).text = "💨 Vento: ${spot.wind_direction ?: "N/A"}"
        view.findViewById<TextView>(R.id.txt_months).text = "📅 Melhor época: ${spot.best_months ?: "Ano todo"}"
        view.findViewById<TextView>(R.id.txt_difficulty).text = getDifficultyText(spot.difficulty)
        view.findViewById<TextView>(R.id.txt_rating).text = "⭐ ${spot.rating} (${spot.rating_count} avaliações)"
        
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
            "iniciante" -> "🟢 Dificuldade: Iniciante - Perfeito para aprender!"
            "intermediário", "intermediario" -> "🟡 Dificuldade: Intermediário - Requer experiência"
            "avançado", "avancado" -> "🔴 Dificuldade: Avançado - Para experts"
            "todos" -> "🔵 Dificuldade: Todos os níveis"
            else -> "Dificuldade: ${diff ?: "N/A"}"
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
