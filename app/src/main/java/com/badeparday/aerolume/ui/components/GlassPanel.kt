package com.badeparday.aerolume.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.badeparday.aerolume.ui.theme.AerolumeThemeTokens

@Composable
fun GlassPanel(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(16.dp),
    padding: Dp = 0.dp,
    containerColor: Color = Color.Unspecified,
    borderColor: Color = Color.Unspecified,
    content: @Composable BoxScope.() -> Unit,
) {
    val colors = AerolumeThemeTokens.colors
    val resolvedContainer = if (containerColor == Color.Unspecified) colors.panelRaised else containerColor
    val resolvedBorder = if (borderColor == Color.Unspecified) colors.border else borderColor
    Box(
        modifier =
            modifier
                .clip(shape)
                .background(resolvedContainer)
                .border(1.dp, resolvedBorder, shape)
                .padding(padding),
        content = content,
    )
}
