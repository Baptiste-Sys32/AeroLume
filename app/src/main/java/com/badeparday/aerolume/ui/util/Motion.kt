package com.badeparday.aerolume.ui.util

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring
import com.badeparday.aerolume.domain.model.MotionIntensity

fun <T> springFor(intensity: MotionIntensity): SpringSpec<T> =
    when (intensity) {
        MotionIntensity.Reduced ->
            spring(
                dampingRatio = 1f,
                stiffness = Spring.StiffnessHigh,
            )

        MotionIntensity.Standard ->
            spring(
                dampingRatio = 0.9f,
                stiffness = Spring.StiffnessMediumLow,
            )

        MotionIntensity.Expressive ->
            spring(
                dampingRatio = 0.8f,
                stiffness = Spring.StiffnessLow,
            )
    }
