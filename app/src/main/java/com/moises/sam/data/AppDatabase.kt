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
    version = 2, // Incrementar versión para migración
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
                .addMigrations(MIGRATION_1_2)
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }

        // Migración de la versión 1 a la 2: agregar saldoAcumulado a configuraciones
        val MIGRATION_1_2 = object : androidx.room.migration.Migration(1, 2) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE configuraciones ADD COLUMN saldoAcumulado REAL NOT NULL DEFAULT 0.0")
            }
        }
    }
}

// Las interfaces DAO ahora están definidas en archivos separados para mejor organización
// Ver: RegistroDao.kt, PagoDao.kt, ConfigDao.kt y AdelantoDao.kt