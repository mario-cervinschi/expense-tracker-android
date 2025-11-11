package com.example.expensetracker.auth

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.expensetracker.R

val TAG_L = "LoginScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(){
    Scaffold (
        topBar = { TopAppBar(title = { Text(text = stringResource(id = R.string.login)) }) },
    ) {
        Column (
            modifier = Modifier
            .padding(it)
            .fillMaxSize()
            .padding(24.dp)
        ) {
            var username by remember { mutableStateOf("") }
            TextField(
                label = { Text(text = "Username") },
                value = username,
                onValueChange = { username = it },
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
                Log.d(TAG_L, "login...");
            }) {
                Text("Login")
            }
        }
    }
}

@Preview()
@Composable
fun LoginScreenPreview() {
    LoginScreen()
}