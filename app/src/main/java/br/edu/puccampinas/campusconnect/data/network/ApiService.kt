package br.edu.puccampinas.campusconnect.data.network

import br.edu.puccampinas.campusconnect.data.model.Establishment
import br.edu.puccampinas.campusconnect.data.model.LoginGoogleRequest
import br.edu.puccampinas.campusconnect.data.model.LoginRequest
import br.edu.puccampinas.campusconnect.data.model.LoginResponse
import br.edu.puccampinas.campusconnect.data.model.Product
import br.edu.puccampinas.campusconnect.data.model.ResponseMessage
import br.edu.puccampinas.campusconnect.data.model.User
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

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

    @GET("api/users/establishments/{id}")
    suspend fun getEstablishmentById(@Path("id") id: String): Response<Establishment>

    @Multipart
    @PUT("api/users/updateProfileImage")
    suspend fun updateProfileImage(@Part("email") email: RequestBody, @Part file: MultipartBody.Part): Response<ResponseMessage>

    @GET("api/users/profileImage")
    suspend fun getProfileImage(@Query("email") email: String): Response<String>

    @PUT("api/users/changePassword")
    fun changePassword(@Query("email") email: String, @Query("newPassword") newPassword: String): Call<ResponseMessage>

    @GET("api/users/users")
    fun getUserByEmail(@Query("email") email: String): Call<User>

    @PUT("api/users/changeName")
    fun changeName(@Query("email") email: String, @Query("newName") newName: String): Call<ResponseMessage>

    @GET("api/users/checkEstablishmentOwner")
    fun checkEstablishmentOwner(@Query("email") email: String): Call<ResponseMessage>
}
