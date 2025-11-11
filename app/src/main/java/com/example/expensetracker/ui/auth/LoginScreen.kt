package com.example.expensetracker.ui.auth

import android.util.Log
import androidx.compose.foundation.layout.Box          // <-- 1. IMPORTĂ BOX
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog     // <-- 2. IMPORTĂ ALERTDIALOG
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator // <-- 3. IMPORTĂ CIRCULAR
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment            // <-- 4. IMPORTĂ ALIGNMENT
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.expensetracker.AuthCheckState
import com.example.expensetracker.R

val TAG_L = "LoginScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    authCheckState: AuthCheckState,
    onDismissError: () -> Unit,
    onLoginSuccess: () -> Unit = {},
    onNavigateToRegister: () -> Unit = {}
){
    val loginViewModel = viewModel<LoginViewModel>(factory = LoginViewModel.Factory)
    val loginUiState = loginViewModel.uiState

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold (
            topBar = { TopAppBar(title = { Text(text = stringResource(id = R.string.login)) }) },
        ) {
            Column (
                modifier = Modifier
                    .padding(it)
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                var email by remember { mutableStateOf("") }
                TextField(
                    label = { Text(text = "Username/Email") },
                    value = email,
                    onValueChange = { email = it },
                    modifier = Modifier.fillMaxWidth()
                )

                var password by remember { mutableStateOf("") }
                TextField(
                    label = { Text(text = "Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    value = password,
                    onValueChange = { password = it },
                    modifier = Modifier.fillMaxWidth()
                )

                Button(onClick = {
                    Log.d(TAG_L, "login...")
                    loginViewModel.login(email, password)
                }, modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Login")
                }

                if (loginUiState.isAuthenticating) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(15.dp)
                    );
                }
                if (loginUiState.authenticationError != null) {
                    Text(text = "Login failed ${loginUiState.authenticationError.message}")
                }

//                OutlinedButton(
//                    onClick = onNavigateToRegister,
//                    modifier = Modifier.fillMaxWidth()
//                ) {
//                    Text("Register")
//                }
            }
        }

        when (authCheckState) {
            is AuthCheckState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            is AuthCheckState.Error -> {
                AlertDialog(
                    onDismissRequest = onDismissError,
                    title = { Text("Eroare de Autentificare") },
                    text = { Text(authCheckState.message) },
                    confirmButton = {
                        Button(onClick = onDismissError) {
                            Text("OK")
                        }
                    }
                )
            }
            is AuthCheckState.Idle -> {
            }
        }
    }

    LaunchedEffect(loginUiState.authenticationCompleted) {
        if (loginUiState.authenticationCompleted) {
            onLoginSuccess();
        }
    }
}

@Preview()
@Composable
fun LoginScreenPreview() {
    LoginScreen(
        authCheckState = AuthCheckState.Idle,
        onDismissError = {}
    )
}