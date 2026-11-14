package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val MahadevColorScheme = darkColorScheme(
    primary = RoyalGold,
    onPrimary = ObsidianBlack,
    primaryContainer = GoldContainer,
    onPrimaryContainer = SoftGold,
    secondary = ElectricBlue,
    onSecondary = ObsidianBlack,
    secondaryContainer = BlueContainer,
    onSecondaryContainer = TextPrimary,
    tertiary = SapphireBlue,
    onTertiary = TextPrimary,
    background = ObsidianBlack,
    onBackground = TextPrimary,
    surface = MidnightSurface,
    onSurface = TextPrimary,
    surfaceVariant = ElevatedSlate,
    onSurfaceVariant = TextSecondary,
    outline = CardBorderBlue,
    outlineVariant = GoldBorder,
    error = StatusDangerRed,
    onError = TextPrimary
)

private val MahadevShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun MahadevPanelTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = MahadevColorScheme,
        typography = Typography,
        shapes = MahadevShapes,
        content = content
    )
}
