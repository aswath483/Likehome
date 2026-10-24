package com.likehome.app.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.likehome.app.ui.theme.LH

/** Small caps label above a group, e.g. "REPEAT". */
@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = LH.Muted,
        modifier = modifier,
    )
}

/** A selectable card with a title and one line of detail; saffron border when chosen. */
@Composable
fun ChoiceCard(
    title: String,
    detail: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badge: String? = null,
) {
    Surface(
        onClick = onClick,
        color = if (selected) LH.MyBubble else LH.Roast,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) LH.Saffron else LH.Line),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(horizontal = 18.dp, vertical = 16.dp)) {
            Text(
                if (badge == null) title else "$title  ·  $badge",
                style = MaterialTheme.typography.titleMedium,
                color = LH.Cream,
            )
            Text(detail, style = MaterialTheme.typography.bodyMedium, color = LH.Oat)
        }
    }
}

/** One option in a row of pills, e.g. the wake-up task picker. */
@Composable
fun ChoicePill(text: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
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

/** Rounded card used to group settings rows. */
@Composable
fun Card(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        color = LH.Roast,
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, LH.Line),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(20.dp), content = content)
    }
}

/** The main saffron call to action. */
@Composable
fun PrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(
            containerColor = LH.Saffron, contentColor = LH.OnSaffron,
            disabledContainerColor = LH.Crema, disabledContentColor = LH.Muted,
        ),
        contentPadding = PaddingValues(horizontal = 24.dp),
        modifier = modifier.fillMaxWidth().height(58.dp),
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
    }
}
