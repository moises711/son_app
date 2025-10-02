package com.moises.sam.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
//import androidx.compose.material.dynamicDarkColorScheme
//import androidx.compose.material.dynamicLightColorScheme
import androidx.compose.material.lightColors
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColors = darkColors(
    primary = PrimaryColor,
    secondary = SecondaryColor,
    background = DarkBackground,
    surface = DarkSurface,
    onSurface = BorderWhite
)

private val LightColors = lightColors(
    primary = Purple40,
    secondary = PurpleGrey40,
    primaryVariant = PrimaryVariant

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun SamTheme(
    // Siempre usamos tema oscuro para esta app
    darkTheme: Boolean = true,
    // No usamos colores dinámicos para mantener nuestra paleta personalizada
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colors = DarkColors

    MaterialTheme(
        colors = colors,
        typography = Typography,
        content = content
    )
}