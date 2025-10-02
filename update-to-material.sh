#!/bin/bash
# Script para cambiar de Material3 a Material

# Reemplazar importaciones
find /home/sonjin/AndroidStudioProjects/sam -name "*.kt" -exec sed -i 's/import androidx.compose.material3\./import androidx.compose.material./g' {} \;
find /home/sonjin/AndroidStudioProjects/sam -name "*.kt" -exec sed -i 's/androidx.compose.material3.MaterialTheme/androidx.compose.material.MaterialTheme/g' {} \;
find /home/sonjin/AndroidStudioProjects/sam -name "*.kt" -exec sed -i 's/androidx.compose.material3.Surface/androidx.compose.material.Surface/g' {} \;
find /home/sonjin/AndroidStudioProjects/sam -name "*.kt" -exec sed -i 's/androidx.compose.material3.Text/androidx.compose.material.Text/g' {} \;
find /home/sonjin/AndroidStudioProjects/sam -name "*.kt" -exec sed -i 's/androidx.compose.material3.Button/androidx.compose.material.Button/g' {} \;
find /home/sonjin/AndroidStudioProjects/sam -name "*.kt" -exec sed -i 's/androidx.compose.material3.ButtonDefaults/androidx.compose.material.ButtonDefaults/g' {} \;
find /home/sonjin/AndroidStudioProjects/sam -name "*.kt" -exec sed -i 's/androidx.compose.material3.OutlinedTextField/androidx.compose.material.OutlinedTextField/g' {} \;
find /home/sonjin/AndroidStudioProjects/sam -name "*.kt" -exec sed -i 's/androidx.compose.material3.TextFieldDefaults/androidx.compose.material.TextFieldDefaults/g' {} \;
find /home/sonjin/AndroidStudioProjects/sam -name "*.kt" -exec sed -i 's/androidx.compose.material3.AlertDialog/androidx.compose.material.AlertDialog/g' {} \;
find /home/sonjin/AndroidStudioProjects/sam -name "*.kt" -exec sed -i 's/androidx.compose.material3.TextButton/androidx.compose.material.TextButton/g' {} \;

# Reemplazar referencias a colorScheme por colors
find /home/sonjin/AndroidStudioProjects/sam -name "*.kt" -exec sed -i 's/MaterialTheme\.colorScheme\./MaterialTheme.colors./g' {} \;

# Reemplazar containerColor por backgroundColor
find /home/sonjin/AndroidStudioProjects/sam -name "*.kt" -exec sed -i 's/containerColor =/backgroundColor =/g' {} \;

# Cambios en el archivo Theme.kt
find /home/sonjin/AndroidStudioProjects/sam -name "Theme.kt" -exec sed -i 's/darkColorScheme(/darkColors(/g' {} \;
find /home/sonjin/AndroidStudioProjects/sam -name "Theme.kt" -exec sed -i 's/lightColorScheme(/lightColors(/g' {} \;