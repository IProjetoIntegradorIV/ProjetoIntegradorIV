package br.edu.puccampinas.campusconnect

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import br.edu.puccampinas.campusconnect.databinding.ActivityCreateEstablishmentBinding

private lateinit var binding: ActivityCreateEstablishmentBinding

class CreateEstablishmentActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCreateEstablishmentBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.comeBack.setOnClickListener {
            comeBack()
        }
    }

    private fun comeBack(){
        val intent = Intent(this, MyEstablishmentActivity::class.java)
        startActivity(intent)
    }
}
