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


class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.comeBack.setOnClickListener {
            comeBack()
        }
        binding.btnLog.setOnClickListener {

            val email = binding.etEmail.text.toString()
            val senha = binding.etPassword.text.toString()

            when {
                email.isEmpty() -> {
                    binding.etEmail.error = "Preencha o E-mail!"
                }

                senha.isEmpty() -> {
                    binding.etPassword.error = "Digite sua senha!"
                }

                else -> {
                    verifyCredentials(email, senha)
                }
            }
        }
    }

    private fun verifyCredentials(email: String, senha: String) {
        Log.d("LoginActivity", "Verificando credenciais para $email")
        val loginRequest = LoginRequest(email, senha)
        RetrofitInstance.api.loginUser(loginRequest).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                Log.d("LoginActivity", "Resposta recebida: ${response.code()}")

                if (response.errorBody() != null) {
                    Log.e("LoginActivity", "Erro: ${response.errorBody()!!.string()}")
                }

                if (response.isSuccessful) {
                    val loginResponse = response.body()
                    Log.d("LoginActivity", "Resposta do corpo: $loginResponse")
                    if (loginResponse != null && loginResponse.success) {
                        val sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE)
                        with(sharedPref.edit()) {
                            putString("logged_user_email", email)
                            apply()
                        }

                        checkEstablishmentOwner(email)
                    } else {
                        Toast.makeText(this@LoginActivity, loginResponse?.message ?: "Erro desconhecido", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    if (response.code() == 401) {
                        Toast.makeText(this@LoginActivity, "Esse usuário não existe.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@LoginActivity, "Erro ao processar o login: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Log.e("LoginError", "Erro: ${t.message}", t)
                Toast.makeText(this@LoginActivity, "Erro: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun checkEstablishmentOwner(email: String) {
        Log.d("LoginActivity", "Verificando status de proprietário do estabelecimento para $email")
        RetrofitInstance.api.checkEstablishmentOwner(email).enqueue(object : Callback<ResponseMessage> {
            override fun onResponse(call: Call<ResponseMessage>, response: Response<ResponseMessage>) {
                if (response.isSuccessful) {
                    val responseMessage = response.body()
                    val isOwner = responseMessage?.message?.contains("true") == true

                    val sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE)
                    with(sharedPref.edit()) {
                        putBoolean("establishment_owner", isOwner)
                        apply()
                    }

                    checkEstablishmentOwnerRedirect(isOwner)
                } else {
                    Toast.makeText(this@LoginActivity, "Erro ao verificar o status do proprietário", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ResponseMessage>, t: Throwable) {
                Log.e("LoginError", "Erro: ${t.message}", t)
                Toast.makeText(this@LoginActivity, "Erro ao verificar o status do proprietário", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun checkEstablishmentOwnerRedirect(isOwner: Boolean) {
        val intent = if (isOwner) {
            Intent(this, CreateAccountActivity::class.java)
        } else {
            Intent(this, EstablishmentActivity::class.java)
        }
        startActivity(intent)
    }

    private fun comeBack(){
        val intent = Intent(this, Inicio::class.java)
        startActivity(intent)
    }
}
