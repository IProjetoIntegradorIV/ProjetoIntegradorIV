package br.edu.puccampinas.campusconnect.data.network

import br.edu.puccampinas.campusconnect.data.model.Establishment
import br.edu.puccampinas.campusconnect.data.model.Evaluate
import br.edu.puccampinas.campusconnect.data.model.LoginGoogleRequest
import br.edu.puccampinas.campusconnect.data.model.LoginRequest
import br.edu.puccampinas.campusconnect.data.model.LoginResponse
import br.edu.puccampinas.campusconnect.data.model.Product
import br.edu.puccampinas.campusconnect.data.model.ResponseMessage
import br.edu.puccampinas.campusconnect.data.model.User
import br.edu.puccampinas.campusconnect.data.model.UserIdResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
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
    fun loginUser(
        @Body request: LoginRequest
    ): Call<LoginResponse>

    @POST("api/users/login/google")
    @Headers("Accept: application/json")
    fun loginUserGoogle(
        @Body request: LoginGoogleRequest
    ): Call<Void>

    @POST("api/users")
    @Headers("Accept: application/json")
    fun createUser(
        @Body user: User
    ): Call<ResponseMessage>

    @GET("api/users/establishments")
    suspend fun getEstablishments(
    ): Response<List<Establishment>>

    @GET("api/users/establishments/{establishmentId}/products")
    suspend fun getProductsByEstablishment(
        @Path("establishmentId") establishmentId: String
    ): Response<List<Product>>

    @GET("api/users/establishments/{id}")
    suspend fun getEstablishmentById(
        @Path("id") id: String
    ): Response<Establishment>

    @Multipart
    @PUT("api/users/updateProfileImage")
    suspend fun updateProfileImage(
        @Part("email") email: RequestBody,
        @Part file: MultipartBody.Part
    ): Response<ResponseMessage>

    @GET("api/users/profileImage")
    suspend fun getProfileImage(
        @Query("email") email: String
    ): Response<String>

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

    @GET("api/users/establishments/owner/{userId}")
    suspend fun getEstablishmentIdByOwnerId(
        @Path("userId") userId: String
    ): Response<Map<String, String>>

    @GET("api/users/isEstablishmentOwner")
    suspend fun isEstablishmentOwner(
        @Query("email") email: String
    ): Response<Map<String, Boolean>>

    @PUT("api/users/changeEstablishmentName")
    suspend fun changeEstablishmentName(
        @Query("establishmentId") establishmentId: String,
        @Query("newName") newName: String
    ): Response<ResponseMessage>

    @PUT("api/users/changeEstablishmentDescription")
    suspend fun changeEstablishmentDescription(
        @Query("establishmentId") establishmentId: String,
        @Query("newDescription") newDescription: String
    ): Response<ResponseMessage>

    @PUT("api/users/changeEstablishmentOpeningHours")
    suspend fun changeEstablishmentOpeningHours(
        @Query("establishmentId") establishmentId: String,
        @Query("newOpeningHours") newOpeningHours: String
    ): Response<ResponseMessage>

    @PUT("api/users/changeEstablishmentPhoto")
    suspend fun changeEstablishmentPhoto(
        @Query("establishmentId") establishmentId: String,
        @Query("newPhoto") newPhoto: String
    ): Response<ResponseMessage>

    @PUT("api/users/changeProductName")
    suspend fun changeProductName(
        @Query("productId") productId: String,
        @Query("newName") newName: String
    ): Response<ResponseMessage>

    @PUT("api/users/changeProductDescription")
    suspend fun changeProductDescription(
        @Query("productId") productId: String,
        @Query("newDescription") newDescription: String
    ): Response<ResponseMessage>

    @PUT("api/users/changeProductPrice")
    suspend fun changeProductPrice(
        @Query("productId") productId: String,
        @Query("newPrice") newPrice: String
    ): Response<ResponseMessage>

    @PUT("api/users/changeProductPhoto")
    suspend fun changeProductPhoto(
        @Query("productId") productId: String,
        @Query("newPhoto") newPhoto: String
    ): Response<ResponseMessage>

    @DELETE("api/users/products/{productId}")
    suspend fun deleteProductById(
        @Path("productId") productId: String
    ): Response<ResponseMessage>

    @DELETE("api/users/establishment/{establishmentId}")
    suspend fun deleteEstablishmentById(
        @Path("establishmentId") establishmentId: String
    ): Response<ResponseMessage>

    @GET("api/users/establishments/search")
    suspend fun searchEstablishments(
        @Query("name") name: String
    ): Response<List<Establishment>>

    @POST("api/users/createProduct")
    @Headers("Accept: application/json")
    suspend fun createProduct(
        @Body product: Product
    ): Response<ResponseMessage>

    @PUT("api/users/changeUserPhoto")
    suspend fun changeUserPhoto(
        @Query("userId") userId: String,
        @Query("newPhoto") newPhoto: String
    ): Response<ResponseMessage>

    @POST("/api/users/evaluate")
    suspend fun submitEvaluation(
        @Query("userId") userId: String,
        @Query("productId") productId: String,
        @Query("rating") rating: Float
    ): Response<ResponseMessage>

    @GET("/api/users/evaluations/{productId}")
    suspend fun getEvaluationsByProductId(
        @Path("productId") productId: String
    ): Response<List<Evaluate>>

    @PUT("api/users/changeProductEvaluation")
    suspend fun changeProductEvaluation(
        @Query("productId") productId: String,
        @Query("newEvaluation") newEvaluation: String
    ): Response<ResponseMessage>

}
