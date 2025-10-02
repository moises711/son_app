#!/bin/bash
# Script para corregir posibles problemas con componentes Material2 vs Material3

# Asegurarse de que estamos usando las APIs correctas de Material
find /home/sonjin/AndroidStudioProjects/sam -name "*.kt" -exec sed -i 's/TextFieldDefaults\.outlinedTextFieldColors(/TextFieldDefaults.outlinedTextFieldColors(/g' {} \;
find /home/sonjin/AndroidStudioProjects/sam -name "*.kt" -exec sed -i 's/TextField(/TextField(/g' {} \;
find /home/sonjin/AndroidStudioProjects/sam -name "*.kt" -exec sed -i 's/TextButton(/TextButton(/g' {} \;
find /home/sonjin/AndroidStudioProjects/sam -name "*.kt" -exec sed -i 's/AlertDialog(/AlertDialog(/g' {} \;

# Asegurarse de que estamos usando correctamente TabRow (puede diferir entre Material2 y Material3)
find /home/sonjin/AndroidStudioProjects/sam -name "*.kt" -exec sed -i 's/TabRow(/TabRow(/g' {} \;
find /home/sonjin/AndroidStudioProjects/sam -name "*.kt" -exec sed -i 's/containerColor =/backgroundColor =/g' {} \;
find /home/sonjin/AndroidStudioProjects/sam -name "*.kt" -exec sed -i 's/contentColor =/contentColor =/g' {} \;