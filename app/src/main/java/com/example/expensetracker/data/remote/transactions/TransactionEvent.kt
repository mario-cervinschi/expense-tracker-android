package com.example.expensetracker.data.remote.transactions

import com.example.expensetracker.data.model.Transaction

data class TransactionEvent(val type : String, val payload : Transaction)
