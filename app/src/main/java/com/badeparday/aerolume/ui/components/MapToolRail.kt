package com.badeparday.aerolume.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.TravelExplore
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.badeparday.aerolume.ui.theme.Ink900

enum class ToolRailMode {
    Radar,
    Search,
    Saved,
    Settings,
}

@Composable
fun MapToolRail(
    selectedMode: ToolRailMode,
    hybridEnabled: Boolean,
    modifier: Modifier = Modifier,
    onShowRadar: () -> Unit,
    onShowSearch: () -> Unit,
    onShowSaved: () -> Unit,
    onToggleLayers: () -> Unit,
    onShowSettings: () -> Unit,
) {
    GlassPanel(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        padding = 6.dp,
        containerColor = Ink900.copy(alpha = 0.90f),
        borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.74f),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ToolRailButton(
                icon = Icons.Rounded.TravelExplore,
                selected = selectedMode == ToolRailMode.Radar,
                contentDescription = "Radar",
                onClick = onShowRadar,
            )
            ToolRailButton(
                icon = Icons.Rounded.Search,
                selected = selectedMode == ToolRailMode.Search,
                contentDescription = "Search",
                onClick = onShowSearch,
            )
            ToolRailButton(
                icon = Icons.Rounded.Bookmark,
                selected = selectedMode == ToolRailMode.Saved,
                contentDescription = "Saved flights",
                onClick = onShowSaved,
            )
            Box(
                modifier =
                    Modifier
                        .size(width = 26.dp, height = 1.dp)
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.34f)),
            )
            ToolRailButton(
                icon = Icons.Rounded.Layers,
                selected = hybridEnabled,
                contentDescription = "Toggle map layers",
                onClick = onToggleLayers,
            )
            ToolRailButton(
                icon = Icons.Rounded.Tune,
                selected = selectedMode == ToolRailMode.Settings,
                contentDescription = "Settings",
                onClick = onShowSettings,
            )
        }
    }
}

@Composable
private fun ToolRailButton(
    icon: ImageVector,
    selected: Boolean,
    contentDescription: String,
    onClick: () -> Unit,
) {
    Box(
        modifier =
            Modifier
                .size(42.dp)
                .background(
                    color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.14f) else Color.Transparent,
                    shape = RoundedCornerShape(10.dp),
                )
                .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
