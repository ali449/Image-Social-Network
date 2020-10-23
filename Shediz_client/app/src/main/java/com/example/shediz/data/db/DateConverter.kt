package com.example.shediz.data.db

import androidx.room.TypeConverter
import java.text.SimpleDateFormat
import java.util.*

object DateConverter
{
    private val df = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    @TypeConverter
    @JvmStatic
    fun toDate(string: String): Date?
    {
        return df.parse(string)
    }

    @TypeConverter
    @JvmStatic
    fun fromDate(date: Date): String
    {
        return df.format(date)
    }
}