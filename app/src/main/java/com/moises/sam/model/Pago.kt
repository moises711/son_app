package com.moises.sam.model

import java.util.Date

data class Pago(
    val id: Long = 0,
    val name: String = "",
    val monto: Double = 0.0,
    val fecha: Date = Date(),
    val observacion: String = ""
) : Comparable<Pago> {
    override operator fun compareTo(other: Pago): Int {
        return fecha.compareTo(other.fecha)
    }
}