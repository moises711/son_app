package com.moises.sam.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "configuraciones")
data class ConfigEntity(
    @PrimaryKey
    val id: Int = 1, // Singleton
    val adelantos: Double = 0.0,
    val saldoAcumulado: Double = 0.0, // Nuevo campo para saldo acumulativo
    val metaIngresos: Double = 0.0, // Meta diaria/mensual de ingresos
    val metaPeriodo: String = "DIA", // DIA | SEMANA | MES
    val moneda: String = "S/",
    val formatoFecha: String = "dd/MM/yy",
    val mostrarDecimales: Boolean = true,
    val ultimaModificacion: Date = Date()
)