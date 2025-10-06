package com.moises.sam.model

import com.moises.sam.TipoServicio
import com.moises.sam.TipoBordado
import java.util.Date

data class Registro(
    val id: Long = 0L,
    val fecha: Date = Date(),
    val cantidad: Int,
    val total: Double,
    val tipo: TipoServicio,
    val tipoBordado: TipoBordado? = null,
    val isPagado: Boolean = false
)
