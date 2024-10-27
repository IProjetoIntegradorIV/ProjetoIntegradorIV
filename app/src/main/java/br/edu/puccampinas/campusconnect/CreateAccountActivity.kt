package br.edu.puccampinas.campusconnect

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import br.edu.puccampinas.campusconnect.data.model.ResponseMessage
import br.edu.puccampinas.campusconnect.data.model.User
import br.edu.puccampinas.campusconnect.data.network.ApiService
import br.edu.puccampinas.campusconnect.data.network.RetrofitInstance
import br.edu.puccampinas.campusconnect.databinding.ActivityCreateAccountBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayInputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.InetSocketAddress
import java.net.Socket

class CreateAccountActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreateAccountBinding
    private var socket: Socket? = null
    private var inputStream: ObjectInputStream? = null
    private var outputStream: ObjectOutputStream? = null
    private var parceiro: Parceiro? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.comeBack.setOnClickListener {
            comeBack()
        }

        binding.btnRegister.setOnClickListener {
            register()
        }
    }

    private fun register() {
        val name = binding.etName.text.toString()
        val email = binding.etEmail.text.toString()
        val password = binding.etPassword.text.toString()
        val confirmPassword = binding.etConfimPassword.text.toString()

        if (name.length < 5) {
            Toast.makeText(
                this,
                "O nome completo deve ter pelo menos 5 caracteres!",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (!email.endsWith("@gmail.com")) {
            Toast.makeText(this, "O e-mail deve ser do domínio @gmail.com!", Toast.LENGTH_SHORT)
                .show()
            return
        }

        if (password.length < 6) {
            Toast.makeText(this, "A senha deve ter pelo menos 6 caracteres!", Toast.LENGTH_SHORT)
                .show()
            return
        }

        if (confirmPassword.length < 6) {
            Toast.makeText(this, "A senha deve ter pelo menos 6 caracteres!", Toast.LENGTH_SHORT)
                .show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(this, "As senhas não coincidem!", Toast.LENGTH_SHORT).show()
            return
        }

        if (TextUtils.isEmpty(name) ||
            TextUtils.isEmpty(email) ||
            TextUtils.isEmpty(password) ||
            TextUtils.isEmpty(confirmPassword)
        ) {
            Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show()
            return
        }

        val newUser = User(name, email, password)

        RetrofitInstance.api.createUser(newUser).enqueue(object : Callback<ResponseMessage> {
            override fun onResponse(call: Call<ResponseMessage>, response: Response<ResponseMessage>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@CreateAccountActivity, response.body()?.message, Toast.LENGTH_SHORT).show()
                    comeBack()
                } else {
                    Toast.makeText(this@CreateAccountActivity, response.errorBody()?.string() ?: "Erro ao criar conta.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ResponseMessage>, t: Throwable) {
                Toast.makeText(this@CreateAccountActivity, "Erro: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })

    }

    private fun sendToServer(nome: String, email: String, senha: String) {
        Thread {
            try {
                // Conectando ao servidor
                socket = Socket()
                socket?.connect(InetSocketAddress("localhost", 4000), 10000)

                if (socket?.isConnected == true) {
                    outputStream = ObjectOutputStream(socket!!.getOutputStream())
                    inputStream = ObjectInputStream(socket!!.getInputStream())

                    // Criar o Parceiro com os streams
                    parceiro = Parceiro(socket!!, inputStream!!, outputStream!!)

                    // Envia os dados de cadastro
                    val pedidoDeRegistro = PedidoDeRegistro(nome, email, senha)
                    parceiro?.receba(pedidoDeRegistro)


                    // Recebe a resposta do servidor
                    val resposta = parceiro?.envie()

                    // Log de confirmação de envio
                    Log.d("CreateAccountActivity", "Resposta do servidor: $resposta")

                    // Verifica se a resposta é um Resultado
                    if (resposta is Resultado) {
                        // Se for um Resultado, verifica a mensagem
                        if (resposta.valorResultante == "Dados recebidos e inseridos com sucesso!") {
                            runOnUiThread {
                                Toast.makeText(this, "Cadastro realizado com sucesso!", Toast.LENGTH_SHORT).show()
                            }
                            Handler(Looper.getMainLooper()).postDelayed({
                                navigateToLoginActivity()
                            }, 2000)
                        } else {
                            runOnUiThread {
                                Toast.makeText(this, "Erro: ${resposta.valorResultante}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        runOnUiThread {
                            Toast.makeText(this, "Resposta inesperada do servidor", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this, "Erro ao conectar ao servidor", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this, "Erro de conexão: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } finally {
                try {
                    parceiro?.receba(PedidoParaSair())
                    outputStream?.close()
                    inputStream?.close()
                    socket?.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }.start()
    }

    private fun navigateToLoginActivity() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            socket?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun comeBack(){
        val intent = Intent(this, Inicio::class.java)
        startActivity(intent)
    }
}
