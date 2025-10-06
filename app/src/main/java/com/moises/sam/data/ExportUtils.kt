package com.moises.sam.data

import android.content.Context
import android.os.Environment
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Locale
import com.moises.sam.model.Registro
import com.moises.sam.model.Pago

object ExportUtils {
    fun exportarDatosCSV(context: Context, registros: List<Registro>, pagos: List<Pago>): File? {
        val exportDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "SAMApp")
        if (!exportDir.exists()) {
            exportDir.mkdirs()
        }
        val file = File(exportDir, "respaldo_sam_${System.currentTimeMillis()}.csv")
        try {
            FileWriter(file).use { writer ->
                // Encabezados
                writer.appendLine("Tipo,ID,Fecha,Cantidad,Total,Pagado,Monto,Observacion")
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                // Registros
                for (r in registros) {
                    writer.appendLine("REGISTRO,${r.id},${dateFormat.format(r.fecha)},${r.cantidad},${r.total},${r.isPagado},,,")
                }
                // Pagos
                for (p in pagos) {
                    writer.appendLine("PAGO,${p.id},${dateFormat.format(p.fecha)},,,,${p.monto},${p.observacion}")
                }
            }
            return file
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}
