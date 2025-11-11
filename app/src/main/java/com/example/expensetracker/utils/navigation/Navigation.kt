package com.example.expensetracker.utils.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Main : Screen("main")
    object Transaction : Screen("transaction/{transactionId}") {
        fun createRoute(transactionId: String?) = "transaction/${transactionId ?: "new"}"
    }
}