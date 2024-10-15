package br.edu.puccampinas.campusconnect

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

        binding.btnVoltar.setOnClickListener {
            LoginActivity()
        }

        binding.btnAlterarSenha.setOnClickListener {
            val againEmail = binding.txtEmailEsqueciSenha.text.toString()
            val novaSenha = binding.txtNovaSenha.text.toString()
            val confirmarSenha = binding.txtConfirmarSenha.text.toString()

            when {
                againEmail.isEmpty() -> {
                    binding.txtEmailEsqueciSenha.error = "Digite seu Email!"
                }
                !againEmail.contains("@gmail.com") -> {
                    binding.txtEmailEsqueciSenha.error = "Email inválido!"
                }
                novaSenha.isEmpty() -> {
                    binding.txtNovaSenha.error = "Digite sua nova senha!"
                }
                novaSenha.length <= 5 -> {
                    binding.txtNovaSenha.error = "A senha precisa ter no mínimo 6 caracteres!"
                }
                confirmarSenha.isEmpty() -> {
                    binding.txtConfirmarSenha.error = "Confirme sua senha!"
                }
                confirmarSenha != novaSenha -> {
                    binding.txtConfirmarSenha.error = "As senhas não se coincidem!"
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
        val email = binding.txtEmailEsqueciSenha.text.toString()

        if (email.isEmpty()) {
            binding.txtEmailEsqueciSenha.error = "Digite seu e-mail!"
            return
        }

        if (!email.contains("@")) {
            binding.txtEmailEsqueciSenha.error = "E-mail inválido!"
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