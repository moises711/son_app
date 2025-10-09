package com.moises.sam.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "configuraciones")
data class ConfigEntity(
    @PrimaryKey
    val id: Int = 1, // Singleton
    val adelantos: Double = 0.0,
    val ultimaModificacion: Date = Date()
)