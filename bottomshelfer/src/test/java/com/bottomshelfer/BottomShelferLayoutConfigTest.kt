package com.bottomshelfer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BottomShelferLayoutConfigTest {

    @Test
    fun defaultValues() {
        val config = BottomShelferLayoutConfig.DEFAULT

        assertEquals(430, config.maxSheetWidthDp)
        assertEquals(0.9f, config.maxHeightFraction)
        assertEquals(44, config.grabberHitAreaHeightDp)
        assertEquals(60, config.grabberPillWidthDp)
        assertEquals(8, config.grabberPillHeightDp)
        assertEquals(12, config.grabberPillBottomOffsetDp)
        assertEquals(2.5f, config.grabberPillCornerRadiusDp)
        assertEquals(28f, config.cornerRadiusDp)
        assertTrue(config.isDimmingEnabled)
        assertTrue(config.isDraggingEnabled)
    }

    @Test
    fun copyModifiesSingleProperty() {
        val original = BottomShelferLayoutConfig.DEFAULT
        val modified = original.copy(cornerRadiusDp = 42f)

        assertEquals(42f, modified.cornerRadiusDp)
        assertEquals(original.maxSheetWidthDp, modified.maxSheetWidthDp)
        assertEquals(original.maxHeightFraction, modified.maxHeightFraction)
    }

    @Test
    fun copyIsImmutable() {
        val config = BottomShelferLayoutConfig.DEFAULT
        val copy = config.copy()

        assertEquals(config, copy)
        assertTrue(config !== copy)
    }
}
