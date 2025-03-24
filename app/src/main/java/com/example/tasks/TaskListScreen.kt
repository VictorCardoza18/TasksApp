package com.example.tasks

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.time.format.DateTimeFormatter
import java.time.LocalDate

@Composable
fun TaskListScreen(viewModel: TaskViewModel, modifier: Modifier = Modifier) {
    val tasks by viewModel.tasks.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var newTaskTitle by remember { mutableStateOf("") }
    var newTaskDescription by remember { mutableStateOf("") }

    fun getCurrentDate(): String {
        val currentDate = LocalDate.now()
        val formatter = DateTimeFormatter.ISO_DATE // Formato YYYY-MM-DD
        return currentDate.format(formatter)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Filled.Add, "Añadir tarea")
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }
            if (errorMessage != null) {
                Text("Error: $errorMessage", color = Color.Red)
            }
            LazyColumn {
                items(tasks) { task ->
                    TaskItem(
                        task = task,
                        onDelete = { taskId ->
                            if (taskId != null) {
                                viewModel.deleteTask(taskId)
                            }
                        },
                        onEdit = { /* TODO: Implementar navegación a pantalla de edición */ }
                    )
                    HorizontalDivider()
                }
            }

            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text("Nueva Tarea") },
                    text = {
                        Column {
                            OutlinedTextField( // Usa el OutlinedTextField de Material Design 3
                                value = newTaskTitle,
                                onValueChange = { newTaskTitle = it },
                                label = { Text("Título") }
                            )
                            OutlinedTextField( // Usa el OutlinedTextField de Material Design 3
                                value = newTaskDescription,
                                onValueChange = { newTaskDescription = it },
                                label = { Text("Descripción") }
                            )
                        }
                    },
                    confirmButton = {
                        Button(onClick = {
                            viewModel.createTask(
                                newTaskTitle, newTaskDescription,
                                date = getCurrentDate(),
                                user = ""
                            )
                            showDialog = false
                            newTaskTitle = ""
                            newTaskDescription = ""
                        }) {
                            Text("Crear")
                        }
                    },
                    dismissButton = {
                        Button(onClick = { showDialog = false }) {
                            Text("Cancelar")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun TaskItem(task: Task, onDelete: (String?) -> Unit, onEdit: (Task) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable { onEdit(task) },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(text = task.title, style = MaterialTheme.typography.titleLarge)
            Text(text = task.description)
        }
        IconButton(onClick = { onDelete(task.id) }) {
            Icon(Icons.Filled.Delete, "Eliminar")
        }
    }
}