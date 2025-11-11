package com.example.expensetracker.data.remote.transactions

import android.os.Build
import androidx.annotation.RequiresApi
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.ToJson
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.format.DateTimeParseException
import java.util.Date
import java.util.Locale

class DateAdapter : JsonAdapter<Date>() {
    private val customFormat = SimpleDateFormat("MMM d, yyyy h:mm:ss a", Locale.US)

    @RequiresApi(Build.VERSION_CODES.O)
    @Synchronized
    override fun fromJson(reader: JsonReader): Date? {
        if (reader.peek() == JsonReader.Token.NULL) {
            return reader.nextNull()
        }

        val dateString = reader.nextString()

        try {
            return Date.from(Instant.parse(dateString))
        } catch (e: DateTimeParseException) {
        }

        try {
            return customFormat.parse(dateString)
        } catch (e: Exception) {
            throw JsonDataException("Date could not be parsed: $dateString")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Synchronized
    @ToJson
    override fun toJson(writer: JsonWriter, value: Date?) {
        if (value == null) {
            writer.nullValue()
        } else {
            val rfcString = Instant.ofEpochMilli(value.time).toString()
            writer.value(rfcString)
        }
    }
}