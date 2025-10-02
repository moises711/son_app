#!/bin/bash
# Script para corregir las referencias a la tipografía de Material3 en Material2

# headlineSmall -> h6
find /home/sonjin/AndroidStudioProjects/sam -name "*.kt" -exec sed -i 's/typography\.headlineSmall/typography.h6/g' {} \;

# headlineMedium -> h5
find /home/sonjin/AndroidStudioProjects/sam -name "*.kt" -exec sed -i 's/typography\.headlineMedium/typography.h5/g' {} \;

# titleLarge -> h5
find /home/sonjin/AndroidStudioProjects/sam -name "*.kt" -exec sed -i 's/typography\.titleLarge/typography.h5/g' {} \;

# titleMedium -> h6
find /home/sonjin/AndroidStudioProjects/sam -name "*.kt" -exec sed -i 's/typography\.titleMedium/typography.h6/g' {} \;

# bodyLarge -> body1
find /home/sonjin/AndroidStudioProjects/sam -name "*.kt" -exec sed -i 's/typography\.bodyLarge/typography.body1/g' {} \;

# bodyMedium -> body2
find /home/sonjin/AndroidStudioProjects/sam -name "*.kt" -exec sed -i 's/typography\.bodyMedium/typography.body2/g' {} \;

# labelSmall -> caption
find /home/sonjin/AndroidStudioProjects/sam -name "*.kt" -exec sed -i 's/typography\.labelSmall/typography.caption/g' {} \;