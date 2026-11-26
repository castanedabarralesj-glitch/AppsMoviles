package com.edy.enfocat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.edy.enfocat.ui.theme.EnfocaTTheme

class MainActivity : ComponentActivity() {

    private lateinit var dbHelper: BaseDatosEnfocaT

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dbHelper = BaseDatosEnfocaT(this)
        enableEdgeToEdge()

        setContent {
            EnfocaTTheme {
                var currentScreen by remember { mutableStateOf("login") }
                var snackbarMessage by remember { mutableStateOf<String?>(null) }
                val snackbarHostState = remember { SnackbarHostState() }

                LaunchedEffect(snackbarMessage) {
                    snackbarMessage?.let {
                        snackbarHostState.showSnackbar(it)
                        snackbarMessage = null
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when (currentScreen) {
                        "login" -> LoginScreen(
                            onLoginSubmit = { email, password ->
                                if (email.isBlank() || password.isBlank()) {
                                    snackbarMessage = "Por favor completa todos los campos"
                                } else {
                                    val cursor = dbHelper.consultarUsuarioPorCorreo(email)
                                    if (cursor != null && cursor.moveToFirst()) {
                                        val hash = cursor.getString(cursor.getColumnIndexOrThrow(BaseDatosEnfocaT.COL_USUARIO_CONTRASENA_HASH))
                                        cursor.close()
                                        if (BaseDatosEnfocaT.verificarContrasena(password, hash)) {
                                            snackbarMessage = "¡Inicio de sesión exitoso!"
                                            currentScreen = "home"
                                        } else {
                                            snackbarMessage = "Contraseña incorrecta"
                                        }
                                    } else {
                                        cursor?.close()
                                        snackbarMessage = "El usuario no existe. Regístrate primero."
                                    }
                                }
                            },
                            onForgotPassword = {
                                snackbarMessage = "Se ha enviado un correo para restablecer tu contraseña."
                            },
                            onRegister = {
                                currentScreen = "register"
                            }
                        )
                        "register" -> RegisterScreen(
                            onRegisterSubmit = { name, email, password, metaSemanal ->
                                val meta = metaSemanal.toIntOrNull() ?: 120
                                val id = dbHelper.registrarUsuario(name, email, password, meta)
                                if (id != -1L) {
                                    snackbarMessage = "¡Registro exitoso! Inicia sesión."
                                    currentScreen = "login"
                                } else {
                                    snackbarMessage = "El correo ya está registrado."
                                }
                            },
                            onBackToLogin = {
                                currentScreen = "login"
                            }
                        )
                        "home" -> HomeScreen(
                            onStartStudySession = {
                                snackbarMessage = "¡Sesión de estudio iniciada con éxito!"
                            },
                            onCreateTask = {
                                snackbarMessage = "Crear nueva tarea"
                            },
                            onTaskClick = { task ->
                                snackbarMessage = "Detalles de: ${task.title}"
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        dbHelper.close()
        super.onDestroy()
    }
}
