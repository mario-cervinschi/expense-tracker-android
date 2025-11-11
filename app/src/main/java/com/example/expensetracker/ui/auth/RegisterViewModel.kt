package com.example.expensetracker.ui.auth

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.expensetracker.MyApplication
import com.example.expensetracker.data.UserPreferences
import com.example.expensetracker.data.UserPreferencesRepository
import com.example.expensetracker.ui.auth.data.AuthRepository
import kotlinx.coroutines.launch

data class RegisterUiState(
    val isRegistering: Boolean = false,
    val registerError: Throwable? = null,
    val registerCompleted: Boolean = false,
//    val token: String = ""
)

class RegisterViewModel(
    private val authRepository: AuthRepository,
//    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel(){
    var uiState: RegisterUiState by mutableStateOf(RegisterUiState())

    init{
        Log.d("RegisterViewModel", "init")
    }

    fun register(email: String, password: String, confirmationPassword: String){
        viewModelScope.launch {
            Log.v("RegisterViewModel", "registering...");
            uiState = uiState.copy(isRegistering = true, registerError = null)

            val result = authRepository.register(email, password, confirmationPassword)
            if (result.isSuccess) {
                uiState = uiState.copy(isRegistering = false, registerCompleted = true)
            } else {
                uiState = uiState.copy(
                    isRegistering = false,
                    registerError = result.exceptionOrNull()
                )
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app =
                    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MyApplication)
                RegisterViewModel(
                    app.container.authRepository,
                )
            }
        }
    }
}