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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.likehome.app.core.RingStyle
import com.likehome.app.core.WakeTask
import com.likehome.app.data.Alarm
import com.likehome.app.ui.theme.LH
import java.time.DayOfWeek

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmEditorSheet(
    initial: Alarm,
    onDismiss: () -> Unit,
    onSave: (Alarm) -> Unit,
    onDelete: (() -> Unit)?,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val time = rememberTimePickerState(initialHour = initial.hour, initialMinute = initial.minute, is24Hour = false)
    var days by remember { mutableStateOf(initial.days) }
    var label by remember { mutableStateOf(initial.label) }
    var task by remember { mutableStateOf(initial.task) }
    var style by remember { mutableStateOf(initial.style) }
    val context = LocalContext.current
    // Steps work without this (accelerometer fallback), but the step sensor is more accurate.
    val activityPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }

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
                .padding(bottom = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                if (onDelete == null) "New alarm" else "Edit alarm",
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

            Spacer(Modifier.height(8.dp))
            Text("REPEAT", style = MaterialTheme.typography.labelMedium, color = LH.Muted, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                DayOfWeek.entries.forEach { day ->
                    val on = day in days
                    DayDot(
                        letter = day.name.first().toString(),
                        selected = on,
                        onClick = { days = if (on) days - day else days + day },
                    )
                }
            }

            Spacer(Modifier.height(22.dp))
            Text("WAKE-UP TASK", style = MaterialTheme.typography.labelMedium, color = LH.Muted, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                WakeTask.entries.forEach { t ->
                    TaskPill(
                        text = t.short,
                        selected = t == task,
                        onClick = {
                            task = t
                            if (t == WakeTask.STEPS && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                                ContextCompat.checkSelfPermission(context, Manifest.permission.ACTIVITY_RECOGNITION) !=
                                PackageManager.PERMISSION_GRANTED
                            ) {
                                activityPermission.launch(Manifest.permission.ACTIVITY_RECOGNITION)
                            }
                        },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "${task.title}: ${task.detail.replaceFirstChar(Char::lowercase)} to stop Mom.",
                style = MaterialTheme.typography.bodyMedium,
                color = LH.Oat,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(22.dp))
            Text("MOM'S STYLE", style = MaterialTheme.typography.labelMedium, color = LH.Muted, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RingStyle.entries.forEach { s ->
                    TaskPill(text = s.short, selected = s == style, onClick = { style = s }, modifier = Modifier.weight(1f))
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "${style.title}: ${style.detail.replaceFirstChar(Char::lowercase)}.",
                style = MaterialTheme.typography.bodyMedium,
                color = LH.Oat,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(22.dp))
            OutlinedTextField(
                value = label,
                onValueChange = { label = it.take(40) },
                label = { Text("Label (optional)") },
                placeholder = { Text("College, Gym, Office…") },
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
            Button(
                onClick = {
                    onSave(initial.copy(hour = time.hour, minute = time.minute, days = days, label = label.trim(), task = task, style = style))
                },
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = LH.Saffron, contentColor = LH.OnSaffron),
                modifier = Modifier.fillMaxWidth().height(58.dp),
            ) {
                Text("Save alarm", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            }
            if (onDelete != null) {
                Spacer(Modifier.height(6.dp))
                TextButton(onClick = onDelete, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                    Text("Delete alarm", color = LH.Danger, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
private fun DayDot(letter: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = if (selected) LH.Saffron else LH.Crema,
        contentColor = if (selected) LH.OnSaffron else LH.Oat,
        border = if (selected) null else BorderStroke(1.dp, LH.Line),
        modifier = Modifier.size(44.dp),
    ) {
        Column(verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Text(letter, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun TaskPill(text: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = if (selected) LH.Saffron else LH.Crema,
        contentColor = if (selected) LH.OnSaffron else LH.Oat,
        border = if (selected) null else BorderStroke(1.dp, LH.Line),
        modifier = modifier.height(44.dp),
    ) {
        Column(verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
        }
    }
}
