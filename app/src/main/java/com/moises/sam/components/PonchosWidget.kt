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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.moises.sam.ui.theme.BalanceGreen
import com.moises.sam.ui.theme.BalanceRed
import com.moises.sam.ui.theme.BalanceYellow
import com.moises.sam.viewmodel.MainViewModel

/**
 * Tercer widget: Planchado de Ponchos
 */
@Composable
fun PonchosWidget(viewModel: MainViewModel) {
    val showRegisterDialog = remember { mutableStateOf(false) }
    val showPaymentDialog = remember { mutableStateOf(false) }
    val cantidad = remember { mutableStateOf("") }
    val pagoMonto = remember { mutableStateOf("") }
    
    // Color del saldo
    val saldoColor = viewModel.obtenerColorSaldo()

    WidgetContainer {
        Text(
            text = "Planchado de Ponchos",
            style = MaterialTheme.typography.h6,
            color = MaterialTheme.colors.onSurface,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Cantidad de ponchos pendientes
        Text(
            text = "Ponchos pendientes: ${viewModel.cantidadPonchos}",
            style = MaterialTheme.typography.subtitle1,
            color = MaterialTheme.colors.onSurface,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Saldo con color según estado
        Text(
            text = "Saldo: S/ ${String.format("%.2f", viewModel.saldoPonchos)}",
            style = MaterialTheme.typography.h5,
            color = saldoColor,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Campo para ingresar cantidad
        OutlinedTextField(
            value = cantidad.value,
            onValueChange = { cantidad.value = it },
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
            onClick = {
                val cantidadNum = cantidad.value.toIntOrNull() ?: 0
                if (cantidadNum > 0) {
                    viewModel.registrarPonchos(cantidadNum)
                    cantidad.value = ""
                }
            },
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
                onClick = { /* Ver registros */ },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = com.moises.sam.ui.theme.SecondaryColor
                )
            ) {
                Text("Ver registros")
            }

            Button(
                onClick = { showPaymentDialog.value = true },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = com.moises.sam.ui.theme.SecondaryColor
                )
            ) {
                Text("Registrar pago")
            }
        }
    }
    
    // Diálogo para registrar pago
    if (showPaymentDialog.value) {
        AlertDialog(
            onDismissRequest = { showPaymentDialog.value = false },
            title = { Text("Registrar Pago") },
            text = {
                Column {
                    Text("Total pendiente: S/ ${String.format("%.2f", viewModel.saldoPonchos)}")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = pagoMonto.value,
                        onValueChange = { pagoMonto.value = it },
                        label = { Text("Monto a pagar") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            textColor = MaterialTheme.colors.onSurface,
                            cursorColor = MaterialTheme.colors.onSurface,
                            focusedBorderColor = com.moises.sam.ui.theme.PrimaryColor,
                            unfocusedBorderColor = MaterialTheme.colors.onSurface
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val monto = pagoMonto.value.toDoubleOrNull() ?: 0.0
                        if (monto > 0) {
                            viewModel.registrarPagoPonchos(monto)
                            pagoMonto.value = ""
                            showPaymentDialog.value = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = com.moises.sam.ui.theme.PrimaryColor
                    )
                ) {
                    Text("Registrar Pago")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showPaymentDialog.value = false }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}