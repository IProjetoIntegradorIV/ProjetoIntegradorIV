package br.edu.puccampinas.campusconnect.data.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    // BASE_URL Gabriel
    //private const val BASE_URL = "http://192.168.15.40:8080"

    // BASE_URL Mateus
    private const val BASE_URL = "http://192.168.1.101:8080"

    // BASE_URL Emulador
    // private const val BASE_URL = "http://10.0.2.2:8080"

    private val client = OkHttpClient.Builder()
        .cache(null)
        .build()

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

}
