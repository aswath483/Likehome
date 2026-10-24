package com.likehome.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.likehome.app.R

/** Warm, quiet palette: espresso grounds, cream text, one saffron accent. */
object LH {
    val Espresso = Color(0xFF120F0C)
    val Roast = Color(0xFF1C1814)
    val Crema = Color(0xFF262019)
    val Line = Color(0xFF3A3129)
    val Cream = Color(0xFFF4ECDF)
    val Oat = Color(0xFFB9AC9C)
    val Muted = Color(0xFF8C8073)
    val Saffron = Color(0xFFF2A65A)
    val SaffronDeep = Color(0xFFD9822B)
    val OnSaffron = Color(0xFF2A1A0A)
    val Bubble = Color(0xFF2B241D)
    val MyBubble = Color(0xFF3B2A18)
    val Danger = Color(0xFFE8786A)
}

/** Comic-book display face for the Comic drama style (SIL OFL, see licenses/). */
val Bangers = FontFamily(Font(R.font.bangers_regular))

private val Display = FontFamily.Serif
private val Body = FontFamily.SansSerif

private val LikeHomeType = Typography(
    displayLarge = TextStyle(fontFamily = Display, fontWeight = FontWeight.Light, fontSize = 88.sp, lineHeight = 92.sp, letterSpacing = (-2).sp),
    displayMedium = TextStyle(fontFamily = Display, fontWeight = FontWeight.Light, fontSize = 56.sp, lineHeight = 60.sp, letterSpacing = (-1).sp),
    headlineLarge = TextStyle(fontFamily = Display, fontWeight = FontWeight.Normal, fontSize = 34.sp, lineHeight = 40.sp),
    headlineSmall = TextStyle(fontFamily = Display, fontWeight = FontWeight.Normal, fontSize = 24.sp, lineHeight = 30.sp),
    titleMedium = TextStyle(fontFamily = Body, fontWeight = FontWeight.Medium, fontSize = 17.sp, lineHeight = 22.sp),
    bodyLarge = TextStyle(fontFamily = Body, fontWeight = FontWeight.Normal, fontSize = 17.sp, lineHeight = 25.sp),
    bodyMedium = TextStyle(fontFamily = Body, fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 21.sp),
    labelLarge = TextStyle(fontFamily = Body, fontWeight = FontWeight.Medium, fontSize = 16.sp, letterSpacing = 0.2.sp),
    labelMedium = TextStyle(fontFamily = Body, fontWeight = FontWeight.Medium, fontSize = 12.sp, letterSpacing = 1.4.sp),
)

private val LikeHomeColors = darkColorScheme(
    primary = LH.Saffron,
    onPrimary = LH.OnSaffron,
    secondary = LH.Oat,
    background = LH.Espresso,
    onBackground = LH.Cream,
    surface = LH.Roast,
    onSurface = LH.Cream,
    surfaceVariant = LH.Crema,
    onSurfaceVariant = LH.Oat,
    surfaceContainerHigh = LH.Crema,
    surfaceContainerHighest = LH.Crema,
    outline = LH.Line,
    error = LH.Danger,
)

@Composable
fun LikeHomeTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = LikeHomeColors, typography = LikeHomeType, content = content)
}
