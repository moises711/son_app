package com.moises.sam.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.Text
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moises.sam.viewmodel.MainViewModel

/**
 * Segundo widget: Planchado de Chompas
 */
@Composable
fun ChompasWidget(viewModel: MainViewModel) {
    WidgetContainer(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { viewModel.mostrarDialogoChompas = true }
    ) {
        Text(
            text = "Planchado de Chompas",
            style = MaterialTheme.typography.h6,
            color = MaterialTheme.colors.onSurface,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Saldo: S/ ${String.format("%.2f", viewModel.saldoChompas)}",
            style = MaterialTheme.typography.h5,
            color = MaterialTheme.colors.onSurface,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Click en cualquier parte para registrar cantidad",
            color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
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
                onClick = { viewModel.mostrarDialogoPago = true },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = com.moises.sam.ui.theme.SecondaryColor
                )
            ) {
                Text("Registrar pago")
            }
        }
    }
    
    // Diálogo para registrar cantidad
    if (viewModel.mostrarDialogoChompas) {
        AlertDialog(
            onDismissRequest = { viewModel.mostrarDialogoChompas = false },
            title = { Text("Registrar Planchado de Chompas") },
            text = {
                Column {
                    Text("Precio unitario: S/ 1.00")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = viewModel.cantidadChompas,
                        onValueChange = { viewModel.cantidadChompas = it },
                        label = { Text("Cantidad de chompas") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.registrarChompas() }
                ) {
                    Text("Registrar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.mostrarDialogoChompas = false }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
    
    // Diálogo para registrar pago
    if (viewModel.mostrarDialogoPago) {
        AlertDialog(
            onDismissRequest = { viewModel.mostrarDialogoPago = false },
            title = { Text("Registrar Pago") },
            text = {
                Column {
                    Text("Total pendiente: S/ ${String.format("%.2f", viewModel.saldoChompas)}")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = viewModel.cantidadPago,
                        onValueChange = { viewModel.cantidadPago = it },
                        label = { Text("Monto a pagar") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.registrarPagoChormpas() }
                ) {
                    Text("Registrar Pago")
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