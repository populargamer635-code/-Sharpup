package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = SparkCobalt,
    onPrimary = Color.White,
    primaryContainer = SparkCobaltContainer,
    onPrimaryContainer = SparkOnCobaltContainer,
    secondary = SparkAmber,
    onSecondary = SparkOnAmberContainer,
    secondaryContainer = SparkAmberContainer,
    onSecondaryContainer = SparkOnAmberContainer,
    tertiary = SparkMint,
    onTertiary = Color.White,
    tertiaryContainer = SparkMintContainer,
    onTertiaryContainer = SparkOnMintContainer,
    error = SparkCoral,
    onError = Color.White,
    errorContainer = SparkCoralContainer,
    onErrorContainer = SparkOnCoralContainer,
    background = SparkCreamBg,
    onBackground = SparkInk,
    surface = SparkSurface,
    onSurface = SparkInk,
    surfaceVariant = SparkSurfaceVariant,
    onSurfaceVariant = SparkMutedInk,
    outline = SparkOutline
)

private val DarkColorScheme = darkColorScheme(
    primary = SparkCobaltLight,
    onPrimary = Color(0xFF0A1D4D),
    primaryContainer = Color(0xFF1E3A8A),
    onPrimaryContainer = Color(0xFFDBEAFE),
    secondary = SparkSunnyYellow,
    onSecondary = Color(0xFF422006),
    secondaryContainer = Color(0xFF78350F),
    onSecondaryContainer = Color(0xFFFEF3C7),
    tertiary = SparkMint,
    onTertiary = Color(0xFF022C22),
    tertiaryContainer = Color(0xFF065F46),
    onTertiaryContainer = Color(0xFFD1FAE5),
    error = SparkCoral,
    onError = Color(0xFF4C0519),
    errorContainer = Color(0xFF881337),
    onErrorContainer = Color(0xFFFFE4E6),
    background = SparkDarkBg,
    onBackground = SparkDarkInk,
    surface = SparkDarkSurface,
    onSurface = SparkDarkInk,
    surfaceVariant = SparkDarkSurfaceVariant,
    onSurfaceVariant = SparkDarkMutedInk,
    outline = SparkDarkOutline
)

val MathSparkShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun MathSparkTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = MathSparkShapes,
        content = content
    )
}
