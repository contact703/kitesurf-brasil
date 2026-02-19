package com.kiteme.app.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kiteme.app.R
import com.kiteme.app.api.ApiClient
import com.kiteme.app.api.Accommodation
import kotlinx.coroutines.*
import java.text.NumberFormat
import java.util.*

class AccommodationsFragment : Fragment() {
    
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private val accommodations = mutableListOf<Accommodation>()
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_accommodations, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        recyclerView = view.findViewById(R.id.recycler_accommodations)
        progressBar = view.findViewById(R.id.progress_bar)
        
        view.findViewById<ImageButton>(R.id.btn_back).setOnClickListener {
            activity?.onBackPressed()
        }
        
        val adapter = AccommodationsAdapter(accommodations) { acc ->
            acc.contact_whatsapp?.let { phone ->
                val url = "https://wa.me/$phone"
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            }
        }
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
        
        loadAccommodations(adapter)
    }
    
    private fun loadAccommodations(adapter: AccommodationsAdapter) {
        progressBar.visibility = View.VISIBLE
        scope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    ApiClient.api.getAccommodations()
                }
                if (response.isSuccessful && response.body() != null) {
                    accommodations.clear()
                    accommodations.addAll(response.body()!!)
                    adapter.notifyDataSetChanged()
                }
            } catch (e: Exception) {
                Toast.makeText(context, getString(R.string.accommodations_error), Toast.LENGTH_SHORT).show()
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

class AccommodationsAdapter(
    private val items: List<Accommodation>,
    private val onWhatsApp: (Accommodation) -> Unit
) : RecyclerView.Adapter<AccommodationsAdapter.ViewHolder>() {
    
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
    
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.txt_name)
        val location: TextView = view.findViewById(R.id.txt_location)
        val description: TextView = view.findViewById(R.id.txt_description)
        val price: TextView = view.findViewById(R.id.txt_price)
        val rating: TextView = view.findViewById(R.id.txt_rating)
        val verified: ImageView = view.findViewById(R.id.img_verified)
        val btnWhatsApp: Button = view.findViewById(R.id.btn_whatsapp)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_accommodation, parent, false)
        return ViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val acc = items[position]
        val context = holder.itemView.context
        
        holder.name.text = acc.name
        holder.location.text = "📍 ${acc.location}, ${acc.state}"
        holder.description.text = acc.description ?: ""
        holder.rating.text = "⭐ ${acc.rating}"
        holder.verified.visibility = if (acc.verified == 1) View.VISIBLE else View.GONE
        
        val priceText = when {
            acc.price_min != null && acc.price_max != null -> 
                "${currencyFormat.format(acc.price_min)} - ${currencyFormat.format(acc.price_max)}${context.getString(R.string.accommodations_per_night)}"
            acc.price_range != null -> acc.price_range
            else -> context.getString(R.string.classifieds_price_consult)
        }
        holder.price.text = "💰 $priceText"
        
        holder.btnWhatsApp.text = context.getString(R.string.accommodations_contact)
        holder.btnWhatsApp.setOnClickListener { onWhatsApp(acc) }
    }
    
    override fun getItemCount() = items.size
}
