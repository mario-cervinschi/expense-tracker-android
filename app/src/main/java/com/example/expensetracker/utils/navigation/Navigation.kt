package com.example.expensetracker

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Main : Screen("main")
    object EditTransaction : Screen("edit/{transactionId}") {
        fun createRoute(transactionId: String) = "edit/$transactionId"
    }
}