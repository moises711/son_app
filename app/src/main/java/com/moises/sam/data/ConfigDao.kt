package com.moises.sam.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface ConfigDao {
    @Query("SELECT * FROM configuraciones WHERE id = 1")
    suspend fun getConfig(): ConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(config: ConfigEntity): Long

    @Update
    suspend fun update(config: ConfigEntity)
}