package com.moises.sam.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RegistroDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(registro: RegistroEntity): Long

    @Update
    suspend fun update(registro: RegistroEntity)
    
    @androidx.room.Delete
    suspend fun delete(registro: RegistroEntity)

    @Query("SELECT * FROM registros WHERE tipo IN (:tipos)")
    suspend fun getRegistrosByTipo(tipos: List<String>): List<RegistroEntity>

    @Query("SELECT * FROM registros WHERE id = :id")
    suspend fun getRegistroById(id: Long): RegistroEntity?
    
    @Query("SELECT * FROM registros ORDER BY fecha DESC")
    suspend fun getAllRegistros(): List<RegistroEntity>
    
    @Query("UPDATE registros SET isPagado = :pagado WHERE id = :registroId")
    suspend fun marcarRegistroComoPagado(registroId: Long, pagado: Boolean)
    
    @Query("DELETE FROM registros WHERE tipo IN ('BORDADO', 'PLANCHADO')")
    suspend fun deleteAllBordadoPlanchadoRegistros()
    
    @Query("DELETE FROM registros WHERE tipo = 'CHOMPA'")
    suspend fun deleteAllChompasRegistros()
    
    @Query("DELETE FROM registros")
    suspend fun deleteAllRegistros()
}