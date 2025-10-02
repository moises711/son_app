package com.moises.sam.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.Text
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.AlertDialog
import androidx.compose.material.TextButton
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.moises.sam.model.TipoServicio
import com.moises.sam.viewmodel.MainViewModel

/**
 * Primer widget: Bordado y Planchado
 */
@Composable
fun BordadoPlanchadoWidget(viewModel: MainViewModel) {
    WidgetContainer {
        // Saldo y botones superiores
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Saldo: S/ ${String.format("%.2f", viewModel.saldoBordadoPlanchado)}",
                color = MaterialTheme.colors.onSurface
            )
            
            Text(
                text = "Adelantos: S/ ${String.format("%.2f", viewModel.adelantos)}",
                color = MaterialTheme.colors.onSurface
            )
            
            Button(
                onClick = { viewModel.mostrarDialogoPago = true },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = com.moises.sam.ui.theme.PrimaryColor
                )
            ) {
                Text("PDF")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Selector de tipo de servicio
        val types = listOf("Bordado", "Planchado")
        val selectedIndex = remember(viewModel.tipoServicioSeleccionado) {
            when(viewModel.tipoServicioSeleccionado) {
                TipoServicio.BORDADO -> 0
                TipoServicio.PLANCHADO -> 1
                else -> 0
            }
        }
        
        TabRow(
            selectedTabIndex = selectedIndex,
            backgroundColor = MaterialTheme.colors.surface,
            contentColor = MaterialTheme.colors.onSurface
        ) {
            types.forEachIndexed { index, title ->
                Tab(
                    selected = selectedIndex == index,
                    onClick = {
                        viewModel.tipoServicioSeleccionado = when(index) {
                            0 -> TipoServicio.BORDADO
                            1 -> TipoServicio.PLANCHADO
                            else -> TipoServicio.BORDADO
                        }
                    },
                    text = { Text(title) }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Campo para ingresar cantidad
        OutlinedTextField(
            value = viewModel.cantidad,
            onValueChange = { viewModel.cantidad = it },
            label = { Text("Cantidad") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    textColor = MaterialTheme.colors.onSurface,
                    cursorColor = MaterialTheme.colors.onSurface,
                    focusedBorderColor = com.moises.sam.ui.theme.PrimaryColor,
                    unfocusedBorderColor = MaterialTheme.colors.onSurface
                )
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Botón de registrar
        Button(
            onClick = { viewModel.registrarServicio() },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = com.moises.sam.ui.theme.PrimaryColor
            )
        ) {
            Text("Registrar")
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Botones inferiores
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = { /* Mostrar registros */ },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = com.moises.sam.ui.theme.SecondaryColor
                )
            ) {
                Text("Ver registros")
            }
            
            Button(
                onClick = { viewModel.mostrarDialogoAdelanto = true },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = com.moises.sam.ui.theme.SecondaryColor
                )
            ) {
                Text("Registrar adelanto")
            }
        }
    }
    
    // Diálogo para registrar adelanto
    if (viewModel.mostrarDialogoAdelanto) {
        AlertDialog(
            onDismissRequest = { viewModel.mostrarDialogoAdelanto = false },
            title = { Text("Registrar Adelanto") },
            text = {
                Column {
                    OutlinedTextField(
                        value = viewModel.cantidadAdelanto,
                        onValueChange = { viewModel.cantidadAdelanto = it },
                        label = { Text("Monto") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.registrarAdelanto() }
                ) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.mostrarDialogoAdelanto = false }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
    
    // Diálogo para generar PDF (pago)
    if (viewModel.mostrarDialogoPago) {
        AlertDialog(
            onDismissRequest = { viewModel.mostrarDialogoPago = false },
            title = { Text("Registrar Pago e Imprimir PDF") },
            text = {
                Column {
                    Text("Total pendiente: S/ ${String.format("%.2f", viewModel.saldoBordadoPlanchado - viewModel.adelantos)}")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = viewModel.cantidadPago,
                        onValueChange = { viewModel.cantidadPago = it },
                        label = { Text("Monto a pagar") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    // Aquí iría un componente para firma digital
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val monto = viewModel.cantidadPago.toDoubleOrNull() ?: 0.0
                        if (viewModel.generarPDF(monto)) {
                            viewModel.cantidadPago = ""
                            viewModel.mostrarDialogoPago = false
                        }
                    }
                ) {
                    Text("Generar PDF")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.mostrarDialogoPago = false }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}