package com.bottomshelfer

data class BottomShelferLayoutConfig(
    val maxSheetWidthDp: Int = 430,
    val maxHeightFraction: Float = 0.9f,
    val grabberHitAreaHeightDp: Int = 44,
    val grabberPillWidthDp: Int = 60,
    val grabberPillHeightDp: Int = 8,
    val grabberPillColor: Int = 0x99000000.toInt(),
    val grabberPillBottomOffsetDp: Int = 12,
    val grabberPillCornerRadiusDp: Float = 2.5f,
    val cornerRadiusDp: Float = 28f,
    val isDimmingEnabled: Boolean = true,
    val isDraggingEnabled: Boolean = true,
    val allowGrabbingNonScrollViews: Boolean = false,
    val dimmingColor: Int = 0x4D000000.toInt()
) {
    companion object {
        @JvmField
        val DEFAULT = BottomShelferLayoutConfig()
    }
}
