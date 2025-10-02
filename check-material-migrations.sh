#!/bin/bash

echo "Verificando referencias restantes de Material3..."

# Verificar si hay importaciones de Material3
MATERIAL3_IMPORTS=$(grep -r "import androidx.compose.material3" --include="*.kt" .)
if [ -n "$MATERIAL3_IMPORTS" ]; then
    echo "Aún hay importaciones de Material3:"
    echo "$MATERIAL3_IMPORTS"
    echo "Corrigiendo importaciones..."
    find . -name "*.kt" -exec sed -i 's/import androidx.compose.material3\./import androidx.compose.material./g' {} \;
fi

# Verificar referencias a colorScheme
COLOR_SCHEME=$(grep -r "colorScheme" --include="*.kt" . | grep -v "grep" | grep -v "sh")
if [ -n "$COLOR_SCHEME" ]; then
    echo "Aún hay referencias a colorScheme:"
    echo "$COLOR_SCHEME"
    echo "Corrigiendo referencias..."
    find . -name "*.kt" -exec sed -i 's/MaterialTheme\.colorScheme\./MaterialTheme.colors./g' {} \;
    find . -name "*.kt" -exec sed -i 's/colorScheme =/colors =/g' {} \;
fi

# Verificar referencias a containerColor
CONTAINER_COLOR=$(grep -r "containerColor" --include="*.kt" . | grep -v "grep" | grep -v "sh")
if [ -n "$CONTAINER_COLOR" ]; then
    echo "Aún hay referencias a containerColor:"
    echo "$CONTAINER_COLOR"
    echo "Corrigiendo referencias..."
    find . -name "*.kt" -exec sed -i 's/containerColor =/backgroundColor =/g' {} \;
fi

# Verificar referencias a tipografía de Material3
TYPOGRAPHY=$(grep -r -E "headlineSmall|headlineMedium|titleLarge|bodyLarge|bodyMedium" --include="*.kt" . | grep -v "grep" | grep -v "sh")
if [ -n "$TYPOGRAPHY" ]; then
    echo "Aún hay referencias a tipografía de Material3:"
    echo "$TYPOGRAPHY"
    echo "Corrigiendo referencias..."
    find . -name "*.kt" -exec sed -i 's/typography\.headlineSmall/typography.h6/g' {} \;
    find . -name "*.kt" -exec sed -i 's/typography\.headlineMedium/typography.h5/g' {} \;
    find . -name "*.kt" -exec sed -i 's/typography\.titleLarge/typography.h5/g' {} \;
    find . -name "*.kt" -exec sed -i 's/typography\.titleMedium/typography.h6/g' {} \;
    find . -name "*.kt" -exec sed -i 's/typography\.bodyLarge/typography.body1/g' {} \;
    find . -name "*.kt" -exec sed -i 's/typography\.bodyMedium/typography.body2/g' {} \;
fi

echo "Verificación y corrección completadas."