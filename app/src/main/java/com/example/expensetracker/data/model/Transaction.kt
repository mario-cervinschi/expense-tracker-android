package com.example.expensetracker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date
import java.util.UUID

@Entity(tableName = "Transactions")
data class Transaction(@PrimaryKey val _id : String = UUID.randomUUID().toString(), val title : String  = "", val date : Date  = Date(), val sum : Double  = 0.0, val income : Boolean = false)
