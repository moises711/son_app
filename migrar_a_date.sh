#!/bin/bash
# Este script busca y reemplaza todas las ocurrencias de LocalDate con Date
# Y reemplaza los métodos específicos de LocalDate con equivalentes de Date

# Reemplazar imports
find . -name "*.kt" -type f -exec sed -i 's/import java.time.LocalDate/import java.util.Date/g' {} \;
find . -name "*.kt" -type f -exec sed -i 's/import java.time.format.DateTimeFormatter/import java.text.SimpleDateFormat\nimport java.util.Locale/g' {} \;

# Reemplazar LocalDate.now() con Date()
find . -name "*.kt" -type f -exec sed -i 's/LocalDate.now()/Date()/g' {} \;

# Reemplazar format con SimpleDateFormat
find . -name "*.kt" -type f -exec sed -i 's/\.format(DateTimeFormatter.ISO_LOCAL_DATE)/\)\nval dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())\ndateFormat.format(/g' {} \;

# Reemplazar parse con SimpleDateFormat
find . -name "*.kt" -type f -exec sed -i 's/LocalDate.parse(\([^,]*\), DateTimeFormatter.ISO_LOCAL_DATE)/val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())\ndateFormat.parse(\1) ?: Date()/g' {} \;

echo "Reemplazo completado. Verifica los archivos manualmente."