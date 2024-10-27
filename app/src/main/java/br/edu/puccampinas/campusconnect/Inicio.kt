package br.edu.puccampinas.campusconnect

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import br.edu.puccampinas.campusconnect.databinding.ActivityInicioBinding

class Inicio : AppCompatActivity() {
    private lateinit var binding: ActivityInicioBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInicioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLoginGoogle.setOnClickListener {
            // Navegar para a tela de login do Google
        }

        binding.btnLoginEmail.setOnClickListener {
            LoginEmailActivity()
        }

        binding.register.setOnClickListener {
            Cadastrar()
        }
    }

    private fun LoginEmailActivity(){
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }

    private fun LoginGoogleActivity(){
        //val intent = Intent(this, LoginGoogleActivity::class.java)
        //startActivity(intent)
    }

    private fun Cadastrar(){
        val intent = Intent(this, CreateAccountActivity::class.java)
        startActivity(intent)
    }
}
