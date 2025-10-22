package com.moises.sam.model

import java.util.Date

data class Configuracion(
    val id: Int = 1,
    val moneda: String = "PEN",
    val mostrarDecimales: Boolean = true,
    val formatoFecha: String = "dd/MM/yyyy",
    val adelantos: Double = 0.0,
    val saldoAcumulado: Double = 0.0,
    val metaIngresos: Double = 0.0,
    val metaPeriodo: String = "DIA",
    val ultimaModificacion: Date = Date()
)
