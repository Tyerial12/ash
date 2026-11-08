package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val WastelandColorScheme = darkColorScheme(
    primary = SolarAmber,
    onPrimary = Color(0xFF121316),
    primaryContainer = Color(0xFF3B2806),
    onPrimaryContainer = WarmSand,
    secondary = RadTeal,
    onSecondary = Color(0xFF042F2E),
    secondaryContainer = Color(0xFF114240),
    onSecondaryContainer = Color(0xFF99F6E4),
    tertiary = BioluminescentViolet,
    onTertiary = Color(0xFF2E1065),
    tertiaryContainer = Color(0xFF3B1764),
    onTertiaryContainer = Color(0xFFE9D5FF),
    background = BunkerCharcoal,
    onBackground = TextPrimary,
    surface = SteelPanel,
    onSurface = TextPrimary,
    surfaceVariant = ElevatedSlate,
    onSurfaceVariant = TextSecondary,
    outline = PanelBorder,
    error = HazardRed
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = WastelandColorScheme,
        typography = Typography,
        content = content
    )
}
