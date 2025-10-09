package com.moises.sam.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "pagos")
data class PagoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fecha: Date = Date(),
    val monto: Double = 0.0,
    val observacion: String = ""
)