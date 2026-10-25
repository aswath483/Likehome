package com.likehome.app.ui.home

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.width
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.likehome.app.core.RingStyle
import com.likehome.app.core.WakeTask
import com.likehome.app.data.Alarm
import com.likehome.app.core.ClockFormat
import com.likehome.app.ui.common.ChoicePill
import com.likehome.app.ui.ring.PhotoStore
import com.likehome.app.ui.styles.CharacterPickerRow
import com.likehome.app.ui.styles.StylePickerRow
import com.likehome.app.ui.common.PrimaryButton
import com.likehome.app.ui.common.rememberIs24Hour
import com.likehome.app.ui.common.withHaptic
import java.time.ZoneId
import java.time.ZonedDateTime
import com.likehome.app.ui.theme.LH
import java.time.DayOfWeek
import androidx.compose.ui.res.stringResource
import com.likehome.app.R
import com.likehome.app.ui.common.letters
import com.likehome.app.ui.common.fullName
import com.likehome.app.ui.common.detailRes
import com.likehome.app.ui.common.titleRes
import com.likehome.app.ui.common.shortRes
import com.likehome.app.ui.common.resources
import com.likehome.app.ui.common.text

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmEditorSheet(
    initial: Alarm,
    onDismiss: () -> Unit,
    onSave: (Alarm) -> Unit,
    onDelete: (() -> Unit)?,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val time = rememberTimePickerState(initialHour = initial.hour, initialMinute = initial.minute, is24Hour = rememberIs24Hour())
    var days by remember { mutableStateOf(initial.days) }
    var label by remember { mutableStateOf(initial.label) }
    var task by remember { mutableStateOf(initial.task) }
    var style by remember { mutableStateOf(initial.style) }
    var character by remember { mutableStateOf(initial.character) }
    var photoRef by remember { mutableStateOf(initial.photoRef) }
    val context = LocalContext.current
    // Steps work without this (accelerometer fallback), but the step sensor is more accurate.
    val activityPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    // Ask for each task's permission when it's picked, never over the lock screen at 7 am.
    val micPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    fun askFor(t: WakeTask) {
        fun missing(permission: String) =
            ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED
        when {
            t == WakeTask.STEPS && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                missing(Manifest.permission.ACTIVITY_RECOGNITION) -> activityPermission.launch(Manifest.permission.ACTIVITY_RECOGNITION)
            t == WakeTask.VOICE && missing(Manifest.permission.RECORD_AUDIO) -> micPermission.launch(Manifest.permission.RECORD_AUDIO)
        }
    }
    var pendingPhoto by remember { mutableStateOf<java.io.File?>(null) }
    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        val file = pendingPhoto
        if (ok && file != null) photoRef = file.name else file?.delete()
    }
    fun takeReference() {
        val file = PhotoStore.newFile(context, "reference")
        pendingPhoto = file
        runCatching { camera.launch(PhotoStore.uri(context, file)) }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = LH.Roast,
        contentColor = LH.Cream,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
    ) {
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .navigationBarsPadding()
                .imePadding()
                .padding(bottom = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                stringResource(if (onDelete == null) R.string.new_alarm else R.string.edit_alarm),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(20.dp))

            TimePicker(
                state = time,
                colors = TimePickerDefaults.colors(
                    clockDialColor = LH.Crema,
                    clockDialSelectedContentColor = LH.OnSaffron,
                    clockDialUnselectedContentColor = LH.Oat,
                    selectorColor = LH.Saffron,
                    periodSelectorBorderColor = LH.Line,
                    periodSelectorSelectedContainerColor = LH.Saffron,
                    periodSelectorSelectedContentColor = LH.OnSaffron,
                    periodSelectorUnselectedContainerColor = LH.Roast,
                    periodSelectorUnselectedContentColor = LH.Oat,
                    timeSelectorSelectedContainerColor = LH.MyBubble,
                    timeSelectorSelectedContentColor = LH.Saffron,
                    timeSelectorUnselectedContainerColor = LH.Crema,
                    timeSelectorUnselectedContentColor = LH.Cream,
                ),
            )

            // Live answer to "when will this actually ring?"
            val now = ZonedDateTime.now(ZoneId.systemDefault())
            val rings = initial.copy(hour = time.hour, minute = time.minute, days = days).nextTrigger(now)
            Text(
                stringResource(R.string.rings_at, ClockFormat.dayOf(now.toLocalDate(), rings.toLocalDate()).text(resources()).lowercase(), ClockFormat.until(now, rings).text(resources())),
                style = MaterialTheme.typography.bodyLarge,
                color = LH.Saffron,
                modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
            )

            Spacer(Modifier.height(8.dp))
            Text(stringResource(R.string.repeat_label), style = MaterialTheme.typography.labelMedium, color = LH.Muted, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                DayOfWeek.entries.forEach { day ->
                    val on = day in days
                    DayDot(
                        letter = day.letters(resources()),
                        name = day.fullName(resources()),
                        selected = on,
                        onClick = withHaptic { days = if (on) days - day else days + day },
                    )
                }
            }

            Spacer(Modifier.height(22.dp))
            Text(stringResource(R.string.task_label), style = MaterialTheme.typography.labelMedium, color = LH.Muted, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))
            WakeTask.entries.chunked(3).forEach { row ->
                Row(Modifier.fillMaxWidth().padding(bottom = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { t ->
                        ChoicePill(
                            text = stringResource(t.shortRes),
                            selected = t == task,
                            onClick = {
                                task = t
                                askFor(t)
                            },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
            Text(
                stringResource(R.string.title_detail, stringResource(task.titleRes), stringResource(task.detailRes).replaceFirstChar(Char::lowercase)),
                style = MaterialTheme.typography.bodyMedium,
                color = LH.Oat,
                modifier = Modifier.fillMaxWidth(),
            )
            if (task == WakeTask.PHOTO) {
                Spacer(Modifier.height(12.dp))
                ReferencePhoto(photoRef, onTake = { takeReference() })
            }

            Spacer(Modifier.height(22.dp))
            Text(stringResource(R.string.who_label), style = MaterialTheme.typography.labelMedium, color = LH.Muted, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))
            CharacterPickerRow(selected = character, onPick = { character = it })

            Spacer(Modifier.height(22.dp))
            Text(stringResource(R.string.style_label), style = MaterialTheme.typography.labelMedium, color = LH.Muted, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))
            StylePickerRow(selected = style, onPick = { style = it })
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(R.string.title_detail, stringResource(style.titleRes), stringResource(style.detailRes).replaceFirstChar(Char::lowercase)),
                style = MaterialTheme.typography.bodyMedium,
                color = LH.Oat,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(22.dp))
            OutlinedTextField(
                value = label,
                onValueChange = { label = it.take(40) },
                label = { Text(stringResource(R.string.label_optional)) },
                placeholder = { Text(stringResource(R.string.label_hint)) },
                singleLine = true,
                shape = RoundedCornerShape(18.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = LH.Saffron,
                    unfocusedBorderColor = LH.Line,
                    focusedLabelColor = LH.Saffron,
                    unfocusedLabelColor = LH.Muted,
                    cursorColor = LH.Saffron,
                    focusedTextColor = LH.Cream,
                    unfocusedTextColor = LH.Cream,
                    focusedPlaceholderColor = LH.Muted,
                    unfocusedPlaceholderColor = LH.Muted,
                ),
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(26.dp))
            PrimaryButton(stringResource(R.string.save_alarm), onClick = {
                onSave(initial.copy(hour = time.hour, minute = time.minute, days = days, label = label.trim(), task = task, style = style, character = character, photoRef = photoRef))
            })
            if (onDelete != null) {
                Spacer(Modifier.height(6.dp))
                TextButton(onClick = onDelete, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                    Text(stringResource(R.string.delete_alarm), color = LH.Danger, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
private fun DayDot(letter: String, name: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.size(44.dp).semantics {
            contentDescription = name
            this.selected = selected
        },
        shape = CircleShape,
        color = if (selected) LH.Saffron else LH.Crema,
        contentColor = if (selected) LH.OnSaffron else LH.Oat,
        border = if (selected) null else BorderStroke(1.dp, LH.Line),
    ) {
        Column(verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Text(letter, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
        }
    }
}


/** The spot the photo check will ask for: a preview and a button to (re)take it. */
@Composable
private fun ReferencePhoto(photoRef: String, onTake: () -> Unit) {
    val context = LocalContext.current
    val thumb = remember(photoRef) { PhotoStore.thumbnail(context, photoRef) }
    Surface(color = LH.Crema, shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            if (thumb != null) {
                androidx.compose.foundation.Image(
                    thumb.asImageBitmap(),
                    contentDescription = stringResource(R.string.reference_photo),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                    modifier = Modifier.size(64.dp).clip(RoundedCornerShape(14.dp)),
                )
                Spacer(Modifier.width(12.dp))
            }
            Text(
                stringResource(if (thumb == null) R.string.reference_photo_hint else R.string.reference_photo_set),
                style = MaterialTheme.typography.bodyMedium,
                color = if (thumb == null) LH.Saffron else LH.Oat,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = onTake) { Text(stringResource(if (thumb == null) R.string.take_photo else R.string.retake), color = LH.Saffron) }
        }
    }
}
