package com.moises.sam

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity() {
    
    // Botones principales
    private lateinit var btnPlanchados: MaterialButton
    private lateinit var btnRegistroJefe: MaterialButton
    private lateinit var btnAdelantos: MaterialButton
    private lateinit var btnPagosClemente: MaterialButton
    private lateinit var btnPagosLalo: MaterialButton
    
    // Permisos
    private val requestPermissionLauncher = registerForActivityResult(
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
        
        initViews()
        setupClickListeners()
    }
    
    private fun initViews() {
        // Solo inicializar los botones que realmente usamos
        btnPlanchados = findViewById(R.id.btn_planchados)
        btnRegistroJefe = findViewById(R.id.btn_registro_jefe)
        btnAdelantos = findViewById(R.id.btn_adelantos)
        btnPagosClemente = findViewById(R.id.btn_pagos_clemente)
        btnPagosLalo = findViewById(R.id.btn_pagos_lalo)
    }
    
    private fun setupClickListeners() {
        // MODAL: Registro de planchado para Jefe
        btnPlanchados.setOnClickListener {
            mostrarDialogoRegistroPago { cantidad ->
                Toast.makeText(this, "Planchado registrado (Jefe): $cantidad", Toast.LENGTH_SHORT).show()
                // Aquí puedes agregar lógica para guardar el planchado del jefe
            }
        }
        
        // MODAL: Registro de pago simple para Jefe
        btnRegistroJefe.setOnClickListener {
            mostrarDialogoRegistroPago { cantidad ->
                Toast.makeText(this, "Pago registrado (Jefe): S/$cantidad", Toast.LENGTH_SHORT).show()
                // Aquí puedes agregar lógica para guardar el pago del jefe
            }
        }
        
        // MODAL: Registro de adelantos para Jefe
        btnAdelantos.setOnClickListener {
            mostrarDialogoRegistroPago { cantidad ->
                Toast.makeText(this, "Adelanto registrado (Jefe): S/$cantidad", Toast.LENGTH_SHORT).show()
                // Aquí puedes agregar lógica para guardar el adelanto del jefe
            }
        }
        
        // MODAL: Registro de pago simple para Clemente
        btnPagosClemente.setOnClickListener {
            mostrarDialogoRegistroPago { cantidad ->
                Toast.makeText(this, "Pago registrado (Clemente): S/$cantidad", Toast.LENGTH_SHORT).show()
                // Aquí puedes agregar lógica para guardar el pago
            }
        }
        
        // MODAL: Registro de pago simple para Lalo
        btnPagosLalo.setOnClickListener {
            mostrarDialogoRegistroPago { cantidad ->
                Toast.makeText(this, "Pago registrado (Lalo): S/$cantidad", Toast.LENGTH_SHORT).show()
                // Aquí puedes agregar lógica para guardar el pago
            }
        }
    }

    // Función para mostrar el diálogo de registro de pagos
    private fun mostrarDialogoRegistroPago(onRegistrar: (Double) -> Unit) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_registro_pago, null)
        val etCantidad = dialogView.findViewById<EditText>(R.id.etCantidadPago)
        val btnRegistrar = dialogView.findViewById<Button>(R.id.btnRegistrarPago)

        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        btnRegistrar.setOnClickListener {
            val cantidad = etCantidad.text.toString().toDoubleOrNull()
            if (cantidad != null && cantidad > 0) {
                onRegistrar(cantidad)
                dialog.dismiss()
            } else {
                etCantidad.error = "Ingresa un monto válido"
            }
        }
        dialog.show()
    }
    
    // Verifico permisos de almacenamiento para futuro uso
    private fun hasStoragePermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    private fun requestStoragePermissions() {
        Log.d("MainActivity", "Iniciando verificación de permisos...")
        
        val permissionsToRequest = mutableListOf<String>()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.READ_MEDIA_IMAGES)
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
        
        if (permissionsToRequest.isNotEmpty()) {
            requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
        } else {
            Log.i("MainActivity", "Todos los permisos ya están concedidos")
        }
    }
}