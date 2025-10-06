package com.moises.sam.model

import java.util.Date

data class Pago(
    val id: Long = 0L,
    val fecha: Date = Date(),
    val monto: Double,
    val observacion: String = ""
)