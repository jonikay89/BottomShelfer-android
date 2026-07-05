package com.bottomshelfer

import android.content.Context
import android.util.DisplayMetrics
import android.view.WindowManager

data class BottomShelferDetent(val height: Int) {

    companion object {
        @JvmStatic
        fun small(context: Context): BottomShelferDetent {
            val screenHeight = getScreenHeight(context)
            return BottomShelferDetent((screenHeight * 0.25f).toInt())
        }

        @JvmStatic
        fun medium(context: Context): BottomShelferDetent {
            val screenHeight = getScreenHeight(context)
            return BottomShelferDetent((screenHeight * 0.5f).toInt())
        }

        @JvmStatic
        fun large(context: Context): BottomShelferDetent {
            val screenHeight = getScreenHeight(context)
            return BottomShelferDetent((screenHeight * 0.9f).toInt())
        }

        @JvmStatic
        fun custom(height: Int): BottomShelferDetent = BottomShelferDetent(height)

        @JvmStatic
        fun detentsForContentHeight(
            contentHeight: Int,
            context: Context,
            maxHeightFraction: Float = 0.9f
        ): List<BottomShelferDetent> {
            val screenHeight = getScreenHeight(context)
            val maxHeight = (screenHeight * maxHeightFraction).toInt()
            val clampedContentHeight = maxOf(200, minOf(contentHeight, maxHeight))
        return listOf(
            BottomShelferDetent((clampedContentHeight * 0.4f).toInt()),
            BottomShelferDetent(clampedContentHeight),
            BottomShelferDetent(minOf((clampedContentHeight * 1.5f).toInt(), maxHeight)),
        )
        }

        private fun getScreenHeight(context: Context): Int {
            val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val metrics = DisplayMetrics()
            @Suppress("DEPRECATION")
            wm.defaultDisplay.getRealMetrics(metrics)
            return metrics.heightPixels
        }
    }
}
