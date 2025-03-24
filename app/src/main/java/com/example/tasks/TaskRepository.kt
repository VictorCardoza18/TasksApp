package com.example.tasks

import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Response

class TaskRepository(private val api: TaskApi) {

    suspend fun getTasks(): Response<List<Task>> {
        return api.getTasks()
    }

    suspend fun createTask(task: Task): Response<Task> {
        return api.createTask(task)
    }

    suspend fun getTask(id: String): Response<Task> {
        return api.getTask(id)
    }

    suspend fun deleteTask(id: String): Response<Response<Void>> {
        return try {
            Response.success(api.deleteTask(id)) // Envuelve la respuesta Void en Response.success
        } catch (e: Exception) {
            Response.error(404, "".toResponseBody(null)) // Maneja errores de conexión, etc.
        }
    }

    suspend fun updateTask(id: String, task: Task): Response<Task> {
        return api.updateTask(id, task)
    }
}