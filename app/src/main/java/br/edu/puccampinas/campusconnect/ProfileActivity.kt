package br.edu.puccampinas.campusconnect

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import br.edu.puccampinas.campusconnect.databinding.ActivityProfileBinding

private lateinit var binding: ActivityProfileBinding

class ProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.logout.setOnClickListener {
            logout()
        }
        binding.comeBack.setOnClickListener {
            comeBack()
        }
    }

    private fun comeBack(){
        val intent = Intent(this, EstablishmentActivity::class.java)
        startActivity(intent)
    }

    private fun logout(){
        val intent = Intent(this, Inicio::class.java)
        startActivity(intent)
    }
}
