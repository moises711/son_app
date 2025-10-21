package com.moises.sam

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.FileProvider
import androidx.core.text.HtmlCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.itextpdf.text.BaseColor
import com.itextpdf.text.Document
import com.itextpdf.text.Element
import com.itextpdf.text.Font
import com.itextpdf.text.PageSize
import com.itextpdf.text.Paragraph
import com.itextpdf.text.Phrase
import com.itextpdf.text.pdf.PdfPCell
import com.itextpdf.text.pdf.PdfPTable
import com.itextpdf.text.pdf.PdfWriter
import com.moises.sam.data.RegistroEntity
import com.moises.sam.model.Adelanto
import com.moises.sam.model.Configuracion
import com.moises.sam.data.PagoEntity
import com.moises.sam.data.ConfigEntity
import com.moises.sam.model.Pago
import com.moises.sam.model.Registro
import com.moises.sam.model.TipoBordado
import com.moises.sam.model.TipoServicio
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * MainActivity: Pantalla principal de la aplicación SAM
 */
class MainActivity : AppCompatActivity() {

    // Repositorio para acceder a los datos
    private lateinit var repository: SamRepository
    
    // Botones en la parte superior
    private lateinit var btnBordado: MaterialButton
    private lateinit var btnPlanchado: MaterialButton
    private lateinit var btnDescargarPdf: Button
    private lateinit var btnAdelantos: Button
    private lateinit var btnConfiguracion: Button
    
    // Card con configuración actual
    // Estos elementos no existen en el layout actual
    // private lateinit var cardConfig: CardView
    // private lateinit var tvNombre: TextView
    // private lateinit var tvTaller: TextView
    
    // Card de estados financieros
    private lateinit var tvSaldoGeneral: TextView
    private lateinit var tvIngresosHoy: TextView
    
    // Card del jefe (Bordado y planchado)
    private lateinit var tvJefeSaldo: TextView
    private lateinit var tvJefeSaldos: TextView
    private lateinit var tvJefeTotalRegistros: TextView
    // Estos elementos no existen en el layout actual, los comentamos
    //private lateinit var tvBordadosNumero: TextView
    //private lateinit var tvPlanchadosNumero: TextView
    
    // Card de clemente (chompas)
    private lateinit var btnRegistroClemente: MaterialButton
    private lateinit var btnPagosClemente: MaterialButton
    private lateinit var btnRegistroLalo: MaterialButton
    private lateinit var btnPagosLalo: MaterialButton
    
    // Labels de saldos
    private lateinit var tvChompasNumero: TextView
    private lateinit var tvClementeSaldo: TextView
    private lateinit var tvPonchosNumero: TextView
    private lateinit var tvLaloSaldo: TextView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // Inicializar repositorio
        repository = SamRepository.getInstance(this)
        
        // Referencias a vistas
        inicializarVistas()
        
        // Configuración inicial
        configurarEventos()
        
        // Cargar datos
        cargarDatos()
    }
    
    /**
     * Método para convertir tipos de datos
     */
    private fun RegistroEntity.toRegistro(): Registro {
        return Registro(
            id = this.id,
            tipo = TipoServicio.valueOf(this.tipo),
            tipoBordado = this.tipoBordado?.let { TipoBordado.valueOf(it) },
            fecha = this.fecha,
            monto = this.monto,
            total = this.monto,
            cantidad = this.cantidad,
            isPagado = this.isPagado
        )
    }
    
    /**
     * Inicializa las referencias a las vistas
     */
    private fun inicializarVistas() {
        // Referencias a vistas
        btnBordado = findViewById(R.id.btn_bordados)
        btnPlanchado = findViewById(R.id.btn_planchados)
        btnDescargarPdf = findViewById(R.id.btn_descargar_pdf)
        btnAdelantos = findViewById(R.id.btn_adelantos)
        btnConfiguracion = findViewById(R.id.btn_configuracion)
        
        // Para simplificar, eliminamos cardConfig, tvNombre y tvTaller que parecen no existir
        // en el layout actual
        
        tvSaldoGeneral = findViewById(R.id.tv_saldo_general)
        tvIngresosHoy = findViewById(R.id.tv_ingresos_hoy)
        
        tvJefeSaldo = findViewById(R.id.tv_jefe_saldo)
        tvJefeSaldos = findViewById(R.id.tv_jefe_saldos)
        tvJefeTotalRegistros = findViewById(R.id.tv_jefe_total_registros)
        // Estos elementos no existen en el layout actual
        //tvBordadosNumero = findViewById(R.id.tv_bordados_numero)
        //tvPlanchadosNumero = findViewById(R.id.tv_planchados_numero)
        
        btnRegistroClemente = findViewById(R.id.btn_registro_clemente)
        btnPagosClemente = findViewById(R.id.btn_pagos_clemente)
        btnRegistroLalo = findViewById(R.id.btn_registro_lalo)
        btnPagosLalo = findViewById(R.id.btn_pagos_lalo)
        
        tvChompasNumero = findViewById(R.id.tv_chompas_numero)
        tvClementeSaldo = findViewById(R.id.tv_clemente_saldo)
        tvPonchosNumero = findViewById(R.id.tv_ponchos_numero)
        tvLaloSaldo = findViewById(R.id.tv_lalo_saldo)
    }
    
    /**
     * Configurar eventos de los botones
     */
    private fun configurarEventos() {
        // Botón para registrar bordado
        btnBordado.setOnClickListener {
            mostrarDialogoRegistroServicio(TipoServicio.BORDADO) { cantidad ->
                registrarServicio(TipoServicio.BORDADO, cantidad)
                Toast.makeText(this, "Bordado registrado: $cantidad", Toast.LENGTH_SHORT).show()
            }
        }
        
        // Botón para registrar planchado
        btnPlanchado.setOnClickListener {
            mostrarDialogoRegistroServicio(TipoServicio.PLANCHADO) { cantidad ->
                registrarServicio(TipoServicio.PLANCHADO, cantidad)
                Toast.makeText(this, "Planchado registrado: $cantidad", Toast.LENGTH_SHORT).show()
            }
        }
        
        // Botón configuración
        btnConfiguracion.setOnClickListener {
            mostrarDialogoConfiguracion()
        }

        // Botón para ver registros de Clemente
        btnRegistroClemente.setOnClickListener {
            mostrarDialogoVerRegistros(TipoServicio.CHOMPA)
        }

        // Botón para ver registros de Lalo
        btnRegistroLalo.setOnClickListener {
            mostrarDialogoVerRegistros(TipoServicio.PONCHO)
        }
        
        // Botón para ver registros del Jefe (Bordado y Planchado)
        findViewById<MaterialButton>(R.id.btn_registro_jefe).setOnClickListener {
            mostrarDialogoVerRegistros(TipoServicio.BORDADO)
        }
        
        // Botón para registrar pagos de Clemente
        btnPagosClemente.setOnClickListener {
            mostrarDialogoPagoAdelanto("Registrar pago para Clemente", TipoServicio.CHOMPA)
        }
        
        // Botón para registrar pagos de Lalo
        btnPagosLalo.setOnClickListener {
            mostrarDialogoPagoAdelanto("Registrar pago para Lalo", TipoServicio.PONCHO)
        }

        // Botón para registrar servicio de Clemente
        val btnRegistrarClemente = findViewById<MaterialButton>(R.id.btn_registrar_clemente)
        btnRegistrarClemente.setOnClickListener {
            mostrarDialogoRegistroServicio(TipoServicio.CHOMPA) { cantidad ->
                registrarServicio(TipoServicio.CHOMPA, cantidad)
                Toast.makeText(this, "Chompa registrada: $cantidad", Toast.LENGTH_SHORT).show()
            }
        }

        // Botón para registrar servicio de Lalo
        val btnRegistrarLalo = findViewById<MaterialButton>(R.id.btn_registrar_lalo)
        btnRegistrarLalo.setOnClickListener {
            mostrarDialogoRegistroServicio(TipoServicio.PONCHO) { cantidad ->
                registrarServicio(TipoServicio.PONCHO, cantidad)
                Toast.makeText(this, "Poncho registrada: $cantidad", Toast.LENGTH_SHORT).show()
            }
        }

        // MODAL: Adelantos para Jefe
        btnAdelantos.setOnClickListener {
            mostrarDialogoAdelantos()
        }

        // Botón para generar PDF SOLO con registros de jefe
        btnDescargarPdf.setOnClickListener {
            // Mostrar modal para ingresar monto de pago
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Registrar pago antes de descargar PDF")
            val input = EditText(this)
            input.inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            builder.setView(input)
            builder.setPositiveButton("Registrar y Generar PDF") { _, _ ->
                val monto = try {
                    input.text.toString().toDouble()
                } catch (e: NumberFormatException) {
                    0.0
                }
                
                if (monto > 0) {
                    lifecycleScope.launch {
                        val pagos = withContext(Dispatchers.IO) { repository.getAllPagos() }
                        
                        repository.savePago(Pago(
                            monto = monto,
                            observacion = "Pago registrado antes de generar PDF"
                        ))
                        
                        Toast.makeText(this@MainActivity, "Pago registrado: S/. $monto", Toast.LENGTH_SHORT).show()
                        // Generar PDF con contenido
                        val exito = generarPDF()
                        
                        if (exito) {
                            Toast.makeText(this@MainActivity, "PDF generado correctamente", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@MainActivity, "Error al generar PDF", Toast.LENGTH_SHORT).show()
                        }
                        
                        actualizarSaldos()
                    }
                } else {
                    Toast.makeText(this, "Debe ingresar un monto válido", Toast.LENGTH_SHORT).show()
                }
            }
            builder.setNegativeButton("Cancelar", null)
            builder.show()
        }
    }
    
    /**
     * Carga los datos iniciales
     */
    private fun cargarDatos() {
        lifecycleScope.launch {
            val config = withContext(Dispatchers.IO) { repository.getConfiguracion() }
            withContext(Dispatchers.Main) {
                // Mostrar datos de configuración
                // tvNombre y tvTaller no existen en el layout actual
                // tvNombre.text = config.nombre
                // tvTaller.text = config.taller
                
                // Actualizar saldos y estados financieros
                actualizarSaldos()
            }
        }
    }
    
    // Método para mostrar diálogo de registro de servicio
    private fun mostrarDialogoRegistroServicio(tipoServicio: TipoServicio, callback: (cantidad: Int) -> Unit) {
        val builder = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.dialog_registro_servicio, null)
        builder.setView(view)

        val dialog = builder.create()

        val etCantidad = view.findViewById<EditText>(R.id.et_cantidad)
        
        // Obtener la vista incluida de tipos de bordado
        val layoutTiposBordado = view.findViewById<View>(R.id.layout_tipos_bordado)
        val btnRegistrar = view.findViewById<Button>(R.id.btn_registrar)
        val btnCancel = view.findViewById<Button>(R.id.btn_cancel)
        
        // Buscar elementos dentro del layout incluido
        val tvTitulo = layoutTiposBordado.findViewById<TextView>(R.id.tv_titulo_servicio)
        val btnTroyer = layoutTiposBordado.findViewById<Button>(R.id.btn_bordado_troyer)
        val btnMolde = layoutTiposBordado.findViewById<Button>(R.id.btn_bordado_molde)
        val btnChompas = layoutTiposBordado.findViewById<Button>(R.id.btn_bordado_chompas)

        // Ajustar título según el tipo de servicio
        when (tipoServicio) {
            TipoServicio.BORDADO -> tvTitulo.text = "Registro de Bordado"
            TipoServicio.PLANCHADO -> tvTitulo.text = "Registro de Planchado"
            TipoServicio.CHOMPA -> tvTitulo.text = "Registro de Chompa"
            TipoServicio.PONCHO -> tvTitulo.text = "Registro de Poncho"
            else -> tvTitulo.text = "Registro de Servicio"
        }

        if (tipoServicio == TipoServicio.BORDADO) {
            // Mostrar botones de tipo de bordado
            layoutTiposBordado.visibility = View.VISIBLE
            btnRegistrar.visibility = View.GONE
            
            val onTipoClick = { tipoBordado: TipoBordado ->
                val cantidadText = etCantidad.text.toString()
                if (cantidadText.isNotEmpty()) {
                    val cantidad = cantidadText.toInt()
                    if (cantidad > 0) {
                        registrarServicioBordado(tipoBordado, cantidad)
                        dialog.dismiss()
                    } else {
                        Toast.makeText(this, "La cantidad debe ser mayor a cero", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Ingrese una cantidad válida", Toast.LENGTH_SHORT).show()
                }
            }
            btnTroyer.setOnClickListener { onTipoClick(TipoBordado.TROYER) }
            btnMolde.setOnClickListener { onTipoClick(TipoBordado.MOLDE) }
            btnChompas.setOnClickListener { onTipoClick(TipoBordado.CHOMPAS_LANA) }
        } else {
            // Ocultar botones de tipo de bordado y mostrar el botón de registrar
            layoutTiposBordado.visibility = View.GONE
            btnRegistrar.visibility = View.VISIBLE
            
            btnRegistrar.setOnClickListener {
                val cantidadText = etCantidad.text.toString()
                if (cantidadText.isNotEmpty()) {
                    val cantidad = cantidadText.toInt()
                    if (cantidad > 0) {
                        callback(cantidad)
                        dialog.dismiss()
                    } else {
                        Toast.makeText(this, "La cantidad debe ser mayor a cero", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Ingrese una cantidad válida", Toast.LENGTH_SHORT).show()
                }
            }
        }
        
        // Configurar el botón cancelar para cerrar el diálogo en ambos casos
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    // Nuevo método para registrar bordado con tipo
    private fun registrarServicioBordado(tipoBordado: TipoBordado, cantidad: Int) {
        val monto = 0.5 * cantidad // mismo precio para todos los tipos
        lifecycleScope.launch {
            val registro = Registro(
                tipo = TipoServicio.BORDADO,
                cantidad = cantidad,
                total = monto,
                tipoBordado = tipoBordado
            )
            repository.saveRegistro(registro)
            withContext(Dispatchers.Main) {
                actualizarSaldos()
            }
        }
    }

    private fun registrarServicio(tipo: TipoServicio, cantidad: Int) {
        // Calcular monto según tipo de servicio
        val monto = when (tipo) {
            TipoServicio.BORDADO -> 0.5 * cantidad
            TipoServicio.PLANCHADO -> 1.0 * cantidad
            TipoServicio.CHOMPA -> 1.0 * cantidad  // Corregido a S/1.00 por unidad
            TipoServicio.PONCHO -> 2.5 * cantidad
        }
        
        lifecycleScope.launch {
            // Crear objeto de registro
            val registro = Registro(
                tipo = tipo,
                cantidad = cantidad,
                total = monto
            )
            
            // Guardar en la base de datos
            repository.saveRegistro(registro)
            
            // Actualizar UI en hilo principal
            withContext(Dispatchers.Main) {
                actualizarSaldos()
                Toast.makeText(this@MainActivity, "Servicio registrado correctamente", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun mostrarDialogoPagoAdelanto(titulo: String, tipoServicio: TipoServicio) {
        val builder = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.dialog_pago_adelanto, null)
        builder.setView(view)

        val dialog = builder.create()

        // Usar los IDs correctos del layout dialog_pago_adelanto
        val etMonto = view.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_pago_monto)
        val etObservacion = view.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_pago_observacion)
        val btnRegistrar = view.findViewById<Button>(R.id.btn_pago_registrar)
        val btnCancelar = view.findViewById<Button>(R.id.btn_pago_cancelar)
        
        // Configuración inicial
        dialog.setTitle(titulo)
        
        // Cancelar
        btnCancelar.setOnClickListener {
            dialog.dismiss()
        }
        
        // Registrar pago o adelanto
        btnRegistrar.setOnClickListener {
            val montoText = etMonto.text.toString()
            if (montoText.isNotEmpty()) {
                val monto = montoText.toDouble()
                val observacion = etObservacion.text.toString()
                
                lifecycleScope.launch {
                    // Unificamos: registrar como pago con observación según tipo
                    val tipoObservacion = when (tipoServicio) {
                        TipoServicio.CHOMPA -> "PAGO_CHOMPA"
                        TipoServicio.PONCHO -> "PAGO_PONCHO"
                        else -> "PAGO_JEFE"
                    }
                    val pago = Pago(
                        monto = monto,
                        observacion = "$tipoObservacion: $observacion"
                    )
                    repository.savePago(pago)
                    actualizarRegistrosConPago(tipoServicio, monto)
                    withContext(Dispatchers.Main) {
                        actualizarSaldos()
                        Toast.makeText(this@MainActivity, "Pago registrado: S/. $monto", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    }
                }
            } else {
                Toast.makeText(this, "Ingrese un monto válido", Toast.LENGTH_SHORT).show()
            }
        }
        
        dialog.show()
    }

    /**
     * Muestra el diálogo de configuración de la aplicación
     */
    private fun mostrarDialogoConfiguracion() {
        val builder = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.dialog_configuracion, null)
        builder.setView(view)
        
        val dialog = builder.create()
        
        // Referencias a vistas en el diálogo - usar IDs correctos del XML
        val etMoneda = view.findViewById<EditText>(R.id.et_config_moneda)
        val etFormatoFecha = view.findViewById<EditText>(R.id.et_config_formato_fecha)
        val switchDecimales = view.findViewById<androidx.appcompat.widget.SwitchCompat>(R.id.switch_config_decimales)
        val btnGuardar = view.findViewById<Button>(R.id.btn_config_guardar)
        val btnCancelar = view.findViewById<Button>(R.id.btn_config_cancelar)
        
        // Cargar datos actuales
        lifecycleScope.launch {
            val config = withContext(Dispatchers.IO) { repository.getConfiguracion() }
            withContext(Dispatchers.Main) {
                // Configurar valores según los controles disponibles
                etMoneda.setText(config.moneda ?: "S/")
                etFormatoFecha.setText(config.formatoFecha ?: "dd/MM/yyyy")
                switchDecimales.isChecked = config.mostrarDecimales ?: true
            }
        }
        
        // Cancelar
        btnCancelar.setOnClickListener {
            dialog.dismiss()
        }
        
        // Guardar configuración
        btnGuardar.setOnClickListener {
            val moneda = etMoneda.text.toString()
            val formatoFecha = etFormatoFecha.text.toString()
            val mostrarDecimales = switchDecimales.isChecked
            
            if (moneda.isEmpty() || formatoFecha.isEmpty()) {
                Toast.makeText(this, "Complete todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            lifecycleScope.launch {
                val config = withContext(Dispatchers.IO) { repository.getConfiguracion() }
                val updatedConfig = config.copy(
                    moneda = moneda,
                    formatoFecha = formatoFecha,
                    mostrarDecimales = mostrarDecimales
                )
                repository.updateConfig(updatedConfig)
                
                withContext(Dispatchers.Main) {
                    actualizarSaldos()
                    Toast.makeText(this@MainActivity, "Configuración guardada", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
            }
        }
        
        dialog.show()
    }
    
    /**
     * Muestra el diálogo de adelantos
     */
    private fun mostrarDialogoAdelantos() {
        val view = layoutInflater.inflate(R.layout.dialog_pago_adelanto, null)
        val builder = AlertDialog.Builder(this)
        builder.setView(view)
        val dialog = builder.create()

        // IDs válidos en dialog_pago_adelanto.xml
        val etMonto = view.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_pago_monto)
        val etObservacion = view.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_pago_observacion)
        val rbPago = view.findViewById<android.widget.RadioButton>(R.id.rb_pago)
        val rbAdelanto = view.findViewById<android.widget.RadioButton>(R.id.rb_adelanto)
        val btnCancelar = view.findViewById<Button>(R.id.btn_pago_cancelar)
        val btnRegistrar = view.findViewById<Button>(R.id.btn_pago_registrar)

        btnCancelar.setOnClickListener { dialog.dismiss() }

        btnRegistrar.setOnClickListener {
            val montoText = etMonto.text?.toString().orEmpty()
            if (montoText.isNotEmpty()) {
                val monto = montoText.toDoubleOrNull() ?: 0.0
                val observacion = etObservacion.text?.toString().orEmpty()
                lifecycleScope.launch {
                    if (rbPago.isChecked) {
                        val tipoObs = "PAGO_JEFE" // o según contexto si se pasa tipoServicio
                        repository.savePago(Pago(monto = monto, observacion = "$tipoObs: $observacion"))
                        withContext(Dispatchers.Main) {
                            actualizarSaldos()
                            Toast.makeText(this@MainActivity, "Pago registrado: S/. $monto", Toast.LENGTH_SHORT).show()
                            dialog.dismiss()
                        }
                    } else if (rbAdelanto.isChecked) {
                        val tipoObs = "ADELANTO_JEFE"
                        repository.saveAdelanto(Adelanto(monto = monto, observacion = "$tipoObs: $observacion"))
                        withContext(Dispatchers.Main) {
                            actualizarSaldos()
                            Toast.makeText(this@MainActivity, "Adelanto registrado: S/. $monto", Toast.LENGTH_SHORT).show()
                            dialog.dismiss()
                        }
                    }
                }
            } else {
                etMonto.error = "Ingrese un monto válido"
            }
        }

        dialog.show()
    }

    // Nuevo método para actualizar registros cuando se hace un pago
    private suspend fun actualizarRegistrosConPago(tipoServicio: TipoServicio, montoPagado: Double) {
        withContext(Dispatchers.IO) {
            // Usar una transacción para asegurar que todas las operaciones se completen o ninguna
            try {
                var montoPendiente = montoPagado
                val registros = when (tipoServicio) {
                    TipoServicio.CHOMPA -> repository.getRegistrosChompas()
                    TipoServicio.PONCHO -> repository.getRegistrosPonchos()
                    else -> repository.getRegistrosBordadoPlanchado()
                }
                
                // Ordenar por fecha (los más antiguos primero)
                val registrosOrdenados = registros.sortedBy { it.fecha }
                
                // Lista para registrar cambios
                val registrosAEliminar = mutableListOf<RegistroEntity>()
                val registrosAActualizar = mutableListOf<RegistroEntity>()
                
                for (registro in registrosOrdenados) {
                    if (montoPendiente <= 0) break
                    
                    // Accedemos directamente a las propiedades
                    if (montoPendiente >= registro.monto) {
                        // El pago cubre completamente este registro, agregarlo a la lista de eliminación
                        registrosAEliminar.add(registro)
                        montoPendiente -= registro.monto
                    } else {
                        // El pago cubre parcialmente este registro, actualizarlo
                        val proporcion = montoPendiente / registro.monto
                        val nuevaCantidad = (registro.cantidad * (1 - proporcion)).toInt()
                        val nuevoMonto = registro.monto - montoPendiente
                        
                        // Solo actualizar si queda cantidad significativa
                        if (nuevaCantidad > 0) {
                            val registroActualizado = RegistroEntity(
                                id = registro.id,
                                fecha = Date(), // Actualizar fecha a hoy
                                tipo = registro.tipo,
                                cantidad = nuevaCantidad,
                                monto = nuevoMonto,
                                isPagado = registro.isPagado,
                                tipoBordado = registro.tipoBordado
                            )
                            registrosAActualizar.add(registroActualizado)
                        } else {
                            // Si la cantidad es muy pequeña, agregar a la lista de eliminación
                            registrosAEliminar.add(registro)
                        }
                        
                        // Todo el monto pendiente fue usado
                        montoPendiente = 0.0
                    }
                }
                
                // Ejecutar todas las operaciones de eliminación
                registrosAEliminar.forEach { registro ->
                    repository.delete(registro)
                }
                
                // Ejecutar todas las operaciones de actualización
                registrosAActualizar.forEach { registro ->
                    repository.update(registro)
                }
                
                // Forzar la sincronización con la base de datos
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Registros actualizados correctamente", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Error al actualizar registros: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
        
        // Si queda monto pendiente, se considera crédito a favor
        // No hacemos nada especial, ya que la función actualizarSaldos() mostrará saldo negativo
    }
    
    /**
     * Actualiza los saldos y estados financieros en la UI
     */
    private suspend fun actualizarSaldos() {
        withContext(Dispatchers.IO) {
            try {
                // Obtener datos de la base de datos
                val registrosBordadoPlanchado = repository.getRegistrosBordadoPlanchado()
                val registrosChompas = repository.getRegistrosChompas()
                val registrosPonchos = repository.getRegistrosPonchos()
                val pagos = repository.getAllPagosEntities()
                val config = repository.getConfiguracion()
                val saldoAcumulado = repository.getSaldoAcumulado()

                // Calcular saldos
                var saldoBordadoPlanchado = 0.0
                registrosBordadoPlanchado.forEach { registro ->
                    saldoBordadoPlanchado += registro.monto
                }

                // Calcular pagos por tipo de servicio
                var pagosChompas = 0.0
                var pagosPonchos = 0.0
                var pagosBordadoPlanchado = 0.0
                
                // Filtramos los pagos por tipo de servicio según la observación
                pagos.forEach { pago: PagoEntity ->
                    when {
                        pago.observacion.contains("PAGO_CHOMPA", ignoreCase = true) -> {
                            pagosChompas += pago.monto
                        }
                        pago.observacion.contains("PAGO_PONCHO", ignoreCase = true) -> {
                            pagosPonchos += pago.monto
                        }
                        else -> {
                            // Si no está especificado, va a bordado/planchado
                            pagosBordadoPlanchado += pago.monto
                        }
                    }
                }
                
                // Calcular saldos netos basados únicamente en registros existentes
                // Los pagos ya están reflejados al eliminar/reducir registros en actualizarRegistrosConPago
                var saldoChompas = 0.0
                registrosChompas.forEach { registro ->
                    saldoChompas += registro.monto
                }
                // Ya no restamos los pagos porque se han eliminado los registros correspondientes
                
                var saldoPonchos = 0.0
                registrosPonchos.forEach { registro ->
                    saldoPonchos += registro.monto
                }
                // Ya no restamos los pagos porque se han eliminado los registros correspondientes

                // Actualizar UI en hilo principal
                withContext(Dispatchers.Main) {
                    // Jefe (Bordado y Planchado)
                    tvJefeSaldos.text = "BP: S/ ${String.format(Locale.getDefault(), "%.2f", saldoBordadoPlanchado)}"
                    val jefeSaldoFinal = saldoBordadoPlanchado + saldoAcumulado - config.adelantos
                    tvJefeSaldo.text = "Saldo: S/ ${String.format(Locale.getDefault(), "%.2f", jefeSaldoFinal)}"
                    if (jefeSaldoFinal > 0) {
                        tvJefeSaldo.setTextColor(resources.getColor(R.color.balance_yellow, theme))
                    } else if (jefeSaldoFinal < 0) {
                        tvJefeSaldo.setTextColor(resources.getColor(R.color.error_color, theme))
                    } else {
                        tvJefeSaldo.setTextColor(resources.getColor(R.color.success_color, theme))
                    }

                    // Total de dinero de los registros del jefe
                    val totalMontoRegistrosJefe = registrosBordadoPlanchado.sumOf { it.monto.toDouble() }
                    tvJefeTotalRegistros.text = "Total registros: S/ ${String.format(Locale.getDefault(), "%.2f", totalMontoRegistrosJefe)}"

                    // Chompas (Clemente)
                    tvChompasNumero.text = registrosChompas.size.toString()
                    tvClementeSaldo.text = "Saldo: S/ ${String.format(Locale.getDefault(), "%.2f", saldoChompas)}"
                    if (saldoChompas > 0) {
                        tvClementeSaldo.setTextColor(resources.getColor(R.color.balance_yellow, theme))
                    } else if (saldoChompas < 0) {
                        tvClementeSaldo.setTextColor(resources.getColor(R.color.error_color, theme))
                    } else {
                        tvClementeSaldo.setTextColor(resources.getColor(R.color.success_color, theme))
                    }

                    // Ponchos (Lalo)
                    tvPonchosNumero.text = registrosPonchos.size.toString()
                    tvLaloSaldo.text = "Saldo: S/ ${String.format(Locale.getDefault(), "%.2f", saldoPonchos)}"
                    if (saldoPonchos > 0) {
                        tvLaloSaldo.setTextColor(resources.getColor(R.color.balance_yellow, theme))
                    } else if (saldoPonchos < 0) {
                        tvLaloSaldo.setTextColor(resources.getColor(R.color.error_color, theme))
                    } else {
                        tvLaloSaldo.setTextColor(resources.getColor(R.color.success_color, theme))
                    }

                    // Saldo general (suma de todos los saldos)
                    val saldoGeneral = jefeSaldoFinal + saldoChompas + saldoPonchos
                    tvSaldoGeneral.text = "S/ ${String.format(Locale.getDefault(), "%.2f", saldoGeneral)}"
                    
                    // Calcular ingresos del día
                    val hoy = Calendar.getInstance()
                    val calInicio = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    val fechaInicio = calInicio.time
                    
                    val calFin = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, 23)
                        set(Calendar.MINUTE, 59)
                        set(Calendar.SECOND, 59)
                        set(Calendar.MILLISECOND, 999)
                    }
                    val fechaFin = calFin.time
                    
                    var ingresos = 0.0
                    // Registros de hoy
                    registrosBordadoPlanchado.forEach { registro ->
                        if (registro.fecha in fechaInicio..fechaFin) {
                            ingresos += registro.monto
                        }
                    }
                    registrosChompas.forEach { registro ->
                        if (registro.fecha in fechaInicio..fechaFin) {
                            ingresos += registro.monto
                        }
                    }
                    registrosPonchos.forEach { registro ->
                        if (registro.fecha in fechaInicio..fechaFin) {
                            ingresos += registro.monto
                        }
                    }
                    
                    tvIngresosHoy.text = "S/ ${String.format(Locale.getDefault(), "%.2f", ingresos)}"
                    
                    // Actualizar contador de registros
                    // Estos elementos no existen en el layout
                    // tvBordadosNumero.text = registrosBordadoPlanchado.count { it.tipo == "BORDADO" }.toString()
                    // tvPlanchadosNumero.text = registrosBordadoPlanchado.count { it.tipo == "PLANCHADO" }.toString()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Error al actualizar saldos: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     * Muestra un diálogo para ver los registros de un tipo de servicio
     */
    private fun mostrarDialogoVerRegistros(tipoServicio: TipoServicio) {
        // Usamos un layout personalizado para mostrar los registros
        val dialogView = layoutInflater.inflate(R.layout.dialog_ver_registros, null)
        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)
        
        // Configuramos el título en el TextView del layout personalizado
        val tvTitle = dialogView.findViewById<TextView>(R.id.tv_dialog_title)
        tvTitle.text = "Registros de ${tipoServicio.name}"
        
        val dialog = builder.create()
        
        lifecycleScope.launch {
            val registros = when (tipoServicio) {
                TipoServicio.BORDADO, TipoServicio.PLANCHADO -> withContext(Dispatchers.IO) {
                    repository.getRegistrosBordadoPlanchado().filter { it.tipo == tipoServicio.name }
                }
                TipoServicio.CHOMPA -> withContext(Dispatchers.IO) {
                    repository.getRegistrosChompas()
                }
                TipoServicio.PONCHO -> withContext(Dispatchers.IO) {
                    repository.getRegistrosPonchos()
                }
            }
            
            val tableRegistros = dialogView.findViewById<TableLayout>(R.id.table_registros)
            val tvNoRecords = dialogView.findViewById<TextView>(R.id.tv_no_records)
            val btnClose = dialogView.findViewById<Button>(R.id.btn_close)
            
            // Guardamos una referencia al contexto para usarla dentro de la coroutina
            val context = this@MainActivity
            
            if (registros.isEmpty()) {
                tableRegistros.visibility = View.GONE
                tvNoRecords.visibility = View.VISIBLE
            } else {
                tvNoRecords.visibility = View.GONE
                tableRegistros.visibility = View.VISIBLE
                
                // Limpiar tabla existente (excepto la primera fila que es el encabezado)
                if (tableRegistros.childCount > 1) {
                    tableRegistros.removeViews(1, tableRegistros.childCount - 1)
                }
                
                // Agregar filas para cada registro
                registros.sortedByDescending { it.fecha }.forEach { registro ->
                    val dateFormat = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
                    val fechaStr = dateFormat.format(registro.fecha)
                    
                    // Crear una nueva fila
                    val tableRow = TableRow(context)
                    tableRow.layoutParams = TableRow.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.WRAP_CONTENT
                    )
                    tableRow.setPadding(5, 10, 5, 10)
                    
                    // Agregar celdas a la fila
                    val tvFecha = TextView(context)
                    tvFecha.text = fechaStr
                    tvFecha.setTextColor(context.resources.getColor(R.color.white, context.theme))
                    tableRow.addView(tvFecha)
                    
                    val tvTipo = TextView(context)
                    tvTipo.text = registro.tipoBordado ?: "-"
                    tvTipo.setTextColor(context.resources.getColor(R.color.white, context.theme))
                    tableRow.addView(tvTipo)
                    
                    val tvCantidad = TextView(context)
                    tvCantidad.text = registro.cantidad.toString()
                    tvCantidad.setTextColor(context.resources.getColor(R.color.white, context.theme))
                    tableRow.addView(tvCantidad)
                    
                    val tvMonto = TextView(context)
                    tvMonto.text = String.format(Locale.getDefault(), "%.2f", registro.monto)
                    tvMonto.setTextColor(context.resources.getColor(R.color.white, context.theme))
                    tableRow.addView(tvMonto)
                    
                    // Agregar la fila a la tabla
                    tableRegistros.addView(tableRow)
                }
            }
            
            btnClose.setOnClickListener {
                dialog.dismiss()
            }
            
            withContext(Dispatchers.Main) {
                dialog.show()
            }
        }
    }
    
    /**
     * Genera un archivo PDF con los registros y pagos
     */
    private fun generarPDF(): Boolean {
        try {
            // Directorio para guardar PDF
            val pdfDir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            pdfDir?.mkdirs()
            
            // Nombre del archivo
            val fileName = "sam_report_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.pdf"
            val pdfFile = File(pdfDir, fileName)
            
            // Crear documento PDF
            val document = Document(PageSize.A4)
            PdfWriter.getInstance(document, FileOutputStream(pdfFile))
            document.open()
            
            // Fuentes
            val fontTitle = Font(Font.FontFamily.HELVETICA, 16f, Font.BOLD)
            val fontSubtitle = Font(Font.FontFamily.HELVETICA, 14f, Font.BOLD)
            val fontText = Font(Font.FontFamily.HELVETICA, 12f)
            val fontBold = Font(Font.FontFamily.HELVETICA, 12f, Font.BOLD)
            
            // Título del documento
            val title = Paragraph("Reporte de Servicios y Pagos", fontTitle)
            title.alignment = Element.ALIGN_CENTER
            title.spacingAfter = 20f
            document.add(title)
            
            // Información general
            document.add(Paragraph("Fecha: ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())}", fontText))
            document.add(Paragraph("Hora: ${SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())}", fontText))
            document.add(Paragraph("\n"))
            
            // Agregar tabla de registros
            document.add(Paragraph("Registros", fontSubtitle))
            
            val tableRegistros = PdfPTable(4)
            tableRegistros.widthPercentage = 100f
            tableRegistros.setWidths(floatArrayOf(3f, 2f, 2f, 2f))
            
            // Cabecera de tabla
            var cell = PdfPCell(Phrase("Fecha", fontBold))
            cell.horizontalAlignment = Element.ALIGN_CENTER
            cell.backgroundColor = BaseColor.LIGHT_GRAY
            tableRegistros.addCell(cell)
            
            cell = PdfPCell(Phrase("Tipo", fontBold))
            cell.horizontalAlignment = Element.ALIGN_CENTER
            cell.backgroundColor = BaseColor.LIGHT_GRAY
            tableRegistros.addCell(cell)
            
            cell = PdfPCell(Phrase("Cantidad", fontBold))
            cell.horizontalAlignment = Element.ALIGN_CENTER
            cell.backgroundColor = BaseColor.LIGHT_GRAY
            tableRegistros.addCell(cell)
            
            cell = PdfPCell(Phrase("Monto (S/.)", fontBold))
            cell.horizontalAlignment = Element.ALIGN_CENTER
            cell.backgroundColor = BaseColor.LIGHT_GRAY
            tableRegistros.addCell(cell)
            
            // Agregar registros
            lifecycleScope.launch {
                val registrosBordadoPlanchado = withContext(Dispatchers.IO) { repository.getRegistrosBordadoPlanchado() }
                val registrosChompas = withContext(Dispatchers.IO) { repository.getRegistrosChompas() }
                val registrosPonchos = withContext(Dispatchers.IO) { repository.getRegistrosPonchos() }
                val pagos = withContext(Dispatchers.IO) { repository.getAllPagos() }
                val config = withContext(Dispatchers.IO) { repository.getConfiguracion() }
                
                val allRegistros = registrosBordadoPlanchado + registrosChompas + registrosPonchos
                val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                
                allRegistros.sortedByDescending { it.fecha }.forEach { registro ->
                    tableRegistros.addCell(dateFormat.format(registro.fecha))
                    tableRegistros.addCell(registro.tipo)
                    tableRegistros.addCell(registro.cantidad.toString())
                    tableRegistros.addCell(String.format(Locale.getDefault(), "%.2f", registro.monto))
                }
                
                document.add(tableRegistros)
                document.add(Paragraph("\n"))
                
                // Tabla de pagos
                document.add(Paragraph("Pagos", fontSubtitle))
                
                val tablePagos = PdfPTable(3)
                tablePagos.widthPercentage = 100f
                tablePagos.setWidths(floatArrayOf(3f, 2f, 4f))
                
                // Cabecera de tabla de pagos
                var cellPago = PdfPCell(Phrase("Fecha", fontBold))
                cellPago.horizontalAlignment = Element.ALIGN_CENTER
                cellPago.backgroundColor = BaseColor.LIGHT_GRAY
                tablePagos.addCell(cellPago)
                
                cellPago = PdfPCell(Phrase("Monto (S/.)", fontBold))
                cellPago.horizontalAlignment = Element.ALIGN_CENTER
                cellPago.backgroundColor = BaseColor.LIGHT_GRAY
                tablePagos.addCell(cellPago)
                
                cellPago = PdfPCell(Phrase("Observación", fontBold))
                cellPago.horizontalAlignment = Element.ALIGN_CENTER
                cellPago.backgroundColor = BaseColor.LIGHT_GRAY
                tablePagos.addCell(cellPago)
                
                pagos.sortedByDescending { it.fecha }.forEach { pago ->
                    tablePagos.addCell(dateFormat.format(pago.fecha))
                    tablePagos.addCell(String.format(Locale.getDefault(), "%.2f", pago.monto))
                    tablePagos.addCell(pago.observacion ?: "")
                }
                
                document.add(tablePagos)
                document.add(Paragraph("\n"))
                
                // Resumen
                document.add(Paragraph("Resumen", fontSubtitle))
                
                val tableSummary = PdfPTable(2)
                tableSummary.widthPercentage = 70f
                tableSummary.horizontalAlignment = Element.ALIGN_LEFT
                
                tableSummary.addCell(PdfPCell(Phrase("Concepto", fontBold)).apply {
                    backgroundColor = BaseColor.LIGHT_GRAY
                    horizontalAlignment = Element.ALIGN_CENTER
                })
                
                tableSummary.addCell(PdfPCell(Phrase("Monto (S/.)", fontBold)).apply {
                    backgroundColor = BaseColor.LIGHT_GRAY
                    horizontalAlignment = Element.ALIGN_CENTER
                })
                
                // Calcular totales
                val totalRegistros = allRegistros.sumOf { it.monto.toDouble() }
                val totalPagos = pagos.sumOf { it.monto.toDouble() }
                val saldoTotal = totalRegistros - totalPagos
                
                tableSummary.addCell("Total registros")
                tableSummary.addCell(String.format(Locale.getDefault(), "%.2f", totalRegistros))
                
                tableSummary.addCell("Total pagos")
                tableSummary.addCell(String.format(Locale.getDefault(), "%.2f", totalPagos))
                
                tableSummary.addCell(PdfPCell(Phrase("Saldo", fontBold)))
                tableSummary.addCell(PdfPCell(Phrase(String.format(Locale.getDefault(), "%.2f", saldoTotal), fontBold)))
                
                document.add(tableSummary)
                
                document.close()
                
                // Abrir el PDF
                abrirPDF(pdfFile)
            }
            
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }
    
    /**
     * Abre el archivo PDF generado
     */
    private fun abrirPDF(file: File) {
        val uri = FileProvider.getUriForFile(this, "${applicationContext.packageName}.provider", file)
        
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(uri, "application/pdf")
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "No se encontró una aplicación para abrir PDF", Toast.LENGTH_LONG).show()
        }
    }
    
    /**
     * Mostrar diálogo de ver PDF
     */
    private fun mostrarDialogoVerPDF() {
        val pdfDir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        val files = pdfDir?.listFiles { _, name -> name.endsWith(".pdf") }?.sortedByDescending { it.lastModified() }
        
        if (files.isNullOrEmpty()) {
            Toast.makeText(this, "No hay archivos PDF para mostrar", Toast.LENGTH_SHORT).show()
            return
        }
        
        val fileNames = files.map { it.name }.toTypedArray()
        
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Seleccione un PDF")
        builder.setItems(fileNames) { _, which ->
            abrirPDF(files[which])
        }
        builder.setNegativeButton("Cancelar", null)
        builder.show()
    }
}