package com.example.expensetracker.ui.auth

import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.expensetracker.R

val TAG_R = "RegisterScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {}
){
    val registerViewModel = viewModel<RegisterViewModel>(factory = RegisterViewModel.Factory)
    val registerUiState = registerViewModel.uiState

    Scaffold (
        topBar = { TopAppBar(title = { Text(text = stringResource(id = R.string.register)) }) },
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

            var firstPassword by remember { mutableStateOf("") }
            TextField(
                label = { Text(text = "Password") },
                visualTransformation = PasswordVisualTransformation(),
                value = firstPassword,
                onValueChange = { firstPassword = it },
                modifier = Modifier.fillMaxWidth()
            )

            var retypePassword by remember { mutableStateOf("") }
            TextField(
                label = { Text(text = "Retype Password") },
                visualTransformation = PasswordVisualTransformation(),
                value = retypePassword,
                onValueChange = { retypePassword = it },
                modifier = Modifier.fillMaxWidth()
            )

            Button(onClick = {
                Log.d("RegisterScreen", "register...")
                registerViewModel.register(email, firstPassword, retypePassword)
            }, modifier = Modifier.fillMaxWidth()
            ) {
                Text("Register")
            }

            if (registerUiState.isRegistering) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(15.dp)
                );
            }

            if (registerUiState.registerError != null) {
                Text(text = "Login failed ${registerUiState.registerError.message}")
            }

            TextButton(
                onClick = onNavigateToLogin,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Already have an account? Login")
            }
        }
    }
    LaunchedEffect(registerUiState.registerCompleted) {
        if (registerUiState.registerCompleted) {
            onRegisterSuccess()
        }
    }
}

@Preview()
@Composable
fun RegisterScreenPreview() {
    RegisterScreen()
}