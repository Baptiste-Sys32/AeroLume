package com.badeparday.aerolume.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

val Ink950 = Color(0xFF020202)
val Ink900 = Color(0xFF050505)
val Slate850 = Color(0xFF090909)
val Slate800 = Color(0xFF0C0C0D)
val Slate700 = Color(0xFF111214)
val Frost = Color(0xFFF4F5F7)
val Mist = Color(0xFFADB0B4)
val Haze = Color(0xFF73777B)
val AviationBlue = Color(0xFF7898B0)
val CyanGlow = Color(0xFF9FA8B0)
val SoftGreen = Color(0xFF8ED6AF)
val AmberSignal = Color(0xFFFFB86B)
val RoseAlert = Color(0xFFFF7B7B)

@Immutable
data class AerolumeExtendedColors(
    val panel: Color,
    val panelRaised: Color,
    val panelMuted: Color,
    val glass: Color,
    val border: Color,
    val glow: Color,
    val success: Color,
    val warning: Color,
)

val DefaultExtendedColors =
    AerolumeExtendedColors(
        panel = Color(0xFF080808),
        panelRaised = Color(0xFF0B0B0C),
        panelMuted = Color(0xFF101011),
        glass = Color(0xD9060606),
        border = Color(0x16FFFFFF),
        glow = Color(0x0A000000),
        success = SoftGreen,
        warning = AmberSignal,
    )
