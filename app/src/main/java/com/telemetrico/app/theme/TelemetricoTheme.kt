package com.telemetrico.app.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val F1WarmRed = Color(0xFFFF1E00)
val F1CarbonBlack = Color(0xFF15151E)
val F1Carbon90 = Color(0xFF2C2C34)
val F1Carbon70 = Color(0xFF5B5B61)
val F1Carbon50 = Color(0xFF89898E)
val F1OffWhite = Color(0xFFF7F4F1)
val F1HighVisWhite = Color(0xFFFFFFFF)
val DrsCyan = Color(0xFF00E5FF)
val TelemetryGreen = Color(0xFF00E58B)
val WarningYellow = Color(0xFFFFD600)

private val TelemetricoColors = darkColorScheme(
    primary = F1WarmRed,
    background = F1CarbonBlack,
    surface = F1CarbonBlack,
    onPrimary = F1CarbonBlack,
    onBackground = F1HighVisWhite,
    onSurface = F1HighVisWhite,
)

@Composable
fun TelemetricoTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = TelemetricoColors,
        content = content,
    )
}
