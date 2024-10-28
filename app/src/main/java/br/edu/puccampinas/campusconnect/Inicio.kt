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

class Inicio : AppCompatActivity() {
    private lateinit var binding: ActivityInicioBinding
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInicioBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.btnLoginGoogle.setOnClickListener {
            loginGoogle()
        }

        binding.btnLoginEmail.setOnClickListener {
            loginEmailActivity()
        }

        binding.register.setOnClickListener {
            cadastrar()
        }
    }

    @SuppressLint("CoroutineCreationDuringComposition")
    private fun loginGoogle() {
        val credentialManager = CredentialManager.create(this)

        val rawNonce = UUID.randomUUID().toString()
        val bytes = rawNonce.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        val hashedNonce = digest.fold("") { str, it -> str + "%02x".format(it) }

        val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(BuildConfig.GOOGLE_CLIENT_ID)
            .setNonce(hashedNonce)
            .build()

        val request: GetCredentialRequest = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        coroutineScope.launch {
            try {
                val result = credentialManager.getCredential(
                    request = request,
                    context = this@Inicio
                )

                val credential = result.credential
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val googleIdToken = googleIdTokenCredential.idToken

                Log.i(TAG, googleIdToken)
                sendIdTokenGoogleApi(googleIdToken)
            } catch (e: GetCredentialException) {
                Toast.makeText(this@Inicio, "Erro ao realizar o login com o google: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun sendIdTokenGoogleApi(idToken: String) {
        val loginGoogleRequest = LoginGoogleRequest(idToken)

        RetrofitInstance.api.loginUserGoogle(loginGoogleRequest).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Log.i(TAG, "Login successful")
                    Toast.makeText(this@Inicio, "Login bem-sucedido. Seja bem-vindo!", Toast.LENGTH_SHORT).show()
                } else {
                    Log.e(TAG, "Failed to authenticate: ${response.errorBody()?.string()}")
                    Toast.makeText(this@Inicio, "Falha na autenticação. Tente novamente.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e(TAG, "API call failed: ${t.message}")
                Toast.makeText(this@Inicio, "Falha na autenticação. Tente novamente.", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun loginEmailActivity(){
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }

    private fun cadastrar(){
        val intent = Intent(this, CreateAccountActivity::class.java)
        startActivity(intent)
    }

    companion object {
        const val TAG = "LoginActivity"
    }
}
