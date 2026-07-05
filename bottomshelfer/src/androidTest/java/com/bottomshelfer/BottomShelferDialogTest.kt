package com.bottomshelfer

import android.widget.TextView
import androidx.test.core.app.ActivityScenario
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class BottomShelferDialogTest {

    @Test
    fun dialogShowsAndDismisses() {
        var sheet: BottomShelferSheet? = null
        var dialog: BottomShelferDialog? = null

        ActivityScenario.launch(TestActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                sheet = BottomShelferSheet(activity)
                sheet!!.setDetents(listOf(BottomShelferDetent.custom(200)))
                sheet!!.addContentView(TextView(activity).apply { text = "hello" })

                dialog = BottomShelferDialog(activity, sheet!!)
                dialog!!.show()
            }
            androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().waitForIdleSync()
            scenario.onActivity {
                assertTrue(sheet!!.isVisible)
                assertEquals(dialog, sheet!!.parentDialog)
            }
            scenario.onActivity {
                dialog!!.dismissImmediately()
            }
            androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().waitForIdleSync()
            scenario.onActivity {
                assertFalse(sheet!!.isVisible)
            }
        }
    }

    @Test
    fun dialogDismissOnHideDefaultsToFalse() {
        ActivityScenario.launch(TestActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val sheet = BottomShelferSheet(activity)
                val dialog = BottomShelferDialog(activity, sheet)
                assertFalse(dialog.dismissOnHide)
            }
        }
    }

    @Test
    fun dialogDismissOnHideCanBeSet() {
        ActivityScenario.launch(TestActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val sheet = BottomShelferSheet(activity)
                val dialog = BottomShelferDialog(activity, sheet)
                dialog.dismissOnHide = true
                assertTrue(dialog.dismissOnHide)
            }
        }
    }

    @Test
    fun sheetParentDialogIsSetOnCreate() {
        var sheet: BottomShelferSheet? = null
        var dialog: BottomShelferDialog? = null

        ActivityScenario.launch(TestActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                sheet = BottomShelferSheet(activity)
                dialog = BottomShelferDialog(activity, sheet!!)
                dialog!!.show()
            }
            androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().waitForIdleSync()
            scenario.onActivity {
                assertEquals(dialog, sheet!!.parentDialog)
                dialog!!.dismissImmediately()
            }
        }
    }
}
