package com.moises.sam.model

import com.moises.sam.data.RegistroEntity
import java.util.Date

/**
 * Extensión para convertir entidades a objetos de modelo
 */
fun Any.toRegistro(): Registro {
    return when(this) {
        is RegistroEntity -> Registro(
            id = this.id,
            fecha = this.fecha,
            tipo = TipoServicio.valueOf(this.tipo),
            cantidad = this.cantidad,
            total = this.monto,
            isPagado = this.isPagado,
            tipoBordado = this.tipoBordado?.let { TipoBordado.valueOf(it) }
        )
        else -> Registro(
            id = 0,
            fecha = Date(),
            tipo = TipoServicio.BORDADO,
            cantidad = 0,
            total = 0.0
        )
    }
}