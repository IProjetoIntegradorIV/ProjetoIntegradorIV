package br.edu.puccampinas.campusconnect.data.network

import br.edu.puccampinas.campusconnect.data.model.Establishment
import br.edu.puccampinas.campusconnect.data.model.LoginGoogleRequest
import br.edu.puccampinas.campusconnect.data.model.LoginRequest
import br.edu.puccampinas.campusconnect.data.model.LoginResponse
import br.edu.puccampinas.campusconnect.data.model.Product
import br.edu.puccampinas.campusconnect.data.model.ResponseMessage
import br.edu.puccampinas.campusconnect.data.model.User
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    @POST("api/users/login")
    @Headers("Accept: application/json")
    fun loginUser(@Body request: LoginRequest): Call<LoginResponse>

    @POST("api/users/login/google")
    @Headers("Accept: application/json")
    fun loginUserGoogle(@Body request: LoginGoogleRequest): Call<Void>

    @POST("api/users")
    @Headers("Accept: application/json")
    fun createUser(@Body user: User): Call<ResponseMessage>

    @GET("api/users/establishments")
    suspend fun getEstablishments(): Response<List<Establishment>>

    @GET("api/users/establishments/{establishmentId}/products")
    suspend fun getProductsByEstablishment(@Path("establishmentId") establishmentId: String): Response<List<Product>>

}
