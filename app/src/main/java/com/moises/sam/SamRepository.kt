package com.moises.sam

import android.content.Context
import com.moises.sam.data.AdelantoEntity
import com.moises.sam.data.ConfigEntity
import com.moises.sam.data.RegistroEntity
import com.moises.sam.data.PagoEntity
import com.moises.sam.data.SamRepository as DataSamRepository
import com.moises.sam.model.Adelanto
import com.moises.sam.model.Configuracion
import com.moises.sam.model.Pago
import com.moises.sam.model.Registro
import com.moises.sam.model.TipoServicio
import kotlinx.coroutines.runBlocking

/**
 * Wrapper para el repositorio de datos
 */
class SamRepository(private val context: Context) {
    // Repositorio de datos real
    private val dataRepository = DataSamRepository(context)

    // Métodos síncronos que llaman a los métodos suspendidos del repositorio de datos
    fun getRegistrosBordadoPlanchado(): List<RegistroEntity> = runBlocking {
        dataRepository.getRegistrosBordadoPlanchado()
    }

    fun getRegistrosChompas(): List<RegistroEntity> = runBlocking {
        dataRepository.getRegistrosChompas()
    }

    fun getRegistrosPonchos(): List<RegistroEntity> = runBlocking {
        dataRepository.getRegistrosPonchos()
    }

    fun getAllPagos(): List<Pago> = runBlocking {
        dataRepository.getAllPagos()
    }

    fun getConfiguracion(): ConfigEntity = runBlocking {
        dataRepository.getConfiguracion()
    }

    fun getSaldoAcumulado(): Double = runBlocking {
        dataRepository.getSaldoAcumulado()
    }

    fun getTotalAdelantos(): Double = runBlocking {
        dataRepository.getTotalAdelantos()
    }

    fun saveRegistro(registro: Registro): Unit = runBlocking<Unit> {
        dataRepository.saveRegistro(registro)
    }

    fun savePago(pago: Pago): Unit = runBlocking<Unit> {
        dataRepository.savePago(pago)
    }

    fun saveAdelanto(adelanto: Adelanto): Unit = runBlocking<Unit> {
        dataRepository.saveAdelanto(adelanto)
    }

    fun actualizarSaldoAcumulado(monto: Double): Unit = runBlocking<Unit> {
        dataRepository.actualizarSaldoAcumulado(monto)
    }

    fun actualizarAdelantos(monto: Double): Unit = runBlocking<Unit> {
        dataRepository.actualizarAdelantos(monto)
    }
    
    fun actualizarConfiguracion(config: Configuracion): Unit = runBlocking<Unit> {
        // Convertir Configuracion a ConfigEntity y usar updateConfig
        val configEntity = ConfigEntity(
            moneda = config.moneda,
            formatoFecha = config.formatoFecha,
            mostrarDecimales = config.mostrarDecimales,
            saldoAcumulado = config.saldoAcumulado,
            adelantos = config.adelantos
        )
        dataRepository.updateConfig(configEntity)
    }

    fun eliminarRegistrosBordadoPlanchado(): Unit = runBlocking<Unit> {
        dataRepository.eliminarRegistrosBordadoPlanchado()
    }

    // Métodos adicionales
    fun getTipoServicio(tipoBordado: String): TipoServicio {
        return when (tipoBordado) {
            "Bordado" -> TipoServicio.BORDADO
            "Planchado" -> TipoServicio.PLANCHADO
            "Chompa" -> TipoServicio.CHOMPA
            "Poncho" -> TipoServicio.PONCHO
            else -> TipoServicio.BORDADO
        }
    }

    fun getTipoBordado(): String {
        return "Bordado"
    }

    fun getCantidad(tipoBordado: String): Int {
        return 0
    }
    
    fun updateConfig(config: ConfigEntity): Unit = runBlocking<Unit> {
        dataRepository.updateConfig(config)
    }
    
    fun update(registro: RegistroEntity): Unit = runBlocking<Unit> {
        dataRepository.update(registro)
    }
    
    fun saveRegistroEntity(registro: RegistroEntity): Unit = runBlocking<Unit> {
        dataRepository.saveRegistroEntity(registro)
    }
    
    fun delete(registro: RegistroEntity): Unit = runBlocking<Unit> {
        try {
            // Forzamos a que se ejecute inmediatamente y verificamos que se elimine correctamente
            dataRepository.deleteRegistro(registro)
            // Verificación opcional: verificar que ya no exista el registro
            val check = dataRepository.getRegistroById(registro.id)
            if (check != null) {
                // Si todavía existe, intentamos de nuevo
                dataRepository.deleteRegistro(registro)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Reintentar en caso de error
            dataRepository.deleteRegistro(registro)
        }
    }
    
    fun getAllPagosEntities(): List<PagoEntity> = runBlocking {
        dataRepository.getAllPagosEntities()
    }
    
    fun getAllAdelantos(): List<AdelantoEntity> = runBlocking {
        dataRepository.getAllAdelantos()
    }
    
    fun getRegistroById(id: Long): RegistroEntity? = runBlocking {
        dataRepository.getRegistroById(id)
    }
    
    companion object {
        @Volatile
        private var INSTANCE: SamRepository? = null
        
        fun getInstance(context: Context): SamRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SamRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
