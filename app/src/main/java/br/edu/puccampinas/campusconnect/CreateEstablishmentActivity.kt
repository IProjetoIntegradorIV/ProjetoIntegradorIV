package br.edu.puccampinas.campusconnect

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import br.edu.puccampinas.campusconnect.data.network.RetrofitInstance
import br.edu.puccampinas.campusconnect.databinding.ActivityCreateEstablishmentBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.Socket

class CreateEstablishmentActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreateEstablishmentBinding
    // Variáveis para armazenar o email do usuário logado e seu ID
    private var loggedUserEmail: String? = null
    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateEstablishmentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Recupera o email do usuário logado das shared preferences
        val sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE)
        loggedUserEmail = sharedPref.getString("logged_user_email", null)

        // Obtém o ID do usuário com base no email
        loggedUserEmail?.let { fetchUserIdByEmail(it) }

        // Botão de registro
        binding.btnRegister.setOnClickListener {
            // Obtém os valores digitados pelo usuário nos campos de entrada
            val cnpj = binding.etCnpj.text.toString()
            val name = binding.etName.text.toString()
            val photo = binding.etPhoto.text.toString()
            val description = binding.etDescription.text.toString()
            val openingHours = binding.etOpeningHours.text.toString()

            // Validação básica para verificar se todos os campos foram preenchidos
            if (cnpj.isEmpty() || name.isEmpty() || photo.isEmpty() || description.isEmpty() || openingHours.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Envio dos dados ao servidor usando corrotinas
            lifecycleScope.launch {
                userId?.let { it1 ->
                    sendDataToServer(cnpj, name, photo, description, openingHours,
                        it1
                    )
                }
            }
        }

        // Botão de voltar
        binding.comeBack.setOnClickListener {
            comeBack()
        }
    }

    // Função que navega de volta para a tela de "Meu Estabelecimento"
    private fun comeBack() {
        val intent = Intent(this, MyEstablishmentActivity::class.java)
        startActivity(intent)
    }

    // Busca o ID do usuário pelo email, chamando um endpoint da API
    fun fetchUserIdByEmail(email: String) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                // Faz a requisição na thread de IO para evitar bloqueios
                val userIdResponse = withContext(Dispatchers.IO) {
                    RetrofitInstance.api.getUserIdByEmail(email)
                }
                userId = userIdResponse.userId // Armazena o ID do usuário retornado
            } catch (e: Exception) {
                Log.e("fetchUserIdByEmail", "Erro: ${e.message}", e)
            }
        }
    }

    // Exibe o resultado (sucesso ou erro) para o usuário
    private fun showResults(mensagem: String, erros: List<String>?) {
        if (erros.isNullOrEmpty()) {
            // Exibe a mensagem de sucesso no Toast
            Toast.makeText(this@CreateEstablishmentActivity, mensagem, Toast.LENGTH_LONG).show()
        } else {
            // Exibe os erros em um AlertDialog
            val erroMessages = erros.joinToString("\n")
            val alerta = AlertDialog.Builder(this)
                .setTitle("Erros")
                .setMessage("$mensagem\n\nErros:\n$erroMessages")
                .setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
            alerta.show()
        }
    }

    // Função que envia os dados do estabelecimento ao servidor via Socket
    private suspend fun sendDataToServer(
        cnpj: String,
        name: String,
        photo: String,
        description: String,
        openingHours: String,
        ownerId: String
    ) = withContext(Dispatchers.IO) {
        try {
            // Conectar ao servidor na porta 4000
            val socket = Socket("10.0.2.2", 4000)

            // Criar streams de entrada e saída para o socket
            val outputStream = ObjectOutputStream(socket.getOutputStream())
            val inputStream = ObjectInputStream(socket.getInputStream())

            // Criar um objeto Estabelecimento e enviar para o servidor
            val estabelecimento = Establishment(
                cnpj,
                name,
                description,
                openingHours,
                photo,
                ownerId
            )
            outputStream.writeObject(estabelecimento) // Envia o objeto
            outputStream.flush()

            // Lê a resposta do servidor
            val resposta = inputStream.readObject()
            if (resposta is Resultado) {
                // Exibe a mensagem de resposta
                runOnUiThread {
                    showResults(resposta.mensagem, resposta.erros)
                }
            } else {
                println("Objeto recebido não é do tipo Resultado.")
            }

            // Fechar o socket
            socket.close()

        } catch (e: Exception) {
            e.printStackTrace()
            runOnUiThread {
                Toast.makeText(this@CreateEstablishmentActivity, "Erro ao se conectar ao servidor", Toast.LENGTH_LONG).show()
            }
        }
    }

    companion object {
        const val TAG = "CreateEstablishment" // Tag usada para logs.
    }
}
