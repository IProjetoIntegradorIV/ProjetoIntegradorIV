package br.edu.puccampinas.campusconnect

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import br.edu.puccampinas.campusconnect.data.model.LoginRequest
import br.edu.puccampinas.campusconnect.data.model.LoginResponse
import br.edu.puccampinas.campusconnect.data.model.ResponseMessage
import br.edu.puccampinas.campusconnect.data.network.RetrofitInstance
import br.edu.puccampinas.campusconnect.databinding.ActivityLoginBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// Classe de atividade para realizar login do usuário
class LoginActivity : AppCompatActivity() {

    // Variável de binding para vincular os elementos de layout ao código
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater) // Inicializa o binding com o layout
        setContentView(binding.root)

        // Define o listener para o botão de voltar
        binding.comeBack.setOnClickListener {
            comeBack()
        }

        // Define o listener para o botão de login
        binding.btnLog.setOnClickListener {
            val email = binding.etEmail.text.toString() // Obtém o texto do campo de email
            val senha = binding.etPassword.text.toString() // Obtém o texto do campo de senha

            // Valida os campos de email e senha
            when {
                email.isEmpty() -> {
                    binding.etEmail.error = "Preencha o E-mail!"
                }
                senha.isEmpty() -> {
                    binding.etPassword.error = "Digite sua senha!"
                }
                else -> {
                    verifyCredentials(email, senha) // Verifica as credenciais
                }
            }
        }
    }

    // Função para verificar as credenciais do usuário chamando o backend
    private fun verifyCredentials(email: String, senha: String) {
        Log.d("LoginActivity", "Verificando credenciais para $email")
        val loginRequest = LoginRequest(email, senha) // Cria o objeto de requisição de login
        RetrofitInstance.api.loginUser(loginRequest).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                Log.d("LoginActivity", "Resposta recebida: ${response.code()}")

                if (response.errorBody() != null) {
                    Log.e("LoginActivity", "Erro: ${response.errorBody()!!.string()}")
                }

                // Verifica se a resposta foi bem-sucedida
                if (response.isSuccessful) {
                    val loginResponse = response.body()
                    Log.d("LoginActivity", "Resposta do corpo: $loginResponse")

                    // Se o login foi bem-sucedido, salva o email no SharedPreferences
                    if (loginResponse != null && loginResponse.success) {
                        val sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE)
                        with(sharedPref.edit()) {
                            putString("logged_user_email", email)
                            apply()
                        }
                        // Verifica se o usuário é proprietário de um estabelecimento
                        checkEstablishmentOwner(email)
                    } else {
                        Toast.makeText(this@LoginActivity, loginResponse?.message ?: "Erro desconhecido", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Trata o caso onde o usuário não existe ou outros erros
                    if (response.code() == 401) {
                        Toast.makeText(this@LoginActivity, "Esse usuário não existe.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@LoginActivity, "Erro ao processar o login: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            // Trata falhas de conexão ou de processamento da requisição
            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Log.e("LoginError", "Erro: ${t.message}", t)
                Toast.makeText(this@LoginActivity, "Erro: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Função para verificar se o usuário é proprietário de um estabelecimento
    private fun checkEstablishmentOwner(email: String) {
        Log.d("LoginActivity", "Verificando status de proprietário do estabelecimento para $email")
        RetrofitInstance.api.checkEstablishmentOwner(email).enqueue(object : Callback<ResponseMessage> {
            override fun onResponse(call: Call<ResponseMessage>, response: Response<ResponseMessage>) {
                if (response.isSuccessful) {
                    val responseMessage = response.body()
                    val isOwner = responseMessage?.message?.contains("true") == true

                    // Salva o status de proprietário de estabelecimento no SharedPreferences
                    val sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE)
                    with(sharedPref.edit()) {
                        putBoolean("establishment_owner", isOwner)
                        apply()
                    }

                    // Redireciona para a tela apropriada com base no status de proprietário
                    checkEstablishmentOwnerRedirect(isOwner)
                } else {
                    Toast.makeText(this@LoginActivity, "Erro ao verificar o status do proprietário", Toast.LENGTH_SHORT).show()
                }
            }

            // Trata falhas de conexão ou de processamento da requisição
            override fun onFailure(call: Call<ResponseMessage>, t: Throwable) {
                Log.e("LoginError", "Erro: ${t.message}", t)
                Toast.makeText(this@LoginActivity, "Erro ao verificar o status do proprietário", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Função para redirecionar o usuário para a tela apropriada com base no status de proprietário
    private fun checkEstablishmentOwnerRedirect(isOwner: Boolean) {
        val intent = if (isOwner) {
            Intent(this, MyEstablishmentActivity::class.java) // Redireciona para a atividade de estabelecimento próprio
        } else {
            Intent(this, EstablishmentActivity::class.java) // Redireciona para a atividade de estabelecimentos em geral
        }
        startActivity(intent)
    }

    // Função para voltar para a tela inicial
    private fun comeBack(){
        val intent = Intent(this, Inicio::class.java)
        startActivity(intent)
    }
}
