package br.com.sensorauto.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = OrangeAmber,
    onPrimary = SurfaceDark,
    secondary = YellowWait,
    tertiary = GreenSuccess,
    background = SurfaceDark,
    surface = SurfaceDark
)

private val LightColorScheme = lightColorScheme(
    primary = OrangeAmber,
    onPrimary = SurfaceLight,
    secondary = YellowWait,
    tertiary = GreenSuccess,
    background = SurfaceLight,
    surface = SurfaceLight
)

@Composable
fun SensorAutoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
