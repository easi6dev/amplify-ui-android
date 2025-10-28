package com.amplifyframework.ui.liveness.tada

import android.util.Size
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp

data class LivenessCoordinatorTargetParam(
    val targetWidthDp: Dp,
    val targetHeightDp: Dp,
    val density: Density,
    val targetFpsMin: Int = 24,
    val targetFpsMax: Int = 24,
    val targetEncodeBitrate: Int = (1024 * 1024 * .6).toInt(),
    val targetEncodeKeyFrameInternal: Int = 1,
) {
    val targetWidth: Int = targetWidthDp.toEvenPx()
    val targetHeight: Int = targetHeightDp.toEvenPx()
    val targetResolutionSize: Size = Size(
        targetWidth,
        targetHeight,
    )
    val targetAspectRatio: Float = targetWidth.toFloat() / targetHeight.toFloat()

    private fun Dp.toEvenPx(): Int {
        val px = with(density) { toPx() }
        val rounded = px.toInt()
        return if (rounded % 2 == 0) rounded else (rounded + 1)
    }
}
