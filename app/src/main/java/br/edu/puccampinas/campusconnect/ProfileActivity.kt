package br.edu.puccampinas.campusconnect

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import br.edu.puccampinas.campusconnect.data.model.ResponseMessage
import br.edu.puccampinas.campusconnect.data.model.User
import br.edu.puccampinas.campusconnect.data.network.RetrofitInstance
import br.edu.puccampinas.campusconnect.databinding.ActivityProfileBinding
import com.bumptech.glide.Glide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private var loggedUserEmail: String? = null  // Armazena o email do usuário logado
    private var userId: String? = null  // Armazena o ID do usuário para identificá-lo nas requisições

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Recupera o email do usuário logado das SharedPreferences
        val sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE)
        loggedUserEmail = sharedPref.getString("logged_user_email", null)

        // Chama o método para buscar os dados do usuário
        fetchUserData()

        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configura os listeners de clique para os botões
        binding.logout.setOnClickListener {
            logout()  // Chama o método logout ao clicar no botão de logout
        }

        binding.comeBack.setOnClickListener {
            // Verifica se o usuário é dono de algum estabelecimento ao clicar em voltar
            loggedUserEmail?.let { it1 -> fetchIsEstablishmentOwner(it1) }
        }

        binding.imgPencil.setOnClickListener {
            changeName()  // Chama o método para alterar o nome do usuário
        }

        binding.editPhoto.setOnClickListener {
            changeUserPhoto()  // Chama o método para alterar a foto do usuário
        }

        binding.btnUpdate.setOnClickListener {
            changePassword()  // Chama o método para alterar a senha do usuário
        }
    }

    // Método para buscar os dados do usuário
    private fun fetchUserData() {
        val email = loggedUserEmail ?: run {
            Toast.makeText(this, "Email do usuário não encontrado!", Toast.LENGTH_SHORT).show()
            return
        }

        // Faz uma requisição para obter os dados do usuário pelo email
        RetrofitInstance.api.getUserByEmail(email).enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful) {
                    val user = response.body()
                    user?.let {
                        // Preenche os campos do layout com as informações do usuário
                        userId = it.id
                        binding.tvEmail.setText(it.email)
                        binding.etName.setText(it.name)
                        binding.tvPassword.setText(it.password)
                        if(it.photo != null){
                            // Carrega a foto do usuário usando Glide
                        Glide.with(this@ProfileActivity)
                            .load(it.photo)
                            .circleCrop()
                            .into(binding.imgLogo)}
                    }
                } else {
                    Log.e("ProfileActivity", "Erro ao buscar dados do usuário: ${response.errorBody()?.string()}")
                    Toast.makeText(this@ProfileActivity, "Erro ao carregar dados do usuário.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                Log.e("ProfileActivity", "Falha na requisição: ${t.message}")
                Toast.makeText(this@ProfileActivity, "Erro na conexão.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Método para alterar a foto do usuário
    private fun changeUserPhoto() {
        val photo = binding.etPhoto.text.toString()

        if (TextUtils.isEmpty(photo)) {
            Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show()
            return
        }

        // Verifica se o ID do usuário existe
        userId?.let {
            // Lançando uma Coroutine para executar a função suspensa
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = RetrofitInstance.api.changeUserPhoto(it, photo)

                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful) {
                            Log.d("ProfileActivity", "Foto alterada com sucesso: ${response.body()?.message}")
                            Toast.makeText(this@ProfileActivity, "User photo updated successfully.", Toast.LENGTH_SHORT).show()
                        } else {
                            Log.e("ProfileActivity", "Erro ao mudar a foto: ${response.errorBody()?.string()}")
                            Toast.makeText(this@ProfileActivity, "Error changing the photo.", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Log.e("ProfileActivity", "Falha na requisição: ${e.message}")
                        Toast.makeText(this@ProfileActivity, "Connection error.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    // Método para alterar o nome do usuário
    private fun changeName() {
        val name = binding.etName.text.toString()

        val email = loggedUserEmail ?: run {
            Toast.makeText(this, "Email do usuário não encontrado!", Toast.LENGTH_SHORT).show()
            return
        }

        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show()
            return
        }

        // Envia a requisição para alterar o nome do usuário
        RetrofitInstance.api.changeName(email, name).enqueue(object : Callback<ResponseMessage> {
            override fun onResponse(call: Call<ResponseMessage>, response: Response<ResponseMessage>) {
                Log.d("ProfileActivity", "Resposta recebida: ${response.code()}")

                if (response.isSuccessful) {
                    Log.d("ProfileActivity", "Nome alterado com sucesso: ${response.body()?.message}")
                    Toast.makeText(this@ProfileActivity, "Nome alterado com sucesso!", Toast.LENGTH_SHORT).show()
                } else {
                    Log.e("ProfileActivity", "Erro ao mudar o nome: ${response.errorBody()?.string()}")
                    Toast.makeText(this@ProfileActivity, "Erro ao alterar o nome.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ResponseMessage>, t: Throwable) {
                Log.e("ProfileActivity", "Falha na requisição: ${t.message}")
                Toast.makeText(this@ProfileActivity, "Erro na conexão.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Método para alterar a senha do usuário
    private fun changePassword() {
        val password = binding.etPassword.text.toString()
        val confirmPassword = binding.etConfirmPassword.text.toString()

        val email = loggedUserEmail ?: run {
            Toast.makeText(this, "Email do usuário não encontrado!", Toast.LENGTH_SHORT).show()
            return
        }

        if (TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.length < 6) {
            Toast.makeText(this, "A senha deve ter pelo menos 6 caracteres!", Toast.LENGTH_SHORT).show()
            return
        }

        if (confirmPassword.length < 6) {
            Toast.makeText(this, "A senha deve ter pelo menos 6 caracteres!", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(this, "As senhas não coincidem!", Toast.LENGTH_SHORT).show()
            return
        }

        // Envia a requisição para alterar a senha
        RetrofitInstance.api.changePassword(email, password).enqueue(object : Callback<ResponseMessage> {
            override fun onResponse(call: Call<ResponseMessage>, response: Response<ResponseMessage>) {
                Log.d("ProfileActivity", "Resposta recebida: ${response.code()}")

                if (response.isSuccessful) {
                    Log.d("ProfileActivity", "Senha alterada com sucesso: ${response.body()?.message}")
                    Toast.makeText(this@ProfileActivity, "Senha alterada com sucesso!", Toast.LENGTH_SHORT).show()
                } else {
                    Log.e("ProfileActivity", "Erro ao mudar a senha: ${response.errorBody()?.string()}")
                    Toast.makeText(this@ProfileActivity, "Erro ao alterar a senha.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ResponseMessage>, t: Throwable) {
                Log.e("ProfileActivity", "Falha na requisição: ${t.message}")
                Toast.makeText(this@ProfileActivity, "Erro na conexão.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Método para deslogar o usuário
    private fun logout() {
        val sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE)
        with(sharedPref.edit()) {
            remove("logged_user_email")  // Remove o email do usuário das SharedPreferences
            apply()
        }
        val intent = Intent(this, Inicio::class.java)  // Redireciona para a tela inicial
        startActivity(intent)
    }

    // Método para verificar se o usuário é proprietário de um estabelecimento
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
        val intent = Intent(this, MyEstablishmentActivity::class.java)
        startActivity(intent)
    }

    // Função chamada quando o usuário não é proprietário
    private fun onNotOwnerFound() {
        val intent = Intent(this, EstablishmentActivity::class.java)
        startActivity(intent)
    }

    fun showToast(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }

}
