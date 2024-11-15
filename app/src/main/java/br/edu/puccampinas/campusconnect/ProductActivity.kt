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
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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
        enableEdgeToEdge()
        binding = ActivityProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE)
        loggedUserEmail = sharedPref.getString("logged_user_email", null)

        loggedUserEmail?.let { fetchIsEstablishmentOwner(it) }

        binding.comeBack.setOnClickListener {
            val intent = Intent(this, nextActivityClass)
            intent.putExtra("establishmentId", establishmentId)
            startActivity(intent)
        }

        productId = intent.getStringExtra("productId") ?:" N/A"
        establishmentId = intent.getStringExtra("establishmentId") ?: "N/A"
        val productName = intent.getStringExtra("productName") ?: "N/A"
        val productDescription = intent.getStringExtra("productDescription") ?: "N/A"
        val productEvaluation = intent.getStringExtra("productEvaluation") ?: "N/A"
        val productPhoto = intent.getStringExtra("productPhoto") ?: "N/A"
        val productPrice = intent.getStringExtra("productPrice")?: "N/A"

        binding.name.text = productName
        binding.description.text = productDescription
        binding.evaluation.text = productEvaluation
        binding.price.text = productPrice
        Glide.with(this).load(productPhoto).into(binding.imageView)

        binding.etname.hint = productName
        binding.etdescription.hint = productDescription
        binding.etPrice.hint = productPrice

        binding.editName.setOnClickListener {
            changeProductName()
        }

        binding.editDescription.setOnClickListener {
            changeProductDescription()
        }

        binding.editPrice.setOnClickListener {
            changeProductPrice()
        }

        binding.editPhoto.setOnClickListener {
            changeProductPhoto()
        }

        binding.delete.setOnClickListener {
            showPopup()
        }
    }

    private fun fetchIsEstablishmentOwner(email: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
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
                    showToast("Exceção: ${e.message}")
                }
            }
        }
    }

    // Função chamada quando o usuário é proprietário
    private fun onOwnerFound() {
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

        nextActivityClass = MyEstablishmentActivity::class.java
    }

    // Função chamada quando o usuário não é proprietário
    private fun onNotOwnerFound() {
        nextActivityClass =ProductsActivity::class.java

    }

    fun showToast(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }

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
                            Log.d("MyEstablishmentActivity", "Nome alterado com sucesso: ${response.body()?.message}")
                            Toast.makeText(this@ProductActivity, "Product name updated successfully.", Toast.LENGTH_SHORT).show()
                        } else {
                            Log.e("MyEstablishmentActivity", "Erro ao mudar o nome: ${response.errorBody()?.string()}")
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
                            Log.d("MyEstablishmentActivity", "Descrição alterada com sucesso: ${response.body()?.message}")
                            Toast.makeText(this@ProductActivity, "Product description updated successfully.", Toast.LENGTH_SHORT).show()
                        } else {
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
                            Log.d("MyEstablishmentActivity", "Preço alterado com sucesso: ${response.body()?.message}")
                            Toast.makeText(this@ProductActivity, "Product price updated successfully.", Toast.LENGTH_SHORT).show()
                        } else {
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
                            Log.d("MyEstablishmentActivity", "Foto alterada com sucesso: ${response.body()?.message}")
                            Toast.makeText(this@ProductActivity, "Product photo updated successfully.", Toast.LENGTH_SHORT).show()
                        } else {
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
