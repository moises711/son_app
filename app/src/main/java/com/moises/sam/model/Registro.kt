package com.moises.sam.model

import java.util.Date

data class Registro(
    val id: Long = 0,
    val name: String = "",
    val tipo: TipoServicio = TipoServicio.BORDADO,
    val tipoBordado: TipoBordado? = null,
    val fecha: Date = Date(),
    val monto: Double = 0.0,
    val total: Double = 0.0,
    val adelantos: Double = 0.0,
    val descripcion: String = "",
    val cantidad: Int = 0,
    val isPagado: Boolean = false
)
