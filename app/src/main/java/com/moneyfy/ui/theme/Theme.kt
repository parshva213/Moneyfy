package com.moneyfy.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Custom color palette to hold extended colors
data class MoneyfyColors(
    val background: Color,
    val surface: Color,
    val card: Color,
    val surfaceVariant: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val income: Color = IncomeGreen,
    val expense: Color = ExpenseRed,
    val transfer: Color = TransferBlue
)

val LocalMoneyfyColors = staticCompositionLocalOf<MoneyfyColors> {
    error("No MoneyfyColors provided")
}

private val DarkCustomColors = MoneyfyColors(
    background = DarkBackground,
    surface = DarkSurface,
    card = DarkCard,
    surfaceVariant = DarkSurfaceVariant,
    textPrimary = TextPrimaryDark,
    textSecondary = TextSecondaryDark,
    textMuted = TextMutedDark
)

private val LightCustomColors = MoneyfyColors(
    background = LightBackground,
    surface = LightSurface,
    card = LightCard,
    surfaceVariant = LightSurfaceVariant,
    textPrimary = TextPrimaryLight,
    textSecondary = TextSecondaryLight,
    textMuted = TextMutedLight
)

private val DarkColorScheme = darkColorScheme(
    primary = Primary,
    onPrimary = Color.Black,
    primaryContainer = PrimaryDark,
    onPrimaryContainer = PrimaryLight,
    secondary = Accent,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF3D2D6B),
    onSecondaryContainer = AccentLight,
    tertiary = TransferBlue,
    onTertiary = Color.White,
    background = DarkBackground,
    onBackground = TextPrimaryDark,
    surface = DarkSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondaryDark,
    outline = TextMutedDark,
    error = ExpenseRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryDark,
    onPrimary = Color.White,
    primaryContainer = PrimaryLight,
    onPrimaryContainer = PrimaryDark,
    secondary = Accent,
    onSecondary = Color.White,
    secondaryContainer = AccentLight,
    onSecondaryContainer = Color(0xFF3D2D6B),
    tertiary = TransferBlue,
    onTertiary = Color.White,
    background = LightBackground,
    onBackground = TextPrimaryLight,
    surface = LightSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = TextSecondaryLight,
    outline = TextMutedLight,
    error = ExpenseRed,
    onError = Color.White
)

object MoneyfyTheme {
    val colors: MoneyfyColors
        @Composable
        get() = LocalMoneyfyColors.current
}

@Composable
fun MoneyfyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val customColors = if (darkTheme) DarkCustomColors else LightCustomColors

    CompositionLocalProvider(
        LocalMoneyfyColors provides customColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}