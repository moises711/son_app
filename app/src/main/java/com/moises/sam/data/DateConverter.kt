package com.moises.sam.data

import androidx.room.TypeConverter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Conversor de tipos para Room
 */
class DateConverter {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    
    @TypeConverter
    fun fromDate(date: Date): String {
        return dateFormat.format(date)
    }
    
    @TypeConverter
    fun toDate(dateString: String): Date {
        return try {
            dateFormat.parse(dateString) ?: Date()
        } catch (e: Exception) {
            Date()
        }
    }
}