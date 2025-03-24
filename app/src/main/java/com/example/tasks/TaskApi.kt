package com.example.tasks

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import com.example.tasks.data.AuthResponse
import com.example.tasks.data.LoginRequest
import com.example.tasks.data.RegisterRequest
import retrofit2.Response

interface TaskApi {

    @GET("tasks")
    suspend fun getTasks(): Response<List<Task>>

    @POST("tasks")
    suspend fun createTask(@Body task: Task): Response<Task>

    @GET("tasks/{id}")
    suspend fun getTask(@Path("id") id: String): Response<Task>

    @DELETE("tasks/{id}")
    suspend fun deleteTask(@Path("id") id: String): Response<Void> // Void porque el backend responde con 204 sin cuerpo

    @PUT("tasks/{id}")
    suspend fun updateTask(@Path("id") id: String, @Body task: Task): Response<Task>

    @POST("login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<AuthResponse>

    @POST("register")
    suspend fun register(@Body registerRequest: RegisterRequest): Response<AuthResponse>
}