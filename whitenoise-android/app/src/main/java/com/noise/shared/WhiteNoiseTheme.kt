package com.noise.shared

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Typography
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Custom typography styles for the WhiteNoise app, defining headers, body text, and buttons
val WhiteNoiseTypography = Typography(
  h1 = TextStyle(
    fontSize = 30.sp,
    fontWeight = FontWeight.Bold,
    color = Color.Unspecified
  ),
  body1 = TextStyle(
    fontSize = 16.sp,
    fontWeight = FontWeight.Normal,
    color = Color.Unspecified
  ),
  button = TextStyle(
    fontSize = 14.sp,
    fontWeight = FontWeight.Medium,
    color = Color.Unspecified
  )
)

@Composable
// Applies the WhiteNoise app's custom theme, dynamically using light or dark mode
fun WhiteNoiseTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colors = if (darkTheme) DarkColors else LightColors,
    typography = WhiteNoiseTypography,
    content = content,
  )
}

// Light color palette used when darkTheme is false
private val LightColors = lightColors(
  primary = Color(0xFF573c27),
  primaryVariant = Color(0xFFa98360),
  secondary = Color(0xFFffadc6),
  secondaryVariant = Color(0xFFe34989),
  background = Color(0xFFF1ECE4),
)

// Dark color palette used when darkTheme is true
private val DarkColors = darkColors(
  primary = Color(0xFF856C62),
  primaryVariant = Color(0xFF856C62),
  secondary = Color(0xFFAB788B),
  secondaryVariant = Color(0xFFE8A2B5),
)
