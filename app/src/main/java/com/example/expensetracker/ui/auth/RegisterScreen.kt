package com.example.expensetracker.auth

import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.expensetracker.R

val TAG_R = "RegisterScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(){
    Scaffold (
        topBar = { TopAppBar(title = { Text(text = stringResource(id = R.string.register)) }) },
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
                Log.d(TAG_R, "registering...");
            }) {
                Text("Register")
            }
        }
    }
}

@Preview()
@Composable
fun RegisterScreenPreview() {
    RegisterScreen()
}