package com.example.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TaskViewModel(private val repository: TaskRepository) : ViewModel() {

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _taskDetail = MutableStateFlow<Task?>(null)
    val taskDetail: StateFlow<Task?> = _taskDetail

    init {
        loadTasks()
    }

    fun loadTasks() {
        _isLoading.update { true }
        _errorMessage.update { null }
        viewModelScope.launch {
            val response = repository.getTasks()
            _isLoading.update { false }
            if (response.isSuccessful) {
                _tasks.update { response.body() ?: emptyList() }
            } else {
                _errorMessage.update { "Error al cargar las tareas: ${response.errorBody()?.string() ?: response.message()}" }
            }
        }
    }

    fun createTask(title: String, description: String, date: String? = null, user: String) {
        _isLoading.update { true }
        _errorMessage.update { null }
        viewModelScope.launch {
            val newTask = Task(
                title = title, description = description, date = date,
                user = User, id = null
            )
            val response = repository.createTask(newTask)
            _isLoading.update { false }
            if (response.isSuccessful) {
                response.body()?.let { savedTask ->
                    _tasks.update { it + savedTask }
                }
            } else {
                _errorMessage.update { "Error al crear la tarea: ${response.errorBody()?.string() ?: response.message()}" }
            }
        }
    }

    fun getTask(id: String) {
        _isLoading.update { true }
        _errorMessage.update { null }
        viewModelScope.launch {
            val response = repository.getTask(id)
            _isLoading.update { false }
            if (response.isSuccessful) {
                _taskDetail.update { response.body() }
            } else {
                _errorMessage.update { "Error al obtener la tarea: ${response.errorBody()?.string() ?: response.message()}" }
            }
        }
    }

    fun deleteTask(id: String) {
        _isLoading.update { true }
        _errorMessage.update { null }
        viewModelScope.launch {
            val response = repository.deleteTask(id)
            _isLoading.update { false }
            if (response.isSuccessful) {
                _tasks.update { it.filter { task -> task.id != id } }
            } else {
                _errorMessage.update { "Error al eliminar la tarea: ${response.errorBody()?.string() ?: response.message()}" }
            }
        }
    }

    fun updateTask(task: Task) {
        _isLoading.update { true }
        _errorMessage.update { null }
        viewModelScope.launch {
            val response = repository.updateTask(task.id!!, task)
            _isLoading.update { false }
            if (response.isSuccessful) {
                response.body()?.let { updatedTask ->
                    _tasks.update { currentTasks ->
                        currentTasks.map { if (it.id == updatedTask.id) updatedTask else it }
                    }
                }
            } else {
                _errorMessage.update { "Error al actualizar la tarea: ${response.errorBody()?.string() ?: response.message()}" }
            }
        }
    }

    class Factory(private val repository: TaskRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(TaskViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return TaskViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}