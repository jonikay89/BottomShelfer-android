package com.bottomshelfer.sample

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.bottomshelfer.BottomShelferCallback
import com.bottomshelfer.BottomShelferDetent
import com.bottomshelfer.BottomShelferDialog
import com.bottomshelfer.BottomShelferLayoutConfig
import com.bottomshelfer.BottomShelferSheet
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Demo(
    val title: String,
    val subtitle: String,
    val action: () -> Unit
)

object Demos {

    fun create(activity: MainActivity): List<Demo> = listOf(
        Demo("Default sheet", "Single large detent, package-default layout") {
            presentDefaultSheet(activity)
        },
        Demo("Multi-detent sheet", "Small / medium / large snap points + buttons") {
            presentFiltersSheet(activity)
        },
        Demo("Custom layout", "Wider sheet, big grabber, 0.6 max-height fraction") {
            presentCustomLayoutSheet(activity)
        },
        Demo("Scrollable sheet", "Embedded scroll view, drag-to-dismiss") {
            presentScrollableSheet(activity)
        },
        Demo("Keyboard-aware", "Sheet lifts above the keyboard when editing") {
            presentKeyboardSheet(activity)
        },
        Demo("No dimming scrim", "Transparent backdrop, content behind stays visible") {
            presentTransparentSheet(activity)
        },
        Demo("Non-draggable", "Dragging disabled — only dismissible via button") {
            presentFixedSheet(activity)
        },
        Demo("Custom grabber", "Wider, thicker, indigo grabber pill") {
            presentGrabberPillSheet(activity)
        },
        Demo("Compose content", "Jetpack Compose UI embedded in a bottom sheet") {
            presentComposeSheet(activity)
        },
        Demo("Hidden grabber pill", "Drag works, but the pill is invisible") {
            presentHiddenGrabberSheet(activity)
        },
        Demo("Custom grabber view", "Rainbow gradient grabber with custom animation") {
            presentCustomGrabberSheet(activity)
        },
        Demo("Drag & dismiss events", "Callback log for grabber, content, dismiss") {
            presentEventsSheet(activity)
        },
    )

    private fun presentDefaultSheet(activity: MainActivity) {
        val sheet = BottomShelferSheet(activity)
        sheet.setDetents(listOf(BottomShelferDetent.medium(activity)))
        sheet.setSelectedDetentIndex(0)

        val content = makeLabelButtonLayout(
            activity, "Tap the scrim or the button to dismiss.\nTry dragging the grabber too."
        ) { addDismissButton(activity, sheet) }
        sheet.addContentView(content)

        val dialog = BottomShelferDialog(activity, sheet)
        dialog.dismissOnHide = true
        dialog.show()
    }

    private fun presentFiltersSheet(activity: MainActivity) {
        val sheet = BottomShelferSheet(activity)
        sheet.setDetents(BottomShelferDetent.detentsForContentHeight(420, activity))
        sheet.setSelectedDetentIndex(1)

        val content = makeVerticalLayout(activity).apply {
            addView(makeLabel(activity, "Drag the grabber, or use the buttons below."))
            addView(makeButton(activity, "Snap to small") {
                val h = BottomShelferDetent.detentsForContentHeight(420, activity).firstOrNull()?.height ?: 200
                sheet.snapToHeight(h)
            })
            addView(makeButton(activity, "Snap to large") {
                val h = BottomShelferDetent.detentsForContentHeight(420, activity).lastOrNull()?.height ?: 1000
                sheet.snapToHeight(h)
            })
            addView(makeButton(activity, "Dismiss") {
                sheet.parentDialog?.dismiss()
            })
        }
        sheet.addContentView(content)

        val dialog = BottomShelferDialog(activity, sheet)
        dialog.show()
    }

    private fun presentCustomLayoutSheet(activity: MainActivity) {
        val sheet = BottomShelferSheet(activity)
        sheet.config = BottomShelferLayoutConfig(
            maxSheetWidthDp = 500,
            grabberPillWidthDp = 60,
            grabberPillHeightDp = 8,
            grabberPillBottomOffsetDp = 14,
            maxHeightFraction = 0.6f,
            cornerRadiusDp = 28f,
            dimmingColor = 0x66000000.toInt(),
        )
        sheet.setDetents(listOf(BottomShelferDetent.large(activity)))
        sheet.setSelectedDetentIndex(0)

        val content = makeLabelButtonLayout(
            activity, "Custom layout: wider, bigger grabber.\n60% max height, 28pt corners."
        ) { addDismissButton(activity, sheet) }
        sheet.addContentView(content)

        val dialog = BottomShelferDialog(activity, sheet)
        dialog.dismissOnHide = true
        dialog.show()
    }

    private fun presentScrollableSheet(activity: MainActivity) {
        val sheet = BottomShelferSheet(activity)
        sheet.setDetents(listOf(BottomShelferDetent.medium(activity), BottomShelferDetent.large(activity)))
        sheet.setSelectedDetentIndex(0)

        val content = RecyclerView(activity).apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                    val tv = TextView(parent.context).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                        setPadding(48, 24, 48, 24)
                        textSize = 16f
                    }
                    return object : RecyclerView.ViewHolder(tv) {}
                }
                override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                    (holder.itemView as TextView).text = "Row ${position + 1}"
                }
                override fun getItemCount() = 60
            }
        }
        sheet.addContentView(content, ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
        ))

        val dialog = BottomShelferDialog(activity, sheet)
        dialog.show()
    }

    private fun presentKeyboardSheet(activity: MainActivity) {
        val sheet = BottomShelferSheet(activity)
        sheet.setDetents(listOf(BottomShelferDetent.custom(200)))
        sheet.setSelectedDetentIndex(0)

        val content = makeVerticalLayout(activity).apply {
            addView(makeLabel(activity, "Tap the field — the sheet should lift for the keyboard."))
            addView(EditText(activity).apply {
                hint = "Type something..."
                inputType = InputType.TYPE_CLASS_TEXT
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
                )
            })
            addView(makeButton(activity, "Dismiss") {
                sheet.parentDialog?.dismiss()
            })
        }
        sheet.addContentView(content)

        val dialog = BottomShelferDialog(activity, sheet)
        dialog.show()
    }

    private fun presentTransparentSheet(activity: MainActivity) {
        val sheet = BottomShelferSheet(activity)
        sheet.config = sheet.config.copy(isDimmingEnabled = false)
        sheet.setDetents(listOf(BottomShelferDetent.medium(activity)))
        sheet.setSelectedDetentIndex(0)

        val content = makeLabelButtonLayout(
            activity, "No dimming scrim behind this sheet.\nTap the button to dismiss."
        ) { addDismissButton(activity, sheet) }
        sheet.addContentView(content)

        val dialog = BottomShelferDialog(activity, sheet)
        dialog.dismissOnHide = true
        dialog.show()
    }

    private fun presentFixedSheet(activity: MainActivity) {
        val sheet = BottomShelferSheet(activity)
        sheet.config = sheet.config.copy(isDraggingEnabled = false)
        sheet.setDetents(listOf(BottomShelferDetent.small(activity)))
        sheet.setSelectedDetentIndex(0)

        val content = makeLabelButtonLayout(
            activity, "This sheet cannot be dragged.\nUse the button to dismiss."
        ) { addDismissButton(activity, sheet) }
        sheet.addContentView(content)

        val dialog = BottomShelferDialog(activity, sheet)
        dialog.show()
    }

    private fun presentGrabberPillSheet(activity: MainActivity) {
        val sheet = BottomShelferSheet(activity)
        sheet.config = BottomShelferLayoutConfig(
            grabberPillWidthDp = 56,
            grabberPillHeightDp = 6,
            grabberPillCornerRadiusDp = 3f,
            grabberPillBottomOffsetDp = 16,
        )
        sheet.setDetents(listOf(BottomShelferDetent.medium(activity)))
        sheet.setSelectedDetentIndex(0)

        val content = makeLabelButtonLayout(
            activity, "Custom grabber: wider, thicker,\nbright pill with rounded ends."
        ) { addDismissButton(activity, sheet) }
        sheet.addContentView(content)

        val dialog = BottomShelferDialog(activity, sheet)
        dialog.dismissOnHide = true
        dialog.show()
    }

    private fun presentComposeSheet(activity: MainActivity) {
        val sheet = BottomShelferSheet(activity)
        sheet.setDetents(listOf(BottomShelferDetent.medium(activity), BottomShelferDetent.large(activity)))
        sheet.setSelectedDetentIndex(0)

        val content = makeVerticalLayout(activity).apply {
            addView(makeLabel(activity, "Jetpack Compose inside BottomShelfer").apply {
                textSize = 18f
                setTypeface(null, android.graphics.Typeface.BOLD)
            })

            val composeView = ComposeView(activity).apply {
                setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                setViewTreeLifecycleOwner(activity)
                setViewTreeViewModelStoreOwner(activity)
                setViewTreeSavedStateRegistryOwner(activity)
                setContent { ComposeDemoContent() }
            }
            addView(composeView, LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
            ))

            addView(makeButton(activity, "Dismiss") {
                sheet.parentDialog?.dismiss()
            })
        }
        sheet.addContentView(content)

        val dialog = BottomShelferDialog(activity, sheet)
        dialog.show()
    }

    private fun presentHiddenGrabberSheet(activity: MainActivity) {
        val sheet = BottomShelferSheet(activity)
        sheet.config = BottomShelferLayoutConfig(
            grabberPillWidthDp = 0,
            grabberPillHeightDp = 0,
            grabberPillBottomOffsetDp = 0,
        )
        sheet.setDetents(listOf(BottomShelferDetent.medium(activity)))
        sheet.setSelectedDetentIndex(0)

        val content = makeLabelButtonLayout(
            activity, "Hidden grabber pill.\nDrag still works via the top area!"
        ) { addDismissButton(activity, sheet) }
        sheet.addContentView(content)

        val dialog = BottomShelferDialog(activity, sheet)
        dialog.dismissOnHide = true
        dialog.show()
    }

    private fun presentCustomGrabberSheet(activity: MainActivity) {
        val sheet = BottomShelferSheet(activity)
        sheet.config = sheet.config.copy(
            grabberPillWidthDp = 0,
            grabberPillHeightDp = 0,
            grabberPillBottomOffsetDp = 0,
        )
        sheet.setDetents(listOf(BottomShelferDetent.medium(activity)))
        sheet.setSelectedDetentIndex(0)

        val rainbowGrabber = RainbowGrabberView(activity)
        sheet.callback = object : BottomShelferCallback {
            override fun onGrabberDragBegan() {
                rainbowGrabber.animate()
                    .scaleX(1.5f).scaleY(1.8f)
                    .rotation(3f)
                    .alpha(0.7f)
                    .setDuration(250)
                    .start()
            }
            override fun onGrabberDragEnded() {
                rainbowGrabber.animate()
                    .scaleX(1.0f).scaleY(1.0f)
                    .rotation(0f)
                    .alpha(1.0f)
                    .setDuration(250)
                    .start()
            }
        }

        val labelLayout = makeLabelButtonLayout(
            activity, "Custom rainbow grabber,\ndrag it to move the sheet."
        ) { addDismissButton(activity, sheet) }

        val container = FrameLayout(activity)
        container.addView(rainbowGrabber, FrameLayout.LayoutParams(96, 12).apply {
            gravity = Gravity.CENTER_HORIZONTAL or Gravity.TOP
            topMargin = 8
        })
        container.addView(labelLayout)

        sheet.addContentView(container)

        val dialog = BottomShelferDialog(activity, sheet)
        dialog.dismissOnHide = true
        dialog.show()
    }

    private fun presentEventsSheet(activity: MainActivity) {
        val sheet = BottomShelferSheet(activity)
        sheet.setDetents(BottomShelferDetent.detentsForContentHeight(420, activity))
        sheet.setSelectedDetentIndex(1)

        val eventLog = TextView(activity).apply {
            text = "Drag the grabber or tap the scrim..."
            setPadding(16, 16, 16, 16)
            setBackgroundColor(0x1A000000)
            textSize = 14f
            typeface = android.graphics.Typeface.MONOSPACE
            minHeight = 100
        }

        sheet.callback = object : BottomShelferCallback {
            private var logCount = 0
            private val fmt = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

            private fun log(msg: String) {
                logCount++
                val time = fmt.format(Date())
                val text = "#$logCount  [$time]  $msg"
                eventLog.text = text
                android.util.Log.d("BottomShelfer", text)
            }
            override fun onDismiss() { log("onDismiss") }
            override fun onGrabberDragBegan() { log("onGrabberDragBegan") }
            override fun onGrabberDragEnded() { log("onGrabberDragEnded") }
            override fun onContentDragBegan() { log("onContentDragBegan") }
            override fun onContentDragEnded() { log("onContentDragEnded") }
            override fun onDetentChanged(index: Int, height: Int) {
                log("onDetentChanged idx=$index h=${height}pt")
            }
        }

        val content = makeVerticalLayout(activity).apply {
            addView(makeLabel(activity, "Drag & dismiss events").apply {
                textSize = 18f
                setTypeface(null, android.graphics.Typeface.BOLD)
            })
            addView(eventLog, LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = 8 })
            addView(makeButton(activity, "Dismiss") {
                sheet.parentDialog?.dismiss()
            })
        }
        sheet.addContentView(content)

        val dialog = BottomShelferDialog(activity, sheet)
        dialog.show()
    }

    private fun makeVerticalLayout(context: Context): LinearLayout {
        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 60, 24, 24)
        }
    }

    private fun makeLabel(context: Context, text: String): TextView {
        return TextView(context).apply {
            this.text = text
            textSize = 16f
            gravity = Gravity.CENTER
        }
    }

    private fun makeButton(context: Context, text: String, onClick: () -> Unit): Button {
        return Button(context).apply {
            this.text = text
            setOnClickListener { onClick() }
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { gravity = Gravity.CENTER_HORIZONTAL }
        }
    }

    private fun makeLabelButtonLayout(
        context: Context,
        labelText: String,
        addButton: LinearLayout.() -> Unit
    ): LinearLayout {
        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 60, 24, 24)
            gravity = Gravity.CENTER_HORIZONTAL

            addView(makeLabel(context, labelText).apply {
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = 16 }
            })

            val btnContainer = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                addButton()
            }
            addView(btnContainer)
        }
    }

    private fun LinearLayout.addDismissButton(context: Context, sheet: BottomShelferSheet) {
        addView(makeButton(context, "Dismiss") {
            sheet.parentDialog?.dismiss()
        })
    }
}

class RainbowGrabberView(context: Context) : View(context) {
    private val colors = intArrayOf(
        Color.RED, Color.rgb(255, 165, 0), Color.YELLOW,
        Color.GREEN, Color.BLUE, Color.rgb(128, 0, 128)
    )
    private var gradient: LinearGradient? = null

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        gradient = LinearGradient(0f, 0f, w.toFloat(), 0f, colors, null, Shader.TileMode.CLAMP)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.shader = gradient
        canvas.drawRoundRect(0f, 0f, width.toFloat(), height.toFloat(), 6f, 6f, paint)
    }
}

@Composable
private fun ComposeDemoContent() {
    var selectedColor by remember { mutableStateOf("Blue") }
    var fontSize by remember { mutableFloatStateOf(16f) }
    var isEnabled by remember { mutableStateOf(true) }

    val allColors = listOf("Blue", "Green", "Orange", "Purple", "Red")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Color", style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.weight(1f))
            Text(
                selectedColor,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clickable {
                        val idx = allColors.indexOf(selectedColor)
                        selectedColor = allColors[(idx + 1) % allColors.size]
                    }
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                "Font size: ${fontSize.toInt()}pt",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Slider(
                value = fontSize,
                onValueChange = { fontSize = it },
                valueRange = 12f..28f,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Enabled", style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.weight(1f))
            Switch(checked = isEnabled, onCheckedChange = { isEnabled = it })
        }
    }
}
