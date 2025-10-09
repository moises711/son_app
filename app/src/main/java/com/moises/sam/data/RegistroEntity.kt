package com.moises.sam.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "registros")
data class RegistroEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fecha: Date = Date(),
    val tipo: String = "",
    val cantidad: Int = 0,
    val monto: Double = 0.0,
    val isPagado: Boolean = false,
    val tipoBordado: String? = null
)