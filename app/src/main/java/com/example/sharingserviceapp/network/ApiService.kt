package com.example.sharingserviceapp.network

import com.example.sharingserviceapp.models.AuthResponse
import com.example.sharingserviceapp.models.LoginRequest
import com.example.sharingserviceapp.models.RegisterRequest
import com.example.sharingserviceapp.models.UserProfileResponse
import com.example.sharingserviceapp.models.Category
import com.example.sharingserviceapp.models.TaskerProfileResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import com.example.sharingserviceapp.models.*
import retrofit2.http.*

interface ApiService {

    // 🔹 Register a new user
    @POST("/auth/register")
    fun registerUser(@Body request: RegisterRequest): Call<AuthResponse>

    // 🔹 Login user
    @POST("auth/login")
    fun loginUser(@Body request: LoginRequest): Call<AuthResponse>

    // 🔹 Get tasker profile
    @GET("tasker/profile/")
    fun getUserTaskerProfile(@Header("Authorization") token: String,
    ): Call<TaskerProfileResponse>

    @POST("tasker/profile/")
    fun postUserTaskerProfile(
        @Header("Authorization") token: String,
        @Body body: TaskerProfileRequest
    ): Call<TaskerProfileResponse>

    // GET CITIES
    @GET("cities")
    fun getCities(): Call<List<City>>

    // Get users full view
    @GET("/users/complete/id/token")
    fun getUserProfileToken(): Call<UserProfileResponse>

    // 🔹 Get list of categories
    @GET("category")
    fun getCategories(): Call<List<Category>>

    // 🔹 Create tasker profile
    @POST("/tasker/create")
    fun createProfile(@Body profile: TaskerProfileRequest): Call<TaskerProfileResponse>

    // 🔹 Update tasker profile
    @PUT("/tasker/update")
    fun updateProfile(@Body profile: TaskerProfileRequest): Call<TaskerProfileResponse>

    // 🔹 Get tasker's assigned tasks
    @GET("/tasker/tasks")
    fun getTasks(): Call<List<Task>>

    // 🔹 Delete tasker profile
    @DELETE("/tasker/delete")
    fun deleteProfile(): Call<Void>


}