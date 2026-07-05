package com.bottomshelfer

import android.view.View
import android.widget.TextView
import androidx.test.core.app.ActivityScenario
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class BottomShelferSheetTest {

    @Test
    fun visibilityStartsGone() {
        ActivityScenario.launch(TestActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val sheet = BottomShelferSheet(activity)
                assertEquals(View.GONE, sheet.visibility)
            }
        }
    }

    @Test
    fun isVisibleIsInitiallyFalse() {
        ActivityScenario.launch(TestActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val sheet = BottomShelferSheet(activity)
                assertFalse(sheet.isVisible)
            }
        }
    }

    @Test
    fun setDetentsAndGetSelectedIndex() {
        ActivityScenario.launch(TestActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val sheet = BottomShelferSheet(activity)
                sheet.setDetents(listOf(
                    BottomShelferDetent.custom(200),
                    BottomShelferDetent.custom(400),
                    BottomShelferDetent.custom(600)
                ))
                assertEquals(0, sheet.getSelectedDetentIndex())
            }
        }
    }

    @Test
    fun setSelectedDetentIndex() {
        ActivityScenario.launch(TestActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val sheet = BottomShelferSheet(activity)
                sheet.setDetents(listOf(
                    BottomShelferDetent.custom(200),
                    BottomShelferDetent.custom(400)
                ))
                sheet.setSelectedDetentIndex(1)
                assertEquals(1, sheet.getSelectedDetentIndex())
            }
        }
    }

    @Test
    fun addContentViewAddsChild() {
        ActivityScenario.launch(TestActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val sheet = BottomShelferSheet(activity)
                val content = TextView(activity).apply { text = "test" }
                sheet.addContentView(content)

                assertEquals(1, sheet.contentLayout.childCount)
                assertEquals(content, sheet.contentLayout.getChildAt(0))
            }
        }
    }

    @Test
    fun configPropertyDelegationCornerRadiusDp() {
        ActivityScenario.launch(TestActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val sheet = BottomShelferSheet(activity)
                sheet.cornerRadiusDp = 30f
                assertEquals(30f, sheet.config.cornerRadiusDp)
            }
        }
    }

    @Test
    fun configPropertyDelegationIsDraggingEnabled() {
        ActivityScenario.launch(TestActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val sheet = BottomShelferSheet(activity)
                sheet.isDraggingEnabled = false
                assertFalse(sheet.config.isDraggingEnabled)
            }
        }
    }

    @Test
    fun snapToHeightSetsSelectedDetentIndex() {
        var sheet: BottomShelferSheet? = null

        ActivityScenario.launch(TestActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                sheet = BottomShelferSheet(activity)
                sheet!!.setDetents(listOf(
                    BottomShelferDetent.custom(200),
                    BottomShelferDetent.custom(400),
                    BottomShelferDetent.custom(600)
                ))
                val dialog = BottomShelferDialog(activity, sheet!!)
                dialog.show()
            }
            androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().waitForIdleSync()
            scenario.onActivity {
                sheet!!.snapToHeight(400)
            }
            androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().waitForIdleSync()
            scenario.onActivity {
                assertEquals(1, sheet!!.getSelectedDetentIndex())
            }
        }
    }

    @Test
    fun callbackOnDismissCalledOnHide() {
        ActivityScenario.launch(TestActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val sheet = BottomShelferSheet(activity)
                sheet.setDetents(listOf(BottomShelferDetent.custom(200)))
                activity.container.addView(sheet)

                var dismissed = false
                sheet.callback = object : BottomShelferCallback {
                    override fun onDismiss() { dismissed = true }
                }
                sheet.show(animate = false)
                sheet.hide(animate = false)
                assertTrue(dismissed)
            }
        }
    }

    @Test
    fun setDetentsClampedToMaxHeightFraction() {
        ActivityScenario.launch(TestActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val sheet = BottomShelferSheet(activity)
                sheet.config = sheet.config.copy(maxHeightFraction = 0.1f)
                sheet.setDetents(listOf(BottomShelferDetent.custom(99999)))
                activity.container.addView(sheet)
                sheet.show(animate = false)

                sheet.snapToHeight(99999)
                assertEquals(0, sheet.getSelectedDetentIndex())
            }
        }
    }

    @Test
    fun autoFocusDefaultsToFalse() {
        ActivityScenario.launch(TestActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val sheet = BottomShelferSheet(activity)
                assertFalse(sheet.autoFocus)
            }
        }
    }

    @Test
    fun addContentViewWithLayoutParams() {
        ActivityScenario.launch(TestActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val sheet = BottomShelferSheet(activity)
                val content = TextView(activity).apply { text = "test" }
                sheet.addContentView(content, android.widget.FrameLayout.LayoutParams(100, 100))
                assertEquals(content, sheet.contentLayout.getChildAt(0))
            }
        }
    }
}
