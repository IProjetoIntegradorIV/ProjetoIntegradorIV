package br.edu.puccampinas.campusconnect

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.edu.puccampinas.campusconnect.data.network.RetrofitInstance
import br.edu.puccampinas.campusconnect.databinding.ActivityEstablishmentBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private lateinit var binding: ActivityEstablishmentBinding
private lateinit var establishmentAdapter: EstablishmentAdapter
private lateinit var recyclerView: RecyclerView

class EstablishmentActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEstablishmentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)

        fetchEstablishments()

        binding.search.setOnClickListener {
            val searchText = binding.etSearch.text.toString().trim()
            if (searchText.isNotEmpty()) {
                searchEstablishmentsByName(searchText)
            } else {
                fetchEstablishments()
            }
        }

        binding.comeBack.setOnClickListener {
            comeBack()
        }

        binding.profile.setOnClickListener {
            profile()
        }

    }

    private fun fetchEstablishments() {
        CoroutineScope(Dispatchers.IO).launch {
            val response = RetrofitInstance.api.getEstablishments()
            withContext(Dispatchers.Main) {
                if (response.isSuccessful && response.body() != null) {
                    val establishments = response.body()!!
                    establishmentAdapter = EstablishmentAdapter(establishments)
                    recyclerView.adapter = establishmentAdapter
                } else {
                    Toast.makeText(this@EstablishmentActivity, "Failed to load establishments", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun searchEstablishmentsByName(name: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val response = RetrofitInstance.api.searchEstablishments(name)
            withContext(Dispatchers.Main) {
                if (response.isSuccessful && response.body() != null) {
                    val establishments = response.body()!!
                    establishmentAdapter = EstablishmentAdapter(establishments)
                    recyclerView.adapter = establishmentAdapter
                } else {
                    Toast.makeText(this@EstablishmentActivity, "No results found", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun comeBack(){
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }

    private fun profile(){
        val intent = Intent(this, ProfileActivity::class.java)
        startActivity(intent)
    }
}
