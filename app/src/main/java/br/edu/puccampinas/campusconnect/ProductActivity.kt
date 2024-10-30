package br.edu.puccampinas.campusconnect

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.edu.puccampinas.campusconnect.data.network.RetrofitInstance
import br.edu.puccampinas.campusconnect.databinding.ActivityProductBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.bumptech.glide.Glide


private lateinit var binding: ActivityProductBinding
private lateinit var productAdapter: ProductAdapter
private lateinit var recyclerView: RecyclerView
private lateinit var establishmentName: String
private lateinit var establishmentDescription: String
private lateinit var establishmentOpeningHours: String
private lateinit var establishmentPhoto: String


class ProductActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.comeBack.setOnClickListener {
            comeBack()
        }

        establishmentName = intent.getStringExtra("establishmentName") ?: "N/A"
        establishmentDescription = intent.getStringExtra("establishmentDescription") ?: "N/A"
        establishmentOpeningHours = intent.getStringExtra("establishmentOpeningHours") ?: "N/A"
        establishmentPhoto = intent.getStringExtra("establishmentPhoto") ?: "N/A"

        displayEstablishmentDetails()

        recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)

        val establishmentId = intent.getStringExtra("establishmentId")

        if (establishmentId != null) {
            fetchProducts(establishmentId)
        } else {
            Toast.makeText(this, "Establishment ID not found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchProducts(establishmentId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val response = RetrofitInstance.api.getProductsByEstablishment(establishmentId)
            withContext(Dispatchers.Main) {
                if (response.isSuccessful && response.body() != null) {
                    val products = response.body()!!
                    productAdapter = ProductAdapter(products)
                    recyclerView.adapter = productAdapter
                } else {
                    Toast.makeText(this@ProductActivity, "Failed to load products", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun displayEstablishmentDetails() {

        binding.name.text = establishmentName
        binding.description.text= establishmentDescription
        binding.openingHours.text = establishmentOpeningHours
        Glide.with(this).load(establishmentPhoto).into(binding.establishmentPhoto)
    }

    private fun comeBack(){
        val intent = Intent(this, EstablishmentActivity::class.java)
        startActivity(intent)
    }
}
