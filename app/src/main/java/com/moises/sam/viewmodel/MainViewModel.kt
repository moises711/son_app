package com.moises.sam.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.moises.sam.model.Pago
import com.moises.sam.model.Registro
import com.moises.sam.model.TipoServicio
import java.time.LocalDate

class MainViewModel : ViewModel() {
    // Estados para el widget de Bordado/Planchado
    var tipoServicioSeleccionado by mutableStateOf(TipoServicio.BORDADO)
    var cantidad by mutableStateOf("")
    var saldoBordadoPlanchado by mutableStateOf(0.0)
    var adelantos by mutableStateOf(0.0)
    var registrosBordadoPlanchado by mutableStateOf<List<Registro>>(emptyList())
    
    // Estados para el widget de Chompas
    var saldoChompas by mutableStateOf(0.0)
    var registrosChompas by mutableStateOf<List<Registro>>(emptyList())
    
    // Estados para el widget de Ponchos
    var saldoPonchos by mutableStateOf(0.0)
    var cantidadPonchos by mutableStateOf(0)
    var registrosPonchos by mutableStateOf<List<Registro>>(emptyList())
    
    // Pagos recibidos
    var pagosRecibidos by mutableStateOf<List<Pago>>(emptyList())
    
    // Estado para diálogos
    var mostrarDialogoChompas by mutableStateOf(false)
    var cantidadChompas by mutableStateOf("")
    var mostrarDialogoPago by mutableStateOf(false)
    var cantidadPago by mutableStateOf("")
    var mostrarDialogoAdelanto by mutableStateOf(false)
    var cantidadAdelanto by mutableStateOf("")
    
    // Barra superior
    val saldoGeneral: Double
        get() = saldoBordadoPlanchado + saldoChompas + saldoPonchos
    
    val ingresosDiarios: Double
        get() = calcularIngresosDiarios()
    
    // Funciones para registros
    fun registrarServicio() {
        val cantidadNum = cantidad.toIntOrNull() ?: return
        if (cantidadNum <= 0) return
        
        val precio = when (tipoServicioSeleccionado) {
            TipoServicio.BORDADO -> 0.50
            TipoServicio.PLANCHADO -> 1.0
            else -> 0.0
        }
        
        val total = cantidadNum * precio
        saldoBordadoPlanchado += total
        
        val registro = Registro(
            tipo = tipoServicioSeleccionado,
            cantidad = cantidadNum,
            total = total
        )
        
        registrosBordadoPlanchado = registrosBordadoPlanchado + registro
        cantidad = ""
    }
    
    fun registrarAdelanto() {
        val monto = cantidadAdelanto.toDoubleOrNull() ?: return
        if (monto <= 0) return
        
        adelantos += monto
        cantidadAdelanto = ""
        mostrarDialogoAdelanto = false
    }
    
    fun registrarChompas() {
        val cantidadNum = cantidadChompas.toIntOrNull() ?: return
        if (cantidadNum <= 0) return
        
        val total = cantidadNum * 1.0
        saldoChompas += total
        
        val registro = Registro(
            tipo = TipoServicio.CHOMPA,
            cantidad = cantidadNum,
            total = total
        )
        
        registrosChompas = registrosChompas + registro
        cantidadChompas = ""
        mostrarDialogoChompas = false
    }
    
    fun registrarPagoChormpas() {
        val monto = cantidadPago.toDoubleOrNull() ?: return
        if (monto <= 0) return
        
        if (monto >= saldoChompas) {
            // Registrar el pago completo
            val pago = Pago(
                monto = saldoChompas,
                observacion = "Pago total planchado de chompas"
            )
            pagosRecibidos = pagosRecibidos + pago
            
            // Reiniciar saldo y registros
            saldoChompas = 0.0
            registrosChompas = emptyList()
        } else {
            // Registrar pago parcial
            val pago = Pago(
                monto = monto,
                observacion = "Pago parcial planchado de chompas"
            )
            pagosRecibidos = pagosRecibidos + pago
            
            saldoChompas -= monto
        }
        
        cantidadPago = ""
        mostrarDialogoPago = false
    }
    
    fun registrarPonchos(cantidad: Int) {
        if (cantidad <= 0) return
        
        val total = cantidad * 2.5
        saldoPonchos += total
        cantidadPonchos += cantidad
        
        val registro = Registro(
            tipo = TipoServicio.PONCHO,
            cantidad = cantidad,
            total = total
        )
        
        registrosPonchos = registrosPonchos + registro
    }
    
    fun registrarPagoPonchos(monto: Double) {
        if (monto <= 0) return
        
        val pago = Pago(
            monto = monto,
            observacion = "Pago planchado de ponchos"
        )
        pagosRecibidos = pagosRecibidos + pago
        
        saldoPonchos -= monto
        // Actualizamos la cantidad de ponchos pendientes
        cantidadPonchos = (saldoPonchos / 2.5).toInt()
    }
    
    fun generarPDF(montoPago: Double): Boolean {
        val saldoPendiente = saldoBordadoPlanchado - adelantos
        
        // Registramos el pago
        val pago = Pago(
            monto = montoPago,
            observacion = "Pago generación PDF"
        )
        pagosRecibidos = pagosRecibidos + pago
        
        // Calculamos el saldo final
        val saldoFinal = if (montoPago >= saldoPendiente) {
            0.0
        } else {
            saldoPendiente - montoPago
        }
        
        // Actualizamos el saldo y limpiamos registros y adelantos
        saldoBordadoPlanchado = saldoFinal
        registrosBordadoPlanchado = emptyList()
        adelantos = 0.0
        
        return true
    }
    
    private fun calcularIngresosDiarios(): Double {
        val hoy = LocalDate.now()
        return pagosRecibidos
            .filter { it.fecha == hoy }
            .sumOf { it.monto }
    }
    
    // Colores para los saldos de ponchos
    fun obtenerColorSaldo(): androidx.compose.ui.graphics.Color {
        return when {
            saldoPonchos > 0 -> com.moises.sam.ui.theme.BalanceYellow
            saldoPonchos == 0.0 -> com.moises.sam.ui.theme.BalanceGreen
            else -> com.moises.sam.ui.theme.BalanceRed
        }
    }
}