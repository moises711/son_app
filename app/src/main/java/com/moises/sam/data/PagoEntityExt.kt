package com.moises.sam.data

import com.moises.sam.model.Pago

fun PagoEntity.toPago(): Pago = Pago(
    id = this.id,
    fecha = this.fecha,
    monto = this.monto,
    observacion = this.observacion
)
