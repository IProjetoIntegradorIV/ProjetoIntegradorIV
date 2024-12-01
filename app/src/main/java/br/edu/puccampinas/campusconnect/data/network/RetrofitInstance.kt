package br.edu.puccampinas.campusconnect.data.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

// Objeto singleton que fornece uma instância única de Retrofit para realizar chamadas de rede.
object RetrofitInstance {
    // URL base para o backend da aplicação, dependendo do ambiente de desenvolvimento.

    // URL base para o ambiente do Gabriel (rede local).
    //private const val BASE_URL = "http://192.168.15.40:8080"

    // URL base para o ambiente do Mateus (rede local).
    //private const val BASE_URL = "http://192.168.1.101:8080"

    // URL base para o emulador Android. O IP 10.0.2.2 é utilizado para redirecionar chamadas do emulador
    // para o localhost da máquina host, pois o localhost padrão do emulador é isolado.
    private const val BASE_URL = "http://10.0.2.2:8080"

    // Configuração do cliente HTTP usado pelo Retrofit.
    private val client = OkHttpClient.Builder()
        .cache(null) // Não utiliza cache para as requisições, garantindo que sempre busque os dados atualizados.
        .build()

    // Instância do serviço de API criada de forma "lazy" (somente quando é chamada pela primeira vez).
    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL) // Define a URL base para as requisições.
            .client(client) // Associa o cliente HTTP configurado ao Retrofit.
            .addConverterFactory(ScalarsConverterFactory.create()) // Adiciona suporte para converter respostas em texto simples.
            .addConverterFactory(GsonConverterFactory.create())    // Adiciona suporte para converter respostas em JSON.
            .build() // Cria a instância do Retrofit.
            .create(ApiService::class.java) // Cria uma implementação da interface ApiService, que define os endpoints da API.
    }
}
