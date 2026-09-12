package com.vitacare.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Emerald = Color(0xFF0E9F6E)
val EmeraldDark = Color(0xFF0B7A55)
val Mint = Color(0xFFDFF5EB)
val MintSoft = Color(0xFFEAF8F1)
val Bg = Color(0xFFF6FBF8)
val Ink = Color(0xFF12241E)
val InkSoft = Color(0xFF5B6B64)

private val LightColors = lightColorScheme(
    primary = Emerald,
    onPrimary = Color.White,
    primaryContainer = Mint,
    onPrimaryContainer = Ink,
    secondary = Emerald,
    secondaryContainer = MintSoft,
    background = Bg,
    onBackground = Ink,
    surface = Color.White,
    onSurface = Ink,
    onSurfaceVariant = InkSoft,
    surfaceVariant = MintSoft,
    outline = Color(0xFFCFE5DA)
)

@Composable
fun VitaCareTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = LightColors, typography = Typography(), content = content)
}
