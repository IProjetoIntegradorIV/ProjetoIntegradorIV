package br.edu.puccampinas.campusconnect

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import br.edu.puccampinas.campusconnect.data.model.ResponseMessage
import br.edu.puccampinas.campusconnect.data.model.User
import br.edu.puccampinas.campusconnect.data.network.ApiService
import br.edu.puccampinas.campusconnect.data.network.RetrofitInstance
import br.edu.puccampinas.campusconnect.databinding.ActivityProfileBinding
import com.bumptech.glide.Glide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private val PICK_IMAGE_REQUEST = 1
    private var loggedUserEmail: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE)
        loggedUserEmail = sharedPref.getString("logged_user_email", null)

        //loadProfileImage()
        fetchUserData()

        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.logout.setOnClickListener {
            logout()
        }

        binding.comeBack.setOnClickListener {
            comeBack()
        }

        binding.imgPencil.setOnClickListener {
            changeName()
        }

        binding.imgPencil2.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        binding.btnUpdate.setOnClickListener {
            changePassword()
        }
    }

    private fun fetchUserData() {
        val email = loggedUserEmail ?: run {
            Toast.makeText(this, "Email do usuário não encontrado!", Toast.LENGTH_SHORT).show()
            return
        }

        RetrofitInstance.api.getUserByEmail(email).enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful) {
                    val user = response.body()
                    user?.let {
                        binding.tvEmail.setText(it.email)
                        binding.etName.setText(it.name)
                        binding.tvPassword.setText(it.password)
                    }
                } else {
                    Log.e("ProfileActivity", "Erro ao buscar dados do usuário: ${response.errorBody()?.string()}")
                    Toast.makeText(this@ProfileActivity, "Erro ao carregar dados do usuário.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                Log.e("ProfileActivity", "Falha na requisição: ${t.message}")
                Toast.makeText(this@ProfileActivity, "Erro na conexão.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun changeName() {
        val name = binding.etName.text.toString()

        val email = loggedUserEmail ?: run {
            Toast.makeText(this, "Email do usuário não encontrado!", Toast.LENGTH_SHORT).show()
            return
        }

        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show()
            return
        }

        RetrofitInstance.api.changeName(email, name).enqueue(object : Callback<ResponseMessage> {
            override fun onResponse(call: Call<ResponseMessage>, response: Response<ResponseMessage>) {
                Log.d("ProfileActivity", "Resposta recebida: ${response.code()}")

                if (response.isSuccessful) {
                    Log.d("ProfileActivity", "Nome alterado com sucesso: ${response.body()?.message}")
                    Toast.makeText(this@ProfileActivity, "Nome alterado com sucesso!", Toast.LENGTH_SHORT).show()
                } else {
                    Log.e("ProfileActivity", "Erro ao mudar o nome: ${response.errorBody()?.string()}")
                    Toast.makeText(this@ProfileActivity, "Erro ao alterar o nome.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ResponseMessage>, t: Throwable) {
                Log.e("ProfileActivity", "Falha na requisição: ${t.message}")
                Toast.makeText(this@ProfileActivity, "Erro na conexão.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun changePassword() {
        val password = binding.etPassword.text.toString()
        val confirmPassword = binding.etConfirmPassword.text.toString()

        val email = loggedUserEmail ?: run {
            Toast.makeText(this, "Email do usuário não encontrado!", Toast.LENGTH_SHORT).show()
            return
        }

        if (TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.length < 6) {
            Toast.makeText(this, "A senha deve ter pelo menos 6 caracteres!", Toast.LENGTH_SHORT).show()
            return
        }

        if (confirmPassword.length < 6) {
            Toast.makeText(this, "A senha deve ter pelo menos 6 caracteres!", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(this, "As senhas não coincidem!", Toast.LENGTH_SHORT).show()
            return
        }

        RetrofitInstance.api.changePassword(email, password).enqueue(object : Callback<ResponseMessage> {
            override fun onResponse(call: Call<ResponseMessage>, response: Response<ResponseMessage>) {
                Log.d("ProfileActivity", "Resposta recebida: ${response.code()}")

                if (response.isSuccessful) {
                    Log.d("ProfileActivity", "Senha alterada com sucesso: ${response.body()?.message}")
                    Toast.makeText(this@ProfileActivity, "Senha alterada com sucesso!", Toast.LENGTH_SHORT).show()
                } else {
                    Log.e("ProfileActivity", "Erro ao mudar a senha: ${response.errorBody()?.string()}")
                    Toast.makeText(this@ProfileActivity, "Erro ao alterar a senha.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ResponseMessage>, t: Throwable) {
                Log.e("ProfileActivity", "Falha na requisição: ${t.message}")
                Toast.makeText(this@ProfileActivity, "Erro na conexão.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun comeBack() {
        val intent = Intent(this, EstablishmentActivity::class.java)
        startActivity(intent)
    }

    private fun logout() {
        val sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE)
        with(sharedPref.edit()) {
            remove("logged_user_email")
            apply()
        }
        val intent = Intent(this, Inicio::class.java)
        startActivity(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            val imageUri = data.data
            imageUri?.let {
                saveImageToDatabase(it)
            }
        }
    }

    private fun saveImageToDatabase(imageUri: Uri) {
        val inputStream = contentResolver.openInputStream(imageUri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()

        // Crie um arquivo temporário para enviar com a chamada da API
        val tempFile = File.createTempFile("image", ".png", cacheDir)
        tempFile.writeBytes(byteArray)

        val requestFile = RequestBody.create(MediaType.parse("image/png"), tempFile)
        val body = MultipartBody.Part.createFormData("file", tempFile.name, requestFile)

        loggedUserEmail?.let { email ->
            val emailRequestBody = RequestBody.create(MediaType.parse("text/plain"), email)
            updateProfileImage(emailRequestBody, body)
        } ?: run {
            Toast.makeText(this, "Erro: email do usuário não encontrado", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateProfileImage(email: RequestBody, imageFile: MultipartBody.Part) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.api.updateProfileImage(email, imageFile)
                if (response.isSuccessful) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@ProfileActivity, "Imagem atualizada com sucesso!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@ProfileActivity, "Erro ao atualizar imagem", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ProfileActivity, "Erro: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
/*
    private fun loadProfileImage() {
        loggedUserEmail?.let { email ->
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = RetrofitInstance.api.getProfileImage(email)
                    if (response.isSuccessful) {
                        val imageUrl = response.body()
                        if (imageUrl != null) {
                            withContext(Dispatchers.Main) {
                                Glide.with(this@ProfileActivity)
                                    .load(imageUrl)
                                    .into(binding.imgLogo)
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(this@ProfileActivity, "Nenhuma imagem de perfil encontrada.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@ProfileActivity, "Erro ao carregar a imagem de perfil: ${response.message()}", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@ProfileActivity, "Erro: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
    */

}
