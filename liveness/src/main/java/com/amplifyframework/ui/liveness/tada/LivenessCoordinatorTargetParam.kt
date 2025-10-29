package com.amplifyframework.ui.liveness.tada

import android.util.Size
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp

data class LivenessCoordinatorTargetParam(
    val targetWidthDp: Dp,
    val targetHeightDp: Dp,
    val videoWidth: Int,
    val videoHeight: Int,
    val videoResolutionSize: Size,
    val targetAspectRatio: Float,
    val density: Density,
    val targetFpsMin: Int = 24,
    val targetFpsMax: Int = 24,
    val targetEncodeBitrate: Int = (1024 * 1024 * .6).toInt(),
    val targetEncodeKeyFrameInternal: Int = 1,
)