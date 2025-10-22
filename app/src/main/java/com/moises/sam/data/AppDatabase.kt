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
        AdelantoEntity::class,
        CreditoEntity::class
    ],
    version = 5, // Incrementar versión para nuevas migraciones
    exportSchema = true
)
@TypeConverters(DateConverter::class)
abstract class AppDatabase : RoomDatabase() {
    
    // DAOs
    abstract fun registroDao(): RegistroDao
    abstract fun pagoDao(): PagoDao
    abstract fun configDao(): ConfigDao
    abstract fun adelantoDao(): AdelantoDao
    abstract fun creditoDao(): CreditoDao
    
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
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
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

        // Migración de la versión 2 a la 3: agregar metaIngresos a configuraciones
        val MIGRATION_2_3 = object : androidx.room.migration.Migration(2, 3) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE configuraciones ADD COLUMN metaIngresos REAL NOT NULL DEFAULT 0.0")
            }
        }

        // Migración de la versión 3 a la 4: agregar metaPeriodo a configuraciones
        val MIGRATION_3_4 = object : androidx.room.migration.Migration(3, 4) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE configuraciones ADD COLUMN metaPeriodo TEXT NOT NULL DEFAULT 'DIA'")
            }
        }

        // Migración de la versión 4 a la 5: crear tabla de créditos por tipo (CHOMPA, PONCHO)
        val MIGRATION_4_5 = object : androidx.room.migration.Migration(4, 5) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS creditos (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        tipo TEXT NOT NULL,
                        monto REAL NOT NULL DEFAULT 0.0
                    );
                    """.trimIndent()
                )
                // Índice único para tipo, para facilitar upsert por tipo
                database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_creditos_tipo ON creditos(tipo)")
            }
        }
    }
}

// Las interfaces DAO ahora están definidas en archivos separados para mejor organización
// Ver: RegistroDao.kt, PagoDao.kt, ConfigDao.kt y AdelantoDao.kt