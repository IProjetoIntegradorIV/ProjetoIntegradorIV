package br.edu.puccampinas.campusconnect

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import br.edu.puccampinas.campusconnect.databinding.ActivityCreateAccountBinding


class CreateAccountActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreateAccountBinding
    // private val firestore = FirebaseFirestore.getInstance()
    // private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnVoltar.setOnClickListener {
            LoginActivity()
        }

        binding.btnCadastrar.setOnClickListener {
            LoginActivity()
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

        /* Aqui é para adicionar a pessoa no banco de dados

        auth.createUserWithEmailAndPassword(email, senha)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val uid = user?.uid

                    val pessoa = hashMapOf(
                        "nomeCompleto" to nomeCompleto,
                        "email" to email,
                        "gerente" to false
                    )

                    uid?.let {
                        firestore.collection("pessoas").document(it).set(pessoa)
                            .addOnSuccessListener {
                                Toast.makeText(
                                    this,
                                    "Cadastro realizado com sucesso!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                Handler(Looper.getMainLooper()).postDelayed({
                                    val intent = Intent(this, LoginActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                }, 2000)
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(
                                    this,
                                    "Erro ao salvar dados: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
                } else {
                    Toast.makeText(
                        this,
                        "Erro ao cadastrar: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }*/
    }
}
