package com.moises.sam.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.moises.sam.ui.theme.BorderWhite
import com.moises.sam.ui.theme.DarkWidgetBackground

/**
 * Contenedor común para todos los widgets con fondo oscuro y bordes blancos
 */
@Composable
fun WidgetContainer(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkWidgetBackground)
            .border(2.dp, BorderWhite, shape = RoundedCornerShape(12.dp))
            .padding(16.dp),
        content = content
    )
}

/**
 * Barra superior que muestra el saldo general y los ingresos diarios
 */
@Composable
fun TopSummaryBar(saldoGeneral: Double, ingresosDiarios: Double) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkWidgetBackground)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Saldo general (verde)
        Text(
            text = String.format("%.2f", saldoGeneral),
            style = MaterialTheme.typography.h5,
            color = com.moises.sam.ui.theme.BalanceGreen
        )
        
        // Ingresos diarios (azul)
        Text(
            text = String.format("%.2f", ingresosDiarios),
            style = MaterialTheme.typography.h5,
            color = com.moises.sam.ui.theme.BalanceBlue
        )
    }
}