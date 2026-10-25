package com.likehome.app.ui.family

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.likehome.app.R
import com.likehome.app.core.FamilyLink
import com.likehome.app.data.SettingsRepository
import com.likehome.app.ui.theme.LH

/** Opens WhatsApp (or the browser, without it) with [text] ready to send to the family number. */
fun messageFamily(context: Context, text: String): Boolean {
    val number = SettingsRepository.get(context).current.familyNumber
    val link = FamilyLink.whatsApp(number, text) ?: return false
    return runCatching {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        true
    }.getOrElse {
        Toast.makeText(context, R.string.install_whatsapp_family, Toast.LENGTH_LONG).show()
        false
    }
}

/** Enter or change Mom's WhatsApp number; it must include the country code. */
@Composable
fun FamilyNumberDialog(current: String, onSave: (String) -> Unit, onDismiss: () -> Unit) {
    var number by remember { mutableStateOf(current) }
    val valid = FamilyLink.normalise(number) != null
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.family_number_title)) },
        text = {
            Column {
                Text(stringResource(R.string.family_number_body), style = MaterialTheme.typography.bodyMedium)
                OutlinedTextField(
                    value = number,
                    onValueChange = { number = it.take(24) },
                    placeholder = { Text("+91 98765 43210") },
                    singleLine = true,
                    isError = number.isNotBlank() && !valid,
                    supportingText = { if (number.isNotBlank() && !valid) Text(stringResource(R.string.family_number_invalid)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LH.Saffron,
                        unfocusedBorderColor = LH.Line,
                        cursorColor = LH.Saffron,
                        focusedTextColor = LH.Cream,
                        unfocusedTextColor = LH.Cream,
                    ),
                    modifier = Modifier.padding(top = 12.dp),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(number.trim()) }, enabled = valid) {
                Text(stringResource(R.string.save), color = if (valid) LH.Saffron else LH.Muted)
            }
        },
        dismissButton = {
            if (current.isNotBlank()) {
                TextButton(onClick = { onSave("") }) { Text(stringResource(R.string.remove), color = LH.Danger) }
            } else {
                TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
            }
        },
        containerColor = LH.Roast,
        titleContentColor = LH.Cream,
        textContentColor = LH.Oat,
    )
}
