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

// Variáveis para a Activity e Adapter
private lateinit var binding: ActivityEstablishmentBinding
private lateinit var establishmentAdapter: EstablishmentAdapter
private lateinit var recyclerView: RecyclerView
private var loggedUserEmail: String? = null

class EstablishmentActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configuração do ViewBinding para inflar o layout da Activity
        binding = ActivityEstablishmentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configuração do RecyclerView para exibir a lista de estabelecimentos
        recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Carrega todos os estabelecimentos ao iniciar a Activity
        fetchEstablishments()

        // Recupera o e-mail do usuário logado das shared preferences
        val sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE)
        loggedUserEmail = sharedPref.getString("logged_user_email", null)

        // Busca os dados do usuário para exibir a foto de perfil
        fetchUserData()

        // Configura o botão de busca para filtrar estabelecimentos pelo nome
        binding.search.setOnClickListener {
            val searchText = binding.etSearch.text.toString().trim()
            if (searchText.isNotEmpty()) {
                searchEstablishmentsByName(searchText)
            } else {
                fetchEstablishments() // Recarrega todos os estabelecimentos se a busca estiver vazia
            }
        }

        binding.profile.setOnClickListener {
            profile()
        }
    }

    // Função para buscar todos os estabelecimentos da API
    private fun fetchEstablishments() {
        CoroutineScope(Dispatchers.IO).launch {
            val response = RetrofitInstance.api.getEstablishments()
            withContext(Dispatchers.Main) {
                if (response.isSuccessful && response.body() != null) {
                    val establishments = response.body()!!
                    // Configura o Adapter com a lista de estabelecimentos e conecta ao RecyclerView
                    establishmentAdapter = EstablishmentAdapter(establishments)
                    recyclerView.adapter = establishmentAdapter
                } else {
                    // Exibe mensagem de erro caso a resposta não seja bem-sucedida
                    Toast.makeText(this@EstablishmentActivity, "Failed to load establishments", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Função para buscar estabelecimentos pelo nome
    private fun searchEstablishmentsByName(name: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val response = RetrofitInstance.api.searchEstablishments(name)
            withContext(Dispatchers.Main) {
                if (response.isSuccessful && response.body() != null) {
                    val establishments = response.body()!!
                    // Atualiza o Adapter com o resultado da busca
                    establishmentAdapter = EstablishmentAdapter(establishments)
                    recyclerView.adapter = establishmentAdapter
                } else {
                    // Exibe mensagem caso nenhum resultado seja encontrado
                    Toast.makeText(this@EstablishmentActivity, "No results found", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Função para voltar para a Activity de Login
    private fun comeBack() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }

    // Função para abrir a Activity de Perfil do Usuário
    private fun profile() {
        val intent = Intent(this, ProfileActivity::class.java)
        startActivity(intent)
    }

    // Função para buscar dados do usuário logado pelo e-mail
    private fun fetchUserData() {
        val email = loggedUserEmail ?: run {
            // Exibe mensagem se o e-mail do usuário não foi encontrado nas preferências
            Toast.makeText(this, "Email do usuário não encontrado!", Toast.LENGTH_SHORT).show()
            return
        }

        // Realiza a requisição para obter os dados do usuário
        RetrofitInstance.api.getUserByEmail(email).enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful) {
                    val user = response.body()
                    user?.let {
                        // Carrega a foto do usuário no ImageView de perfil, se disponível
                        if (it.photo != null) {
                            Glide.with(this@EstablishmentActivity)
                                .load(it.photo)
                                .circleCrop()
                                .into(binding.profile)
                        }
                    }
                } else {
                    // Loga e exibe um erro se a resposta não for bem-sucedida
                    Log.e("EstablishmentActivity", "Erro ao buscar dados do usuário: ${response.errorBody()?.string()}")
                    Toast.makeText(this@EstablishmentActivity, "Erro ao carregar dados do usuário.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                // Loga e exibe um erro se a requisição falhar
                Log.e("EstablishmentActivity", "Falha na requisição: ${t.message}")
                Toast.makeText(this@EstablishmentActivity, "Erro na conexão.", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
