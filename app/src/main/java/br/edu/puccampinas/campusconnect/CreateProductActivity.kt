package br.edu.puccampinas.campusconnect

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import br.edu.puccampinas.campusconnect.data.model.Product
import br.edu.puccampinas.campusconnect.data.network.RetrofitInstance
import br.edu.puccampinas.campusconnect.databinding.ActivityCreateProductBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CreateProductActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreateProductBinding
    private var loggedUserEmail: String? = null
    private lateinit var establishmentId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCreateProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE)
        loggedUserEmail = sharedPref.getString("logged_user_email", null)

        loggedUserEmail?.let { fetchUserIdByEmail(it) }

        binding.comeBack.setOnClickListener {
            comeBack()
        }

        binding.btnRegister.setOnClickListener {
            validateFields()
        }
    }

    fun fetchUserIdByEmail(email: String) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val userIdResponse = withContext(Dispatchers.IO) {
                    RetrofitInstance.api.getUserIdByEmail(email)
                }
                val userId = userIdResponse.userId
                fetchEstablishmentId(userId)} catch (e: Exception) {
                Log.e("fetchUserIdByEmail", "Erro: ${e.message}", e)
                showToast("Erro: ${e.message}")
            }
        }
    }

    private fun fetchEstablishmentId(userId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.api.getEstablishmentIdByOwnerId(userId)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        // Armazena o establishmentId na variável de instância
                        establishmentId = response.body()?.get("establishmentId").toString()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showToast("Exception: ${e.message}")
                }
            }
        }
    }

    private fun validateFields() {
        val name = binding.etName.text.toString().trim()
        val url = binding.etPhoto.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()
        val price = binding.etPrice.text.toString().trim()

        if (name.isEmpty()) {
            showToast("Por favor, preencha o campo Nome.")
            return
        }

        if (url.isEmpty()) {
            showToast("Por favor, preencha o campo URL.")
            return
        }

        if (description.isEmpty() || description.length > 100) {
            showToast("A descrição deve ter no máximo 100 caracteres.")
            return
        }

        if (!price.matches(Regex("^R\\$\\d{1,3}(,\\d{2})?\$"))) {
            showToast("Por favor, preencha o preço no formato correto (R$00,00).")
            return
        }

        createProduct(name, url, description, price, establishmentId)
    }

    private fun createProduct(name: String, url: String, description: String, price: String, establishmentId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Cria o objeto de produto com os dados inseridos
                val product = Product(
                    establishmentId = establishmentId,
                    name = name,
                    description = description,
                    evaluation = "0.0",
                    photo = url,
                    price = price
                )

                // Chama o endpoint para criar o produto
                val response = RetrofitInstance.api.createProduct(product)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        showToast("Produto criado com sucesso!")
                        comeBack()
                    } else {
                        showToast("Erro ao criar produto: ${response.errorBody()?.string()}")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("CreateProductActivity", "Erro: ${e.message}", e)
                    showToast("Erro: ${e.message}")
                }
            }
        }
    }

    private fun comeBack() {
        val intent = Intent(this, MyEstablishmentActivity::class.java)
        startActivity(intent)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}

