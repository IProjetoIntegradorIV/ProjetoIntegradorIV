package br.edu.puccampinas.campusconnect

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import br.edu.puccampinas.campusconnect.data.model.LoginRequest
import br.edu.puccampinas.campusconnect.data.model.LoginResponse
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
        binding.btnLog.setOnClickListener { view ->

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
                    verifyCredentials(view, email, senha)
                }
            }
        }
    }

    private fun verifyCredentials(view: View, email: String, senha: String) {
        Log.d("LoginActivity", "Verificando credenciais para $email")
        val loginRequest = LoginRequest(email, senha)
        RetrofitInstance.api.loginUser(loginRequest).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                Log.d("LoginActivity", "Resposta recebida: ${response.code()}")
                if (response.isSuccessful) {
                    val loginResponse = response.body()
                    Log.d("LoginActivity", "Resposta do corpo: $loginResponse")
                    if (loginResponse != null && loginResponse.success) {
                        Toast.makeText(this@LoginActivity, loginResponse.message, Toast.LENGTH_SHORT).show()
                        navigateMainScreen()
                    } else {
                        // Caso a resposta não seja bem-sucedida, mas a resposta tenha sido recebida
                        Toast.makeText(this@LoginActivity, loginResponse?.message ?: "Erro desconhecido", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Verificando se o código de resposta é 401 = usuário inexistente
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

    private fun navigateMainScreen(){
        val intent = Intent(this, Inicio::class.java)
        startActivity(intent)
    }

    private fun comeBack(){
        val intent = Intent(this, Inicio::class.java)
        startActivity(intent)
    }
}
