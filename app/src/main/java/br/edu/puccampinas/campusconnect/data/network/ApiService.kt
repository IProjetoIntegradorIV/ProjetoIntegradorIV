package br.edu.puccampinas.campusconnect.data.network

import br.edu.puccampinas.campusconnect.Establishment
import br.edu.puccampinas.campusconnect.data.model.Evaluate
import br.edu.puccampinas.campusconnect.data.model.LoginGoogleRequest
import br.edu.puccampinas.campusconnect.data.model.LoginRequest
import br.edu.puccampinas.campusconnect.data.model.LoginResponse
import br.edu.puccampinas.campusconnect.data.model.Product
import br.edu.puccampinas.campusconnect.data.model.ResponseMessage
import br.edu.puccampinas.campusconnect.data.model.User
import br.edu.puccampinas.campusconnect.data.model.UserIdResponse
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @POST("api/users/login")
    @Headers("Accept: application/json")
    fun loginUser(
        @Body request: LoginRequest
    ): Call<LoginResponse>

    @POST("api/users/login/google")
    @Headers("Accept: application/json")
    fun loginUserGoogle(
        @Body request: LoginGoogleRequest
    ): Call<String>


    @POST("api/users/createUser")
    @Headers("Accept: application/json")
    fun createUser(
        @Body user: User
    ): Call<ResponseMessage>

    @GET("api/establishment/establishments")
    suspend fun getEstablishments(
    ): Response<List<Establishment>>

    @GET("api/product/establishments/{establishmentId}/products")
    suspend fun getProductsByEstablishment(
        @Path("establishmentId") establishmentId: String
    ): Response<List<Product>>

    @GET("api/establishment/establishments/{id}")
    suspend fun getEstablishmentById(
        @Path("id") id: String
    ): Response<Establishment>

    @PUT("api/users/changePassword")
    fun changePassword(
        @Query("email") email: String,
        @Query("newPassword") newPassword: String
    ): Call<ResponseMessage>

    @GET("api/users/users")
    fun getUserByEmail(
        @Query("email") email: String
    ): Call<User>

    @PUT("api/users/changeName")
    fun changeName(
        @Query("email") email: String,
        @Query("newName") newName: String
    ): Call<ResponseMessage>

    @GET("api/users/checkEstablishmentOwner")
    fun checkEstablishmentOwner(
        @Query("email") email: String
    ): Call<ResponseMessage>

    @GET("/api/users/getUserId")
    suspend fun getUserIdByEmail(
        @Query("email") email: String
    ): UserIdResponse

    @GET("api/establishment/establishments/owner/{userId}")
    suspend fun getEstablishmentIdByOwnerId(
        @Path("userId") userId: String
    ): Response<Map<String, String>>

    @GET("api/users/isEstablishmentOwner")
    suspend fun isEstablishmentOwner(
        @Query("email") email: String
    ): Response<Map<String, Boolean>>

    @PUT("api/establishment/changeEstablishmentName")
    suspend fun changeEstablishmentName(
        @Query("establishmentId") establishmentId: String,
        @Query("newName") newName: String
    ): Response<ResponseMessage>

    @PUT("api/establishment/changeEstablishmentDescription")
    suspend fun changeEstablishmentDescription(
        @Query("establishmentId") establishmentId: String,
        @Query("newDescription") newDescription: String
    ): Response<ResponseMessage>

    @PUT("api/establishment/changeEstablishmentOpeningHours")
    suspend fun changeEstablishmentOpeningHours(
        @Query("establishmentId") establishmentId: String,
        @Query("newOpeningHours") newOpeningHours: String
    ): Response<ResponseMessage>

    @PUT("api/establishment/changeEstablishmentPhoto")
    suspend fun changeEstablishmentPhoto(
        @Query("establishmentId") establishmentId: String,
        @Query("newPhoto") newPhoto: String
    ): Response<ResponseMessage>

    @PUT("api/product/changeProductName")
    suspend fun changeProductName(
        @Query("productId") productId: String,
        @Query("newName") newName: String
    ): Response<ResponseMessage>

    @PUT("api/product/changeProductDescription")
    suspend fun changeProductDescription(
        @Query("productId") productId: String,
        @Query("newDescription") newDescription: String
    ): Response<ResponseMessage>

    @PUT("api/product/changeProductPrice")
    suspend fun changeProductPrice(
        @Query("productId") productId: String,
        @Query("newPrice") newPrice: String
    ): Response<ResponseMessage>

    @PUT("api/product/changeProductPhoto")
    suspend fun changeProductPhoto(
        @Query("productId") productId: String,
        @Query("newPhoto") newPhoto: String
    ): Response<ResponseMessage>

    @DELETE("api/product/products/{productId}")
    suspend fun deleteProductById(
        @Path("productId") productId: String
    ): Response<ResponseMessage>

    @DELETE("api/establishment/establishment/{establishmentId}")
    suspend fun deleteEstablishmentById(
        @Path("establishmentId") establishmentId: String
    ): Response<ResponseMessage>

    @GET("api/establishment/establishments/search")
    suspend fun searchEstablishments(
        @Query("name") name: String
    ): Response<List<Establishment>>

    @POST("api/product/createProduct")
    @Headers("Accept: application/json")
    suspend fun createProduct(
        @Body product: Product
    ): Response<ResponseMessage>

    @PUT("api/users/changeUserPhoto")
    suspend fun changeUserPhoto(
        @Query("userId") userId: String,
        @Query("newPhoto") newPhoto: String
    ): Response<ResponseMessage>

    @POST("/api/evaluation/evaluate")
    suspend fun submitEvaluation(
        @Query("userId") userId: String,
        @Query("productId") productId: String,
        @Query("rating") rating: Float
    ): Response<ResponseMessage>

    @GET("/api/evaluation/evaluations/{productId}")
    suspend fun getEvaluationsByProductId(
        @Path("productId") productId: String
    ): Response<List<Evaluate>>

    @PUT("api/product/changeProductEvaluation")
    suspend fun changeProductEvaluation(
        @Query("productId") productId: String,
        @Query("newEvaluation") newEvaluation: String
    ): Response<ResponseMessage>
}
