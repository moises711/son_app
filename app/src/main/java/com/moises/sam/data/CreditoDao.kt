package com.moises.sam.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface CreditoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(credito: CreditoEntity): Long

    @Query("SELECT monto FROM creditos WHERE tipo = :tipo LIMIT 1")
    suspend fun getMontoByTipo(tipo: String): Double?

    @Query("UPDATE creditos SET monto = :monto WHERE tipo = :tipo")
    suspend fun setMonto(tipo: String, monto: Double)
}
