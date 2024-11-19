package br.edu.puccampinas.campusconnect

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import br.edu.puccampinas.campusconnect.data.model.LoginGoogleRequest
import br.edu.puccampinas.campusconnect.data.network.RetrofitInstance
import br.edu.puccampinas.campusconnect.databinding.ActivityInicioBinding
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.security.MessageDigest
import java.util.UUID

// Classe de atividade inicial que permite login via Google ou email, além de navegação para cadastro.
class Inicio : AppCompatActivity() {

    // Declaração do binding para vincular os elementos de layout ao código
    private lateinit var binding: ActivityInicioBinding
    private val coroutineScope = CoroutineScope(Dispatchers.Main) // Escopo para operações assíncronas no contexto principal

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInicioBinding.inflate(layoutInflater) // Inicializa o binding com o layout
        setContentView(binding.root)

        // Define o listener para o botão de login com Google
        binding.btnLoginGoogle.setOnClickListener {
            loginGoogle()
        }

        // Define o listener para o botão de login com email
        binding.btnLoginEmail.setOnClickListener {
            loginEmailActivity()
        }

        // Define o listener para o botão de registro
        binding.register.setOnClickListener {
            cadastrar()
        }
    }

    // Função para iniciar o login com Google usando CredentialManager
    @SuppressLint("CoroutineCreationDuringComposition")
    private fun loginGoogle() {
        // Cria uma instância do CredentialManager para gerenciar as credenciais
        val credentialManager = CredentialManager.create(this)

        // Gera um nonce seguro para a autenticação
        val rawNonce = UUID.randomUUID().toString()
        val bytes = rawNonce.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        val hashedNonce = digest.fold("") { str, it -> str + "%02x".format(it) }

        // Configura as opções de autenticação com o Google
        val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(BuildConfig.GOOGLE_CLIENT_ID)
            .setNonce(hashedNonce)
            .build()

        // Cria a solicitação de credenciais para o login com Google
        val request: GetCredentialRequest = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        // Lança uma coroutine para realizar o login de forma assíncrona
        coroutineScope.launch {
            try {
                // Obtém a credencial do usuário
                val result = credentialManager.getCredential(
                    request = request,
                    context = this@Inicio
                )

                // Extrai o ID token da credencial do Google
                val credential = result.credential
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val googleIdToken = googleIdTokenCredential.idToken

                Log.i(TAG, googleIdToken)

                // Envia o token de ID para o backend
                sendIdTokenGoogleApi(googleIdToken)
            } catch (e: GetCredentialException) {
                // Trata erros de obtenção de credencial
                Toast.makeText(this@Inicio, "Erro ao realizar o login com o google: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "Failed to authenticate: ${e.message}")
            }
        }
    }

    // Função para enviar o token de ID do Google ao backend
    private fun sendIdTokenGoogleApi(idToken: String) {
        val loginGoogleRequest = LoginGoogleRequest(idToken) // Cria a requisição de login com o ID token

        // Faz uma chamada assíncrona à API para autenticação com o token do Google
        RetrofitInstance.api.loginUserGoogle(loginGoogleRequest).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Log.i(TAG, "Login successful")
                    Toast.makeText(this@Inicio, "Login bem-sucedido. Seja bem-vindo!", Toast.LENGTH_SHORT).show()

                    // Redireciona o usuário para a atividade de estabelecimentos
                    redirectEstEstablishment()
                } else {
                    // Trata falhas de autenticação
                    Log.e(TAG, "Failed to authenticate: ${response.errorBody()?.string()}")
                    Toast.makeText(this@Inicio, "Falha na autenticação. Tente novamente.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                // Trata erros na conexão com a API
                Log.e(TAG, "API call failed: ${t.message}")
                Toast.makeText(this@Inicio, "Falha na autenticação. Tente novamente.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Função para redirecionar o usuário para a activity de estabelecimentos após login
    private fun redirectEstEstablishment() {
        val intent = Intent(this, EstablishmentActivity::class.java)
        startActivity(intent)
    }

    // Função para redirecionar para a activity de login por email
    private fun loginEmailActivity(){
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }

    // Função para redirecionar para a activity de cadastro
    private fun cadastrar(){
        val intent = Intent(this, CreateAccountActivity::class.java)
        startActivity(intent)
    }

    // Declaração de uma constante para uso como tag de log
    companion object {
        const val TAG = "LoginActivity"
    }
}
