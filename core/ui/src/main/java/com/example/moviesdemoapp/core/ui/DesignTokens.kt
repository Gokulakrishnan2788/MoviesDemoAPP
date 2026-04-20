package com.example.moviesdemoapp.core.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Single source of truth for all design primitives in MovieApp. */
object DesignTokens {
    // Colors
    val ScreenBackground: Color = Color(0xFF0D0F14)
    val CardBackground: Color = Color(0xFF1A1D27)
    val Surface: Color = Color(0xFF1E2132)
    val PrimaryText: Color = Color(0xFFFFFFFF)
    val SecondaryText: Color = Color(0xFFAAAAAA)
    val Accent: Color = Color(0xFFE05C5C)

    // Spacing
    val SpacingXs: Dp = 4.dp
    val SpacingSm: Dp = 8.dp
    val SpacingMd: Dp = 16.dp
    val SpacingLg: Dp = 24.dp
    val SpacingXl: Dp = 32.dp

    // Typography
    val TextSm: TextUnit = 12.sp
    val TextMd: TextUnit = 14.sp
    val TextLg: TextUnit = 16.sp
    val TextXl: TextUnit = 20.sp
    val TextXxl: TextUnit = 24.sp

    // Shape radii
    val RadiusSm: Dp = 4.dp
    val RadiusMd: Dp = 8.dp
    val RadiusLg: Dp = 12.dp
}

/** Resolves an SDUI color token string to its [Color] value. */
fun colorFromToken(token: String): Color = when (token) {
    "screenBackground" -> DesignTokens.ScreenBackground
    "cardBackground" -> DesignTokens.CardBackground
    "surface" -> DesignTokens.Surface
    "primaryText" -> DesignTokens.PrimaryText
    "secondaryText" -> DesignTokens.SecondaryText
    "accent" -> DesignTokens.Accent
    else -> Color.Unspecified
}
