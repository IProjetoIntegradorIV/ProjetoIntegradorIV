package br.edu.puccampinas.campusconnect

import android.content.Intent
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.edu.puccampinas.campusconnect.data.model.Establishment
import br.edu.puccampinas.campusconnect.data.network.RetrofitInstance
import com.bumptech.glide.Glide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import br.edu.puccampinas.campusconnect.data.model.ResponseMessage
import br.edu.puccampinas.campusconnect.data.model.UserIdResponse
import br.edu.puccampinas.campusconnect.data.network.ApiService
import br.edu.puccampinas.campusconnect.databinding.ActivityMyEstablishmentBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private lateinit var binding: ActivityMyEstablishmentBinding
private lateinit var productAdapter: ProductAdapter
private lateinit var recyclerView: RecyclerView
private var loggedUserEmail: String? = null
private var userId: String? = null
private lateinit var establishmentId: String

class MyEstablishmentActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyEstablishmentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.logout.setOnClickListener {
            logout()
        }

        binding.profile.setOnClickListener {
            profile()
        }

        // Recupera o email do usuário logado das preferências
        val sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE)
        loggedUserEmail = sharedPref.getString("logged_user_email", null)

        loggedUserEmail?.let { fetchUserIdByEmail(it) }

        // Configura o RecyclerView
        recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)

        binding.editName.setOnClickListener {
            changeEstablishmentName()
        }

        binding.editDescription.setOnClickListener {
            changeEstablishmentDescription()
        }

        binding.editOpeningHours.setOnClickListener {
            changeEstablishmentOpeningHours()
        }

        binding.editPhoto.setOnClickListener {
            changeEstablishmentPhoto()
        }

        binding.delete.setOnClickListener {
            showPopup()
        }
    }

    fun fetchUserIdByEmail(email: String) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val userIdResponse = withContext(Dispatchers.IO) {
                    RetrofitInstance.api.getUserIdByEmail(email)
                }
                val userId = userIdResponse.userId
                fetchEstablishmentId(userId)            } catch (e: Exception) {
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

                        if (establishmentId != null) {
                            binding.register.visibility = View.GONE
                            binding.btnRegister.visibility = View.GONE
                            fetchEstablishmentDetails(establishmentId!!)
                        } else {
                            showToast("Establishment ID not found in response.")
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showToast("Exception: ${e.message}")
                }
            }
        }
    }


    private fun fetchProducts(establishmentId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.api.getProductsByEstablishment(establishmentId)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        val products = response.body()!!
                        productAdapter = ProductAdapter(products)
                        recyclerView.adapter = productAdapter
                    } else {
                        showToast("Failed to load products. Code: ${response.code()}")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showToast("Exception3: ${e.message}")
                }
            }
        }
    }

    private fun fetchEstablishmentDetails(establishmentId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.api.getEstablishmentById(establishmentId)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        val establishment = response.body()!!

                        binding.establishmentPhoto.visibility = View.VISIBLE
                        binding.name.visibility = View.VISIBLE
                        binding.description.visibility = View.VISIBLE
                        binding.openingHours.visibility = View.VISIBLE
                        binding.products.visibility = View.VISIBLE
                        binding.editPhoto.visibility = View.VISIBLE
                        binding.editName.visibility = View.VISIBLE
                        binding.editDescription.visibility = View.VISIBLE
                        binding.editOpeningHours.visibility = View.VISIBLE
                        binding.etPhoto.visibility = View.VISIBLE
                        binding.delete.visibility = View.VISIBLE
                        binding.btnAddProduct.visibility = View.VISIBLE

                        displayEstablishmentDetails(establishment)
                        fetchProducts(establishmentId)
                    } else {
                        showToast("Failed to load establishment details. Code: ${response.code()}")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showToast("Exception4: ${e.message}")
                }
            }
        }
    }

    private fun displayEstablishmentDetails(establishment: Establishment) {
        binding.name.hint = establishment.name
        binding.description.hint = establishment.description
        binding.openingHours.hint = establishment.openingHours
        Glide.with(this).load(establishment.photo).into(binding.establishmentPhoto)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun logout() {
        val sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE)
        with(sharedPref.edit()) {
            remove("logged_user_email")
            apply()
        }
        val intent = Intent(this, Inicio::class.java)
        startActivity(intent)
    }

    private fun profile() {
        val intent = Intent(this,ProfileActivity::class.java)
        startActivity(intent)
    }

    private fun changeEstablishmentName() {
        val name = binding.name.text.toString()

        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show()
            return
        }

        // Verifica se o ID do estabelecimento existe
        establishmentId?.let {
            // Lançando uma Coroutine para executar a função suspensa
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = RetrofitInstance.api.changeEstablishmentName(it, name)

                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful) {
                            Log.d("MyEstablishmentActivity", "Nome alterado com sucesso: ${response.body()?.message}")
                            Toast.makeText(this@MyEstablishmentActivity, "Establishment name updated successfully.", Toast.LENGTH_SHORT).show()
                        } else {
                            Log.e("MyEstablishmentActivity", "Erro ao mudar o nome: ${response.errorBody()?.string()}")
                            Toast.makeText(this@MyEstablishmentActivity, "Error changing the name.", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Log.e("MyEstablishmentActivity", "Falha na requisição: ${e.message}")
                        Toast.makeText(this@MyEstablishmentActivity, "Connection error.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun changeEstablishmentDescription() {
        val description = binding.description.text.toString()

        if (TextUtils.isEmpty(description)) {
            Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show()
            return
        }

        // Verifica se o ID do estabelecimento existe
        establishmentId?.let {
            // Lançando uma Coroutine para executar a função suspensa
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = RetrofitInstance.api.changeEstablishmentDescription(it, description)

                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful) {
                            Log.d("MyEstablishmentActivity", "Descrição alterada com sucesso: ${response.body()?.message}")
                            Toast.makeText(this@MyEstablishmentActivity, "Establishment description updated successfully.", Toast.LENGTH_SHORT).show()
                        } else {
                            Log.e("MyEstablishmentActivity", "Erro ao mudar o nome: ${response.errorBody()?.string()}")
                            Toast.makeText(this@MyEstablishmentActivity, "Error changing the description.", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Log.e("MyEstablishmentActivity", "Falha na requisição: ${e.message}")
                        Toast.makeText(this@MyEstablishmentActivity, "Connection error.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun changeEstablishmentOpeningHours() {
        val openingHours = binding.openingHours.text.toString()

        if (TextUtils.isEmpty(openingHours)) {
            Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show()
            return
        }

        // Verifica se o ID do estabelecimento existe
        establishmentId?.let {
            // Lançando uma Coroutine para executar a função suspensa
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = RetrofitInstance.api.changeEstablishmentOpeningHours(it, openingHours)

                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful) {
                            Log.d("MyEstablishmentActivity", "Horário de funcionamento alterado com sucesso: ${response.body()?.message}")
                            Toast.makeText(this@MyEstablishmentActivity, "Establishment opening hours updated successfully.", Toast.LENGTH_SHORT).show()
                        } else {
                            Log.e("MyEstablishmentActivity", "Erro ao mudar o horário de funcionamento: ${response.errorBody()?.string()}")
                            Toast.makeText(this@MyEstablishmentActivity, "Error changing the opening hours.", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Log.e("MyEstablishmentActivity", "Falha na requisição: ${e.message}")
                        Toast.makeText(this@MyEstablishmentActivity, "Connection error.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun changeEstablishmentPhoto() {
        val photo = binding.etPhoto.text.toString()

        if (TextUtils.isEmpty(photo)) {
            Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show()
            return
        }

        // Verifica se o ID do estabelecimento existe
        establishmentId?.let {
            // Lançando uma Coroutine para executar a função suspensa
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = RetrofitInstance.api.changeEstablishmentPhoto(it, photo)

                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful) {
                            Log.d("MyEstablishmentActivity", "Foto alterada com sucesso: ${response.body()?.message}")
                            Toast.makeText(this@MyEstablishmentActivity, "Establishment photo updated successfully.", Toast.LENGTH_SHORT).show()
                        } else {
                            Log.e("MyEstablishmentActivity", "Erro ao mudar a foto: ${response.errorBody()?.string()}")
                            Toast.makeText(this@MyEstablishmentActivity, "Error changing the photo.", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Log.e("MyEstablishmentActivity", "Falha na requisição: ${e.message}")
                        Toast.makeText(this@MyEstablishmentActivity, "Connection error.", Toast.LENGTH_SHORT).show()
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

        val text = dialogView.findViewById<TextView>(R.id.text)
        text.text = "Tem certeza que deseja excluir esse estabelecimento?"

        val btnClose = dialogView.findViewById<ImageView>(R.id.btnClose)
        btnClose.setOnClickListener {
            dialog.dismiss()
        }

        val btnDelete = dialogView.findViewById<Button>(R.id.btnDelete)
        btnDelete.setOnClickListener {
            dialog.dismiss()
            deleteEstablishment()
            startActivity(Intent(this,LoginActivity::class.java))
        }

        dialog.show()
    }

    private fun deleteEstablishment() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.api.deleteEstablishmentById(establishmentId)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        showToast("establishment deleted successfully.")
                    } else {
                        Log.e("MyEstablishmentActivity", "Error deleting establishment: ${response.errorBody()?.string()}")
                        showToast("Error deleting product.")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("MyEstablishmentActivity", "Exception during delete: ${e.message}")
                    showToast("Connection error.")
                }
            }
        }
    }

}
