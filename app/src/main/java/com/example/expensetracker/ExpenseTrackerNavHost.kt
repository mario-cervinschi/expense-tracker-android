package com.example.expensetracker

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.expensetracker.data.UserPreferences
import com.example.expensetracker.data.remote.Api
import com.example.expensetracker.data.ui.UserPreferencesViewModel
import com.example.expensetracker.ui.auth.LoginScreen
import com.example.expensetracker.ui.auth.RegisterScreen
import com.example.expensetracker.ui.home.HomeScreen
import com.example.expensetracker.ui.home.TransactionScreen
import com.example.expensetracker.utils.navigation.Screen

sealed interface AuthCheckState {
    object Idle : AuthCheckState
    object Loading : AuthCheckState
    data class Error(val message: String) : AuthCheckState
}

@Composable
fun ExpenseTrackerNavHost() {
    val navController = rememberNavController()

    val userPreferencesViewModel =
        viewModel<UserPreferencesViewModel>(factory = UserPreferencesViewModel.Factory)
    val userPreferencesUiState by userPreferencesViewModel.uiState.collectAsStateWithLifecycle(
        initialValue = UserPreferences()
    )
    val expenseTrackerViewModel = viewModel<ExpenseTrackerViewModel>(factory = ExpenseTrackerViewModel.Factory)

    var authCheckState by remember { mutableStateOf<AuthCheckState>(AuthCheckState.Idle) }
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(route = Screen.Login.route) {
            LoginScreen(
                authCheckState = authCheckState,
                onDismissError = { authCheckState = AuthCheckState.Idle },
                onLoginSuccess = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                }
            )
        }

        composable(route = Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        composable(route = Screen.Main.route) {
            HomeScreen(
                onTransactionClick = { transactionId ->
                    Log.d("MyAppNavHost", "navigate to transaction $transactionId")
                    navController.navigate(Screen.Transaction.createRoute(transactionId))
                },
                onAddTransaction = {
                    Log.d("MyAppNavHost", "navigate to add transaction")
                    navController.navigate(Screen.Transaction.createRoute(null))
                },
                onLogout = {
                    expenseTrackerViewModel.logout()
                    Api.tokenInterceptor.token = null
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.Transaction.route,
            arguments = listOf(
                navArgument("transactionId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val transactionId = backStackEntry.arguments?.getString("transactionId")
            TransactionScreen(
                transactionId = if (transactionId == "new") null else transactionId,
                onClose = {
                    Log.d("MyAppNavHost", "navigate back to list")
                    navController.popBackStack()
                }
            )
        }
    }
    LaunchedEffect(userPreferencesUiState.token) {
        val token = userPreferencesUiState.token
        if (token.isNotEmpty()) {

            authCheckState = AuthCheckState.Loading
            Api.tokenInterceptor.token = token

            val result = expenseTrackerViewModel.verifyToken()

            if (result.isSuccess) {
                authCheckState = AuthCheckState.Idle
                expenseTrackerViewModel.startWebSocketListener()
                navController.navigate(Screen.Main.route) {
                    popUpTo(0)
                }
            } else {
                val errorMessage = expenseTrackerViewModel.getErrorMessage(result.exceptionOrNull())
                authCheckState = AuthCheckState.Error(errorMessage)

                expenseTrackerViewModel.logout()
            }
        }
    }
}