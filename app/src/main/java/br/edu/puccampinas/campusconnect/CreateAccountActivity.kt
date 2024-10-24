package br.edu.puccampinas.campusconnect

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import br.edu.puccampinas.campusconnect.databinding.ActivityCreateAccountBinding
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

        binding.setaVoltar.setOnClickListener {
            Voltar()
        }

        binding.btnCadastrar.setOnClickListener {
            cadastrarPessoa()
        }
    }

    private fun cadastrarPessoa() {
        val nomeCompleto = binding.etNome.text.toString()
        val email = binding.etEmail.text.toString()
        val senha = binding.etSenha.text.toString()
        val confirmar_senha = binding.etConfirmarSenha.text.toString()

        if (nomeCompleto.length < 5) {
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

        if (senha.length < 6) {
            Toast.makeText(this, "A senha deve ter pelo menos 6 caracteres!", Toast.LENGTH_SHORT)
                .show()
            return
        }

        if (confirmar_senha.length < 6) {
            Toast.makeText(this, "A senha deve ter pelo menos 6 caracteres!", Toast.LENGTH_SHORT)
                .show()
            return
        }

        if (senha != confirmar_senha) {
            Toast.makeText(this, "As senhas não coincidem!", Toast.LENGTH_SHORT).show()
            return
        }

        if (TextUtils.isEmpty(nomeCompleto) ||
            TextUtils.isEmpty(email) ||
            TextUtils.isEmpty(senha) ||
            TextUtils.isEmpty(confirmar_senha)
        ) {
            Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show()
            return
        }

        sendToServer(nomeCompleto, email, senha)
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

    private fun Voltar(){
        val intent = Intent(this, Inicio::class.java)
        startActivity(intent)
    }
}
