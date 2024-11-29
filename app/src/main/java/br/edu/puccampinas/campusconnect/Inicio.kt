package br.edu.puccampinas.campusconnect

import android.annotation.SuppressLint
import android.content.Context
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
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
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

class Inicio : AppCompatActivity() {
    private lateinit var binding: ActivityInicioBinding // View Binding para acessar os elementos da UI.
    private val coroutineScope = CoroutineScope(Dispatchers.Main) // Escopo de corrotinas para operações assíncronas na UI.

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInicioBinding.inflate(layoutInflater) // Inicializa o binding.
        setContentView(binding.root) // Define a raiz do layout.

        // Configura o botão de login com Google.
        binding.btnLoginGoogle.setOnClickListener {
            if (checkGooglePlayServices()) { // Verifica se os serviços do Google Play estão disponíveis.
                loginGoogle()
            } else {
                Toast.makeText(
                    this,
                    "Google Play Services não está disponível ou atualizado. Atualize para continuar.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        // Configura o botão de login com e-mail.
        binding.btnLoginEmail.setOnClickListener {
            loginEmailActivity()
        }

        // Configura o botão de cadastro.
        binding.register.setOnClickListener {
            cadastrar()
        }
    }

    // Função para realizar login com Google usando CredentialManager.
    @SuppressLint("CoroutineCreationDuringComposition")
    private fun loginGoogle() {
        val credentialManager = CredentialManager.create(this) // Instancia o gerenciador de credenciais.

        // Gera um nonce aleatório e o converte para SHA-256.
        val rawNonce = UUID.randomUUID().toString()
        val bytes = rawNonce.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        val hashedNonce = digest.fold("") { str, it -> str + "%02x".format(it) }

        // Configura as opções de login do Google.
        val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false) // Permite contas não autorizadas.
            .setServerClientId(BuildConfig.GOOGLE_CLIENT_ID) // ID do cliente do servidor.
            .setNonce(hashedNonce) // Nonce para maior segurança.
            .build()

        // Configura a solicitação de credenciais.
        val request: GetCredentialRequest = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        // Realiza a chamada assíncrona para obter credenciais.
        coroutineScope.launch {
            try {
                val result = credentialManager.getCredential(
                    request = request,
                    context = this@Inicio
                )

                // Obtém o token de ID do Google.
                val credential = result.credential
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val googleIdToken = googleIdTokenCredential.idToken

                Log.i(TAG, googleIdToken) // Loga o token.
                sendIdTokenGoogleApi(googleIdToken) // Envia o token para a API.
            } catch (e: GetCredentialException) {
                Toast.makeText(
                    this@Inicio,
                    "Erro ao realizar o login com o Google: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    // Envia o token de ID do Google para a API para autenticação.
    private fun sendIdTokenGoogleApi(idToken: String) {
        val loginGoogleRequest = LoginGoogleRequest(idToken)

        RetrofitInstance.api.loginUserGoogle(loginGoogleRequest).enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful) {
                    val email = response.body() ?: return
                    Log.i(TAG, "Login bem-sucedido com email: $email")

                    // Salva o email do usuário
                    saveUserEmail(email)

                    Toast.makeText(this@Inicio, "Login bem-sucedido. Seja bem-vindo!", Toast.LENGTH_SHORT).show()
                    redirectEstEstablishment()
                } else {
                    Log.e(TAG, "Falha na autenticação: ${response.errorBody()?.string()}")
                    Toast.makeText(this@Inicio, "Falha na autenticação. Tente novamente.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.e(TAG, "Erro na chamada à API: ${t.message}")
                Toast.makeText(this@Inicio, "Falha na autenticação. Tente novamente.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun saveUserEmail(email: String) {
        val sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString("logged_user_email", email)
            apply()
        }
    }

    // Redireciona o usuário para a tela de estabelecimentos após o login.
    private fun redirectEstEstablishment() {
        val intent = Intent(this, EstablishmentActivity::class.java)
        startActivity(intent)
    }

    // Inicia a atividade de login com e-mail.
    private fun loginEmailActivity() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }

    // Inicia a atividade de cadastro de novo usuário.
    private fun cadastrar() {
        val intent = Intent(this, CreateAccountActivity::class.java)
        startActivity(intent)
    }

    // Verifica se o Google Play Services está disponível no dispositivo.
    private fun checkGooglePlayServices(): Boolean {
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val status = googleApiAvailability.isGooglePlayServicesAvailable(this)

        return when (status) {
            ConnectionResult.SUCCESS -> true // Serviços disponíveis.
            ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED -> {
                showPlayServicesUpdateDialog(googleApiAvailability)
                false
            }
            ConnectionResult.SERVICE_MISSING, ConnectionResult.SERVICE_DISABLED -> {
                showPlayServicesErrorDialog(googleApiAvailability, status)
                false
            }
            else -> {
                Toast.makeText(
                    this,
                    "Este dispositivo não suporta os serviços do Google Play.",
                    Toast.LENGTH_LONG
                ).show()
                false
            }
        }
    }

    // Mostra o diálogo para atualizar o Google Play Services.
    private fun showPlayServicesUpdateDialog(googleApiAvailability: GoogleApiAvailability) {
        googleApiAvailability.makeGooglePlayServicesAvailable(this).addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Toast.makeText(
                    this,
                    "Por favor, atualize o Google Play Services para continuar.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    // Mostra um diálogo de erro caso os serviços do Google Play estejam desativados ou ausentes.
    private fun showPlayServicesErrorDialog(
        googleApiAvailability: GoogleApiAvailability,
        status: Int
    ) {
        googleApiAvailability.getErrorDialog(this, status, 9000)?.show()
    }

    companion object {
        const val TAG = "LoginActivity" // Tag usada para logs.
    }
}
