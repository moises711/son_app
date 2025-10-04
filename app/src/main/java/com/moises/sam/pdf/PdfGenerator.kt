package com.moises.sam.pdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.annotation.RequiresApi
import com.moises.sam.model.Pago
import com.moises.sam.model.Registro
import com.moises.sam.model.TipoServicio
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.core.graphics.scale

// Extension para obtener el precio unitario
val Registro.precioUnitario: Double
    get() = if (cantidad > 0) total / cantidad else 0.0

// Extension para obtener el nombre del tipo de servicio
val TipoServicio.nombre: String
    get() = when(this) {
        TipoServicio.BORDADO -> "Bordado"
        TipoServicio.PLANCHADO -> "Planchado"
        TipoServicio.CHOMPA -> "Chompa"
        TipoServicio.PONCHO -> "Poncho"
    }

/**
 * Clase para generar PDF con reportes de servicios
 */
class PdfGenerator(private val context: Context) {
    
    companion object {
        private const val TAG = "PdfGenerator"
        // Constantes para formato del PDF
        private const val PAGE_WIDTH = 595 // A4 width
        private const val PAGE_HEIGHT = 842 // A4 height
        private const val MARGIN_LEFT = 50f
        private const val MARGIN_RIGHT = 545f
        private const val HEADER_TEXT_SIZE = 18f
        private const val SUBTITLE_TEXT_SIZE = 16f
        private const val NORMAL_TEXT_SIZE = 12f
        private const val LINE_HEIGHT = 20f
    }
    
    /**
     * Genera un PDF con los registros y pagos
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun generarPdf(
        registrosBordado: List<Registro>,
        registrosPlanchado: List<Registro>,
        pagos: List<Pago>,
        saldoAnterior: Double,
        adelantos: Double,
        pago: Double,
        firma: Bitmap?
    ): File? {
        Log.i(TAG, "=== INICIANDO GENERACIÓN DE PDF ===")
        Log.d(TAG, "Registros bordado: ${registrosBordado.size}")
        Log.d(TAG, "Registros planchado: ${registrosPlanchado.size}")
        Log.d(TAG, "Pagos: ${pagos.size}")
        Log.d(TAG, "Saldo anterior: $saldoAnterior")
        Log.d(TAG, "Adelantos: $adelantos")
        Log.d(TAG, "Pago: $pago")
        Log.d(TAG, "Firma presente: ${firma != null}")
        
        try {
            Log.d(TAG, "Creando documento PDF...")
            val document = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create() // A4
            val page = document.startPage(pageInfo)
            val canvas = page.canvas
            
            Log.d(TAG, "Página creada exitosamente")
        
        // Fondo negro
        canvas.drawRGB(18, 18, 18) // Color negro
        
        val paint = Paint().apply {
            color = Color.WHITE
            textSize = NORMAL_TEXT_SIZE
            isAntiAlias = true
        }
        
        val boldPaint = Paint().apply {
            color = Color.WHITE
            textSize = NORMAL_TEXT_SIZE
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        
        // Título
        boldPaint.textSize = HEADER_TEXT_SIZE
        val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val title = "REPORTE DE SERVICIOS - ${LocalDate.now().format(dateFormatter)}"
        canvas.drawText(title, MARGIN_LEFT, 50f, boldPaint)
        
        // Separador
        paint.strokeWidth = 2f
        canvas.drawLine(MARGIN_LEFT, 60f, MARGIN_RIGHT, 60f, paint)
        
        // Sección Bordados
        boldPaint.textSize = SUBTITLE_TEXT_SIZE
        canvas.drawText("BORDADOS", MARGIN_LEFT, 90f, boldPaint)
        
        // Separador
        paint.strokeWidth = 1f
        canvas.drawLine(MARGIN_LEFT, 100f, MARGIN_RIGHT, 100f, paint)
        
        // Cabecera de tabla
        boldPaint.textSize = NORMAL_TEXT_SIZE
        canvas.drawText("FECHA", 60f, 120f, boldPaint)
        canvas.drawText("CANTIDAD", 160f, 120f, boldPaint)
        canvas.drawText("PRECIO", 240f, 120f, boldPaint)
        canvas.drawText("TOTAL", 320f, 120f, boldPaint)
        
        // Determinar cuántos registros mostrar por página
        val maxRegistrosPorPagina = 15
        val bordadosPaginados = registrosBordado.chunked(maxRegistrosPorPagina)
        
        var y = 140f
        val dateFormat = DateTimeFormatter.ofPattern("dd/MM/yy")
        
        // Mostrar primera página de bordados
        if (bordadosPaginados.isNotEmpty()) {
            bordadosPaginados[0].forEach { registro ->
                canvas.drawText(
                    registro.fecha.format(dateFormat),
                    60f, y, paint
                )
                canvas.drawText(registro.cantidad.toString(), 160f, y, paint)
                canvas.drawText(
                    "S/ " + String.format(Locale.US, "%.2f", registro.precioUnitario),
                    240f, y, paint
                )
                canvas.drawText(
                    "S/ " + String.format(Locale.US, "%.2f", registro.total),
                    320f, y, paint
                )
                y += LINE_HEIGHT
            }
        }
        
        // Total de bordados
        val totalBordados = registrosBordado.sumOf { it.total }
        val totalCantidadBordados = registrosBordado.sumOf { it.cantidad }
        y += LINE_HEIGHT
        
        // Línea de subtotal
        paint.strokeWidth = 1f
        canvas.drawLine(60f, y - 5f, 400f, y - 5f, paint)
        
        boldPaint.textSize = NORMAL_TEXT_SIZE
        canvas.drawText(
            "TOTAL BORDADOS: $totalCantidadBordados unid.   S/ ${String.format(Locale.US, "%.2f", totalBordados)}",
            60f, y + 15f, boldPaint
        )
        
        // Sección Planchados
        y += 50f
        boldPaint.textSize = SUBTITLE_TEXT_SIZE
        canvas.drawText("PLANCHADOS", MARGIN_LEFT, y, boldPaint)
        
        // Separador
        y += 10f
        paint.strokeWidth = 1f
        canvas.drawLine(MARGIN_LEFT, y, MARGIN_RIGHT, y, paint)
        
        // Cabecera de tabla
        y += 20f
        boldPaint.textSize = NORMAL_TEXT_SIZE
        canvas.drawText("FECHA", 60f, y, boldPaint)
        canvas.drawText("CANTIDAD", 160f, y, boldPaint)
        canvas.drawText("PRECIO", 240f, y, boldPaint)
        canvas.drawText("TOTAL", 320f, y, boldPaint)
        
        // Determinar cuántos registros mostrar por página
        val planchadosPaginados = registrosPlanchado.chunked(maxRegistrosPorPagina)
        
        // Primera página de planchados
        y += 20f
        if (planchadosPaginados.isNotEmpty()) {
            planchadosPaginados[0].forEach { registro ->
                canvas.drawText(
                    registro.fecha.format(dateFormat),
                    60f, y, paint
                )
                canvas.drawText(registro.cantidad.toString(), 160f, y, paint)
                canvas.drawText(
                    "S/ " + String.format(Locale.US, "%.2f", registro.precioUnitario),
                    240f, y, paint
                )
                canvas.drawText(
                    "S/ " + String.format(Locale.US, "%.2f", registro.total),
                    320f, y, paint
                )
                y += LINE_HEIGHT
            }
        }
        
        // Total de planchados
        val totalPlanchados = registrosPlanchado.sumOf { it.total }
        val totalCantidadPlanchados = registrosPlanchado.sumOf { it.cantidad }
        y += LINE_HEIGHT
        
        // Línea de subtotal
        paint.strokeWidth = 1f
        canvas.drawLine(60f, y - 5f, 400f, y - 5f, paint)
        
        boldPaint.textSize = NORMAL_TEXT_SIZE
        canvas.drawText(
            "TOTAL PLANCHADOS: $totalCantidadPlanchados unid.   S/ ${String.format(Locale.US, "%.2f", totalPlanchados)}",
            60f, y + 15f, boldPaint
        )
        
        // Resumen de saldos
        y += 60f
        boldPaint.textSize = SUBTITLE_TEXT_SIZE
        canvas.drawText("RESUMEN", MARGIN_LEFT, y, boldPaint)
        
        // Separador
        y += 10f
        paint.strokeWidth = 1f
        canvas.drawLine(MARGIN_LEFT, y, MARGIN_RIGHT, y, paint)
        
        // Detalles de resumen
        y += 30f
        paint.textSize = NORMAL_TEXT_SIZE
        
        // Crear un marco para el resumen
        val boxTop = y - 20f
        val boxWidth = 350f
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.5f
        canvas.drawRect(MARGIN_LEFT, boxTop, MARGIN_LEFT + boxWidth, boxTop + 170f, paint)
        paint.style = Paint.Style.FILL
        
        // Contenido del resumen
        canvas.drawText("Total Bordados:     S/ ${String.format(Locale.US, "%.2f", totalBordados)}", 60f, y, paint)
        y += LINE_HEIGHT
        canvas.drawText("Total Planchados:   S/ ${String.format(Locale.US, "%.2f", totalPlanchados)}", 60f, y, paint)
        y += LINE_HEIGHT
        
        // Línea divisoria
        paint.strokeWidth = 1f
        canvas.drawLine(60f, y - 5f, MARGIN_LEFT + boxWidth - 10f, y - 5f, paint)
        
        val totalGeneral = totalBordados + totalPlanchados
        canvas.drawText("Total General:      S/ ${String.format(Locale.US, "%.2f", totalGeneral)}", 60f, y, boldPaint)
        y += LINE_HEIGHT
        canvas.drawText("Saldo anterior:     S/ ${String.format(Locale.US, "%.2f", saldoAnterior)}", 60f, y, paint)
        y += LINE_HEIGHT
        
        // Segunda línea divisoria
        paint.strokeWidth = 1f
        canvas.drawLine(60f, y - 5f, MARGIN_LEFT + boxWidth - 10f, y - 5f, paint)
        
        val totalAPagar = totalGeneral + saldoAnterior
        canvas.drawText("Total a pagar:      S/ ${String.format(Locale.US, "%.2f", totalAPagar)}", 60f, y, boldPaint)
        y += LINE_HEIGHT
        canvas.drawText("Adelantos:          S/ ${String.format(Locale.US, "%.2f", adelantos)}", 60f, y, paint)
        y += LINE_HEIGHT
        canvas.drawText("Pago:               S/ ${String.format(Locale.US, "%.2f", pago)}", 60f, y, paint)
        y += LINE_HEIGHT
        
        // Tercera línea divisoria
        paint.strokeWidth = 1f
        canvas.drawLine(60f, y - 5f, MARGIN_LEFT + boxWidth - 10f, y - 5f, paint)
        
        // Cálculo de saldo final
        val saldoFinal = totalAPagar - (adelantos + pago)
        canvas.drawText("Saldo final:        S/ ${String.format(Locale.US, "%.2f", saldoFinal)}", 60f, y, boldPaint)
        
        // Espacio para firma
        y += 60f
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        val firmaBoxWidth = 200f
        val firmaBoxHeight = 80f
        val firmaX = (MARGIN_RIGHT - MARGIN_LEFT - firmaBoxWidth) / 2 + MARGIN_LEFT
        canvas.drawRect(firmaX, y, firmaX + firmaBoxWidth, y + firmaBoxHeight, paint)
        paint.style = Paint.Style.FILL
        
        // Texto de firma
        y += firmaBoxHeight + 15f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("Firma", PAGE_WIDTH / 2f, y, paint)
        paint.textAlign = Paint.Align.LEFT
        
        // Si hay firma, dibujarla dentro del recuadro
        if (firma != null) {
            val scaledBitmap = firma.scale(firmaBoxWidth.toInt(), firmaBoxHeight.toInt())
            canvas.drawBitmap(scaledBitmap, firmaX, y - firmaBoxHeight - 15f, null)
        }
        
        // Fecha y hora
        y += 40f
        val now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
        canvas.drawText("Fecha y hora: $now", MARGIN_LEFT, y, paint)
        
        // Información de contacto (opcional)
        y += 40f
        val contactInfo = Paint().apply {
            color = Color.LTGRAY
            textSize = NORMAL_TEXT_SIZE * 0.9f
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("© SAM - Sistema de Administración de Maquila", PAGE_WIDTH / 2f, y, contactInfo)
        
        document.finishPage(page)
        Log.d(TAG, "Página finalizada exitosamente")
        
        // Verificar si es necesario generar páginas adicionales para los registros no mostrados
        // Implementar páginas adicionales aquí si es necesario
        
        // Guardar el documento
        Log.d(TAG, "Preparando para guardar el documento...")
        val directory = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        Log.d(TAG, "Directorio de destino: ${directory?.absolutePath}")
        
        if (directory == null) {
            Log.e(TAG, "Error: No se pudo obtener el directorio de documentos")
            document.close()
            return null
        }
        
        if (!directory.exists()) {
            Log.d(TAG, "Creando directorio: ${directory.absolutePath}")
            val created = directory.mkdirs()
            Log.d(TAG, "Directorio creado: $created")
        }
        
        val fileName = "reporte_${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))}.pdf"
        val file = File(directory, fileName)
        Log.d(TAG, "Ruta completa del archivo: ${file.absolutePath}")
        
        try {
            Log.d(TAG, "Escribiendo archivo PDF...")
            FileOutputStream(file).use {
                document.writeTo(it)
            }
            document.close()
            Log.i(TAG, "PDF generado exitosamente en: ${file.absolutePath}")
            Log.i(TAG, "Tamaño del archivo: ${file.length()} bytes")
            
            // Verificar que el archivo se creó correctamente
            if (file.exists() && file.length() > 0) {
                Log.i(TAG, "Verificación exitosa: El archivo PDF existe y tiene contenido")
                return file
            } else {
                Log.e(TAG, "Error: El archivo PDF no se creó correctamente o está vacío")
                return null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al guardar el PDF: ${e.message}", e)
            Log.e(TAG, "Tipo de excepción: ${e.javaClass.simpleName}")
            document.close()
            return null
        }
        } catch (e: Exception) {
            Log.e(TAG, "Error durante la generación del PDF: ${e.message}", e)
            Log.e(TAG, "Tipo de excepción: ${e.javaClass.simpleName}")
            return null
        } finally {
            Log.i(TAG, "=== FINALIZANDO GENERACIÓN DE PDF ===")
        }
    }
    
    /**
     * Genera un PDF simplificado con resumen de servicios
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun generarResumenPdf(
        registros: List<Registro>,
        saldoAnterior: Double,
        adelantos: Double,
        pago: Double,
        firma: Bitmap?
    ): File? {
        Log.i(TAG, "=== INICIANDO GENERACIÓN DE RESUMEN PDF ===")
        Log.d(TAG, "Total registros: ${registros.size}")
        Log.d(TAG, "Saldo anterior: $saldoAnterior")
        Log.d(TAG, "Adelantos: $adelantos")
        Log.d(TAG, "Pago: $pago")
        Log.d(TAG, "Firma presente: ${firma != null}")
        
        try {
            Log.d(TAG, "Creando documento PDF de resumen...")
            val document = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create() // A4
            val page = document.startPage(pageInfo)
            val canvas = page.canvas
            
            Log.d(TAG, "Página de resumen creada exitosamente")
        
        // Fondo
        canvas.drawRGB(18, 18, 18) // Color negro
        
        val paint = Paint().apply {
            color = Color.WHITE
            textSize = NORMAL_TEXT_SIZE
            isAntiAlias = true
        }
        
        val boldPaint = Paint().apply {
            color = Color.WHITE
            textSize = NORMAL_TEXT_SIZE
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        
        // Título
        boldPaint.textSize = HEADER_TEXT_SIZE
        val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val title = "RESUMEN DE SERVICIOS - ${LocalDate.now().format(dateFormatter)}"
        canvas.drawText(title, MARGIN_LEFT, 50f, boldPaint)
        
        // Separador
        paint.strokeWidth = 2f
        canvas.drawLine(MARGIN_LEFT, 60f, MARGIN_RIGHT, 60f, paint)
        
        // Agrupar registros por tipo de servicio
        val registrosPorTipo: Map<TipoServicio, List<Registro>> = registros.groupBy { it.tipo }
        
        var y = 90f
        var totalGeneral = 0.0
        
        // Iterar por cada tipo de servicio
        registrosPorTipo.forEach { entry: Map.Entry<TipoServicio, List<Registro>> ->
            val tipoServicio = entry.key
            val registrosTipo = entry.value
            boldPaint.textSize = SUBTITLE_TEXT_SIZE
            canvas.drawText(tipoServicio.nombre.uppercase(), MARGIN_LEFT, y, boldPaint)
            
            y += 10f
            paint.strokeWidth = 1f
            canvas.drawLine(MARGIN_LEFT, y, MARGIN_RIGHT, y, paint)
            
            // Cabecera de tabla
            y += 20f
            boldPaint.textSize = NORMAL_TEXT_SIZE
            canvas.drawText("FECHA", 60f, y, boldPaint)
            canvas.drawText("CANT.", 160f, y, boldPaint)
            canvas.drawText("PRECIO", 220f, y, boldPaint)
            canvas.drawText("TOTAL", 300f, y, boldPaint)
            
            // Datos
            y += 20f
            val dateFormat = DateTimeFormatter.ofPattern("dd/MM/yy")
            for (registro in registrosTipo) {
                canvas.drawText(
                    registro.fecha.format(dateFormat),
                    60f, y, paint
                )
                canvas.drawText(registro.cantidad.toString(), 160f, y, paint)
                canvas.drawText(
                    "S/ " + String.format(Locale.US, "%.2f", registro.precioUnitario),
                    220f, y, paint
                )
                canvas.drawText(
                    "S/ " + String.format(Locale.US, "%.2f", registro.total),
                    300f, y, paint
                )
                y += LINE_HEIGHT
            }
            
            // Total por tipo de servicio
            val totalTipo = registrosTipo.sumOf { it.total }
            val cantidadTipo = registrosTipo.sumOf { it.cantidad }
            totalGeneral += totalTipo
            
            // Línea de subtotal
            y += 5f
            paint.strokeWidth = 1f
            canvas.drawLine(60f, y - 10f, 400f, y - 10f, paint)
            
            boldPaint.textSize = NORMAL_TEXT_SIZE
            canvas.drawText(
                "TOTAL ${tipoServicio.nombre.uppercase()}: $cantidadTipo unid.   S/ ${String.format(Locale.US, "%.2f", totalTipo)}",
                60f, y, boldPaint
            )
            
            y += 30f
        }
        
        // Resumen general
        y += 30f
        boldPaint.textSize = SUBTITLE_TEXT_SIZE
        canvas.drawText("RESUMEN GENERAL", MARGIN_LEFT, y, boldPaint)
        
        // Separador
        y += 10f
        paint.strokeWidth = 1f
        canvas.drawLine(MARGIN_LEFT, y, MARGIN_RIGHT, y, paint)
        
        // Detalles de resumen
        y += 30f
        
        // Crear un marco para el resumen
        val boxTop = y - 20f
        val boxWidth = 300f
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.5f
        canvas.drawRect(MARGIN_LEFT, boxTop, MARGIN_LEFT + boxWidth, boxTop + 140f, paint)
        paint.style = Paint.Style.FILL
        
        // Contenido del resumen
        canvas.drawText("Total Servicios:    S/ ${String.format(Locale.US, "%.2f", totalGeneral)}", 60f, y, paint)
        y += LINE_HEIGHT
        canvas.drawText("Saldo anterior:     S/ ${String.format(Locale.US, "%.2f", saldoAnterior)}", 60f, y, paint)
        y += LINE_HEIGHT
        
        // Línea divisoria
        paint.strokeWidth = 1f
        canvas.drawLine(60f, y - 5f, MARGIN_LEFT + boxWidth - 10f, y - 5f, paint)
        
        val totalAPagar = totalGeneral + saldoAnterior
        canvas.drawText("Total a pagar:      S/ ${String.format(Locale.US, "%.2f", totalAPagar)}", 60f, y, boldPaint)
        y += LINE_HEIGHT
        canvas.drawText("Adelantos:          S/ ${String.format(Locale.US, "%.2f", adelantos)}", 60f, y, paint)
        y += LINE_HEIGHT
        canvas.drawText("Pago:               S/ ${String.format(Locale.US, "%.2f", pago)}", 60f, y, paint)
        y += LINE_HEIGHT
        
        // Línea divisoria
        paint.strokeWidth = 1f
        canvas.drawLine(60f, y - 5f, MARGIN_LEFT + boxWidth - 10f, y - 5f, paint)
        
        // Saldo final
        val saldoFinal = totalAPagar - (adelantos + pago)
        canvas.drawText("Saldo final:        S/ ${String.format(Locale.US, "%.2f", saldoFinal)}", 60f, y, boldPaint)
        
        // Firma
        y += 60f
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        val firmaBoxWidth = 200f
        val firmaBoxHeight = 80f
        val firmaX = (MARGIN_RIGHT - MARGIN_LEFT - firmaBoxWidth) / 2 + MARGIN_LEFT
        canvas.drawRect(firmaX, y, firmaX + firmaBoxWidth, y + firmaBoxHeight, paint)
        paint.style = Paint.Style.FILL
        
        // Texto de firma
        y += firmaBoxHeight + 15f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("Firma", PAGE_WIDTH / 2f, y, paint)
        paint.textAlign = Paint.Align.LEFT
        
        // Si hay firma, dibujarla dentro del recuadro
        if (firma != null) {
            val scaledBitmap = firma.scale(firmaBoxWidth.toInt(), firmaBoxHeight.toInt())
            canvas.drawBitmap(scaledBitmap, firmaX, y - firmaBoxHeight - 15f, null)
        }
        
        // Fecha y hora
        y += 40f
        val now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
        canvas.drawText("Fecha y hora: $now", MARGIN_LEFT, y, paint)
        
        document.finishPage(page)
        Log.d(TAG, "Página de resumen finalizada exitosamente")
        
        // Guardar el documento
        Log.d(TAG, "Preparando para guardar el resumen PDF...")
        val directory = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        Log.d(TAG, "Directorio de destino para resumen: ${directory?.absolutePath}")
        
        if (directory == null) {
            Log.e(TAG, "Error: No se pudo obtener el directorio de documentos para resumen")
            document.close()
            return null
        }
        
        if (!directory.exists()) {
            Log.d(TAG, "Creando directorio para resumen: ${directory.absolutePath}")
            val created = directory.mkdirs()
            Log.d(TAG, "Directorio para resumen creado: $created")
        }
        
        val fileName = "resumen_${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))}.pdf"
        val file = File(directory, fileName)
        Log.d(TAG, "Ruta completa del resumen PDF: ${file.absolutePath}")
        
        try {
            Log.d(TAG, "Escribiendo archivo de resumen PDF...")
            FileOutputStream(file).use {
                document.writeTo(it)
            }
            document.close()
            Log.i(TAG, "Resumen PDF generado exitosamente en: ${file.absolutePath}")
            Log.i(TAG, "Tamaño del archivo de resumen: ${file.length()} bytes")
            
            // Verificar que el archivo se creó correctamente
            if (file.exists() && file.length() > 0) {
                Log.i(TAG, "Verificación exitosa: El resumen PDF existe y tiene contenido")
                return file
            } else {
                Log.e(TAG, "Error: El resumen PDF no se creó correctamente o está vacío")
                return null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al guardar el resumen PDF: ${e.message}", e)
            Log.e(TAG, "Tipo de excepción en resumen: ${e.javaClass.simpleName}")
            document.close()
            return null
        }
        } catch (e: Exception) {
            Log.e(TAG, "Error durante la generación del resumen PDF: ${e.message}", e)
            Log.e(TAG, "Tipo de excepción en resumen: ${e.javaClass.simpleName}")
            return null
        } finally {
            Log.i(TAG, "=== FINALIZANDO GENERACIÓN DE RESUMEN PDF ===")
        }
    }
}