package com.moises.sam.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import java.util.Date

@Dao
interface PagoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(pago: PagoEntity): Long

    @Query("SELECT * FROM pagos ORDER BY fecha DESC")
    suspend fun getAllPagos(): List<PagoEntity>
    
    @Query("SELECT * FROM pagos WHERE fecha >= :fecha ORDER BY fecha DESC")
    suspend fun getPagosDesde(fecha: Date): List<PagoEntity>
    
    @Query("DELETE FROM pagos")
    suspend fun deleteAllPagos()
}