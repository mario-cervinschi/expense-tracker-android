package com.example.expensetracker.data.model

import java.util.Date

data class Transaction(val _id : String = "", val title : String  = "", val date : Date  = Date(), val sum : Double  = 0.0, val income : Boolean = false)
