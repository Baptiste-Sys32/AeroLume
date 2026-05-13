package com.badeparday.aerolume.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.badeparday.aerolume.ui.theme.Ink900

@Composable
fun RadarTopBar(
    trafficSummary: String,
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    GlassPanel(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        padding = 14.dp,
        containerColor = Ink900.copy(alpha = 0.86f),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(
                    text = "RADAR",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "Radar",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = trafficSummary.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            RadarActionRail(
                onSearchClick = onSearchClick,
                onSettingsClick = onSettingsClick,
            )
        }
    }
}

@Composable
private fun RadarActionRail(
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    GlassPanel(
        shape = RoundedCornerShape(10.dp),
        padding = 0.dp,
        containerColor = Color.Black.copy(alpha = 0.22f),
        borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.72f),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadarActionButton(
                imageVector = Icons.Rounded.Search,
                onClick = onSearchClick,
            )
            Box(
                modifier =
                    Modifier
                        .height(18.dp)
                        .width(1.dp)
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
            )
            RadarActionButton(
                imageVector = Icons.Rounded.Tune,
                onClick = onSettingsClick,
            )
        }
    }
}

@Composable
private fun RadarActionButton(
    imageVector: ImageVector,
    onClick: () -> Unit,
) {
    Box(
        modifier =
            Modifier
                .size(38.dp)
                .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface,
        )
    }
}
