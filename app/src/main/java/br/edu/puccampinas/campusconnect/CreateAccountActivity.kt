package br.edu.puccampinas.campusconnect

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import br.edu.puccampinas.campusconnect.data.model.ResponseMessage
import br.edu.puccampinas.campusconnect.data.model.User
import br.edu.puccampinas.campusconnect.data.network.RetrofitInstance
import br.edu.puccampinas.campusconnect.databinding.ActivityCreateAccountBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CreateAccountActivity : AppCompatActivity() {

    // Variável de binding para acessar os elementos da interface
    private lateinit var binding: ActivityCreateAccountBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializa o binding
        binding = ActivityCreateAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Define ação para o botão "Voltar" que retorna à tela de início
        binding.comeBack.setOnClickListener {
            comeBack()
        }

        // Define ação para o botão "Registrar" que chama a função de registro
        binding.btnRegister.setOnClickListener {
            register()
        }

        // Configuração dos checkboxes para não permitir seleção mútua
        binding.checkBox1.setOnClickListener {
            binding.checkBox1.isChecked = true
            binding.checkBox2.isChecked = false
        }

        binding.checkBox2.setOnClickListener {
            binding.checkBox2.isChecked = true
            binding.checkBox1.isChecked = false
        }
    }

    // Função que lida com o registro do usuário após validação dos campos
    private fun register() {
        val name = binding.etName.text.toString()
        val email = binding.etEmail.text.toString()
        val password = binding.etPassword.text.toString()
        val confirmPassword = binding.etConfimPassword.text.toString()

        // Verifica se um dos checkboxes foi selecionado
        if (!binding.checkBox1.isChecked && !binding.checkBox2.isChecked) {
            Toast.makeText(
                this,
                "Escolha se você é dono de estabelecimento ou cliente!",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        // Validação de comprimento do nome
        if (name.length < 5) {
            Toast.makeText(
                this,
                "O nome completo deve ter pelo menos 5 caracteres!",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        // Validação de domínio do e-mail
        if (!email.endsWith("@gmail.com")) {
            Toast.makeText(this, "O e-mail deve ser do domínio @gmail.com!", Toast.LENGTH_SHORT).show()
            return
        }

        // Validação de comprimento da senha e confirmação de senha
        if (password.length < 6) {
            Toast.makeText(this, "A senha deve ter pelo menos 6 caracteres!", Toast.LENGTH_SHORT).show()
            return
        }

        if (confirmPassword.length < 6) {
            Toast.makeText(this, "A senha deve ter pelo menos 6 caracteres!", Toast.LENGTH_SHORT).show()
            return
        }

        // Verifica se as senhas coincidem
        if (password != confirmPassword) {
            Toast.makeText(this, "As senhas não coincidem!", Toast.LENGTH_SHORT).show()
            return
        }

        // Verifica se todos os campos estão preenchidos
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show()
            return
        }

        // Define se o usuário é dono de estabelecimento com base no checkbox selecionado
        val establishmentOwner = binding.checkBox1.isChecked

        // Cria o objeto 'User' com os dados inseridos pelo usuário
        val newUser = User(name, email, password, establishmentOwner)

        // Faz a chamada para a API usando Retrofit para criar o novo usuário
        RetrofitInstance.api.createUser(newUser).enqueue(object : Callback<ResponseMessage> {
            override fun onResponse(call: Call<ResponseMessage>, response: Response<ResponseMessage>) {
                // Verifica se a resposta foi bem-sucedida
                if (response.isSuccessful || response.code() == 201) {
                    val message = response.body()?.message ?: "Mensagem não disponível"
                    Toast.makeText(this@CreateAccountActivity, "Conta criada com sucesso!", Toast.LENGTH_SHORT).show()

                    // Navega para a tela de login após o registro bem-sucedido
                    goToLogin()
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "Erro ao criar conta."
                    Toast.makeText(this@CreateAccountActivity, errorMessage, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ResponseMessage>, t: Throwable) {
                // Exibe uma mensagem de erro se a chamada à API falhar
                Toast.makeText(this@CreateAccountActivity, "Erro: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Função para retornar à tela de início
    private fun comeBack() {
        val intent = Intent(this, Inicio::class.java)
        startActivity(intent)
    }

    // Função para navegar para a tela de login
    private fun goToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }
}
