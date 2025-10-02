package com.moises.sam.pdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfDocument
import android.os.Environment
import com.moises.sam.model.Pago
import com.moises.sam.model.Registro
import com.moises.sam.model.TipoServicio
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Clase para generar PDF con reportes de servicios
 */
class PdfGenerator(private val context: Context) {
    
    /**
     * Genera un PDF con los registros y pagos
     */
    fun generarPdf(
        registrosBordado: List<Registro>,
        registrosPlanchado: List<Registro>,
        pagos: List<Pago>,
        saldoAnterior: Double,
        adelantos: Double,
        pago: Double,
        firma: Bitmap?
    ): File? {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
        val page = document.startPage(pageInfo)
        val canvas = page.canvas
        
        // Fondo negro
        canvas.drawRGB(18, 18, 18) // Color negro
        
        val paint = android.graphics.Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 12f
        }
        
        // Título
        paint.textSize = 18f
        val title = "Reporte de Servicios - ${LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}"
        canvas.drawText(title, 50f, 50f, paint)
        
        // Separador
        paint.strokeWidth = 2f
        canvas.drawLine(50f, 60f, 545f, 60f, paint)
        
        // Sección Bordados
        paint.textSize = 16f
        canvas.drawText("Bordados", 50f, 90f, paint)
        
        // Separador
        paint.strokeWidth = 1f
        canvas.drawLine(50f, 100f, 545f, 100f, paint)
        
        // Cabecera de tabla
        paint.textSize = 12f
        canvas.drawText("Fecha", 60f, 120f, paint)
        canvas.drawText("Cantidad", 160f, 120f, paint)
        
        // Si hay muchos registros, usar dos columnas
        val bordadosDivididos = if (registrosBordado.size > 10) {
            val mitad = registrosBordado.size / 2
            Pair(
                registrosBordado.subList(0, mitad),
                registrosBordado.subList(mitad, registrosBordado.size)
            )
        } else {
            Pair(registrosBordado, emptyList())
        }
        
        // Primera columna
        var y = 140f
        bordadosDivididos.first.forEach { registro ->
            canvas.drawText(
                registro.fecha.format(DateTimeFormatter.ofPattern("dd/MM/yy")),
                60f, y, paint
            )
            canvas.drawText(registro.cantidad.toString(), 160f, y, paint)
            y += 20f
        }
        
        // Segunda columna si es necesario
        if (bordadosDivididos.second.isNotEmpty()) {
            y = 140f
            bordadosDivididos.second.forEach { registro ->
                canvas.drawText(
                    registro.fecha.format(DateTimeFormatter.ofPattern("dd/MM/yy")),
                    300f, y, paint
                )
                canvas.drawText(registro.cantidad.toString(), 400f, y, paint)
                y += 20f
            }
        }
        
        // Total de bordados
        val totalBordados = registrosBordado.sumOf { it.total }
        y += 20f
        canvas.drawText(
            "Total = ${registrosBordado.sumOf { it.cantidad }}   S/ ${String.format("%.2f", totalBordados)}",
            60f, y, paint
        )
        
        // Sección Planchados
        y += 40f
        paint.textSize = 16f
        canvas.drawText("Planchados", 50f, y, paint)
        
        // Separador
        y += 10f
        paint.strokeWidth = 1f
        canvas.drawLine(50f, y, 545f, y, paint)
        
        // Cabecera de tabla
        y += 20f
        paint.textSize = 12f
        canvas.drawText("Fecha", 60f, y, paint)
        canvas.drawText("Cantidad", 160f, y, paint)
        
        // Si hay muchos registros, usar dos columnas
        val planchadosDivididos = if (registrosPlanchado.size > 10) {
            val mitad = registrosPlanchado.size / 2
            Pair(
                registrosPlanchado.subList(0, mitad),
                registrosPlanchado.subList(mitad, registrosPlanchado.size)
            )
        } else {
            Pair(registrosPlanchado, emptyList())
        }
        
        // Primera columna
        y += 20f
        val inicioY = y
        planchadosDivididos.first.forEach { registro ->
            canvas.drawText(
                registro.fecha.format(DateTimeFormatter.ofPattern("dd/MM/yy")),
                60f, y, paint
            )
            canvas.drawText(registro.cantidad.toString(), 160f, y, paint)
            y += 20f
        }
        
        // Segunda columna si es necesario
        if (planchadosDivididos.second.isNotEmpty()) {
            y = inicioY
            planchadosDivididos.second.forEach { registro ->
                canvas.drawText(
                    registro.fecha.format(DateTimeFormatter.ofPattern("dd/MM/yy")),
                    300f, y, paint
                )
                canvas.drawText(registro.cantidad.toString(), 400f, y, paint)
                y += 20f
            }
        }
        
        // Total de planchados
        val totalPlanchados = registrosPlanchado.sumOf { it.total }
        y += 20f
        canvas.drawText(
            "Total = ${registrosPlanchado.sumOf { it.cantidad }}   S/ ${String.format("%.2f", totalPlanchados)}",
            60f, y, paint
        )
        
        // Resumen de saldos
        y += 60f
        paint.textSize = 16f
        canvas.drawText("Resumen", 50f, y, paint)
        
        // Separador
        y += 10f
        paint.strokeWidth = 1f
        canvas.drawLine(50f, y, 545f, y, paint)
        
        // Detalles de resumen
        y += 30f
        paint.textSize = 12f
        canvas.drawText("Total Bordado:     S/ ${String.format("%.2f", totalBordados)}", 60f, y, paint)
        y += 20f
        canvas.drawText("Total Planchado:   S/ ${String.format("%.2f", totalPlanchados)}", 60f, y, paint)
        y += 20f
        canvas.drawText("Total General:     S/ ${String.format("%.2f", totalBordados + totalPlanchados)}", 60f, y, paint)
        y += 20f
        canvas.drawText("Saldo anterior:    S/ ${String.format("%.2f", saldoAnterior)}", 60f, y, paint)
        y += 20f
        canvas.drawText("Adelantos:         S/ ${String.format("%.2f", adelantos)}", 60f, y, paint)
        y += 20f
        canvas.drawText("Pago:              S/ ${String.format("%.2f", pago)}", 60f, y, paint)
        y += 20f
        
        // Cálculo de saldo final
        val saldoFinal = (totalBordados + totalPlanchados + saldoAnterior) - (adelantos + pago)
        canvas.drawText("Saldo final:       S/ ${String.format("%.2f", saldoFinal)}", 60f, y, paint)
        
        // Firma
        y += 50f
        canvas.drawText("Firma: ___________________________", 60f, y, paint)
        
        // Si hay firma, dibujarla
        if (firma != null) {
            canvas.drawBitmap(firma, 150f, y - 40f, paint)
        }
        
        // Fecha y hora
        y += 40f
        val now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
        canvas.drawText("Fecha y hora: $now", 60f, y, paint)
        
        document.finishPage(page)
        
        // Guardar el documento
        val directory = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        val fileName = "reporte_${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))}.pdf"
        val file = File(directory, fileName)
        
        try {
            FileOutputStream(file).use {
                document.writeTo(it)
            }
            document.close()
            return file
        } catch (e: Exception) {
            e.printStackTrace()
            document.close()
            return null
        }
    }
}