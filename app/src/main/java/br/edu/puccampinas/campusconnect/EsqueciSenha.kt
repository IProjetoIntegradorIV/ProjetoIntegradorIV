/*package br.edu.puccampinas.campusconnect

import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import br.edu.puccampinas.campusconnect.databinding.ActivityEsqueciSenhaBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Firebase
import com.google.firebase.auth.auth


class EsqueciSenha : AppCompatActivity() {
    private lateinit var binding: ActivityEsqueciSenhaBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEsqueciSenhaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnEnviar.setOnClickListener {
            // colocar a classe que faz com que receba o email
        }

        binding.setaVoltar.setOnClickListener {
            voltar()
        }

        binding.btnAlterarSenha.setOnClickListener {
            val againEmail = binding.etEmailEsqueciSenha.text.toString()
            val novaSenha = binding.etNovaSenha.text.toString()
            val confirmarSenha = binding.etConfirmarSenha.text.toString()

            when {
                againEmail.isEmpty() -> {
                    binding.etEmailEsqueciSenha.error = "Digite seu Email!"
                }
                !againEmail.contains("@gmail.com") -> {
                    binding.etEmailEsqueciSenha.error = "Email inválido!"
                }
                novaSenha.isEmpty() -> {
                    binding.etNovaSenha.error = "Digite sua nova senha!"
                }
                novaSenha.length <= 5 -> {
                    binding.etNovaSenha.error = "A senha precisa ter no mínimo 6 caracteres!"
                }
                confirmarSenha.isEmpty() -> {
                    binding.etConfirmarSenha.error = "Confirme sua senha!"
                }
                confirmarSenha != novaSenha -> {
                    binding.etConfirmarSenha.error = "As senhas não se coincidem!"
                }
            }
        }
    }

    private fun alterar(view: View) {
        // val progressBar = binding.progessBar
        // progressBar.visibility = View.VISIBLE

        binding.btnAlterarSenha.isEnabled = false
        binding.btnAlterarSenha.setTextColor(Color.parseColor("#FFFFFF"))

        Handler(Looper.getMainLooper()).postDelayed({
            val snackbar = Snackbar.make(view, "Senha alterada com sucesso!", Snackbar.LENGTH_SHORT)
            snackbar.show()
        }, 3000)
    }

    private fun voltar() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }

    private fun enviarEmailDeVerificacao() {
        val email = binding.etEmailEsqueciSenha.text.toString()

        if (email.isEmpty()) {
            binding.etEmailEsqueciSenha.error = "Digite seu e-mail!"
            return
        }

        if (!email.contains("@")) {
            binding.etEmailEsqueciSenha.error = "E-mail inválido!"
            return
        }

        val snackbar = Snackbar.make(binding.root, "Verifique seu e-mail para a confirmação!", Snackbar.LENGTH_LONG)
        snackbar.show()

        Handler(Looper.getMainLooper()).postDelayed({
            enviarEmail(email) // Simulação de envio de e-mail
        }, 2000) // Atraso de 2 segundos
    }

    private fun enviarEmail(email: String) {
        // Função eviar um e-mail de redefinição de senha
        val emailAddress = "user@example.com"
        Firebase.auth.sendPasswordResetEmail(emailAddress)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Email sent.")
                }
            }
        // Para simulação, apenas mostramos um Toast
        Toast.makeText(this, "E-mail de verificação enviado para $email", Toast.LENGTH_SHORT).show()
    }
}

*/
