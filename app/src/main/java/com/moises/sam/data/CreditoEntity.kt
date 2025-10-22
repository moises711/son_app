package com.moises.sam.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "creditos",
    indices = [Index(value = ["tipo"], unique = true)]
)
data class CreditoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tipo: String, // CHOMPA o PONCHO
    val monto: Double // monto a favor (crédito); se restará del saldo de registros
)
