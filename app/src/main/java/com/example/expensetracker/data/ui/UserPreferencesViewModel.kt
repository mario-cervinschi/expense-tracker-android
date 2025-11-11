package com.example.expensetracker.data.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.expensetracker.MyApplication
import com.example.expensetracker.data.UserPreferences
import com.example.expensetracker.data.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class UserPreferencesViewModel(private val userPreferencesRepository: UserPreferencesRepository) :
    ViewModel() {
    val uiState: Flow<UserPreferences> = userPreferencesRepository.userPreferencesStream

    init {
        Log.d("UserPreferencesViewModel", "init")
    }

    fun save(userPreferences: UserPreferences) {
        viewModelScope.launch {
            Log.d("UserPreferencesViewModel", "saveUserPreferences")
            userPreferencesRepository.save(userPreferences)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app =
                    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MyApplication)
                UserPreferencesViewModel(app.container.userPreferencesRepository)
            }
        }
    }
}
