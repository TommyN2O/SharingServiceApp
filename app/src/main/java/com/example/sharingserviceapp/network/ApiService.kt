package com.example.sharingserviceapp.network

import com.example.sharingserviceapp.models.AuthResponse
import com.example.sharingserviceapp.models.LoginRequest
import com.example.sharingserviceapp.models.RegisterRequest
import com.example.sharingserviceapp.models.Category
import com.example.sharingserviceapp.models.TaskerProfileResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import com.example.sharingserviceapp.models.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*
import kotlin.time.Duration

interface ApiService {

    // 🔹 Register a new user
    @POST("auth/register")
    fun registerUser(@Body request: RegisterRequest): Call<AuthResponse>

    // 🔹 Login user
    @POST("auth/login")
    fun loginUser(@Body request: LoginRequest): Call<AuthResponse>

    // 🔹 Get tasker profile
    @GET("tasker/profile/")
    fun getUserTaskerProfile(@Header("Authorization") token: String,
    ): Call<TaskerProfileResponse>

    //create
    @Multipart
    @POST("tasker/profile")
    fun createTaskerProfile(
        @Header("Authorization") token: String,
        @Part profileImage: MultipartBody.Part?,
        @Part("tasker_profile_json") taskerProfileJson: RequestBody,
        @Part galleryImages: List<MultipartBody.Part>  // Multiple gallery images
    ): Call<TaskerProfileResponse>

    // update
    @Multipart
    @PUT("tasker/profile")
    fun updateTaskerProfile(
        @Header("Authorization") token: String,
        @Part profileImage: MultipartBody.Part?,
        @Part("tasker_profile_json") taskerProfileJson: RequestBody,
        @Part galleryImages: List<MultipartBody.Part>  // Multiple gallery images
    ): Call<TaskerProfileResponse>

    @PUT("availability")
    fun updateTaskerAvailability(
        @Header("Authorization") token: String,
        @Body body: RequestBody
    ): Call<Void>

    //Get taskers
    @GET("tasker/profiles/")
    fun getTaskerList(
        @Header("Authorization") token: String,
        @Query("category") category: Int? = null,
        @Query("city") city: String? = null,
        @Query("date") date: String? = null,       // or LocalDate if you handle it properly
        @Query("timeFrom") timeFrom: String? = null,  // HH:mm format
        @Query("timeTo") timeTo: String? = null,
        @Query("minPrice") minPrice: Int? = null,
        @Query("maxPrice") maxPrice: Int? = null,
        @Query("rating") rating: Int? = null
    ): Call<List<TaskerHelper>>

    @GET("open-tasks/")
    fun getOpenedTasksList(
        @Header("Authorization") token: String,
        @Query("category") category: Int? = null,
        @Query("city") city: String? = null,
        @Query("date") date: String? = null,
        @Query("minBudget") minBudget: Int? = null,
        @Query("maxBudget") maxBudget: Int? = null,
        @Query("duration") duration: Int? = null,
    ): Call<List<OpenedTasksHelper>>

    // GET CITIES
    @GET("cities")
    fun getCities(): Call<List<City>>


    //Delete tasker profile
    @DELETE("tasker/profile/")
    fun deleteUserTaskerProfile(
        @Header("Authorization") token: String,
    ): Call<TaskerProfileResponse>

    // 🔹 Get list of categories
    @GET("category")
    fun getCategories(): Call<List<Category>>

    @GET("tasker/profiles/{id}")
    fun getTaskerProfileById(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Call<TaskerHelper>

    @GET("open-tasks/{id}/dates")
    fun getOpenTaskById(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Call<OpenedTasksHelper>

    @Multipart
    @POST("tasker/send-request")
    fun sendTaskRequest(
        @Header("Authorization") token: String,
        @Part("taskData") taskerRequestJson: RequestBody,
        @Part galleryImages: List<MultipartBody.Part>
    ): Call<TaskRequestBody>
//#TODO
    @GET("tasker/check")
    fun CheckIfIsTasker(
        @Header("Authorization") token: String,
    ): Call<TaskerCheck>

    @POST("open-tasks/{id}/offers")
    fun sendOpenTaskOffer(
        @Header("Authorization") token: String,
        @Body request: OpenTaskOffer,
        @Path("id") id: Int
    ): Call<OpenTaskOffer>

    @Multipart
    @POST("open-tasks")
    fun createOpenTask(
        @Header("Authorization") token: String,
        @Part("taskData") taskerRequestJson: RequestBody,
        @Part galleryImages: List<MultipartBody.Part>
    ): Call<OpenTaskBody>

    @GET("tasker/tasks/sent")
    fun getMyTasks(
        @Header("Authorization") token: String,
    ): Call<List<TaskResponse>>

    @GET("tasker/tasks/sent/{id}")
    fun getMyTasksById(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Call<TaskResponse>

    @GET("open-tasks/{id}/offers")
    fun getOffersForTask(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Call<List<Offer>>

//fa#TODO so that it deletes it self  and shows the requests and offers window or the ne task detailed
    @POST("open-tasks/offers/{offerId}/accept")
    fun acceptOffer(
        @Header("Authorization") token: String,
        @Path("offerId") offerId: Int
    ): Call<Void>


    @GET("tasker/tasks/received")
    fun getPeopleRequests(
        @Header("Authorization") token: String,
    ): Call<List<TaskResponse>>

    @GET("tasker/tasks/received/{id}")
    fun getPeopleRequestsById(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Call<TaskResponse>

    @PUT("tasker/tasks/received/{id}/status")
    fun updateTaskStatus(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body statusUpdate: StatusUpdate
    ): Call<Void>

    @GET("messages/chats")
    fun getConversations(
        @Header("Authorization") token: String,
    ): Call<List<Message>>

    @GET("messages/chat/{id}/messages")
    fun getConversationsById(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
    ): Call<List<ChatMessages>>


    @GET("tasker/walletpayments")
    fun getWalletPayments(
        @Header("Authorization") token: String,
    ): Call<WalletResponse>

    @POST("messages/send")
    fun sendMessage(
        @Header("Authorization") token: String,
        @Body message: SendMessage
    ): Call<SendMessage>

    @POST("messages/chat")
    fun createChat(
        @Header("Authorization") token: String,
        @Body body: CreateChatBody
    ): Call<CreateChat>

    @POST("payment/create-checkout-session")
    fun payment(
        @Header("Authorization") token: String,
        @Body body: Payment
    ): Call<PaymentResponse>

    @GET("tasker/tasks/sent/paid")
    fun plannedTaskSent(
        @Header("Authorization") token: String,
    ): Call<List<TaskResponse>>

    @GET("tasker/tasks/received/paid")
    fun plannedTaskReceived(
        @Header("Authorization") token: String,
    ): Call<List<TaskResponse>>

    @GET("serverdate")
    fun serverDate():Call<ServerDate>

    @GET("tasker/tasks/sent/completed")
    fun TaskSentHistory(
        @Header("Authorization") token: String,
    ): Call<List<TaskResponse>>

    @GET("tasker/tasks/received/completed")
    fun TaskReceivedHistory(
        @Header("Authorization") token: String,
    ): Call<List<TaskResponse>>

    @GET("users/profile")
    fun getUserProfile(
        @Header("Authorization") token: String,
    ): Call<UserProfileResponse>

    // update
    @Multipart
    @PUT("users/profile")
    fun updateUserProfile(
        @Header("Authorization") token: String,
        @Part profilePhoto: MultipartBody.Part?,
        @Part("profile_data") body: RequestBody,
    ): Call<Void>

    @POST("users/change-password")
    fun changePassword(
        @Header("Authorization") token: String,
        @Body request: ChangePasswordRequest
    ): Call<ChangePasswordResponse>

    @POST("support-tickets")
    fun createSupportTicket(
        @Header("Authorization") token: String,
        @Body request: CreateSupportTicketRequest
    ): Call<CreateTicketResponse>

    @PUT ("tasker/task-requests/{taskRequestId}/convert-to-open")
    fun convertToOpenTask(
        @Header("Authorization") token: String,
        @Body body: SetAsOpenTask,
        @Path("taskRequestId") taskRequestId: Int
    ): Call<OpenTaskResponse>
}