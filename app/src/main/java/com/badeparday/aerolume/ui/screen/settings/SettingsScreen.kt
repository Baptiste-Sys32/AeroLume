package com.badeparday.aerolume.ui.screen.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.badeparday.aerolume.domain.model.MapAppearance
import com.badeparday.aerolume.domain.model.MotionIntensity
import com.badeparday.aerolume.domain.model.UnitSystem
import com.badeparday.aerolume.ui.components.GlassPanel

@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    contentPadding: PaddingValues,
    onDemoModeChanged: (Boolean) -> Unit,
    onMotionIntensityChanged: (MotionIntensity) -> Unit,
    onUnitSystemChanged: (UnitSystem) -> Unit,
    onMapAppearanceChanged: (MapAppearance) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding =
            PaddingValues(
                start = 16.dp,
                top = 18.dp,
                end = 16.dp,
                bottom = contentPadding.calculateBottomPadding() + 108.dp,
            ),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            GlassPanel(
                modifier =
                    Modifier
                        .statusBarsPadding()
                        .fillMaxWidth(),
                padding = 20.dp,
            ) {
                androidx.compose.foundation.layout.Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "SETTINGS",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "Quiet controls for the prototype",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }

        item {
            GlassPanel(
                modifier = Modifier.fillMaxWidth(),
                padding = 18.dp,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    androidx.compose.foundation.layout.Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Demo mode",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = "Use the built-in simulation repository",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(
                        checked = uiState.settings.demoMode,
                        onCheckedChange = onDemoModeChanged,
                    )
                }
            }
        }

        item {
            ChoiceSection(
                title = "Motion intensity",
                subtitle = "Dial motion up or down without changing layout.",
                options = MotionIntensity.entries.map { it to it.name },
                selected = uiState.settings.motionIntensity,
                onSelected = onMotionIntensityChanged,
            )
        }

        item {
            ChoiceSection(
                title = "Units",
                subtitle = "Swap between aviation-native imperial and metric output.",
                options = UnitSystem.entries.map { it to it.name },
                selected = uiState.settings.unitSystem,
                onSelected = onUnitSystemChanged,
            )
        }

        item {
            ChoiceSection(
                title = "Map appearance",
                subtitle = "Vector keeps the radar restrained, hybrid adds terrain context.",
                options =
                    listOf(
                        MapAppearance.NightVector to "Night Vector",
                        MapAppearance.NightHybrid to "Night Hybrid",
                    ),
                selected = uiState.settings.mapAppearance,
                onSelected = onMapAppearanceChanged,
            )
        }
    }
}

@Composable
private fun <T> ChoiceSection(
    title: String,
    subtitle: String,
    options: List<Pair<T, String>>,
    selected: T,
    onSelected: (T) -> Unit,
) {
    GlassPanel(
        modifier = Modifier.fillMaxWidth(),
        padding = 18.dp,
    ) {
        androidx.compose.foundation.layout.Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            androidx.compose.foundation.layout.Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            androidx.compose.foundation.layout.FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                options.forEach { (value, label) ->
                    val active = value == selected
                    GlassPanel(
                        modifier = Modifier.clickable { onSelected(value) },
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                        padding = 12.dp,
                    ) {
                        Text(
                            text = label.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}
