package com.moises.sam.data

import android.content.Context
import com.moises.sam.model.Adelanto
import com.moises.sam.model.Pago
import com.moises.sam.model.Registro
import com.moises.sam.model.TipoServicio
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Date

/**
 * Repositorio para acceder a los datos de la aplicación
 */
class SamRepository(private val context: Context) {
    
    // Referencia a la base de datos
    private val appDatabase = AppDatabase.getDatabase(context)
    
    // DAO para registros
    private val registroDao = appDatabase.registroDao()
    
    // DAO para pagos
    private val pagoDao = appDatabase.pagoDao()
    
    // DAO para configuración
    private val configDao = appDatabase.configDao()
    
    // DAO para adelantos
    private val adelantoDao = appDatabase.adelantoDao()
    
    // Obtener configuración
    suspend fun getConfiguracion(): ConfigEntity = withContext(Dispatchers.IO) {
        // Obtener configuración o crear si no existe
        val config = configDao.getConfig()
        config ?: ConfigEntity().also { configDao.insert(it) }
    }
    
    // Actualizar adelantos
    suspend fun actualizarAdelantos(monto: Double) = withContext(Dispatchers.IO) {
        val config = getConfiguracion()
        configDao.update(config.copy(adelantos = monto))
    }
    
    // Guardar pago
    suspend fun savePago(pago: Pago) = withContext(Dispatchers.IO) {
        val pagoEntity = PagoEntity(
            id = 0,
            fecha = pago.fecha,
            monto = pago.monto,
            observacion = pago.observacion
        )
        pagoDao.insert(pagoEntity)
    }
    
    // Obtener registros de bordado y planchado
    suspend fun getRegistrosBordadoPlanchado(): List<RegistroEntity> = withContext(Dispatchers.IO) {
        registroDao.getRegistrosByTipo(listOf(TipoServicio.BORDADO.name, TipoServicio.PLANCHADO.name))
    }
    
    // Obtener registros de chompas
    suspend fun getRegistrosChompas(): List<RegistroEntity> = withContext(Dispatchers.IO) {
        registroDao.getRegistrosByTipo(listOf(TipoServicio.CHOMPA.name))
    }
    
    // Obtener registros de ponchos
    suspend fun getRegistrosPonchos(): List<RegistroEntity> = withContext(Dispatchers.IO) {
        registroDao.getRegistrosByTipo(listOf(TipoServicio.PONCHO.name))
    }
    
    // Guardar registro de servicio
    suspend fun saveRegistro(registro: Registro) = withContext(Dispatchers.IO) {
        val registroEntity = RegistroEntity(
            id = 0,
            fecha = registro.fecha,
            tipo = registro.tipo.name,
            cantidad = registro.cantidad,
            monto = registro.total,
            isPagado = registro.isPagado,
            tipoBordado = registro.tipoBordado?.name
        )
        registroDao.insert(registroEntity)
    }
    
    // Guardar adelanto
    suspend fun saveAdelanto(adelanto: Adelanto) = withContext(Dispatchers.IO) {
        val adelantoEntity = AdelantoEntity(
            id = 0,
            fecha = adelanto.fecha,
            monto = adelanto.monto,
            observacion = adelanto.observacion
        )
        adelantoDao.insert(adelantoEntity)
    }
    
    // Obtener todos los adelantos
    suspend fun getAllAdelantos(): List<AdelantoEntity> = withContext(Dispatchers.IO) {
        adelantoDao.getAllAdelantos()
    }
    
    // Obtener el total de adelantos
    suspend fun getTotalAdelantos(): Double = withContext(Dispatchers.IO) {
        adelantoDao.getTotalAdelantos() ?: 0.0
    }
}

// Las entidades ahora están definidas en archivos separados para mejor organización
// Ver: ConfigEntity.kt, RegistroEntity.kt y PagoEntity.kt