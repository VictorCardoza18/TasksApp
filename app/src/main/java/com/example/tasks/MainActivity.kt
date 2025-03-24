package com.example.tasks

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tasks.data.LoginRequest
import com.example.tasks.data.RegisterRequest
import com.example.tasks.ui.theme.TasksTheme
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    private lateinit var api: TaskApi
    private lateinit var retrofit: Retrofit
    private lateinit var tokenManager: TokenManager // Declara TokenManager

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        tokenManager = TokenManager.getInstance(applicationContext) // Inicializa TokenManager

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(AuthInterceptor(applicationContext)) // Añade AuthInterceptor
            .build()

        retrofit = Retrofit.Builder()
            .baseUrl("http://10.0.2.2:3000/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()

        api = retrofit.create(TaskApi::class.java)

        setContent {
            TasksTheme {
                val showLoginScreen = remember { mutableStateOf(true) }
                val isAuthenticated = remember { mutableStateOf(false) }
                val coroutineScope = rememberCoroutineScope()
                val context = LocalContext.current

                if (isAuthenticated.value) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        topBar = {
                            TopAppBar(
                                title = { Text("Tareas") },
                                colors = TopAppBarDefaults.topAppBarColors()
                            )
                        }
                    ) { innerPadding ->
                        TaskListScreenWrapper(api = api, modifier = Modifier.padding(innerPadding)) // Pasa la instancia de api
                    }
                } else {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        topBar = {
                            TopAppBar(
                                title = { Text(if (showLoginScreen.value) "Iniciar Sesión" else "Registrarse") },
                                colors = TopAppBarDefaults.topAppBarColors()
                            )
                        }
                    ) { innerPadding ->
                        if (showLoginScreen.value) {
                            LoginScreen(
                                modifier = Modifier.padding(innerPadding),
                                onNavigateToRegister = { showLoginScreen.value = false },
                                onLoginSuccess = { token ->
                                    tokenManager.saveToken(token) // Guarda el token de forma segura
                                    isAuthenticated.value = true
                                    Toast.makeText(context, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show()
                                },
                                api = api
                            )
                        } else {
                            RegisterScreen(
                                onNavigateToLogin = { showLoginScreen.value = true },
                                onRegisterSuccess = { token ->
                                    tokenManager.saveToken(token) // Guarda el token de forma segura
                                    isAuthenticated.value = true
                                    Toast.makeText(context, "Registro exitoso", Toast.LENGTH_SHORT).show()
                                },
                                api = api
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: (String) -> Unit,
    api: TaskApi
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Iniciar Sesión", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(bottom = 16.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo Electrónico") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            coroutineScope.launch {
                val loginRequest = LoginRequest(email = email, password = password)
                val response = api.login(loginRequest)
                if (response.isSuccessful) {
                    val authResponse = response.body()
                    authResponse?.token?.let { token ->
                        onLoginSuccess(token)
                    } ?: run {
                        Toast.makeText(context, "Error: Token no encontrado en la respuesta", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = if (!errorBody.isNullOrEmpty()) {
                        "Error en el inicio de sesión: $errorBody"
                    } else {
                        "Error en el inicio de sesión: Código ${response.code()}"
                    }
                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                }
            }
        }, modifier = Modifier.fillMaxWidth()) {
            Text("Iniciar Sesión")
        }
        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = onNavigateToRegister) {
            Text("¿No tienes una cuenta? Regístrate")
        }
    }
}

@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit,
    onRegisterSuccess: (String) -> Unit,
    api: TaskApi
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Registrarse", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(bottom = 16.dp))
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Nombre") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo Electrónico") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirmar Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            if (password == confirmPassword) {
                coroutineScope.launch {
                    val registerRequest = RegisterRequest(username = username, email = email, password = password)
                    val response = api.register(registerRequest)
                    if (response.isSuccessful) {
                        val authResponse = response.body()
                        authResponse?.token?.let { token ->
                            onRegisterSuccess(token)
                        } ?: run {
                            Toast.makeText(context, "Error: Token no encontrado en la respuesta", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        val errorBody = response.errorBody()?.string()
                        val errorMessage = if (!errorBody.isNullOrEmpty()) {
                            "Error en el registro: $errorBody"
                        } else {
                            "Error en el registro: Código ${response.code()}"
                        }
                        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(context, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
            }
        }, modifier = Modifier.fillMaxWidth()) {
            Text("Registrarse")
        }
        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = onNavigateToLogin) {
            Text("¿Ya tienes una cuenta? Inicia Sesión")
        }
    }
}

@Composable
fun TaskListScreenWrapper(api: TaskApi, modifier: Modifier = Modifier) { // Recibe la instancia de api
    val viewModel: TaskViewModel = viewModel(
        factory = TaskViewModel.Factory(TaskRepository(api))
    )
    TaskListScreen(viewModel = viewModel, modifier = modifier)
}