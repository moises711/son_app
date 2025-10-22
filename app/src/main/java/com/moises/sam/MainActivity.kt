package com.moises.sam

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import android.widget.ProgressBar
import android.animation.ObjectAnimator
import android.animation.AnimatorSet
import android.view.animation.OvershootInterpolator
import android.widget.ImageView
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
    private lateinit var tvMetaIngresos: TextView
    private lateinit var progressMetaIngresos: ProgressBar
    private lateinit var tvMetaDetalle: TextView
    private lateinit var successOverlay: View
    private lateinit var successCheck: ImageView
    
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
    tvMetaIngresos = findViewById(R.id.tv_meta_ingresos)
    progressMetaIngresos = findViewById(R.id.progress_meta_ingresos)
    tvMetaDetalle = findViewById(R.id.tv_meta_detalle)
    successOverlay = findViewById(R.id.success_check_overlay)
    successCheck = findViewById(R.id.iv_success_check)
        
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
                mostrarCheckExito()
            }
        }
        
        // Botón para registrar planchado
        btnPlanchado.setOnClickListener {
            mostrarDialogoRegistroServicio(TipoServicio.PLANCHADO) { cantidad ->
                registrarServicio(TipoServicio.PLANCHADO, cantidad)
                mostrarCheckExito()
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
            mostrarDialogoVerRegistrosJefe()
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
                mostrarCheckExito()
            }
        }

        // Botón para registrar servicio de Lalo
        val btnRegistrarLalo = findViewById<MaterialButton>(R.id.btn_registrar_lalo)
        btnRegistrarLalo.setOnClickListener {
            mostrarDialogoRegistroServicio(TipoServicio.PONCHO) { cantidad ->
                registrarServicio(TipoServicio.PONCHO, cantidad)
                mostrarCheckExito()
            }
        }

        // MODAL: Adelantos para Jefe (ver lista)
        btnAdelantos.setOnClickListener {
            mostrarDialogoListaAdelantos()
        }

        // Botón para generar PDF SOLO con registros de jefe
        btnDescargarPdf.setOnClickListener {
            // Modal personalizado con fondo propio (no transparente)
            val builder = AlertDialog.Builder(this)
            val view = layoutInflater.inflate(R.layout.dialog_pago_pdf, null)
            builder.setView(view)
            val dialog = builder.create()
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.window?.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            dialog.window?.attributes = dialog.window?.attributes?.apply { dimAmount = 0.6f }

            val etMonto = view.findViewById<TextInputEditText>(R.id.et_pdf_monto)
            val btnCancelar = view.findViewById<Button>(R.id.btn_pdf_cancelar)
            val btnRegistrar = view.findViewById<Button>(R.id.btn_pdf_registrar)

            btnCancelar.setOnClickListener { dialog.dismiss() }

            btnRegistrar.setOnClickListener {
                val monto = etMonto.text?.toString()?.toDoubleOrNull() ?: 0.0
                if (monto <= 0.0) {
                    etMonto.error = "Ingrese un monto válido"
                    return@setOnClickListener
                }
                lifecycleScope.launch {
                    // Registrar pago etiquetado explícitamente como del JEFE para que aparezca en el PDF del jefe
                    repository.savePago(Pago(
                        monto = monto,
                        observacion = "PAGO_JEFE: Pago registrado antes de generar PDF"
                    ))

                    mostrarCheckExito()
                    // Generar PDF con contenido (y cerrar ciclo del Jefe)
                    val exito = generarPDF(monto)

                    if (exito) {
                        Toast.makeText(this@MainActivity, "PDF generado correctamente", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@MainActivity, "Error al generar PDF", Toast.LENGTH_SHORT).show()
                    }

                    actualizarSaldos()
                    dialog.dismiss()
                }
            }

            dialog.show()
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
    dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    dialog.window?.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
    dialog.window?.attributes = dialog.window?.attributes?.apply { dimAmount = 0.6f }
    dialog.window?.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
    dialog.window?.attributes = dialog.window?.attributes?.apply { dimAmount = 0.6f }

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
            // Ajuste por crédito (excedente de pagos) para CHOMPA y PONCHO
            val totalARegistrar = withContext(Dispatchers.IO) {
                if (tipo == TipoServicio.CHOMPA || tipo == TipoServicio.PONCHO) {
                    val credito = repository.getCreditoMonto(tipo)
                    if (credito > 0.0) {
                        val restante = monto - credito
                        when {
                            restante <= 0.0 -> {
                                // Todo cubierto por crédito: consumir y registrar total 0
                                repository.addCredito(tipo, -monto)
                                0.0
                            }
                            else -> {
                                // Parcialmente cubierto: consumir crédito y registrar el resto
                                repository.addCredito(tipo, -credito)
                                restante
                            }
                        }
                    } else monto
                } else monto
            }

            // Crear objeto de registro (monto ajustado por crédito si aplica)
            val registro = Registro(
                tipo = tipo,
                cantidad = cantidad,
                total = totalARegistrar
            )
            
            // Guardar en la base de datos
            repository.saveRegistro(registro)
            
            // Actualizar UI en hilo principal
            withContext(Dispatchers.Main) {
                actualizarSaldos()
                mostrarCheckExito()
            }
        }
    }
        private fun mostrarCheckExito() {
            try {
                successOverlay.visibility = View.VISIBLE
                successOverlay.alpha = 0f
                successOverlay.scaleX = 0.7f
                successOverlay.scaleY = 0.7f

                val fadeIn = ObjectAnimator.ofFloat(successOverlay, View.ALPHA, 0f, 1f).apply { duration = 340 }
                val scaleUpX = ObjectAnimator.ofFloat(successOverlay, View.SCALE_X, 0.7f, 1.12f).apply {
                    duration = 380
                    interpolator = OvershootInterpolator(2.2f)
                }
                val scaleUpY = ObjectAnimator.ofFloat(successOverlay, View.SCALE_Y, 0.7f, 1.12f).apply {
                    duration = 380
                    interpolator = OvershootInterpolator(2.2f)
                }

                val hold = ObjectAnimator.ofFloat(successOverlay, View.ALPHA, 1f, 1f).apply { duration = 1100 }

                val fadeOut = ObjectAnimator.ofFloat(successOverlay, View.ALPHA, 1f, 0f).apply { duration = 300 }
                val scaleDownX = ObjectAnimator.ofFloat(successOverlay, View.SCALE_X, 1.12f, 0.85f).apply { duration = 300 }
                val scaleDownY = ObjectAnimator.ofFloat(successOverlay, View.SCALE_Y, 1.12f, 0.85f).apply { duration = 300 }

                val intro = AnimatorSet().apply { playTogether(fadeIn, scaleUpX, scaleUpY) }
                val outro = AnimatorSet().apply { playTogether(fadeOut, scaleDownX, scaleDownY) }
                val total = AnimatorSet().apply { playSequentially(intro, hold, outro) }
                total.addListener(object : android.animation.AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: android.animation.Animator) {
                        successOverlay.visibility = View.GONE
                    }
                })
                total.start()
            } catch (e: Exception) {
                // Fallback silencioso si la vista no está disponible
            }
        }
    
    private fun mostrarDialogoPagoAdelanto(titulo: String, tipoServicio: TipoServicio) {
        val builder = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.dialog_pago_adelanto, null)
        builder.setView(view)

    val dialog = builder.create()
    dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    dialog.window?.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
    dialog.window?.attributes = dialog.window?.attributes?.apply { dimAmount = 0.6f }

    // Usar los IDs correctos del layout dialog_pago_adelanto
    val tvTitulo = view.findViewById<TextView>(R.id.tv_pago_title)
        val etMonto = view.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_pago_monto)
        val etObservacion = view.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_pago_observacion)
        val btnRegistrar = view.findViewById<Button>(R.id.btn_pago_registrar)
        val btnCancelar = view.findViewById<Button>(R.id.btn_pago_cancelar)
        
    // Configuración inicial: título dentro del layout (evitar barra de título del sistema)
    tvTitulo.text = titulo
        
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
                        mostrarCheckExito()
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
        // Convertido en diálogo de Meta de Ingresos
        val builder = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.dialog_meta_ingresos, null)
        builder.setView(view)

    val dialog = builder.create()
    dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    dialog.window?.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
    dialog.window?.attributes = dialog.window?.attributes?.apply { dimAmount = 0.6f }
    dialog.window?.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
    dialog.window?.attributes = dialog.window?.attributes?.apply { dimAmount = 0.6f }

    val etMeta = view.findViewById<TextInputEditText>(R.id.et_meta_monto)
    val rgPeriodo = view.findViewById<android.widget.RadioGroup>(R.id.rg_meta_periodo)
    val rbDia = view.findViewById<android.widget.RadioButton>(R.id.rb_meta_dia)
    val rbSemana = view.findViewById<android.widget.RadioButton>(R.id.rb_meta_semana)
    val rbMes = view.findViewById<android.widget.RadioButton>(R.id.rb_meta_mes)
        val btnGuardar = view.findViewById<Button>(R.id.btn_meta_guardar)
        val btnCancelar = view.findViewById<Button>(R.id.btn_meta_cancelar)

        lifecycleScope.launch {
            val config = withContext(Dispatchers.IO) { repository.getConfiguracion() }
            withContext(Dispatchers.Main) {
                etMeta.setText(String.format(Locale.getDefault(), "%.2f", config.metaIngresos))
                when (config.metaPeriodo) {
                    "SEMANA" -> rbSemana.isChecked = true
                    "MES" -> rbMes.isChecked = true
                    else -> rbDia.isChecked = true
                }
            }
        }

        btnCancelar.setOnClickListener { dialog.dismiss() }

        btnGuardar.setOnClickListener {
            val metaText = etMeta.text?.toString()?.trim().orEmpty()
            val meta = metaText.toDoubleOrNull()
            if (meta == null || meta < 0) {
                etMeta.error = "Ingrese un monto válido"
                return@setOnClickListener
            }
            lifecycleScope.launch {
                val config = withContext(Dispatchers.IO) { repository.getConfiguracion() }
                val periodo = when (rgPeriodo.checkedRadioButtonId) {
                    R.id.rb_meta_semana -> "SEMANA"
                    R.id.rb_meta_mes -> "MES"
                    else -> "DIA"
                }
                val updated = config.copy(metaIngresos = meta, metaPeriodo = periodo)
                withContext(Dispatchers.IO) { repository.updateConfig(updated) }
                withContext(Dispatchers.Main) {
                    actualizarSaldos()
                    Toast.makeText(this@MainActivity, "Meta guardada", Toast.LENGTH_SHORT).show()
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
    dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    dialog.window?.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
    dialog.window?.attributes = dialog.window?.attributes?.apply { dimAmount = 0.6f }
    dialog.window?.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
    dialog.window?.attributes = dialog.window?.attributes?.apply { dimAmount = 0.6f }

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
                            mostrarCheckExito()
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

    /**
     * Muestra un diálogo con la lista de adelantos registrados
     */
    private fun mostrarDialogoListaAdelantos() {
        val view = layoutInflater.inflate(R.layout.dialog_ver_adelantos, null)
        val builder = AlertDialog.Builder(this)
        builder.setView(view)
    val dialog = builder.create()
    dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val tableAdelantos = view.findViewById<TableLayout>(R.id.table_adelantos)
        val tvNoRecords = view.findViewById<TextView>(R.id.tv_no_adelantos)
        val btnClose = view.findViewById<Button>(R.id.btn_close)
        val etMonto = view.findViewById<TextInputEditText>(R.id.et_adelanto_monto)
        val etObs = view.findViewById<TextInputEditText>(R.id.et_adelanto_observacion)
        val btnAdd = view.findViewById<Button>(R.id.btn_add_adelanto)

        lifecycleScope.launch {
            val adelantos = withContext(Dispatchers.IO) { repository.getAllAdelantos() }

            withContext(Dispatchers.Main) {
                // Limpiar filas anteriores excepto el header (index 0)
                while (tableAdelantos.childCount > 1) {
                    tableAdelantos.removeViewAt(1)
                }

                if (adelantos.isEmpty()) {
                    tvNoRecords.visibility = View.VISIBLE
                } else {
                    tvNoRecords.visibility = View.GONE
                    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

                    adelantos.forEach { a ->
                        val row = TableRow(this@MainActivity)

                        val tvFecha = TextView(this@MainActivity)
                        tvFecha.text = dateFormat.format(a.fecha)
                        tvFecha.setTextColor(resources.getColor(R.color.text_primary, theme))
                        row.addView(tvFecha)

                        val tvObs = TextView(this@MainActivity)
                        tvObs.text = a.observacion ?: ""
                        tvObs.setTextColor(resources.getColor(R.color.text_primary, theme))
                        row.addView(tvObs)

                        val tvMonto = TextView(this@MainActivity)
                        tvMonto.text = String.format(Locale.getDefault(), "%.2f", a.monto)
                        tvMonto.setTextColor(resources.getColor(R.color.balance_blue, theme))
                        row.addView(tvMonto)

                        tableAdelantos.addView(row)
                    }
                }
            }
        }

        btnAdd.setOnClickListener {
            val montoText = etMonto.text?.toString().orEmpty()
            val monto = montoText.toDoubleOrNull()
            if (monto == null || monto <= 0.0) {
                etMonto.error = "Ingrese un monto válido"
                return@setOnClickListener
            }
            val observacion = etObs.text?.toString().orEmpty()

            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    repository.saveAdelanto(Adelanto(monto = monto, observacion = "ADELANTO_JEFE: $observacion"))
                }
                withContext(Dispatchers.Main) {
                    // Mostrar éxito y cerrar el diálogo tras registrar el adelanto
                    mostrarCheckExito()
                    dialog.dismiss()
                    // Actualizar saldos del header
                    lifecycleScope.launch { actualizarSaldos() }
                }
            }
        }

        btnClose.setOnClickListener { dialog.dismiss() }
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
                
                // Si queda monto pendiente luego de cubrir todos los registros, registrar como CRÉDITO a favor
                if (montoPendiente > 0) {
                    if (tipoServicio == TipoServicio.CHOMPA || tipoServicio == TipoServicio.PONCHO) {
                        repository.addCredito(tipoServicio, montoPendiente)
                    }
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
                val totalAdelantos = repository.getTotalAdelantos()

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
                // Restar créditos (excedentes de pago) para Clemente
                val creditoChompa = repository.getCreditoMonto(TipoServicio.CHOMPA)
                saldoChompas -= creditoChompa
                // Ya no restamos los pagos porque se han eliminado los registros correspondientes
                
                var saldoPonchos = 0.0
                registrosPonchos.forEach { registro ->
                    saldoPonchos += registro.monto
                }
                // Restar créditos (excedentes de pago) para Lalo
                val creditoPoncho = repository.getCreditoMonto(TipoServicio.PONCHO)
                saldoPonchos -= creditoPoncho
                // Ya no restamos los pagos porque se han eliminado los registros correspondientes

                // Actualizar UI en hilo principal
                withContext(Dispatchers.Main) {
                    // Jefe (Bordado y Planchado)
                    tvJefeSaldos.text = "BP: S/ ${String.format(Locale.getDefault(), "%.2f", saldoBordadoPlanchado)}"
                    // Saldo del Jefe: mantener fijo y solo actualizar al descargar PDF.
                    // Por lo tanto, mostramos únicamente el saldo acumulado histórico.
                    val jefeSaldoFinal = saldoAcumulado
                    tvJefeSaldo.text = "Saldo: S/ ${String.format(Locale.getDefault(), "%.2f", jefeSaldoFinal)}"
                    if (jefeSaldoFinal > 0) {
                        tvJefeSaldo.setTextColor(resources.getColor(R.color.balance_green, theme))
                    } else if (jefeSaldoFinal < 0) {
                        tvJefeSaldo.setTextColor(resources.getColor(R.color.error_color, theme))
                    } else {
                        tvJefeSaldo.setTextColor(resources.getColor(R.color.balance_yellow, theme))
                    }

                    // Total de dinero de los registros del jefe (oculto en UI)
                    val totalMontoRegistrosJefe = registrosBordadoPlanchado.sumOf { it.monto.toDouble() }
                    tvJefeTotalRegistros.text = "Total registros: S/ ${String.format(Locale.getDefault(), "%.2f", totalMontoRegistrosJefe)}"

                    // Chompas (Clemente)
                    tvChompasNumero.text = registrosChompas.size.toString()
                    tvClementeSaldo.text = "Saldo: S/ ${String.format(Locale.getDefault(), "%.2f", saldoChompas)}"
                    if (saldoChompas > 0) {
                        tvClementeSaldo.setTextColor(resources.getColor(R.color.balance_green, theme))
                    } else if (saldoChompas < 0) {
                        tvClementeSaldo.setTextColor(resources.getColor(R.color.error_color, theme))
                    } else {
                        tvClementeSaldo.setTextColor(resources.getColor(R.color.balance_yellow, theme))
                    }

                    // Ponchos (Lalo)
                    tvPonchosNumero.text = registrosPonchos.size.toString()
                    tvLaloSaldo.text = "Saldo: S/ ${String.format(Locale.getDefault(), "%.2f", saldoPonchos)}"
                    if (saldoPonchos > 0) {
                        tvLaloSaldo.setTextColor(resources.getColor(R.color.balance_green, theme))
                    } else if (saldoPonchos < 0) {
                        tvLaloSaldo.setTextColor(resources.getColor(R.color.error_color, theme))
                    } else {
                        tvLaloSaldo.setTextColor(resources.getColor(R.color.balance_yellow, theme))
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

                    // Calcular ingresos de la semana (Lun-Dom)
                    val calInicioSemana = Calendar.getInstance().apply {
                        firstDayOfWeek = Calendar.MONDAY
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                        set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                    }
                    val inicioSemana = calInicioSemana.time
                    val calFinSemana = Calendar.getInstance().apply {
                        firstDayOfWeek = Calendar.MONDAY
                        set(Calendar.HOUR_OF_DAY, 23)
                        set(Calendar.MINUTE, 59)
                        set(Calendar.SECOND, 59)
                        set(Calendar.MILLISECOND, 999)
                        set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
                    }
                    val finSemana = calFinSemana.time

                    var ingresosSemana = 0.0
                    val acumSiEnSemana: (Date, Double) -> Unit = { fecha, monto ->
                        if (fecha in inicioSemana..finSemana) ingresosSemana += monto
                    }
                    registrosBordadoPlanchado.forEach { acumSiEnSemana(it.fecha, it.monto) }
                    registrosChompas.forEach { acumSiEnSemana(it.fecha, it.monto) }
                    registrosPonchos.forEach { acumSiEnSemana(it.fecha, it.monto) }

                    // Calcular ingresos del mes actual
                    val calInicioMes = Calendar.getInstance().apply {
                        set(Calendar.DAY_OF_MONTH, 1)
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    val inicioMes = calInicioMes.time
                    val calFinMes = Calendar.getInstance().apply {
                        set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
                        set(Calendar.HOUR_OF_DAY, 23)
                        set(Calendar.MINUTE, 59)
                        set(Calendar.SECOND, 59)
                        set(Calendar.MILLISECOND, 999)
                    }
                    val finMes = calFinMes.time

                    var ingresosMes = 0.0
                    val acumSiEnMes: (Date, Double) -> Unit = { fecha, monto ->
                        if (fecha in inicioMes..finMes) ingresosMes += monto
                    }
                    registrosBordadoPlanchado.forEach { acumSiEnMes(it.fecha, it.monto) }
                    registrosChompas.forEach { acumSiEnMes(it.fecha, it.monto) }
                    registrosPonchos.forEach { acumSiEnMes(it.fecha, it.monto) }

                    // Actualizar progreso de meta de ingresos (color y faltante)
                    val meta = config.metaIngresos
                    if (meta > 0) {
                        val ingresosPeriodo = when (config.metaPeriodo) {
                            "SEMANA" -> ingresosSemana
                            "MES" -> ingresosMes
                            else -> ingresos
                        }
                        val porcentajeRaw = (ingresosPeriodo / meta) * 100
                        val porcentaje = porcentajeRaw.coerceIn(0.0, 100.0)
                        progressMetaIngresos.max = 100
                        progressMetaIngresos.progress = porcentaje.toInt()

                        // Texto con faltante o superávit
                        val diferencia = meta - ingresosPeriodo
                        val textoFaltante = if (diferencia > 0) {
                            "Faltan S/ ${String.format(Locale.getDefault(), "%.2f", diferencia)}"
                        } else {
                            val superavit = -diferencia
                            "¡Meta superada por S/ ${String.format(Locale.getDefault(), "%.2f", superavit)}!"
                        }
                        val etiqueta = when (config.metaPeriodo) {
                            "SEMANA" -> "semanal"
                            "MES" -> "mensual"
                            else -> "diaria"
                        }
                        tvMetaIngresos.text = "Meta $etiqueta: S/ ${String.format(Locale.getDefault(), "%.2f", meta)}  (" +
                                "${String.format(Locale.getDefault(), "%.0f", porcentaje)}% - $textoFaltante)"

                        // Detalle bajo la barra
                        tvMetaDetalle.text = "Ingresos del periodo: S/ ${String.format(Locale.getDefault(), "%.2f", ingresosPeriodo)} / S/ ${String.format(Locale.getDefault(), "%.2f", meta)}"

                        // Color dinámico de la barra según porcentaje
                        val colorRes = when {
                            porcentajeRaw < 50.0 -> R.color.error_color
                            porcentajeRaw < 80.0 -> R.color.balance_yellow
                            else -> R.color.balance_green
                        }
                        // setTint compatible
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                            progressMetaIngresos.progressTintList = android.content.res.ColorStateList.valueOf(resources.getColor(colorRes, theme))
                        }

                        tvMetaIngresos.visibility = View.VISIBLE
                        progressMetaIngresos.visibility = View.VISIBLE
                        tvMetaDetalle.visibility = View.VISIBLE
                    } else {
                        tvMetaIngresos.text = "Meta no establecida"
                        progressMetaIngresos.progress = 0
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                            progressMetaIngresos.progressTintList = android.content.res.ColorStateList.valueOf(resources.getColor(R.color.primary_700, theme))
                        }
                        tvMetaIngresos.visibility = View.VISIBLE
                        progressMetaIngresos.visibility = View.VISIBLE
                        tvMetaDetalle.text = ""
                        tvMetaDetalle.visibility = View.GONE
                    }
                    
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
    dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        
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
                    tvFecha.setTextColor(context.resources.getColor(R.color.text_primary, context.theme))
                    tableRow.addView(tvFecha)
                    
                    val tvTipo = TextView(context)
                    tvTipo.text = registro.tipoBordado ?: "-"
                    tvTipo.setTextColor(context.resources.getColor(R.color.text_primary, context.theme))
                    tableRow.addView(tvTipo)
                    
                    val tvCantidad = TextView(context)
                    tvCantidad.text = registro.cantidad.toString()
                    tvCantidad.setTextColor(context.resources.getColor(R.color.text_primary, context.theme))
                    tableRow.addView(tvCantidad)
                    
                    val tvMonto = TextView(context)
                    tvMonto.text = String.format(Locale.getDefault(), "%.2f", registro.monto)
                    tvMonto.setTextColor(context.resources.getColor(R.color.text_primary, context.theme))
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
     * Muestra un diálogo para ver TODOS los registros del Jefe (Bordado y Planchado)
     */
    private fun mostrarDialogoVerRegistrosJefe() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_ver_registros, null)
        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)

        // Título personalizado
        val tvTitle = dialogView.findViewById<TextView>(R.id.tv_dialog_title)
        tvTitle.text = "Registros del Jefe (Bordado y Planchado)"

    val dialog = builder.create()
    dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    dialog.window?.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
    dialog.window?.attributes = dialog.window?.attributes?.apply { dimAmount = 0.6f }

        lifecycleScope.launch {
            val registros = withContext(Dispatchers.IO) {
                repository.getRegistrosBordadoPlanchado()
            }

            val tableRegistros = dialogView.findViewById<TableLayout>(R.id.table_registros)
            val tvNoRecords = dialogView.findViewById<TextView>(R.id.tv_no_records)
            val btnClose = dialogView.findViewById<Button>(R.id.btn_close)

            val context = this@MainActivity

            if (registros.isEmpty()) {
                tableRegistros.visibility = View.GONE
                tvNoRecords.visibility = View.VISIBLE
            } else {
                tvNoRecords.visibility = View.GONE
                tableRegistros.visibility = View.VISIBLE

                // Limpiar tabla existente (excepto encabezado)
                if (tableRegistros.childCount > 1) {
                    tableRegistros.removeViews(1, tableRegistros.childCount - 1)
                }

                registros.sortedByDescending { it.fecha }.forEach { registro ->
                    val dateFormat = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
                    val fechaStr = dateFormat.format(registro.fecha)

                    val tableRow = TableRow(context)
                    tableRow.layoutParams = TableRow.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.WRAP_CONTENT
                    )
                    tableRow.setPadding(5, 10, 5, 10)

                    val tvFecha = TextView(context)
                    tvFecha.text = fechaStr
                    tvFecha.setTextColor(context.resources.getColor(R.color.text_primary, context.theme))
                    tableRow.addView(tvFecha)

                    val tvTipo = TextView(context)
                    // Mostrar subtipo si existe; si no, el tipo (BORDADO/PLANCHADO)
                    tvTipo.text = registro.tipoBordado ?: registro.tipo
                    tvTipo.setTextColor(context.resources.getColor(R.color.text_primary, context.theme))
                    tableRow.addView(tvTipo)

                    val tvCantidad = TextView(context)
                    tvCantidad.text = registro.cantidad.toString()
                    tvCantidad.setTextColor(context.resources.getColor(R.color.text_primary, context.theme))
                    tableRow.addView(tvCantidad)

                    val tvMonto = TextView(context)
                    tvMonto.text = String.format(Locale.getDefault(), "%.2f", registro.monto)
                    tvMonto.setTextColor(context.resources.getColor(R.color.text_primary, context.theme))
                    tableRow.addView(tvMonto)

                    tableRegistros.addView(tableRow)
                }
            }

            btnClose.setOnClickListener { dialog.dismiss() }

            withContext(Dispatchers.Main) { dialog.show() }
        }
    }
    
    /**
     * Genera un archivo PDF con los registros y pagos
     */
    private fun generarPDF(montoPagoJefe: Double = 0.0): Boolean {
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
            
            // Título del documento (solo Jefe)
            val title = Paragraph("Reporte del Jefe (Bordado y Planchado)", fontTitle)
            title.alignment = Element.ALIGN_CENTER
            title.spacingAfter = 20f
            document.add(title)
            
            // Información general
            document.add(Paragraph("Fecha: ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())}", fontText))
            document.add(Paragraph("Hora: ${SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())}", fontText))
            document.add(Paragraph("\n"))
            
            // Sección de Registros
            document.add(Paragraph("Registros", fontSubtitle))
            
            // Agregar registros (solo Jefe: Bordado/Planchado)
            lifecycleScope.launch {
                val registrosBordadoPlanchado = withContext(Dispatchers.IO) { repository.getRegistrosBordadoPlanchado() }
                val config = withContext(Dispatchers.IO) { repository.getConfiguracion() }
                val saldoAnterior = withContext(Dispatchers.IO) { repository.getSaldoAcumulado() }
                // Obtener adelantos para mostrar resumen (no tabla)
                val adelantos = withContext(Dispatchers.IO) { repository.getAllAdelantos() }
                val totalAdelantosMonto = adelantos.sumOf { it.monto }
                val totalAdelantosCantidad = adelantos.size
                
                val allRegistros = registrosBordadoPlanchado
                val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

                fun addSection(titleSec: String, lista: List<RegistroEntity>) {
                    if (lista.isEmpty()) return
                    // Título de sección
                    val secTitle = Paragraph(titleSec, fontBold)
                    secTitle.spacingBefore = 10f
                    secTitle.spacingAfter = 6f
                    document.add(secTitle)

                    val table = PdfPTable(3)
                    table.widthPercentage = 100f
                    table.setWidths(floatArrayOf(3f, 2f, 2f))

                    // Cabecera
                    var h = PdfPCell(Phrase("Fecha", fontBold))
                    h.horizontalAlignment = Element.ALIGN_CENTER
                    h.backgroundColor = BaseColor.LIGHT_GRAY
                    table.addCell(h)

                    h = PdfPCell(Phrase("Cantidad", fontBold))
                    h.horizontalAlignment = Element.ALIGN_CENTER
                    h.backgroundColor = BaseColor.LIGHT_GRAY
                    table.addCell(h)

                    h = PdfPCell(Phrase("Monto (S/.)", fontBold))
                    h.horizontalAlignment = Element.ALIGN_CENTER
                    h.backgroundColor = BaseColor.LIGHT_GRAY
                    table.addCell(h)

                    // Filas
                    lista.sortedByDescending { it.fecha }.forEachIndexed { index, r ->
                        val bg = if (index % 2 == 0) BaseColor.WHITE else BaseColor(0xF7, 0xF7, 0xF7)
                        var c = PdfPCell(Phrase(dateFormat.format(r.fecha), fontText))
                        c.backgroundColor = bg
                        table.addCell(c)

                        c = PdfPCell(Phrase(r.cantidad.toString(), fontText))
                        c.backgroundColor = bg
                        table.addCell(c)

                        c = PdfPCell(Phrase(String.format(Locale.getDefault(), "%.2f", r.monto), fontText))
                        c.backgroundColor = bg
                        table.addCell(c)
                    }

                    document.add(table)
                }

                // Agrupaciones
                val bordado = allRegistros.filter { it.tipo.equals("BORDADO", ignoreCase = true) }
                val troyer = bordado.filter { it.tipoBordado?.equals("TROYER", ignoreCase = true) == true }
                val molde = bordado.filter { it.tipoBordado?.equals("MOLDE", ignoreCase = true) == true }
                val chompasLana = bordado.filter { it.tipoBordado?.equals("CHOMPAS_LANA", ignoreCase = true) == true }
                val planchado = allRegistros.filter { it.tipo.equals("PLANCHADO", ignoreCase = true) }

                addSection("Bordado — Troyer", troyer)
                addSection("Bordado — Molde", molde)
                addSection("Bordado — Chompas de lana", chompasLana)
                addSection("Planchado", planchado)

                document.add(Paragraph("\n"))
                
                // Resumen
                document.add(Paragraph("Resumen (Jefe)", fontSubtitle))
                
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
                val pagoAplicado = montoPagoJefe
                val adelantosAplicados = totalAdelantosMonto
                val saldoNuevo = saldoAnterior + totalRegistros - pagoAplicado - adelantosAplicados

                tableSummary.addCell("Saldo anterior")
                tableSummary.addCell(String.format(Locale.getDefault(), "%.2f", saldoAnterior))

                tableSummary.addCell("Total registros (periodo)")
                tableSummary.addCell(String.format(Locale.getDefault(), "%.2f", totalRegistros))

                tableSummary.addCell("Pago aplicado")
                tableSummary.addCell(String.format(Locale.getDefault(), "%.2f", pagoAplicado))

                tableSummary.addCell("Adelantos aplicados (${totalAdelantosCantidad})")
                tableSummary.addCell(String.format(Locale.getDefault(), "%.2f", adelantosAplicados))

                tableSummary.addCell(PdfPCell(Phrase("Saldo nuevo", fontBold)))
                tableSummary.addCell(PdfPCell(Phrase(String.format(Locale.getDefault(), "%.2f", saldoNuevo), fontBold)))

                // Nota: los adelantos ya fueron aplicados al saldo nuevo y se eliminarán del periodo
                // (Se mantiene solo como comentario visual en el PDF)
                
                document.add(tableSummary)

                document.close()

                // Abrir el PDF
                abrirPDF(pdfFile)

                // Después de generar y abrir el PDF: cerrar ciclo del Jefe
                // 1) Actualizar saldo acumulado con el total de registros del periodo menos el pago realizado ahora
                val saldoAcumuladoActual = withContext(Dispatchers.IO) { repository.getSaldoAcumulado() }
                val nuevoSaldoAcumulado = saldoAcumuladoActual + totalRegistros - montoPagoJefe - totalAdelantosMonto
                withContext(Dispatchers.IO) {
                    repository.actualizarSaldoAcumulado(nuevoSaldoAcumulado)
                    // 2) Borrar todos los registros de Bordado/Planchado para iniciar nuevo periodo
                    repository.eliminarRegistrosBordadoPlanchado()
                    // 3) Borrar todos los adelantos del periodo
                    repository.eliminarTodosAdelantos()
                }

                // 4) Refrescar UI en tiempo real
                withContext(Dispatchers.Main) {
                    actualizarSaldos()
                    Toast.makeText(this@MainActivity, "Datos del Jefe actualizados", Toast.LENGTH_SHORT).show()
                }
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
        val uri = FileProvider.getUriForFile(this, "${applicationContext.packageName}.fileprovider", file)
        
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