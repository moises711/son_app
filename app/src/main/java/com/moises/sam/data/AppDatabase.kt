package com.moises.sam.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/**
 * Base de datos principal de la aplicación
 */
@Database(
    entities = [
        RegistroEntity::class,
        PagoEntity::class,
        ConfigEntity::class,
        AdelantoEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(DateConverter::class)
abstract class AppDatabase : RoomDatabase() {
    
    // DAOs
    abstract fun registroDao(): RegistroDao
    abstract fun pagoDao(): PagoDao
    abstract fun configDao(): ConfigDao
    abstract fun adelantoDao(): AdelantoDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sam_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

// Las interfaces DAO ahora están definidas en archivos separados para mejor organización
// Ver: RegistroDao.kt, PagoDao.kt, ConfigDao.kt y AdelantoDao.kt