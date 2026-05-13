package com.badeparday.aerolume.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

data class BottomBarDestination(
    val route: String,
    val label: String,
    val icon: ImageVector,
)

@Composable
fun PremiumBottomBar(
    currentRoute: String?,
    destinations: List<BottomBarDestination>,
    onNavigate: (BottomBarDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    GlassPanel(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        padding = 4.dp,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            destinations.forEach { destination ->
                val selected = currentRoute == destination.route
                val tint =
                    if (selected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                Column(
                    modifier =
                        Modifier
                            .weight(1f)
                            .clickable { onNavigate(destination) }
                            .padding(vertical = 7.dp, horizontal = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Box(
                        modifier =
                            Modifier
                                .background(
                                    color = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = RoundedCornerShape(1.dp),
                                )
                                .size(width = 16.dp, height = 2.dp),
                    ) {
                    }
                    Icon(
                        imageVector = destination.icon,
                        contentDescription = destination.label,
                        tint = tint,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        text = destination.label.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = tint,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}
