package com.moises.sam.model

import java.time.LocalDate

enum class TipoServicio {
    BORDADO, PLANCHADO, CHOMPA, PONCHO
}

data class Registro(
    val id: Long = 0L,
    val tipo: TipoServicio,
    val cantidad: Int,
    val fecha: LocalDate = LocalDate.now(),
    val total: Double,
    val isPagado: Boolean = false
)

data class Pago(
    val id: Long = 0L,
    val fecha: LocalDate = LocalDate.now(),
    val monto: Double,
    val observacion: String = ""
)