package br.edu.puccampinas.campusconnect

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
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

        val productId = intent.getStringExtra("productId") ?:" N/A"
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

}
