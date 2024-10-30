package br.edu.puccampinas.campusconnect

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.edu.puccampinas.campusconnect.data.model.Establishment
import br.edu.puccampinas.campusconnect.data.network.RetrofitInstance
import br.edu.puccampinas.campusconnect.databinding.ActivityProductsBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.bumptech.glide.Glide

private lateinit var binding: ActivityProductsBinding
private lateinit var productAdapter: ProductAdapter
private lateinit var recyclerView: RecyclerView
private lateinit var establishmentId: String

class ProductsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.comeBack.setOnClickListener {
            comeBack()
        }

        establishmentId = intent.getStringExtra("establishmentId") ?: "N/A"

        recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)

        if (establishmentId != null) {
            fetchEstablishmentDetails(establishmentId)
        } else {
            Toast.makeText(this, "Establishment ID not found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchProducts(establishmentId: String) {
        if (establishmentId == "N/A") {
            Toast.makeText(this, "Establishment ID is invalid", Toast.LENGTH_SHORT).show()
            return
        }
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.api.getProductsByEstablishment(establishmentId)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        val products = response.body()!!
                        productAdapter = ProductAdapter(products)
                        recyclerView.adapter = productAdapter
                    } else {
                        val errorBody = response.errorBody()?.string()
                        val errorMessage = "Failed to load products. Code: ${response.code()}, Error: $errorBody"
                        Toast.makeText(this@ProductsActivity, errorMessage, Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ProductsActivity, "Exception: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }


    private fun comeBack(){
        val intent = Intent(this, EstablishmentActivity::class.java)
        startActivity(intent)
    }

    private fun fetchEstablishmentDetails(establishmentId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.api.getEstablishmentById(establishmentId)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        val establishment = response.body()!!
                        // Atualiza a UI com os dados do estabelecimento
                        displayEstablishmentDetails(establishment)
                        fetchProducts(establishmentId) // Chamando a função para buscar produtos
                    } else {
                        val errorBody = response.errorBody()?.string()
                        val errorMessage = "Failed to load establishment details. Code: ${response.code()}, Error: $errorBody"
                        Toast.makeText(this@ProductsActivity, errorMessage, Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ProductsActivity, "Exception: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun displayEstablishmentDetails(establishment: Establishment) {
        binding.name.text = establishment.name
        binding.description.text = establishment.description
        binding.openingHours.text = establishment.openingHours
        Glide.with(this).load(establishment.photo).into(binding.establishmentPhoto)
    }
}
