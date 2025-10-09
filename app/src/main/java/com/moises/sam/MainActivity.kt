package com.moises.sam

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.moises.sam.data.SamRepository
import com.moises.sam.model.Adelanto
import com.moises.sam.model.Pago
import com.moises.sam.model.Registro
import com.moises.sam.model.TipoBordado
import com.moises.sam.model.toRegistro
import com.moises.sam.model.TipoServicio
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {
    /**
     * Muestra un diálogo con los registros en una tabla según el tipo de servicio
     */
    private fun mostrarDialogoVerRegistros(tipoServicio: TipoServicio) {
        val builder = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.dialog_ver_registros, null)
        builder.setView(view)

        val dialog = builder.create()
        val tableLayout = view.findViewById<TableLayout>(R.id.table_registros)
        val tvNoRecords = view.findViewById<TextView>(R.id.tv_no_records)
        val btnClose = view.findViewById<Button>(R.id.btn_close)
        val tvTitle = view.findViewById<TextView>(R.id.tv_dialog_title)

        // Título según tipo
        tvTitle.text = when (tipoServicio) {
            TipoServicio.BORDADO, TipoServicio.PLANCHADO -> "Registros de Bordado/Planchado"
            TipoServicio.CHOMPA -> "Registros de Chompas"
            TipoServicio.PONCHO -> "Registros de Ponchos"
        }

        // Cargar registros según tipo
        lifecycleScope.launch {
            val registros: List<com.moises.sam.model.Registro> = withContext(Dispatchers.IO) {
                when (tipoServicio) {
                    TipoServicio.BORDADO, TipoServicio.PLANCHADO -> repository.getRegistrosBordadoPlanchado()
                    TipoServicio.CHOMPA -> repository.getRegistrosChompas()
                    TipoServicio.PONCHO -> repository.getRegistrosPonchos()
                }.map { it.toRegistro() }
            }

            withContext(Dispatchers.Main) {
                // Eliminar filas previas (excepto encabezado)
                while (tableLayout.childCount > 1) {
                    tableLayout.removeViewAt(1)
                }

                if (registros.isEmpty()) {
                    tvNoRecords.visibility = View.VISIBLE
                } else {
                    tvNoRecords.visibility = View.GONE
                    val dateFormat = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
                    registros.forEach { registro ->
                        val row = TableRow(this@MainActivity)
                        val tvFecha = TextView(this@MainActivity)
                        val tvTipo = TextView(this@MainActivity)
                        val tvCantidad = TextView(this@MainActivity)
                        val tvMonto = TextView(this@MainActivity)

                        tvFecha.text = dateFormat.format(registro.fecha)
                        tvTipo.text = registro.tipo.name
                        tvCantidad.text = registro.cantidad.toString()
                        tvMonto.text = String.format(Locale.getDefault(), "%.2f", registro.total)

                        tvFecha.setTextColor(resources.getColor(R.color.white, theme))
                        tvTipo.setTextColor(resources.getColor(R.color.white, theme))
                        tvCantidad.setTextColor(resources.getColor(R.color.white, theme))
                        tvMonto.setTextColor(resources.getColor(R.color.white, theme))

                        row.addView(tvFecha)
                        row.addView(tvTipo)
                        row.addView(tvCantidad)
                        row.addView(tvMonto)

                        tableLayout.addView(row)
                    }
                }
            }
        }

        btnClose.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }
    
    // Repositorio para acceder a datos
    private lateinit var repository: SamRepository
    
    // Botones principales
    private lateinit var btnBordados: MaterialButton
    private lateinit var btnPlanchados: MaterialButton
    private lateinit var btnRegistroJefe: MaterialButton
    private lateinit var btnDescargarPdf: MaterialButton
    private lateinit var btnAdelantos: MaterialButton
    private lateinit var btnRegistroClemente: MaterialButton
    private lateinit var btnPagosClemente: MaterialButton
    private lateinit var btnRegistroLalo: MaterialButton
    private lateinit var btnPagosLalo: MaterialButton
    
    // TextViews para mostrar saldos
    private lateinit var tvJefeSaldos: TextView
    private lateinit var tvJefeSaldo: TextView
    private lateinit var tvChompasNumero: TextView
    private lateinit var tvClementeSaldo: TextView
    private lateinit var tvPonchosNumero: TextView
    private lateinit var tvLaloSaldo: TextView
    
    // Permiso para solicitar permisos de almacenamiento
    private val requestPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        var allPermissionsGranted = true
        permissions.entries.forEach {
            Log.d("MainActivity", "Permiso ${it.key}: ${if (it.value) "concedido" else "denegado"}")
            if (!it.value) allPermissionsGranted = false
        }
        
        if (allPermissionsGranted) {
            Log.i("MainActivity", "Todos los permisos necesarios han sido concedidos")
            Toast.makeText(this, "Permisos concedidos.", Toast.LENGTH_SHORT).show()
        } else {
            Log.w("MainActivity", "Algunos permisos fueron denegados")
            Toast.makeText(this, "Algunos permisos fueron denegados.", Toast.LENGTH_LONG).show()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // Inicializar el repositorio
        repository = SamRepository(applicationContext)
        
        initViews()
        setupClickListeners()
        
        // Cargar saldos iniciales
        lifecycleScope.launch {
            actualizarSaldos()
        }
    }
    
    private fun initViews() {
        // Inicializar todos los botones
        btnBordados = findViewById(R.id.btn_bordados)
        btnPlanchados = findViewById(R.id.btn_planchados)
        btnRegistroJefe = findViewById(R.id.btn_registro_jefe)
        btnDescargarPdf = findViewById(R.id.btn_descargar_pdf)
        btnAdelantos = findViewById(R.id.btn_adelantos)
        btnRegistroClemente = findViewById(R.id.btn_registro_clemente)
        btnPagosClemente = findViewById(R.id.btn_pagos_clemente)
        btnRegistroLalo = findViewById(R.id.btn_registro_lalo)
        btnPagosLalo = findViewById(R.id.btn_pagos_lalo)
        
        // Inicializar TextViews
        tvJefeSaldos = findViewById(R.id.tv_jefe_saldos)
        tvJefeSaldo = findViewById(R.id.tv_jefe_saldo)
        tvChompasNumero = findViewById(R.id.tv_chompas_numero)
        tvClementeSaldo = findViewById(R.id.tv_clemente_saldo)
        tvPonchosNumero = findViewById(R.id.tv_ponchos_numero)
        tvLaloSaldo = findViewById(R.id.tv_lalo_saldo)
        
        // Inicializar FAB
        val fabAdd: FloatingActionButton = findViewById(R.id.fab_add)
        fabAdd.setOnClickListener {
            mostrarDialogoSeleccionServicio()
        }
    }
    
    private fun setupClickListeners() {
        // MODAL: Registro de bordados para Jefe
        btnBordados.setOnClickListener {
            mostrarDialogoRegistroServicio(TipoServicio.BORDADO) { cantidad ->
                registrarServicio(TipoServicio.BORDADO, cantidad)
                Toast.makeText(this, "Bordado registrado: $cantidad", Toast.LENGTH_SHORT).show()
            }
        }

        // MODAL: Registro de planchado para Jefe
        btnPlanchados.setOnClickListener {
            mostrarDialogoRegistroServicio(TipoServicio.PLANCHADO) { cantidad ->
                registrarServicio(TipoServicio.PLANCHADO, cantidad)
                Toast.makeText(this, "Planchado registrado: $cantidad", Toast.LENGTH_SHORT).show()
            }
        }

        // MODAL: Ver registros de jefe (bordado/planchado)
        btnRegistroJefe.setOnClickListener {
            mostrarDialogoVerRegistros(TipoServicio.BORDADO)
        }

        // MODAL: Registro de chompa para clemente
        btnRegistroClemente.setOnClickListener {
            mostrarDialogoVerRegistros(TipoServicio.CHOMPA)
        }

        // MODAL: Registro de poncho para Lalo
        btnRegistroLalo.setOnClickListener {
            mostrarDialogoVerRegistros(TipoServicio.PONCHO)
        }

        // MODAL: Adelantos para Jefe
        btnAdelantos.setOnClickListener {
            mostrarDialogoAdelantos()
        }
    }
    
    private fun mostrarDialogoAdelantos() {
        // Implementar lógica para mostrar diálogo de adelantos
        val builder = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.dialogo_adelanto, null)
        builder.setView(view)
        
        val dialog = builder.create()
        
        val etMonto = view.findViewById<EditText>(R.id.et_monto_adelanto)
        val etObservacion = view.findViewById<EditText>(R.id.et_observacion_adelanto)
        val btnGuardar = view.findViewById<Button>(R.id.btn_guardar_adelanto)
        
        btnGuardar.setOnClickListener {
            val montoText = etMonto.text.toString()
            if (montoText.isNotEmpty()) {
                val monto = montoText.toDouble()
                val observacion = etObservacion.text.toString()
                
                lifecycleScope.launch {
                    // Guardar adelanto
                    val adelanto = Adelanto(
                        monto = monto,
                        observacion = observacion
                    )
                    repository.saveAdelanto(adelanto)
                    repository.actualizarAdelantos(repository.getTotalAdelantos())
                    
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, "Adelanto guardado: S/. $monto", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                        actualizarSaldos()
                    }
                }
            } else {
                Toast.makeText(this, "Ingrese un monto válido", Toast.LENGTH_SHORT).show()
            }
        }
        
        dialog.show()
    }
    
    private fun mostrarDialogoSeleccionServicio() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Seleccionar servicio")
        
        val options = arrayOf("Bordado", "Planchado", "Chompa", "Poncho")
        
        builder.setItems(options) { dialog, which ->
            val tipoServicio = when (which) {
                0 -> TipoServicio.BORDADO
                1 -> TipoServicio.PLANCHADO
                2 -> TipoServicio.CHOMPA
                3 -> TipoServicio.PONCHO
                else -> TipoServicio.BORDADO
            }
            
            mostrarDialogoRegistroServicio(tipoServicio) { cantidad ->
                registrarServicio(tipoServicio, cantidad)
                Toast.makeText(this, "${options[which]} registrado: $cantidad", Toast.LENGTH_SHORT).show()
            }
        }
        
        builder.create().show()
    }
    
    private fun mostrarDialogoRegistroServicio(tipoServicio: TipoServicio, callback: (cantidad: Int) -> Unit) {
        val builder = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.dialogo_registro_servicio, null)
        builder.setView(view)
        
        val dialog = builder.create()
        
        val etCantidad = view.findViewById<EditText>(R.id.et_cantidad)
        val btnGuardar = view.findViewById<Button>(R.id.btn_guardar)
        val tvTitulo = view.findViewById<TextView>(R.id.tv_titulo_servicio)
        
        // Ajustar título según el tipo de servicio
        when (tipoServicio) {
            TipoServicio.BORDADO -> tvTitulo.text = "Registro de Bordado"
            TipoServicio.PLANCHADO -> tvTitulo.text = "Registro de Planchado"
            TipoServicio.CHOMPA -> tvTitulo.text = "Registro de Chompa"
            TipoServicio.PONCHO -> tvTitulo.text = "Registro de Poncho"
        }
        
        btnGuardar.setOnClickListener {
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
        
        dialog.show()
    }
    
    private fun registrarServicio(tipo: TipoServicio, cantidad: Int) {
        // Calcular monto según tipo de servicio
        val monto = when (tipo) {
            TipoServicio.BORDADO -> 2.0 * cantidad
            TipoServicio.PLANCHADO -> 0.5 * cantidad
            TipoServicio.CHOMPA -> 1.0 * cantidad
            TipoServicio.PONCHO -> 5.0 * cantidad
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
            }
        }
    }
    
    private suspend fun actualizarSaldos() {
        withContext(Dispatchers.IO) {
            try {
                // Obtener datos de la base de datos
                val registrosBordadoPlanchado = repository.getRegistrosBordadoPlanchado()
                val registrosChompas = repository.getRegistrosChompas()
                val registrosPonchos = repository.getRegistrosPonchos()
                val config = repository.getConfiguracion()
                
                // Calcular saldos
                var saldoBordadoPlanchado = 0.0
                registrosBordadoPlanchado.forEach { registro ->
                    saldoBordadoPlanchado += registro.monto
                }
                
                var saldoChompas = 0.0
                registrosChompas.forEach { registro ->
                    saldoChompas += registro.monto
                }
                
                var saldoPonchos = 0.0
                registrosPonchos.forEach { registro ->
                    saldoPonchos += registro.monto
                }
                
                // Actualizar UI en hilo principal
                withContext(Dispatchers.Main) {
                    // Jefe (Bordado y Planchado)
                    tvJefeSaldos.text = "BP: S/ ${String.format("%.2f", saldoBordadoPlanchado)}"
                    
                    val jefeSaldoFinal = saldoBordadoPlanchado - config.adelantos
                    tvJefeSaldo.text = "Saldo: S/ ${String.format("%.2f", jefeSaldoFinal)}"
                    if (jefeSaldoFinal > 0) {
                        tvJefeSaldo.setTextColor(resources.getColor(R.color.balance_yellow, theme)) // Amarillo: te deben
                    } else if (jefeSaldoFinal < 0) {
                        tvJefeSaldo.setTextColor(resources.getColor(R.color.error_color, theme)) // Rojo: debes
                    } else {
                        tvJefeSaldo.setTextColor(resources.getColor(R.color.success_color, theme)) // Verde: saldo cero
                    }
                    
                    // Chompas (Clemente)
                    tvChompasNumero.text = registrosChompas.size.toString()
                    
                    tvClementeSaldo.text = "Saldo: S/ ${String.format("%.2f", saldoChompas)}"
                    if (saldoChompas > 0) {
                        tvClementeSaldo.setTextColor(resources.getColor(R.color.balance_yellow, theme)) // Amarillo: te deben
                    } else if (saldoChompas < 0) {
                        tvClementeSaldo.setTextColor(resources.getColor(R.color.error_color, theme)) // Rojo: debes
                    } else {
                        tvClementeSaldo.setTextColor(resources.getColor(R.color.success_color, theme)) // Verde: saldo cero
                    }
                    
                    // Ponchos (Lalo)
                    tvPonchosNumero.text = registrosPonchos.size.toString()
                    
                    tvLaloSaldo.text = "Saldo: S/ ${String.format("%.2f", saldoPonchos)}"
                    if (saldoPonchos > 0) {
                        tvLaloSaldo.setTextColor(resources.getColor(R.color.balance_yellow, theme)) // Amarillo: te deben
                    } else if (saldoPonchos < 0) {
                        tvLaloSaldo.setTextColor(resources.getColor(R.color.error_color, theme)) // Rojo: debes
                    } else {
                        tvLaloSaldo.setTextColor(resources.getColor(R.color.success_color, theme)) // Verde: saldo cero
                    }
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error al actualizar saldos", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Error al actualizar saldos: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
