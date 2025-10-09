package com.moises.sam.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import java.util.Date

/**
 * DAO para operaciones de adelantos
 */
@Dao
interface AdelantoDao {
    
    @Insert
    suspend fun insert(adelanto: AdelantoEntity): Long
    
    @Query("SELECT * FROM adelantos ORDER BY fecha DESC")
    suspend fun getAllAdelantos(): List<AdelantoEntity>
    
    @Query("SELECT * FROM adelantos WHERE fecha BETWEEN :startDate AND :endDate ORDER BY fecha DESC")
    suspend fun getAdelantosByDateRange(startDate: Date, endDate: Date): List<AdelantoEntity>
    
    @Query("SELECT SUM(monto) FROM adelantos")
    suspend fun getTotalAdelantos(): Double?
}