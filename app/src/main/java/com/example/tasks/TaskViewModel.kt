package com.example.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TaskViewModel(private val repository: TaskRepository) : ViewModel() {
    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks

    private val _task = MutableStateFlow<Task?>(null)
    val task: StateFlow<Task?> = _task

    init {
        loadTasks()
    }

    fun loadTasks() {
        viewModelScope.launch {
            _tasks.value = repository.getTasks()
        }
    }

    fun loadTask(id: Int) {
        viewModelScope.launch {
            _task.value = repository.getTask(id)
        }
    }

    fun createTask(task: Task) {
        viewModelScope.launch {
            repository.createTask(task)
            loadTasks() // Recargar la lista
        }
    }

    fun updateTask(id: Int, task: Task) {
        viewModelScope.launch {
            repository.updateTask(id, task)
            loadTasks() // Recargar la lista
        }
    }

    fun deleteTask(id: Int) {
        viewModelScope.launch {
            repository.deleteTask(id)
            loadTasks() // Recargar la lista
        }
    }

    // Agregar la Factory aquí
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