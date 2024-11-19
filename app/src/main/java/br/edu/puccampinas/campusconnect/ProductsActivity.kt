package br.edu.puccampinas.campusconnect

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
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
        binding = ActivityProductsBinding.inflate(layoutInflater)  // Infla o layout da activity com ViewBinding
        setContentView(binding.root)

        // Configura o listener para o botão de voltar
        binding.comeBack.setOnClickListener {
            comeBack()
        }

        // Obtém o ID do estabelecimento a partir do Intent
        establishmentId = intent.getStringExtra("establishmentId") ?: "N/A"

        recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)  // Define o layout do RecyclerView como LinearLayoutManager

        // Verifica se o ID do estabelecimento foi obtido corretamente
        if (establishmentId != null) {
            fetchEstablishmentDetails(establishmentId)  // Chama a função para buscar os detalhes do estabelecimento
        } else {
            Toast.makeText(this, "Establishment ID not found", Toast.LENGTH_SHORT).show()  // Exibe uma mensagem de erro caso o ID seja inválido
        }
    }

    // Função para buscar os produtos de um estabelecimento
    private fun fetchProducts(establishmentId: String) {
        if (establishmentId == "N/A") {
            Toast.makeText(this, "Establishment ID is invalid", Toast.LENGTH_SHORT).show()  // Verifica se o ID do estabelecimento é válido
            return
        }

        // Lança uma coroutine para buscar os produtos em segundo plano
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.api.getProductsByEstablishment(establishmentId)  // Chama o endpoint da API para buscar os produtos
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        val products = response.body()!!  // Obtém os produtos da resposta
                        productAdapter = ProductAdapter(products)  // Cria o adaptador com a lista de produtos
                        recyclerView.adapter = productAdapter  // Define o adaptador no RecyclerView
                    } else {
                        // Exibe uma mensagem de erro caso a resposta da API não seja bem-sucedida
                        val errorBody = response.errorBody()?.string()
                        val errorMessage = "Failed to load products. Code: ${response.code()}, Error: $errorBody"
                        Toast.makeText(this@ProductsActivity, errorMessage, Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    // Exibe uma mensagem de erro em caso de exceção
                    Toast.makeText(this@ProductsActivity, "Exception: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // Função para voltar para a tela de estabelecimento
    private fun comeBack() {
        val intent = Intent(this, EstablishmentActivity::class.java)  // Cria um novo Intent para voltar à tela de estabelecimento
        startActivity(intent)  // Inicia a activity de estabelecimento
    }

    // Função para buscar os detalhes do estabelecimento
    private fun fetchEstablishmentDetails(establishmentId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.api.getEstablishmentById(establishmentId)  // Chama o endpoint da API para buscar os detalhes do estabelecimento
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        val establishment = response.body()!!  // Obtém os detalhes do estabelecimento da resposta
                        displayEstablishmentDetails(establishment)  // Exibe os detalhes na UI
                        fetchProducts(establishmentId)  // Chama a função para buscar os produtos
                    } else {
                        // Exibe uma mensagem de erro caso a resposta da API não seja bem-sucedida
                        val errorBody = response.errorBody()?.string()
                        val errorMessage = "Failed to load establishment details. Code: ${response.code()}, Error: $errorBody"
                        Toast.makeText(this@ProductsActivity, errorMessage, Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    // Exibe uma mensagem de erro em caso de exceção
                    Toast.makeText(this@ProductsActivity, "Exception: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // Função para exibir os detalhes do estabelecimento na UI
    private fun displayEstablishmentDetails(establishment: Establishment) {
        binding.name.text = establishment.name  // Exibe o nome do estabelecimento
        binding.description.text = establishment.description  // Exibe a descrição do estabelecimento
        binding.openingHours.text = establishment.openingHours  // Exibe o horário de funcionamento
        Glide.with(this).load(establishment.photo).into(binding.establishmentPhoto)  // Carrega e exibe a foto do estabelecimento
    }

}
