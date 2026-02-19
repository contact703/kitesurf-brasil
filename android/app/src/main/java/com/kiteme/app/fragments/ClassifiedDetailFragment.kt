package com.kiteme.app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.kiteme.app.MainActivity
import com.kiteme.app.R
import com.kiteme.app.api.ApiClient
import com.kiteme.app.api.Classified
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
                Toast.makeText(context, getString(R.string.classifieds_error), Toast.LENGTH_SHORT).show()
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }
    
    private fun displayClassified(view: View, item: Classified) {
        view.findViewById<TextView>(R.id.txt_title).text = item.title
        view.findViewById<TextView>(R.id.txt_price).text = item.price?.let { currencyFormat.format(it) } ?: getString(R.string.classifieds_price_consult)
        view.findViewById<TextView>(R.id.txt_description).text = item.description ?: ""
        view.findViewById<TextView>(R.id.txt_category).text = getCategoryLabel(item.category)
        view.findViewById<TextView>(R.id.txt_condition).text = getConditionLabel(item.condition)
        view.findViewById<TextView>(R.id.txt_location).text = "📍 ${item.location ?: "Brasil"}"
        view.findViewById<TextView>(R.id.txt_views).text = "👁 ${item.views_count}"
        
        // Brand and size
        val details = StringBuilder()
        item.brand?.let { details.append("${getString(R.string.na)}: $it\n") }
        item.size?.let { details.append("Size: $it\n") }
        view.findViewById<TextView>(R.id.txt_details).text = details.toString()
        
        // Seller info
        item.name?.let { name ->
            view.findViewById<TextView>(R.id.txt_seller).text = "$name (@${item.username})"
            view.findViewById<LinearLayout>(R.id.layout_seller).setOnClickListener {
                (activity as? MainActivity)?.navigateToProfile(item.user_id)
            }
        }
        
        // Contact button
        view.findViewById<Button>(R.id.btn_contact).text = getString(R.string.classifieds_contact)
        view.findViewById<Button>(R.id.btn_contact).setOnClickListener {
            (activity as? MainActivity)?.navigateToConversation(item.user_id, item.name ?: getString(R.string.user))
        }
    }
    
    private fun getCategoryLabel(cat: String): String {
        return when (cat) {
            "kites" -> "🪁 Kite"
            "pranchas" -> getString(R.string.classifieds_boards)
            "trapezios" -> getString(R.string.classifieds_harnesses)
            "acessorios" -> getString(R.string.classifieds_accessories)
            "roupas" -> getString(R.string.classifieds_clothes)
            "aulas" -> getString(R.string.classifieds_lessons)
            else -> cat
        }
    }
    
    private fun getConditionLabel(cond: String?): String {
        return when (cond?.lowercase()) {
            "novo", "new" -> getString(R.string.condition_new)
            "seminovo", "like new" -> getString(R.string.condition_like_new)
            "usado", "used" -> getString(R.string.condition_used)
            else -> cond ?: ""
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        scope.cancel()
    }
}
