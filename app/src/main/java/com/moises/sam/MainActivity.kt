package com.moises.sam

import android.Manifest
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.animation.DecelerateInterpolator
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity() {
    
    // Views principales
    private lateinit var tvTotal: TextView
    private lateinit var tvHoy: TextView
    private lateinit var tvSaldoGeneral: TextView
    private lateinit var tvJefeSaldos: TextView
    private lateinit var tvJefeSaldo: TextView
    private lateinit var tvChompasPendientes: TextView
    private lateinit var tvChompasNumero: TextView
    private lateinit var tvClementeSaldo: TextView
    private lateinit var tvPonchosPendientes: TextView
    private lateinit var tvPonchosNumero: TextView
    private lateinit var tvLaloSaldo: TextView
    
    // CardViews
    private lateinit var cardJefe: CardView
    private lateinit var cardClemente: CardView
    private lateinit var cardLalo: CardView
    
    // Botones
    private lateinit var btnBordados: MaterialButton
    private lateinit var btnPlanchados: MaterialButton
    private lateinit var btnRegistroJefe: MaterialButton
    private lateinit var btnDescargarPdf: MaterialButton
    private lateinit var btnAdelantos: MaterialButton
    private lateinit var btnRegistroClemente: MaterialButton
    private lateinit var btnPagosClemente: MaterialButton
    private lateinit var btnRegistroLalo: MaterialButton
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
            Toast.makeText(this, "Permisos concedidos. Ya puedes generar PDFs.", Toast.LENGTH_SHORT).show()
        } else {
            Log.w("MainActivity", "Algunos permisos fueron denegados")
            Toast.makeText(this, "Algunos permisos fueron denegados.", Toast.LENGTH_LONG).show()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        initViews()
        requestStoragePermissions()
        setupClickListeners()
        observeViewModel()
        animateEntrance()
    }
    
    private fun initViews() {
        // Barra superior
        tvTotal = findViewById(R.id.tv_total)
        tvHoy = findViewById(R.id.tv_hoy)
        tvSaldoGeneral = findViewById(R.id.tv_saldo_general)
        
        // Cards
        cardJefe = findViewById(R.id.card_jefe)
        cardClemente = findViewById(R.id.card_clemente)
        cardLalo = findViewById(R.id.card_lalo)
        
        // Jefe TextViews
        tvJefeSaldos = findViewById(R.id.tv_jefe_saldos)
        tvJefeSaldo = findViewById(R.id.tv_jefe_saldo)
        
        // Clemente TextViews
        tvChompasPendientes = findViewById(R.id.tv_chompas_pendientes)
        tvChompasNumero = findViewById(R.id.tv_chompas_numero)
        tvClementeSaldo = findViewById(R.id.tv_clemente_saldo)
        
        // Lalo TextViews
        tvPonchosPendientes = findViewById(R.id.tv_ponchos_pendientes)
        tvPonchosNumero = findViewById(R.id.tv_ponchos_numero)
        tvLaloSaldo = findViewById(R.id.tv_lalo_saldo)
        
        // Botones Jefe
        btnBordados = findViewById(R.id.btn_bordados)
        btnPlanchados = findViewById(R.id.btn_planchados)
        btnRegistroJefe = findViewById(R.id.btn_registro_jefe)
        btnDescargarPdf = findViewById(R.id.btn_descargar_pdf)
        btnAdelantos = findViewById(R.id.btn_adelantos)
        
        // Botones Clemente
        btnRegistroClemente = findViewById(R.id.btn_registro_clemente)
        btnPagosClemente = findViewById(R.id.btn_pagos_clemente)
        
        // Botones Lalo
        btnRegistroLalo = findViewById(R.id.btn_registro_lalo)
        btnPagosLalo = findViewById(R.id.btn_pagos_lalo)
    }
    
    private fun setupClickListeners() {
        btnBordados.setOnClickListener {
            Toast.makeText(this, "Bordados", Toast.LENGTH_SHORT).show()
        }
        
        btnPlanchados.setOnClickListener {
            Toast.makeText(this, "Planchados", Toast.LENGTH_SHORT).show()
        }
        
        btnRegistroJefe.setOnClickListener {
            Toast.makeText(this, "Registro Jefe", Toast.LENGTH_SHORT).show()
        }
        
        btnDescargarPdf.setOnClickListener {
            Toast.makeText(this, "Descargando PDF...", Toast.LENGTH_SHORT).show()
        }
        
        btnAdelantos.setOnClickListener {
            Toast.makeText(this, "Adelantos", Toast.LENGTH_SHORT).show()
        }
        
        btnRegistroClemente.setOnClickListener {
            Toast.makeText(this, "Registro Clemente", Toast.LENGTH_SHORT).show()
        }
        
        btnPagosClemente.setOnClickListener {
            Toast.makeText(this, "Pagos Clemente", Toast.LENGTH_SHORT).show()
        }
        
        btnRegistroLalo.setOnClickListener {
            Toast.makeText(this, "Registro Lalo", Toast.LENGTH_SHORT).show()
        }
        
        btnPagosLalo.setOnClickListener {
            Toast.makeText(this, "Pagos Lalo", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun observeViewModel() {
        updateUIValues()
    }
    
    private fun updateUIValues() {
        tvTotal.text = "total s/0.00"
        tvHoy.text = "hoy s/0.00"
        tvSaldoGeneral.text = "saldo general s/0.00"
        
        tvJefeSaldos.text = "saldos s/0.00"
        tvJefeSaldo.text = "saldo s/0.00"
        
        tvChompasPendientes.text = "chompas pendientes"
        tvChompasNumero.text = "0"
        tvClementeSaldo.text = "saldo s/0.00"
        
        tvPonchosPendientes.text = "ponchos pendientes"
        tvPonchosNumero.text = "0"
        tvLaloSaldo.text = "saldo s/0.00"
    }
    
    private fun animateEntrance() {
        val cards = listOf(cardJefe, cardClemente, cardLalo)
        
        cards.forEachIndexed { index, card ->
            card.alpha = 0f
            card.translationY = 100f
            
            val fadeIn = ObjectAnimator.ofFloat(card, "alpha", 0f, 1f)
            val slideUp = ObjectAnimator.ofFloat(card, "translationY", 100f, 0f)
            
            val animatorSet = AnimatorSet()
            animatorSet.playTogether(fadeIn, slideUp)
            animatorSet.duration = 600
            animatorSet.interpolator = DecelerateInterpolator()
            animatorSet.startDelay = (index * 150).toLong()
            
            animatorSet.start()
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