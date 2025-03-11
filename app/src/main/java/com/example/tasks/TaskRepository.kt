package com.example.tasks

class TaskRepository(private val api: TaskApi) {
    suspend fun getTasks(): List<Task> = api.getTasks()
    suspend fun getTask(id: Int): Task = api.getTask(id)
    suspend fun createTask(task: Task): Task = api.createTask(task)
    suspend fun updateTask(id: Int, task: Task): Task = api.updateTask(id, task)
    suspend fun deleteTask(id: Int) = api.deleteTask(id)
}