package br.edu.puccampinas.campusconnect

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import br.edu.puccampinas.campusconnect.databinding.ActivityLoginBinding


class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    // private val firestore = FirebaseFirestore.getInstance()
    // private val auth = FirebaseAuth.getInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.etEsqueci.setOnClickListener {
            EsqueciSenha()
        }

        binding.etCadastro.setOnClickListener {
            CreateAccountActivity()
        }

        binding.btnLogar.setOnClickListener { view ->

            val email = binding.etEmail.text.toString()
            val senha = binding.etSenha.text.toString()

            when {
                email.isEmpty() -> {
                    binding.etEmail.error = "Preencha o E-mail!"
                }

                senha.isEmpty() -> {
                    binding.etSenha.error = "Digite sua senha!"
                }
                /*
                else -> {
                    verificarCredenciais(view, email, senha)
                    auth.signInWithEmailAndPassword(email,senha).addOnCompleteListener { autentificacao ->
                        if(autentificacao.isSuccessful){
                            val user = auth.currentUser
                            val userId = user?.uid
                            val userDocRef = FirebaseFirestore.getInstance().collection("pessoas").document(userId!!)
                            userDocRef.get().addOnSuccessListener { documentSnapshot ->
                                if (documentSnapshot.exists()) {
                                    val gerente = documentSnapshot.getBoolean("gerente")

                                    if (gerente == true) {
                                        (nome da tela quando o usuário logar)
                                    } else {
                                        (nome da tela quando o usuário logar)
                                    }
                                }
                            }
                        }
                    }
                } */
            }
        }
    }

    private fun verificarCredenciais(view: View, email: String, senha: String) {
        // val progressBar = binding.progessBar
       // progressBar.visibility = View.VISIBLE
        /*
        firestore.collection("pessoas")
            .whereEqualTo("email", email)
            .whereEqualTo("senha", senha)
            .get()
            .addOnCompleteListener { task ->
                progressBar.visibility = View.GONE

                if (task.isSuccessful) {
                    val documents = task.result.documents
                    if (documents.isEmpty()) {
                        val snackbar = Snackbar.make(view, "Credenciais inválidas!", Snackbar.LENGTH_SHORT)
                        snackbar.show()
                    } else {
                        val snackbar = Snackbar.make(view, "Login efetuado com sucesso!", Snackbar.LENGTH_SHORT)
                        snackbar.show()
                    }
                } else {
                    val snackbar = Snackbar.make(view, "Erro ao verificar credenciais: ${task.exception?.message}", Snackbar.LENGTH_SHORT)
                    snackbar.show()
                }
            }
    }
         */
    }

    private fun navegarEsqueciSenha(){
        val intent = Intent(this, EsqueciSenha::class.java)
        startActivity(intent)
    }

    private fun navegarCreateAccount(){
        val intent = Intent(this, CreateAccountActivity::class.java)
        startActivity(intent)
    }

    private fun navegarTelaPrincipal(){
        //
    }
}