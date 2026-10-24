package com.likehome.app.ui.ring

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.FileProvider
import com.likehome.app.core.Badge
import java.io.File
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/** Everything the report card shows, on screen and in the shared image. */
data class ReportData(
    val date: LocalDate,
    val wokeAt: LocalTime,
    val snoozes: Int,
    val streak: Int,
    val title: String,
    val body: String,
    val week: List<Int?>,
    val newBadges: Set<Badge>,
    val verdict: String?,
)

/** Draws the report as a 1080×1350 image (Instagram/WhatsApp friendly) and shares it. */
object ReportShare {

    private const val W = 1080
    private const val H = 1350
    private val Espresso = Color.rgb(0x12, 0x0F, 0x0C)
    private val Roast = Color.rgb(0x1C, 0x18, 0x14)
    private val Line = Color.rgb(0x3A, 0x31, 0x29)
    private val Cream = Color.rgb(0xF4, 0xEC, 0xDF)
    private val Oat = Color.rgb(0xB9, 0xAC, 0x9C)
    private val Saffron = Color.rgb(0xF2, 0xA6, 0x5A)
    private val Skin = Color.rgb(0xE9, 0xB9, 0x8A)
    private val Ink = Color.rgb(0x2A, 0x1D, 0x14)

    fun share(context: Context, report: ReportData) {
        val dir = File(context.cacheDir, "share").apply { mkdirs() }
        val file = File(dir, "morning-report.png")
        val bitmap = render(report)
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()

        val uri = FileProvider.getUriForFile(context, "${context.packageName}.files", file)
        val send = Intent(Intent.ACTION_SEND)
            .setType("image/png")
            .putExtra(Intent.EXTRA_STREAM, uri)
            .putExtra(Intent.EXTRA_TEXT, shareText(report))
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        context.startActivity(Intent.createChooser(send, "Share your morning report"))
    }

    private fun shareText(r: ReportData) = when {
        r.snoozes == 0 && r.streak > 1 -> "${r.streak} mornings, zero snoozes. Mom is proud. #LikeHome"
        r.snoozes == 0 -> "Woke up on the first call. Mom is proud. #LikeHome"
        else -> "Snoozed ${r.snoozes}× today and Mom is NOT happy. #LikeHome"
    }

    fun render(r: ReportData): Bitmap {
        val bmp = Bitmap.createBitmap(W, H, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        c.drawColor(Espresso)
        val serif = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
        val sans = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        val sansBold = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        val pad = 90f

        text(c, "LIKE HOME  ·  MORNING REPORT", pad, 150f, 34f, Saffron, sansBold, spacing = 0.12f)
        text(c, r.date.format(DateTimeFormatter.ofPattern("EEEE, d MMMM")), pad, 205f, 36f, Oat, sans)

        drawFace(c, W - pad - 150f, 190f, 150f, r.snoozes)

        val titleBottom = paragraph(c, r.title, pad, 380f, W - 2 * pad, 92f, Cream, serif)
        val bodyBottom = paragraph(c, r.body, pad, titleBottom + 30f, W - 2 * pad, 44f, Oat, sans)

        // Three stat boxes.
        val top = maxOf(bodyBottom + 60f, 700f)
        val gap = 24f
        val boxW = (W - 2 * pad - 2 * gap) / 3
        val stats = listOf(
            "${r.snoozes}" to if (r.snoozes == 1) "snooze" else "snoozes",
            r.wokeAt.format(DateTimeFormatter.ofPattern("h:mm")) to "up at",
            "${r.streak}" to "streak",
        )
        stats.forEachIndexed { i, (value, label) ->
            val left = pad + i * (boxW + gap)
            val box = RectF(left, top, left + boxW, top + 210f)
            c.drawRoundRect(box, 36f, 36f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Roast })
            c.drawRoundRect(box, 36f, 36f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Line; style = Paint.Style.STROKE; strokeWidth = 3f
            })
            text(c, value, left + 36f, top + 115f, 84f, Cream, serif)
            text(c, label, left + 36f, top + 170f, 32f, Oat, sans)
        }

        // The week as dots: saffron = no snooze, outlined = snoozed, faint = no alarm.
        val weekTop = top + 290f
        text(c, "THIS WEEK", pad, weekTop, 30f, Oat, sansBold, spacing = 0.12f)
        r.week.forEachIndexed { i, snoozes ->
            val cx = pad + 34f + i * 120f
            val cy = weekTop + 80f
            val p = Paint(Paint.ANTI_ALIAS_FLAG)
            when (snoozes) {
                null -> { p.color = Line; c.drawCircle(cx, cy, 14f, p) }
                0 -> { p.color = Saffron; c.drawCircle(cx, cy, 30f, p) }
                else -> {
                    p.color = Saffron; p.style = Paint.Style.STROKE; p.strokeWidth = 6f
                    c.drawCircle(cx, cy, 27f, p)
                }
            }
        }

        text(c, "Wake up like Mom is still calling you.", pad, H - 60f, 34f, Oat, sans)
        return bmp
    }

    private fun text(
        c: Canvas, s: String, x: Float, y: Float, size: Float, color: Int, face: Typeface, spacing: Float = 0f,
    ) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color; textSize = size; typeface = face; letterSpacing = spacing
        }
        c.drawText(s, x, y, p)
    }

    /** Wrapped text; returns the y of its bottom edge. */
    private fun paragraph(c: Canvas, s: String, x: Float, y: Float, width: Float, size: Float, color: Int, face: Typeface): Float {
        val p = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color; textSize = size; typeface = face }
        val layout = StaticLayout.Builder.obtain(s, 0, s.length, p, width.toInt())
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(0f, 1.1f)
            .build()
        c.save()
        c.translate(x, y)
        layout.draw(c)
        c.restore()
        return y + layout.height
    }

    /** The same face as the app's mood face, drawn at radius [r] around ([cx], [cy]). */
    private fun drawFace(c: Canvas, cx: Float, cy: Float, r: Float, snoozes: Int) {
        val level = snoozes.coerceIn(0, 3)
        val w = r * 2
        val left = cx - r
        val top = cy - r
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        p.color = Roast; c.drawCircle(cx, cy, r, p)
        p.color = Skin; c.drawCircle(cx, top + w * 0.56f, w * 0.34f, p)
        p.color = Ink
        c.drawArc(RectF(left + w * 0.14f, top + w * 0.14f, left + w * 0.86f, top + w * 0.74f), 180f, 180f, true, p)
        p.color = Saffron; c.drawCircle(cx, top + w * 0.46f, w * 0.03f, p)
        p.color = Ink
        val eyeY = top + w * 0.58f
        c.drawCircle(left + w * 0.4f, eyeY, w * 0.03f, p)
        c.drawCircle(left + w * 0.6f, eyeY, w * 0.03f, p)
        p.style = Paint.Style.STROKE; p.strokeWidth = w * 0.035f; p.strokeCap = Paint.Cap.ROUND
        val tilt = level * w * 0.025f
        c.drawLine(left + w * 0.33f, eyeY - w * 0.08f - tilt, left + w * 0.45f, eyeY - w * 0.07f, p)
        c.drawLine(left + w * 0.67f, eyeY - w * 0.08f - tilt, left + w * 0.55f, eyeY - w * 0.07f, p)
        val mouthTop = top + w * 0.68f
        when (level) {
            0 -> c.drawArc(RectF(left + w * 0.4f, mouthTop - w * 0.06f, left + w * 0.6f, mouthTop + w * 0.06f), 20f, 140f, false, p)
            1 -> c.drawLine(left + w * 0.42f, mouthTop + w * 0.02f, left + w * 0.58f, mouthTop + w * 0.02f, p)
            else -> c.drawArc(RectF(left + w * 0.4f, mouthTop, left + w * 0.6f, mouthTop + w * 0.12f), 200f, 140f, false, p)
        }
    }
}
