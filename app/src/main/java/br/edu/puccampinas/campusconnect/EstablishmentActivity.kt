package br.edu.puccampinas.campusconnect

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.edu.puccampinas.campusconnect.data.model.User
import br.edu.puccampinas.campusconnect.data.network.RetrofitInstance
import br.edu.puccampinas.campusconnect.databinding.ActivityEstablishmentBinding
import com.bumptech.glide.Glide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private lateinit var binding: ActivityEstablishmentBinding
private lateinit var establishmentAdapter: EstablishmentAdapter
private lateinit var recyclerView: RecyclerView
private var loggedUserEmail: String? = null

class EstablishmentActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEstablishmentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)

        fetchEstablishments()

        val sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE)
        loggedUserEmail = sharedPref.getString("logged_user_email", null)
        fetchUserData()

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

    private fun fetchUserData() {
        val email = loggedUserEmail ?: run {
            Toast.makeText(this, "Email do usuário não encontrado!", Toast.LENGTH_SHORT).show()
            return
        }

        RetrofitInstance.api.getUserByEmail(email).enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful) {
                    val user = response.body()
                    user?.let {
                        if(it.photo != null){
                        Glide.with(this@EstablishmentActivity)
                            .load(it.photo)
                            .circleCrop()
                            .into(binding.profile)}
                    }
                } else {
                    Log.e("EstablishmentActivity", "Erro ao buscar dados do usuário: ${response.errorBody()?.string()}")
                    Toast.makeText(this@EstablishmentActivity, "Erro ao carregar dados do usuário.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                Log.e("EstablishmentActivity", "Falha na requisição: ${t.message}")
                Toast.makeText(this@EstablishmentActivity, "Erro na conexão.", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
