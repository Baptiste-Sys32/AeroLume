package com.badeparday.aerolume.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.badeparday.aerolume.domain.model.FlightStatus
import com.badeparday.aerolume.ui.theme.AerolumeTheme
import com.badeparday.aerolume.ui.util.statusLabel

@Composable
fun FlightStatusChip(
    status: FlightStatus,
    modifier: Modifier = Modifier,
) {
    val tint = statusTint(status)
    val background = animateColorAsState(tint.copy(alpha = 0.16f), label = "statusBackground")
    val textColor = animateColorAsState(tint, label = "statusText")

    Row(
        modifier =
            modifier
                .clip(RoundedCornerShape(12.dp))
                .background(background.value.copy(alpha = 0.55f))
                .border(1.dp, textColor.value.copy(alpha = 0.22f), RoundedCornerShape(12.dp))
                .padding(horizontal = 10.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        androidx.compose.foundation.Canvas(modifier = Modifier.size(7.dp)) {
            drawCircle(textColor.value)
        }
        AnimatedContent(targetState = statusLabel(status), label = "statusLabel") { label ->
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = textColor.value,
            )
        }
    }
}

private fun statusTint(status: FlightStatus): Color =
    when (status) {
        FlightStatus.Boarding -> Color(0xFF7EA9CD)
        FlightStatus.Climbing -> Color(0xFF88B4C9)
        FlightStatus.Cruising -> Color(0xFF92B6D0)
        FlightStatus.Descending -> Color(0xFFD8A46E)
        FlightStatus.Final -> Color(0xFFCB8A5F)
        FlightStatus.Delayed -> Color(0xFFD9A164)
        FlightStatus.Landed -> Color(0xFF8CB89C)
    }

@Preview
@Composable
private fun FlightStatusChipPreview() {
    AerolumeTheme {
        FlightStatusChip(status = FlightStatus.Cruising)
    }
}
