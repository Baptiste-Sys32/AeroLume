package com.badeparday.aerolume.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.badeparday.aerolume.ui.theme.AerolumeTheme

@Composable
fun MetricPill(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    accent: Color = MaterialTheme.colorScheme.onSurface,
) {
    GlassPanel(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        padding = 12.dp,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            AnimatedContent(targetState = value, label = "metricValue") { current ->
                Text(
                    text = current,
                    style = MaterialTheme.typography.titleMedium,
                    color = accent,
                )
            }
        }
    }
}

@Preview
@Composable
private fun MetricPillPreview() {
    AerolumeTheme {
        MetricPill(label = "Altitude", value = "36,900 ft", modifier = Modifier.padding(16.dp))
    }
}
