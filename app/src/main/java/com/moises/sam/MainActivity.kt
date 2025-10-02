package com.moises.sam

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.moises.sam.components.*
import com.moises.sam.ui.theme.SamTheme
import com.moises.sam.viewmodel.MainViewModel
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SamTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    MainScreen(viewModel)
                }
            }
        }
    }
}

@Composable
fun MainScreen(viewModel: MainViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
    ) {
        // Barra superior de resumen
        TopSummaryBar(
            saldoGeneral = viewModel.saldoGeneral,
            ingresosDiarios = viewModel.ingresosDiarios
        )
        
        // Widgets en un scroll
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Widget de Bordado y Planchado
            BordadoPlanchadoWidget(viewModel)
            
            // Widget de Planchado de Chompas
            ChompasWidget(viewModel)
            
            // Widget de Planchado de Ponchos
            PonchosWidget(viewModel)
        }
    }
}