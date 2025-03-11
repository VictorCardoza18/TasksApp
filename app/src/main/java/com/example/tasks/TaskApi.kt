package com.example.tasks

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface TaskApi {
    @GET("tasks")
    suspend fun getTasks(): List<Task>

    @GET("tasks/{id}")
    suspend fun getTask(@Path("id") id: Int): Task

    @POST("tasks")
    suspend fun createTask(@Body task: Task): Task

    @PUT("tasks/{id}")
    suspend fun updateTask(@Path("id") id: Int, @Body task: Task): Task

    @DELETE("tasks/{id}")
    suspend fun deleteTask(@Path("id") id: Int)
}