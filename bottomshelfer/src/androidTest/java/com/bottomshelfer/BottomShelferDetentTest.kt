package com.bottomshelfer

import android.app.Application
import android.content.Context
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class BottomShelferDetentTest {

    private val context: Context = ApplicationProvider.getApplicationContext<Application>()

    @Test
    fun customReturnsGivenHeight() {
        val detent = BottomShelferDetent.custom(320)
        assertEquals(320, detent.height)
    }

    @Test
    fun smallIs25PercentOfScreen() {
        val detent = BottomShelferDetent.small(context)
        val expected = (getRealScreenHeight() * 0.25f).toInt()
        assertEquals(expected, detent.height)
    }

    @Test
    fun mediumIs50PercentOfScreen() {
        val detent = BottomShelferDetent.medium(context)
        val expected = (getRealScreenHeight() * 0.5f).toInt()
        assertEquals(expected, detent.height)
    }

    @Test
    fun largeIs90PercentOfScreen() {
        val detent = BottomShelferDetent.large(context)
        val expected = (getRealScreenHeight() * 0.9f).toInt()
        assertEquals(expected, detent.height)
    }

    @Test
    fun detentsForContentHeightReturnsThreeDetents() {
        val detents = BottomShelferDetent.detentsForContentHeight(500, context)
        assertEquals(3, detents.size)
    }

    @Test
    fun detentsForContentHeightMediumMatchesContentHeight() {
        val detents = BottomShelferDetent.detentsForContentHeight(500, context)
        assertEquals(500, detents[1].height)
    }

    @Test
    fun detentsForContentHeightSmallIs40Percent() {
        val detents = BottomShelferDetent.detentsForContentHeight(500, context)
        assertEquals(200, detents[0].height)
    }

    @Test
    fun detentsForContentHeightLargeIs150Percent() {
        val detents = BottomShelferDetent.detentsForContentHeight(500, context)
        assertEquals(750, detents[2].height)
    }

    @Test
    fun detentsForContentHeightClampsBelow200() {
        val detents = BottomShelferDetent.detentsForContentHeight(50, context)
        assertEquals(200, detents[1].height)
    }

    @Test
    fun detentsForContentHeightRespectsMaxHeightFraction() {
        val detents = BottomShelferDetent.detentsForContentHeight(10000, context, maxHeightFraction = 0.5f)
        val screenMax = (getRealScreenHeight() * 0.5f).toInt()
        assertTrue(detents[2].height <= screenMax)
    }

    @Suppress("DEPRECATION")
    private fun getRealScreenHeight(): Int {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val metrics = DisplayMetrics()
        wm.defaultDisplay.getRealMetrics(metrics)
        return metrics.heightPixels
    }
}
