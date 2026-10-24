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
    val Success = Color(0xFF9CC9A0)
    /** Night sky behind the home hero, and the warm dawn it turns into. */
    val Night = Color(0xFF0B0A12)
    val Dawn = Color(0xFF3A2414)
}

/** Comic-book display face for the Comic drama style (SIL OFL, see licenses/). */
val Bangers = FontFamily(Font(R.font.bangers_regular))

/** Editorial serif for clocks and headlines (SIL OFL). */
val Display = FontFamily(Font(R.font.dm_serif_display))

/** Hind, by the Indian Type Foundry: clean Latin with Devanagari for later Hindi lines (SIL OFL). */
val Body = FontFamily(
    Font(R.font.hind_light, FontWeight.Light),
    Font(R.font.hind_regular, FontWeight.Normal),
    Font(R.font.hind_medium, FontWeight.Medium),
    Font(R.font.hind_semibold, FontWeight.SemiBold),
    Font(R.font.hind_bold, FontWeight.Bold),
)

private val LikeHomeType = Typography(
    displayLarge = TextStyle(fontFamily = Display, fontSize = 92.sp, lineHeight = 96.sp, letterSpacing = (-2).sp),
    displayMedium = TextStyle(fontFamily = Display, fontSize = 58.sp, lineHeight = 62.sp, letterSpacing = (-1).sp),
    displaySmall = TextStyle(fontFamily = Display, fontSize = 44.sp, lineHeight = 48.sp, letterSpacing = (-0.5).sp),
    headlineLarge = TextStyle(fontFamily = Display, fontSize = 36.sp, lineHeight = 42.sp, letterSpacing = (-0.3).sp),
    headlineMedium = TextStyle(fontFamily = Display, fontSize = 30.sp, lineHeight = 36.sp),
    headlineSmall = TextStyle(fontFamily = Display, fontSize = 25.sp, lineHeight = 31.sp),
    titleLarge = TextStyle(fontFamily = Body, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, lineHeight = 26.sp),
    titleMedium = TextStyle(fontFamily = Body, fontWeight = FontWeight.SemiBold, fontSize = 17.sp, lineHeight = 23.sp),
    titleSmall = TextStyle(fontFamily = Body, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, lineHeight = 20.sp),
    bodyLarge = TextStyle(fontFamily = Body, fontWeight = FontWeight.Normal, fontSize = 17.sp, lineHeight = 25.sp),
    bodyMedium = TextStyle(fontFamily = Body, fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 21.sp),
    bodySmall = TextStyle(fontFamily = Body, fontWeight = FontWeight.Normal, fontSize = 13.sp, lineHeight = 18.sp),
    labelLarge = TextStyle(fontFamily = Body, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 20.sp, letterSpacing = 0.2.sp),
    labelMedium = TextStyle(fontFamily = Body, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 1.6.sp),
    labelSmall = TextStyle(fontFamily = Body, fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 14.sp, letterSpacing = 0.8.sp),
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
