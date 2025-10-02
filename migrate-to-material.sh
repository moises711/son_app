#!/bin/bash
# Script completo para migrar de Material3 a Material2

echo "Paso 1: Actualizando importaciones..."
# Reemplazar importaciones
find . -name "*.kt" -exec sed -i 's/import androidx.compose.material3\./import androidx.compose.material./g' {} \;
find . -name "*.kt" -exec sed -i 's/androidx.compose.material3.MaterialTheme/androidx.compose.material.MaterialTheme/g' {} \;
find . -name "*.kt" -exec sed -i 's/androidx.compose.material3.Surface/androidx.compose.material.Surface/g' {} \;
find . -name "*.kt" -exec sed -i 's/androidx.compose.material3.Text/androidx.compose.material.Text/g' {} \;
find . -name "*.kt" -exec sed -i 's/androidx.compose.material3.Button/androidx.compose.material.Button/g' {} \;
find . -name "*.kt" -exec sed -i 's/androidx.compose.material3.ButtonDefaults/androidx.compose.material.ButtonDefaults/g' {} \;
find . -name "*.kt" -exec sed -i 's/androidx.compose.material3.OutlinedTextField/androidx.compose.material.OutlinedTextField/g' {} \;
find . -name "*.kt" -exec sed -i 's/androidx.compose.material3.TextFieldDefaults/androidx.compose.material.TextFieldDefaults/g' {} \;
find . -name "*.kt" -exec sed -i 's/androidx.compose.material3.AlertDialog/androidx.compose.material.AlertDialog/g' {} \;
find . -name "*.kt" -exec sed -i 's/androidx.compose.material3.TextButton/androidx.compose.material.TextButton/g' {} \;

echo "Paso 2: Actualizando referencias a colorScheme..."
# Reemplazar referencias a colorScheme por colors
find . -name "*.kt" -exec sed -i 's/MaterialTheme\.colorScheme\./MaterialTheme.colors./g' {} \;

echo "Paso 3: Actualizando containerColor a backgroundColor..."
# Reemplazar containerColor por backgroundColor
find . -name "*.kt" -exec sed -i 's/containerColor =/backgroundColor =/g' {} \;

echo "Paso 4: Actualizando darkColorScheme y lightColorScheme..."
# Cambiar darkColorScheme a darkColors
find . -name "*.kt" -exec sed -i 's/darkColorScheme(/darkColors(/g' {} \;
find . -name "*.kt" -exec sed -i 's/lightColorScheme(/lightColors(/g' {} \;

echo "Paso 5: Actualizando referencias a la tipografía..."
# Actualizar tipografía
find . -name "*.kt" -exec sed -i 's/typography\.headlineSmall/typography.h6/g' {} \;
find . -name "*.kt" -exec sed -i 's/typography\.headlineMedium/typography.h5/g' {} \;
find . -name "*.kt" -exec sed -i 's/typography\.titleLarge/typography.h5/g' {} \;
find . -name "*.kt" -exec sed -i 's/typography\.titleMedium/typography.h6/g' {} \;
find . -name "*.kt" -exec sed -i 's/typography\.bodyLarge/typography.body1/g' {} \;
find . -name "*.kt" -exec sed -i 's/typography\.bodyMedium/typography.body2/g' {} \;
find . -name "*.kt" -exec sed -i 's/typography\.labelSmall/typography.caption/g' {} \;

echo "Paso 6: Actualizando propiedades de los temas..."
# Reemplazar tertiary por primaryVariant
find . -name "*.kt" -exec sed -i 's/tertiary =/primaryVariant =/g' {} \;

echo "Paso 7: Asegurar que MaterialTheme use colors en lugar de colorScheme..."
# Asegurar que MaterialTheme use colors
find . -name "Theme.kt" -exec sed -i 's/colorScheme = colorScheme/colors = colorScheme/g' {} \;

echo "Migración completada."