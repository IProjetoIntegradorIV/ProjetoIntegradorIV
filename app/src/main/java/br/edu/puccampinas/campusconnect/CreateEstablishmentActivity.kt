package br.edu.puccampinas.campusconnect

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import br.edu.puccampinas.campusconnect.databinding.ActivityCreateEstablishmentBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.Socket

class CreateEstablishmentActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreateEstablishmentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateEstablishmentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Botão de registro
        binding.btnRegister.setOnClickListener {
            val cnpj = binding.etCnpj.text.toString()
            val name = binding.etName.text.toString()
            val photo = binding.etPhoto.text.toString()
            val description = binding.etDescription.text.toString()
            val openingHours = binding.etOpeningHours.text.toString()

            // Validação básica
            if (cnpj.isEmpty() || name.isEmpty() || photo.isEmpty() || description.isEmpty() || openingHours.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Envio ao servidor
            lifecycleScope.launch {
                enviarDadosParaServidor(cnpj, name, photo, description, openingHours)
            }
        }

        // Botão de voltar
        binding.comeBack.setOnClickListener {
            comeBack()
        }
    }

    private fun comeBack() {
        val intent = Intent(this, MyEstablishmentActivity::class.java)
        startActivity(intent)
    }


    private fun exibirResultado(mensagem: String, erros: List<String>?) {
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

    // Função para enviar os dados ao servidor
    private suspend fun enviarDadosParaServidor(
        cnpj: String,
        name: String,
        photo: String,
        description: String,
        openingHours: String
    ) = withContext(Dispatchers.IO) {
        try {
            // Conectar ao servidor na porta 4000
            val socket = Socket("10.0.2.2", 4000)

            // Criar streams de entrada e saída para o socket
            val outputStream = ObjectOutputStream(socket.getOutputStream())
            val inputStream = ObjectInputStream(socket.getInputStream())

            // Criar um objeto Estabelecimento e enviar para o servidor
            val estabelecimento = Estabelecimento(
                cnpj,
                name,
                photo,
                description,
                openingHours
            )
            outputStream.writeObject(estabelecimento)
            outputStream.flush()

            val resposta = inputStream.readObject()
            if (resposta is Resultado) {
                // Exibe a mensagem de resposta
                runOnUiThread {
                    exibirResultado(resposta.mensagem, resposta.erros)
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
