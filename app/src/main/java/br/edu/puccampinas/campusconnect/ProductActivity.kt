package br.edu.puccampinas.campusconnect

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import br.edu.puccampinas.campusconnect.databinding.ActivityProductBinding
import com.bumptech.glide.Glide

private lateinit var binding: ActivityProductBinding
private lateinit var establishmentId: String

class ProductActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.comeBack.setOnClickListener {
            comeBack()
        }

        val productId = intent.getStringExtra("productId") ?:" N/A"
        establishmentId = intent.getStringExtra("establishmentId") ?: "N/A"
        val productName = intent.getStringExtra("productName") ?: "N/A"
        val productDescription = intent.getStringExtra("productDescription") ?: "N/A"
        val productEvaluation = intent.getStringExtra("productEvaluation") ?: "N/A"
        val productPhoto = intent.getStringExtra("productPhoto") ?: "N/A"
        val productPrice = intent.getStringExtra("productPrice")?: "N/A"

        binding.name.text = productName
        binding.description.text = productDescription
        binding.evaluation.text = productEvaluation
        binding.price.text = productPrice
        Glide.with(this).load(productPhoto).into(binding.imageView)

    }

    private fun comeBack(){
        val intent = Intent(this, ProductsActivity::class.java)
        intent.putExtra("establishmentId", establishmentId)
        startActivity(intent)
    }

}
