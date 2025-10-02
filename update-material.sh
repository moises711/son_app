#!/bin/bash
# Script para reemplazar todas las instancias de Material a Material3

# Reemplazar colores
find /home/sonjin/AndroidStudioProjects/sam -name "*.kt" -exec sed -i 's/MaterialTheme\.colors\.onSurface/MaterialTheme.colorScheme.onSurface/g' {} \;
find /home/sonjin/AndroidStudioProjects/sam -name "*.kt" -exec sed -i 's/backgroundColor = /containerColor = /g' {} \;
find /home/sonjin/AndroidStudioProjects/sam -name "*.kt" -exec sed -i 's/TextFieldDefaults\. outlinedTextFieldColors/TextFieldDefaults.outlinedTextFieldColors/g' {} \;
find /home/sonjin/AndroidStudioProjects/sam -name "*.kt" -exec sed -i 's/MaterialTheme\.colors\.background/MaterialTheme.colorScheme.background/g' {} \;
find /home/sonjin/AndroidStudioProjects/sam -name "*.kt" -exec sed -i 's/MaterialTheme\.colors\.surface/MaterialTheme.colorScheme.surface/g' {} \;