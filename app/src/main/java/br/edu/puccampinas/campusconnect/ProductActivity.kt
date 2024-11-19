package br.edu.puccampinas.campusconnect

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import br.edu.puccampinas.campusconnect.data.network.RetrofitInstance
import br.edu.puccampinas.campusconnect.databinding.ActivityProductBinding
import com.bumptech.glide.Glide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private lateinit var binding: ActivityProductBinding
private lateinit var establishmentId: String
private lateinit var productId: String
private var loggedUserEmail: String? = null

class ProductActivity : AppCompatActivity() {

    private var nextActivityClass: Class<out AppCompatActivity> = ProductsActivity::class.java

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductBinding.inflate(layoutInflater)  // Associa o layout com o código
        setContentView(binding.root)

        // Recupera o email do usuário logado
        val sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE)
        loggedUserEmail = sharedPref.getString("logged_user_email", null)

        // Verifica se o usuário é proprietário do estabelecimento
        loggedUserEmail?.let { fetchIsEstablishmentOwner(it) }

        // Configura o botão de voltar para a tela anterior
        binding.comeBack.setOnClickListener {
            val intent = Intent(this, nextActivityClass)
            intent.putExtra("establishmentId", establishmentId)
            startActivity(intent)
        }

        // Obtém os dados do produto passados pela Intent
        productId = intent.getStringExtra("productId") ?:" N/A"
        establishmentId = intent.getStringExtra("establishmentId") ?: "N/A"
        val productName = intent.getStringExtra("productName") ?: "N/A"
        val productDescription = intent.getStringExtra("productDescription") ?: "N/A"
        val productEvaluation = intent.getStringExtra("productEvaluation") ?: "N/A"
        val productPhoto = intent.getStringExtra("productPhoto") ?: "N/A"
        val productPrice = intent.getStringExtra("productPrice")?: "N/A"

        // Exibe as informações do produto na tela
        binding.name.text = productName
        binding.description.text = productDescription
        binding.evaluation.text = productEvaluation
        binding.price.text = productPrice
        Glide.with(this).load(productPhoto).into(binding.imageView)

        // Define os hints nos campos de entrada de dados
        binding.etname.hint = productName
        binding.etdescription.hint = productDescription
        binding.etPrice.hint = productPrice

        // Configura os listeners para os botões de edição
        binding.editName.setOnClickListener {
            changeProductName()  // Chama a função para alterar o nome do produto
        }

        binding.editDescription.setOnClickListener {
            changeProductDescription()  // Chama a função para alterar a descrição do produto
        }

        binding.editPrice.setOnClickListener {
            changeProductPrice()  // Chama a função para alterar o preço do produto
        }

        binding.editPhoto.setOnClickListener {
            changeProductPhoto()  // Chama a função para alterar a foto do produto
        }

        binding.delete.setOnClickListener {
            showPopup()  // Exibe o pop-up de confirmação de exclusão
        }
    }

    // Função que verifica se o usuário é o proprietário do estabelecimento
    private fun fetchIsEstablishmentOwner(email: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Faz a requisição para a API para verificar se o usuário é proprietário
                val response = RetrofitInstance.api.isEstablishmentOwner(email)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        // Verifica se o retorno é verdadeiro ou falso
                        val isOwner = response.body()?.get("isEstablishmentOwner") ?: false
                        if (isOwner) {
                            // Chama a função quando for verdadeiro
                            onOwnerFound()
                        } else {
                            // Chama a função quando for falso
                            onNotOwnerFound()
                        }
                    } else {
                        // Se não for bem-sucedido, mostra a mensagem de erro
                        val errorMessage = response.errorBody()?.string() ?: "Erro desconhecido"
                        showToast("Erro: $errorMessage")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    // Exibe uma mensagem caso ocorra uma exceção durante a requisição
                    showToast("Exceção: ${e.message}")
                }
            }
        }
    }

    // Função chamada quando o usuário é proprietário
    private fun onOwnerFound() {
        // Mostra os campos de edição e oculta os campos de visualização
        binding.name.visibility = View.GONE
        binding.description.visibility = View.GONE
        binding.price.visibility = View.GONE
        binding.heartIcon.visibility = View.GONE
        binding.etname.visibility = View.VISIBLE
        binding.editName.visibility = View.VISIBLE
        binding.etdescription.visibility = View.VISIBLE
        binding.editDescription.visibility = View.VISIBLE
        binding.etPrice.visibility = View.VISIBLE
        binding.editPrice.visibility = View.VISIBLE
        binding.etPhoto.visibility = View.VISIBLE
        binding.editPhoto.visibility = View.VISIBLE
        binding.delete.visibility = View.VISIBLE

        // Altera a classe da próxima atividade para MyEstablishmentActivity
        nextActivityClass = MyEstablishmentActivity::class.java
    }

    // Função chamada quando o usuário não é proprietário
    private fun onNotOwnerFound() {
        nextActivityClass =ProductsActivity::class.java

    }

    // Função para exibir Toasts na tela
    fun showToast(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }

    // Função para alterar o nome do produto
    private fun changeProductName() {
        val name = binding.etname.text.toString()

        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show()
            return
        }

        // Verifica se o ID do produto existe
        productId?.let {
            // Lançando uma Coroutine para executar a função suspensa
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = RetrofitInstance.api.changeProductName(it, name)

                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful) {
                            // Se a atualização for bem-sucedida, exibe uma mensagem
                            Log.d("MyEstablishmentActivity", "Nome alterado com sucesso: ${response.body()?.message}")
                            Toast.makeText(this@ProductActivity, "Product name updated successfully.", Toast.LENGTH_SHORT).show()
                        } else {
                            // Se houver erro na requisição, exibe uma mensagem de erro
                            Log.e("MyEstablishmentActivity", "Erro ao mudar o nome: ${response.errorBody()?.string()}")
                            Toast.makeText(this@ProductActivity, "Error changing the name.", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        // Exibe uma mensagem em caso de erro de conexão
                        Log.e("MyEstablishmentActivity", "Falha na requisição: ${e.message}")
                        Toast.makeText(this@ProductActivity, "Connection error.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    // Função para alterar a descrição do produto
    private fun changeProductDescription() {
        val description = binding.etdescription.text.toString()

        if (TextUtils.isEmpty(description)) {
            Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show()
            return
        }

        // Verifica se o ID do produto existe
        productId?.let {
            // Lançando uma Coroutine para executar a função suspensa
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = RetrofitInstance.api.changeProductDescription(it, description)

                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful) {
                            // Se a atualização for bem-sucedida, exibe uma mensagem
                            Log.d("MyEstablishmentActivity", "Descrição alterada com sucesso: ${response.body()?.message}")
                            Toast.makeText(this@ProductActivity, "Product description updated successfully.", Toast.LENGTH_SHORT).show()
                        } else {
                            // Se houver erro na requisição, exibe uma mensagem de erro
                            Log.e("MyEstablishmentActivity", "Erro ao mudar a descrição: ${response.errorBody()?.string()}")
                            Toast.makeText(this@ProductActivity, "Error changing the name.", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Log.e("MyEstablishmentActivity", "Falha na requisição: ${e.message}")
                        Toast.makeText(this@ProductActivity, "Connection error.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    // Função para alterar o preço do produto
    private fun changeProductPrice() {
        val price = binding.etPrice.text.toString()

        if (TextUtils.isEmpty(price)) {
            Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show()
            return
        }

        // Verifica se o ID do produto existe
        productId?.let {
            // Lançando uma Coroutine para executar a função suspensa
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = RetrofitInstance.api.changeProductPrice(it, price)

                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful) {
                            // Se a atualização for bem-sucedida, exibe uma mensagem
                            Log.d("MyEstablishmentActivity", "Preço alterado com sucesso: ${response.body()?.message}")
                            Toast.makeText(this@ProductActivity, "Product price updated successfully.", Toast.LENGTH_SHORT).show()
                        } else {
                            // Se houver erro na requisição, exibe uma mensagem de erro
                            Log.e("MyEstablishmentActivity", "Erro ao mudar o preço: ${response.errorBody()?.string()}")
                            Toast.makeText(this@ProductActivity, "Error changing the name.", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Log.e("MyEstablishmentActivity", "Falha na requisição: ${e.message}")
                        Toast.makeText(this@ProductActivity, "Connection error.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    // Função para alterar a foto do produto
    private fun changeProductPhoto() {
        val photo = binding.etPhoto.text.toString()

        if (TextUtils.isEmpty(photo)) {
            Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show()
            return
        }

        // Verifica se o ID do produto existe
        productId?.let {
            // Lançando uma Coroutine para executar a função suspensa
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = RetrofitInstance.api.changeProductPhoto(it, photo)

                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful) {
                            // Se a atualização for bem-sucedida, exibe uma mensagem
                            Log.d("MyEstablishmentActivity", "Foto alterada com sucesso: ${response.body()?.message}")
                            Toast.makeText(this@ProductActivity, "Product photo updated successfully.", Toast.LENGTH_SHORT).show()
                        } else {
                            // Se houver erro na requisição, exibe uma mensagem de erro
                            Log.e("MyEstablishmentActivity", "Erro ao mudar a foto: ${response.errorBody()?.string()}")
                            Toast.makeText(this@ProductActivity, "Error changing the photo.", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Log.e("MyEstablishmentActivity", "Falha na requisição: ${e.message}")
                        Toast.makeText(this@ProductActivity, "Connection error.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    // Exibe o pop-up de confirmação para deletar o produto
    private fun showPopup() {
        val dialogView = layoutInflater.inflate(R.layout.pop_up, null)
        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)

        builder.setCancelable(true)

        val dialog = builder.create()

        dialog.setOnCancelListener {
            dialog.dismiss()
        }

        val btnClose = dialogView.findViewById<ImageView>(R.id.btnClose)
        btnClose.setOnClickListener {
            dialog.dismiss()
        }

        val btnDelete = dialogView.findViewById<Button>(R.id.btnDelete)
        btnDelete.setOnClickListener {
            dialog.dismiss()
            deleteProduct()
            startActivity(Intent(this,MyEstablishmentActivity::class.java))
        }

        dialog.show()
    }

    // Função para excluir o produto
    private fun deleteProduct() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.api.deleteProductById(productId)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        showToast("Product deleted successfully.")
                    } else {
                        Log.e("ProductActivity", "Error deleting product: ${response.errorBody()?.string()}")
                        showToast("Error deleting product.")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("ProductActivity", "Exception during delete: ${e.message}")
                    showToast("Connection error.")
                }
            }
        }
    }
}
