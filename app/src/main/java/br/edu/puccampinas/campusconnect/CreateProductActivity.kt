package br.edu.puccampinas.campusconnect

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import br.edu.puccampinas.campusconnect.data.model.Product
import br.edu.puccampinas.campusconnect.data.network.RetrofitInstance
import br.edu.puccampinas.campusconnect.databinding.ActivityCreateProductBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CreateProductActivity : AppCompatActivity() {

    // Variável de binding para acessar os elementos da interface
    private lateinit var binding: ActivityCreateProductBinding

    // Variável para armazenar o e-mail do usuário logado
    private var loggedUserEmail: String? = null

    // Variável para armazenar o ID do estabelecimento
    private lateinit var establishmentId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializa o binding
        binding = ActivityCreateProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Carrega o e-mail do usuário logado do shared preferences
        val sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE)
        loggedUserEmail = sharedPref.getString("logged_user_email", null)

        // Busca o userId usando o e-mail do usuário logado
        loggedUserEmail?.let { fetchUserIdByEmail(it) }

        // Configura o botão de voltar para retornar à tela anterior
        binding.comeBack.setOnClickListener {
            comeBack()
        }

        // Configura o botão de registro para iniciar a validação dos campos
        binding.btnRegister.setOnClickListener {
            validateFields()
        }
    }

    // Função para buscar o userId pelo email
    fun fetchUserIdByEmail(email: String) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                // Realiza a chamada da API em uma corrotina de IO
                val userIdResponse = withContext(Dispatchers.IO) {
                    RetrofitInstance.api.getUserIdByEmail(email)
                }

                // Extrai o userId da resposta e busca o establishmentId associado
                val userId = userIdResponse.userId
                fetchEstablishmentId(userId)

            } catch (e: Exception) {
                // Exibe uma mensagem de erro e loga a exceção em caso de falha
                Log.e("fetchUserIdByEmail", "Erro: ${e.message}", e)
                showToast("Erro: ${e.message}")
            }
        }
    }

    // Função para buscar o establishmentId associado ao userId
    private fun fetchEstablishmentId(userId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Faz uma requisição para a API para obter o establishmentId
                val response = RetrofitInstance.api.getEstablishmentIdByOwnerId(userId)

                // Processa a resposta e armazena o establishmentId se a resposta for bem-sucedida
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        establishmentId = response.body()?.get("establishmentId").toString()
                    }
                }
            } catch (e: Exception) {
                // Exibe uma mensagem de erro em caso de exceção
                withContext(Dispatchers.Main) {
                    showToast("Exception: ${e.message}")
                }
            }
        }
    }

    // Função para validar os campos de entrada do formulário
    private fun validateFields() {
        val name = binding.etName.text.toString().trim()
        val url = binding.etPhoto.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()
        val price = binding.etPrice.text.toString().trim()

        // Valida o campo de nome
        if (name.isEmpty()) {
            showToast("Por favor, preencha o campo Nome.")
            return
        }

        // Valida o campo de URL da foto
        if (url.isEmpty()) {
            showToast("Por favor, preencha o campo URL.")
            return
        }

        // Valida a descrição com limite de caracteres
        if (description.isEmpty() || description.length > 100) {
            showToast("A descrição deve ter no máximo 100 caracteres.")
            return
        }

        // Valida o preço para estar no formato R$00,00
        if (!price.matches(Regex("^R\\$\\d{1,3}(,\\d{2})?\$"))) {
            showToast("Por favor, preencha o preço no formato correto (R$00,00).")
            return
        }

        // Chama a função para criar o produto, caso todas as validações sejam satisfeitas
        createProduct(name, url, description, price, establishmentId)
    }

    // Função para criar um novo produto na API
    private fun createProduct(name: String, url: String, description: String, price: String, establishmentId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Cria o objeto de produto com os dados fornecidos
                val product = Product(
                    establishmentId = establishmentId,
                    name = name,
                    description = description,
                    evaluation = "0.0", // Definido como 0.0 inicialmente
                    photo = url,
                    price = price
                )

                // Faz a requisição para criar o produto na API
                val response = RetrofitInstance.api.createProduct(product)

                // Processa a resposta, exibindo mensagens de sucesso ou erro
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        showToast("Produto criado com sucesso!")
                        comeBack() // Retorna à tela anterior após a criação do produto
                    } else {
                        showToast("Erro ao criar produto: ${response.errorBody()?.string()}")
                    }
                }
            } catch (e: Exception) {
                // Exibe uma mensagem de erro em caso de exceção
                withContext(Dispatchers.Main) {
                    Log.e("CreateProductActivity", "Erro: ${e.message}", e)
                    showToast("Erro: ${e.message}")
                }
            }
        }
    }

    // Função para retornar à Activity de estabelecimento
    private fun comeBack() {
        val intent = Intent(this, MyEstablishmentActivity::class.java)
        startActivity(intent)
    }

    // Função auxiliar para exibir mensagens de toast
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
