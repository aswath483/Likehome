package com.likehome.app.ui.faces

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.likehome.app.core.Character
import com.likehome.app.core.CropMath
import com.likehome.app.core.PortraitSource
import com.likehome.app.mom.MomLines
import com.likehome.app.ui.common.CharacterFace
import com.likehome.app.ui.common.PageScaffold
import com.likehome.app.ui.common.Portraits
import com.likehome.app.ui.common.PrimaryButton
import com.likehome.app.ui.common.SecondaryButton
import com.likehome.app.ui.common.SectionLabel
import com.likehome.app.ui.common.rememberPortrait
import com.likehome.app.ui.common.withHaptic
import com.likehome.app.ui.styles.CharacterPickerRow
import com.likehome.app.ui.theme.LH
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

/**
 * Use real photos for the characters: one photo works for every mood, or add one per
 * mood for real expressions. Photos are cropped to a circle and stay on the phone.
 */
@Composable
fun FacesScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var character by remember { mutableStateOf(Character.MOM) }
    var slot by remember { mutableStateOf(0) }
    var cropping by remember { mutableStateOf<Bitmap?>(null) }
    var loadFailed by remember { mutableStateOf(false) }

    // The system photo picker: no storage permission needed.
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            scope.launch {
                val bitmap = withContext(Dispatchers.IO) { decode(context, uri) }
                loadFailed = bitmap == null
                cropping = bitmap
            }
        }
    }
    fun pickFor(level: Int) {
        slot = level
        picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    PageScaffold(title = "Use your own photos", onBack = onBack) {
        Text(
            "Make it really feel like home. One photo works for every mood; add more for real expressions. Photos stay on this phone.",
            style = MaterialTheme.typography.bodyMedium,
            color = LH.Oat,
        )
        SectionLabel("Whose photo", Modifier.padding(top = 8.dp))
        CharacterPickerRow(selected = character, onPick = { character = it })

        SectionLabel("Moods", Modifier.padding(top = 12.dp))
        (0..3).chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { level ->
                    MoodSlot(
                        character = character,
                        level = level,
                        onPick = { pickFor(level) },
                        onRemove = { Portraits.delete(context, character, level) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
        if (loadFailed) {
            Text("That photo couldn't be opened. Try another one.", style = MaterialTheme.typography.bodyMedium, color = LH.Danger)
        }
        Text(
            "Tip: for Full drama, ask Mom to do her angriest face. She will enjoy it.",
            style = MaterialTheme.typography.bodySmall,
            color = LH.Muted,
        )
    }

    cropping?.let { photo ->
        CropDialog(
            photo = photo,
            title = "${character.displayName} · ${MomLines.moodLabel(slot)}",
            onCancel = { cropping = null },
            onDone = { square ->
                Portraits.save(context, character, slot, square)
                cropping = null
            },
        )
    }
}

@Composable
private fun MoodSlot(character: Character, level: Int, onPick: () -> Unit, onRemove: () -> Unit, modifier: Modifier) {
    val picture = rememberPortrait(character, level)
    val own = picture.portrait.source == PortraitSource.USER && picture.portrait.exactMood
    Surface(
        onClick = withHaptic(onPick),
        color = LH.Roast,
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, if (own) LH.Saffron else LH.Line),
        modifier = modifier,
    ) {
        Column(Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            CharacterFace(character, level, Modifier.size(96.dp))
            Spacer(Modifier.height(10.dp))
            Text(MomLines.moodLabel(level), style = MaterialTheme.typography.titleSmall, color = LH.Cream)
            Text(
                when {
                    own -> "Your photo"
                    picture.portrait.source == PortraitSource.USER -> "Using your ${MomLines.moodLabel(picture.portrait.level ?: 0).lowercase()} photo"
                    picture.portrait.source == PortraitSource.BUNDLED -> "Built-in portrait"
                    else -> "Drawn face"
                },
                style = MaterialTheme.typography.bodySmall,
                color = LH.Muted,
                textAlign = TextAlign.Center,
            )
            if (own) {
                TextButton(onClick = withHaptic(onRemove)) { Text("Remove", color = LH.Danger) }
            } else {
                TextButton(onClick = withHaptic(onPick)) { Text("Add photo", color = LH.Saffron) }
            }
        }
    }
}

/** Pinch to zoom, drag to move; the circle shows exactly what will be kept. */
@Composable
private fun CropDialog(photo: Bitmap, title: String, onCancel: () -> Unit, onDone: (Bitmap) -> Unit) {
    val image = remember(photo) { photo.asImageBitmap() }
    var zoom by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    val density = LocalDensity.current
    // The crop circle's size in pixels, measured while laying out; read when cropping.
    val viewport = remember { floatArrayOf(1f) }

    Dialog(onDismissRequest = onCancel, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Column(
            Modifier.fillMaxSize().background(Color.Black).windowInsetsPadding(WindowInsets.safeDrawing).padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(title, style = MaterialTheme.typography.titleLarge, color = Color.White)
            Text("Pinch and drag to frame the face", style = MaterialTheme.typography.bodyMedium, color = LH.Oat)
            Spacer(Modifier.height(16.dp))
            BoxWithConstraints(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                val viewportPx = with(density) { (minOf(maxWidth, maxHeight) * 0.85f).toPx() }
                Canvas(
                    Modifier.fillMaxSize().pointerInput(photo, viewportPx) {
                        detectTransformGestures { _, pan, gestureZoom, _ ->
                            zoom = (zoom * gestureZoom).coerceIn(1f, CropMath.MAX_ZOOM)
                            val (x, y) = CropMath.clampOffset(photo.width, photo.height, viewportPx, zoom, offset.x + pan.x, offset.y + pan.y)
                            offset = Offset(x, y)
                        }
                    }
                ) {
                    val s = CropMath.scale(photo.width, photo.height, viewportPx, zoom)
                    val w = photo.width * s
                    val h = photo.height * s
                    val center = Offset(size.width / 2, size.height / 2)
                    drawImage(
                        image,
                        dstOffset = IntOffset((center.x - w / 2 + offset.x).roundToInt(), (center.y - h / 2 + offset.y).roundToInt()),
                        dstSize = IntSize(w.roundToInt(), h.roundToInt()),
                    )
                    // Dim everything outside the circle.
                    val circle = Rect(center, viewportPx / 2)
                    val mask = Path().apply {
                        fillType = PathFillType.EvenOdd
                        addRect(Rect(Offset.Zero, size))
                        addOval(circle)
                    }
                    drawPath(mask, Color.Black.copy(alpha = 0.6f))
                    drawCircle(Color.White, viewportPx / 2, center, style = Stroke(2.dp.toPx()))
                }
                viewport[0] = viewportPx
            }
            Spacer(Modifier.height(16.dp))
            PrimaryButton("Use this photo", onClick = {
                val (left, top, side) = CropMath.sourceSquare(photo.width, photo.height, viewport[0], zoom, offset.x, offset.y)
                val square = Bitmap.createBitmap(photo, left, top, side, side)
                onDone(Bitmap.createScaledBitmap(square, OUTPUT_PX, OUTPUT_PX, true))
            })
            Spacer(Modifier.height(8.dp))
            SecondaryButton("Cancel", onClick = onCancel)
        }
    }
}

private const val OUTPUT_PX = 512

/** Opens a picked photo upright (EXIF rotation applied on Android 9+), at most ~2048 px. */
private fun decode(context: Context, uri: Uri): Bitmap? = runCatching {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri)) { decoder, info, _ ->
            val longest = maxOf(info.size.width, info.size.height)
            if (longest > 2048) {
                val f = 2048f / longest
                decoder.setTargetSize((info.size.width * f).toInt(), (info.size.height * f).toInt())
            }
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
        }
    } else {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        var sample = 1
        while (maxOf(bounds.outWidth, bounds.outHeight) / (sample * 2) >= 2048) sample *= 2
        context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample })
        }
    }
}.getOrNull()
