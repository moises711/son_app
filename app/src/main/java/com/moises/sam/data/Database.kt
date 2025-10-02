package com.moises.sam.data

import androidx.room.*
import com.moises.sam.model.Pago
import com.moises.sam.model.Registro
import com.moises.sam.model.TipoServicio
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Entidad para registro en la base de datos
 */
@Entity(tableName = "registros")
data class RegistroEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val tipo: String,
    val cantidad: Int,
    val fecha: String,
    val total: Double,
    val isPagado: Boolean
)

/**
 * Entidad para pagos en la base de datos
 */
@Entity(tableName = "pagos")
data class PagoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val fecha: String,
    val monto: Double,
    val observacion: String
)

/**
 * Entidad para configuraciones y saldos
 */
@Entity(tableName = "configuracion")
data class ConfiguracionEntity(
    @PrimaryKey val id: Int = 1,
    val saldoBordadoPlanchado: Double = 0.0,
    val adelantos: Double = 0.0,
    val saldoChompas: Double = 0.0,
    val saldoPonchos: Double = 0.0,
    val cantidadPonchos: Int = 0
)

/**
 * Conversor de tipos para Room
 */
class Converters {
    @TypeConverter
    fun fromTipoServicio(tipo: TipoServicio): String {
        return tipo.name
    }
    
    @TypeConverter
    fun toTipoServicio(tipo: String): TipoServicio {
        return TipoServicio.valueOf(tipo)
    }
    
    @TypeConverter
    fun fromLocalDate(date: LocalDate): String {
        return date.format(DateTimeFormatter.ISO_LOCAL_DATE)
    }
    
    @TypeConverter
    fun toLocalDate(dateString: String): LocalDate {
        return LocalDate.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE)
    }
}

/**
 * DAO para registros
 */
@Dao
interface RegistroDao {
    @Query("SELECT * FROM registros WHERE tipo IN ('BORDADO', 'PLANCHADO') AND isPagado = 0")
    suspend fun getRegistrosBordadoPlanchado(): List<RegistroEntity>
    
    @Query("SELECT * FROM registros WHERE tipo = 'CHOMPA' AND isPagado = 0")
    suspend fun getRegistrosChompas(): List<RegistroEntity>
    
    @Query("SELECT * FROM registros WHERE tipo = 'PONCHO' AND isPagado = 0")
    suspend fun getRegistrosPonchos(): List<RegistroEntity>
    
    @Insert
    suspend fun insertRegistro(registro: RegistroEntity)
    
    @Update
    suspend fun updateRegistro(registro: RegistroEntity)
    
    @Delete
    suspend fun deleteRegistro(registro: RegistroEntity)
    
    @Query("DELETE FROM registros WHERE tipo IN ('BORDADO', 'PLANCHADO') AND isPagado = 0")
    suspend fun deleteAllBordadoPlanchadoRegistros()
    
    @Query("DELETE FROM registros WHERE tipo = 'CHOMPA' AND isPagado = 0")
    suspend fun deleteAllChompasRegistros()
}

/**
 * DAO para pagos
 */
@Dao
interface PagoDao {
    @Query("SELECT * FROM pagos")
    suspend fun getAllPagos(): List<PagoEntity>
    
    @Query("SELECT * FROM pagos WHERE fecha >= :fecha")
    suspend fun getPagosDesde(fecha: String): List<PagoEntity>
    
    @Insert
    suspend fun insertPago(pago: PagoEntity)
}

/**
 * DAO para configuraciones
 */
@Dao
interface ConfiguracionDao {
    @Query("SELECT * FROM configuracion WHERE id = 1")
    suspend fun getConfiguracion(): ConfiguracionEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConfiguracion(config: ConfiguracionEntity)
    
    @Update
    suspend fun updateConfiguracion(config: ConfiguracionEntity)
}

/**
 * Base de datos Room
 */
@Database(entities = [RegistroEntity::class, PagoEntity::class, ConfiguracionEntity::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun registroDao(): RegistroDao
    abstract fun pagoDao(): PagoDao
    abstract fun configuracionDao(): ConfiguracionDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: android.content.Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = androidx.room.Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sam_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

/**
 * Repositorio para acceder a los datos
 */
class SamRepository(private val context: android.content.Context) {
    private val database = AppDatabase.getDatabase(context)
    private val registroDao = database.registroDao()
    private val pagoDao = database.pagoDao()
    private val configuracionDao = database.configuracionDao()
    
    // Registros
    suspend fun getRegistrosBordadoPlanchado(): List<Registro> {
        return registroDao.getRegistrosBordadoPlanchado().map { entity ->
            Registro(
                id = entity.id,
                tipo = TipoServicio.valueOf(entity.tipo),
                cantidad = entity.cantidad,
                fecha = LocalDate.parse(entity.fecha, DateTimeFormatter.ISO_LOCAL_DATE),
                total = entity.total,
                isPagado = entity.isPagado
            )
        }
    }
    
    suspend fun getRegistrosChompas(): List<Registro> {
        return registroDao.getRegistrosChompas().map { entity ->
            Registro(
                id = entity.id,
                tipo = TipoServicio.valueOf(entity.tipo),
                cantidad = entity.cantidad,
                fecha = LocalDate.parse(entity.fecha, DateTimeFormatter.ISO_LOCAL_DATE),
                total = entity.total,
                isPagado = entity.isPagado
            )
        }
    }
    
    suspend fun getRegistrosPonchos(): List<Registro> {
        return registroDao.getRegistrosPonchos().map { entity ->
            Registro(
                id = entity.id,
                tipo = TipoServicio.valueOf(entity.tipo),
                cantidad = entity.cantidad,
                fecha = LocalDate.parse(entity.fecha, DateTimeFormatter.ISO_LOCAL_DATE),
                total = entity.total,
                isPagado = entity.isPagado
            )
        }
    }
    
    suspend fun saveRegistro(registro: Registro) {
        val entity = RegistroEntity(
            id = registro.id,
            tipo = registro.tipo.name,
            cantidad = registro.cantidad,
            fecha = registro.fecha.format(DateTimeFormatter.ISO_LOCAL_DATE),
            total = registro.total,
            isPagado = registro.isPagado
        )
        registroDao.insertRegistro(entity)
    }
    
    suspend fun deleteAllBordadoPlanchadoRegistros() {
        registroDao.deleteAllBordadoPlanchadoRegistros()
    }
    
    suspend fun deleteAllChompasRegistros() {
        registroDao.deleteAllChompasRegistros()
    }
    
    // Pagos
    suspend fun getAllPagos(): List<Pago> {
        return pagoDao.getAllPagos().map { entity ->
            Pago(
                id = entity.id,
                fecha = LocalDate.parse(entity.fecha, DateTimeFormatter.ISO_LOCAL_DATE),
                monto = entity.monto,
                observacion = entity.observacion
            )
        }
    }
    
    suspend fun getPagosDiarios(): List<Pago> {
        val hoy = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        return pagoDao.getPagosDesde(hoy).map { entity ->
            Pago(
                id = entity.id,
                fecha = LocalDate.parse(entity.fecha, DateTimeFormatter.ISO_LOCAL_DATE),
                monto = entity.monto,
                observacion = entity.observacion
            )
        }
    }
    
    suspend fun savePago(pago: Pago) {
        val entity = PagoEntity(
            id = pago.id,
            fecha = pago.fecha.format(DateTimeFormatter.ISO_LOCAL_DATE),
            monto = pago.monto,
            observacion = pago.observacion
        )
        pagoDao.insertPago(entity)
    }
    
    // Configuración y saldos
    suspend fun getConfiguracion(): ConfiguracionEntity {
        return configuracionDao.getConfiguracion() ?: ConfiguracionEntity()
    }
    
    suspend fun saveConfiguracion(config: ConfiguracionEntity) {
        configuracionDao.insertConfiguracion(config)
    }
    
    suspend fun actualizarSaldos(
        saldoBordadoPlanchado: Double,
        adelantos: Double,
        saldoChompas: Double,
        saldoPonchos: Double,
        cantidadPonchos: Int
    ) {
        val config = configuracionDao.getConfiguracion() ?: ConfiguracionEntity()
        val newConfig = config.copy(
            saldoBordadoPlanchado = saldoBordadoPlanchado,
            adelantos = adelantos,
            saldoChompas = saldoChompas,
            saldoPonchos = saldoPonchos,
            cantidadPonchos = cantidadPonchos
        )
        configuracionDao.updateConfiguracion(newConfig)
    }
}