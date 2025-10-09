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
import java.text.SimpleDateFormat
import java.util.Date
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
     * Genera un PDF con los registros y pagos usando la lógica matemática especificada
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun generarInformePDF(
        registros: List<Registro>,
        pagos: List<Pago>,
        saldoAnterior: Double = 0.0,
        adelantos: Double = 0.0,
        pagosTotal: Double = 0.0,
        saldoFinal: Double = 0.0
    ): File? {
        Log.i(TAG, "=== INICIANDO GENERACIÓN DE INFORME PDF ===")
        Log.d(TAG, "Registros: ${registros.size}")
        Log.d(TAG, "Pagos: ${pagos.size}")
        Log.d(TAG, "Saldo anterior: $saldoAnterior")
        Log.d(TAG, "Adelantos: $adelantos")
        Log.d(TAG, "Pagos totales: $pagosTotal")
        Log.d(TAG, "Saldo final calculado: $saldoFinal")
        
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
            val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val title = "INFORME DE CIERRE - ${dateFormatter.format(Date())}"
            canvas.drawText(title, MARGIN_LEFT, 50f, boldPaint)
            
            // Separador
            paint.strokeWidth = 2f
            canvas.drawLine(MARGIN_LEFT, 60f, MARGIN_RIGHT, 60f, paint)
            
            // Agrupar registros por tipo de servicio
            val registrosPorTipo: Map<TipoServicio, List<Registro>> = registros.groupBy { it.tipo }
            
            var y = 90f
            var totalGeneralServicios = 0.0
            
            // Sección: Saldo Anterior
            boldPaint.textSize = SUBTITLE_TEXT_SIZE
            canvas.drawText("SALDO ANTERIOR", MARGIN_LEFT, y, boldPaint)
            y += 25f
            
            canvas.drawText("Saldo del período anterior: S/ ${String.format(Locale.US, "%.2f", saldoAnterior)}", MARGIN_LEFT + 20f, y, paint)
            
            y += 30f
            
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
                canvas.drawText("FECHA", MARGIN_LEFT + 10f, y, boldPaint)
                canvas.drawText("CANT.", MARGIN_LEFT + 110f, y, boldPaint)
                canvas.drawText("PRECIO", MARGIN_LEFT + 170f, y, boldPaint)
                canvas.drawText("TOTAL", MARGIN_LEFT + 250f, y, boldPaint)
                
                // Datos
                y += 20f
                val dateFormat = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
                for (registro in registrosTipo) {
                    canvas.drawText(
                        dateFormat.format(registro.fecha),
                        MARGIN_LEFT + 10f, y, paint
                    )
                    canvas.drawText(registro.cantidad.toString(), MARGIN_LEFT + 110f, y, paint)
                    canvas.drawText(
                        "S/ " + String.format(Locale.US, "%.2f", registro.precioUnitario),
                        MARGIN_LEFT + 170f, y, paint
                    )
                    canvas.drawText(
                        "S/ " + String.format(Locale.US, "%.2f", registro.total),
                        MARGIN_LEFT + 250f, y, paint
                    )
                    y += LINE_HEIGHT
                }
                
                // Total por tipo de servicio
                val totalTipo = registrosTipo.sumOf { it.total }
                val cantidadTipo = registrosTipo.sumOf { it.cantidad }
                totalGeneralServicios += totalTipo
                
                // Línea de subtotal
                y += 5f
                paint.strokeWidth = 1f
                canvas.drawLine(MARGIN_LEFT + 10f, y - 10f, MARGIN_LEFT + 300f, y - 10f, paint)
                
                boldPaint.textSize = NORMAL_TEXT_SIZE
                canvas.drawText(
                    "TOTAL ${tipoServicio.nombre.uppercase()}: $cantidadTipo unid.   S/ ${String.format(Locale.US, "%.2f", totalTipo)}",
                    MARGIN_LEFT + 10f, y, boldPaint
                )
                
                y += 40f
            }
            
            // Resumen general de servicios
            boldPaint.textSize = SUBTITLE_TEXT_SIZE
            canvas.drawText("RESUMEN GENERAL", MARGIN_LEFT, y, boldPaint)
            y += 25f
            
            // Tabla de resumen
            val boxWidth = 400f
            paint.textAlign = Paint.Align.LEFT
            
            // Servicios actuales
            canvas.drawText("Servicios actuales:   S/ ${String.format(Locale.US, "%.2f", totalGeneralServicios)}", MARGIN_LEFT + 10f, y, paint)
            y += LINE_HEIGHT
            
            // Saldo anterior
            canvas.drawText("Saldo anterior:       S/ ${String.format(Locale.US, "%.2f", saldoAnterior)}", MARGIN_LEFT + 10f, y, paint)
            y += LINE_HEIGHT
            
            // Primera línea divisoria
            paint.strokeWidth = 1f
            canvas.drawLine(MARGIN_LEFT + 10f, y - 5f, MARGIN_LEFT + 300f, y - 5f, paint)
            
            // Total a cobrar (servicios actuales + saldo anterior)
            val totalACobrar = totalGeneralServicios + saldoAnterior
            boldPaint.textSize = NORMAL_TEXT_SIZE
            canvas.drawText("Total a cobrar:      S/ ${String.format(Locale.US, "%.2f", totalACobrar)}", MARGIN_LEFT + 10f, y, boldPaint)
            y += LINE_HEIGHT * 1.5f
            
            // Deducciones (adelantos y pagos)
            canvas.drawText("Adelantos:           S/ ${String.format(Locale.US, "%.2f", adelantos)}", MARGIN_LEFT + 10f, y, paint)
            y += LINE_HEIGHT
            
            canvas.drawText("Pagos realizados:    S/ ${String.format(Locale.US, "%.2f", pagosTotal)}", MARGIN_LEFT + 10f, y, paint)
            y += LINE_HEIGHT
            
            // Segunda línea divisoria
            paint.strokeWidth = 1f
            canvas.drawLine(MARGIN_LEFT + 10f, y - 5f, MARGIN_LEFT + 300f, y - 5f, paint)
            
            // Saldo final (total a cobrar - adelantos - pagos)
            boldPaint.textSize = NORMAL_TEXT_SIZE
            canvas.drawText("SALDO FINAL:        S/ ${String.format(Locale.US, "%.2f", saldoFinal)}", MARGIN_LEFT + 10f, y, boldPaint)
            y += LINE_HEIGHT * 2
            
            // Detalle de pagos
            if (pagos.isNotEmpty()) {
                boldPaint.textSize = SUBTITLE_TEXT_SIZE
                canvas.drawText("DETALLE DE PAGOS", MARGIN_LEFT, y, boldPaint)
                y += 20f
                
                // Cabecera de tabla
                boldPaint.textSize = NORMAL_TEXT_SIZE
                canvas.drawText("FECHA", MARGIN_LEFT + 10f, y, boldPaint)
                canvas.drawText("MONTO", MARGIN_LEFT + 110f, y, boldPaint)
                canvas.drawText("OBSERVACIÓN", MARGIN_LEFT + 170f, y, boldPaint)
                y += 20f
                
                // Datos de pagos
                val dateFormat = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
                for (pago in pagos) {
                    canvas.drawText(
                        dateFormat.format(pago.fecha),
                        MARGIN_LEFT + 10f, y, paint
                    )
                    canvas.drawText(
                        "S/ " + String.format(Locale.US, "%.2f", pago.monto),
                        MARGIN_LEFT + 110f, y, paint
                    )
                    canvas.drawText(
                        pago.observacion,
                        MARGIN_LEFT + 170f, y, paint
                    )
                    y += LINE_HEIGHT
                }
            }
            
            // Nota sobre el saldo
            y += 30f
            paint.textSize = NORMAL_TEXT_SIZE
            canvas.drawText("Nota: El saldo final se considerará como saldo inicial en el próximo período", MARGIN_LEFT, y, paint)
            
            // Firma
            y += 40f
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
            canvas.drawText("Firma de conformidad", PAGE_WIDTH / 2f, y, paint)
            paint.textAlign = Paint.Align.LEFT
            
            // Fecha y hora de generación
            y += 30f
            val now = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
            canvas.drawText("Generado el: $now", MARGIN_LEFT, y, paint)
            
            document.finishPage(page)
            Log.d(TAG, "Páginas del informe generadas exitosamente")
            
            // Guardar el documento
            Log.d(TAG, "Preparando para guardar el PDF...")
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
            
            val fileNameFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            val fileName = "informe_${fileNameFormat.format(Date())}.pdf"
            val file = File(directory, fileName)
            Log.d(TAG, "Ruta completa del PDF: ${file.absolutePath}")
            
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
            Log.i(TAG, "=== FINALIZANDO GENERACIÓN DE INFORME PDF ===")
        }
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
        val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val title = "REPORTE DE SERVICIOS - ${dateFormatter.format(Date())}"
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
        val dateFormat = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
        
        // Mostrar primera página de bordados
        if (bordadosPaginados.isNotEmpty()) {
            bordadosPaginados[0].forEach { registro ->
                canvas.drawText(
                    dateFormat.format(registro.fecha),
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
                    dateFormat.format(registro.fecha),
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
        val now = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
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
        
        val fileNameFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        val fileName = "reporte_${fileNameFormat.format(Date())}.pdf"
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
        val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val title = "RESUMEN DE SERVICIOS - ${dateFormatter.format(Date())}"
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
            val dateFormat = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
            for (registro in registrosTipo) {
                canvas.drawText(
                    dateFormat.format(registro.fecha),
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
        val now = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
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
        
        val fileNameFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        val fileName = "resumen_${fileNameFormat.format(Date())}.pdf"
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