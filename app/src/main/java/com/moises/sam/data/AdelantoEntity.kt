package com.moises.sam.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Entidad para la tabla de adelantos
 */
@Entity(tableName = "adelantos")
data class AdelantoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val fecha: Date = Date(),
    val monto: Double,
    val observacion: String = ""
)