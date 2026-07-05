package com.bottomshelfer

import android.content.Context
import android.content.ContextWrapper
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.activity.ComponentDialog
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner

class BottomShelferDialog(
    context: Context,
    private val sheet: BottomShelferSheet
) : ComponentDialog(context, R.style.Theme_BottomShelfer_Transparent) {

    var dismissOnHide: Boolean = false

    private val dimmingView: View = View(context)
    private var isKeyboardVisible = false

    init {
        dimmingView.setBackgroundColor(sheet.config.dimmingColor)
        dimmingView.alpha = 0f
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sheet.parentDialog = this

        window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            setGravity(Gravity.CENTER)
            setBackgroundDrawableResource(android.R.color.transparent)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                setDecorFitsSystemWindows(false)
            } else {
                @Suppress("DEPRECATION")
                setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_UNSPECIFIED)
            }
            setFormat(android.graphics.PixelFormat.TRANSLUCENT)
        }

        val container = FrameLayout(context)
        container.setBackgroundColor(Color.TRANSPARENT)
        container.clipChildren = false
        container.clipToPadding = false
        container.addView(dimmingView, FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
        ))

        val sheetLp = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
        }
        container.addView(sheet, sheetLp)

        window?.decorView?.apply {
            ViewCompat.setOnApplyWindowInsetsListener(this) { _, insets ->
                val imeBottom = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
                isKeyboardVisible = imeBottom > 0
                sheet.setKeyboardOffset(imeBottom)
                insets
            }
            ViewCompat.requestApplyInsets(this)
        }

        if (sheet.config.isDimmingEnabled) {
            dimmingView.setOnClickListener {
                handleDimmingTap()
            }
        } else {
            dimmingView.isClickable = false
            dimmingView.alpha = 0f
        }

        // Try to find host owners from the context (e.g. Activity)
        val hostLifecycle = findOwner<LifecycleOwner>()
        val hostViewModelStore = findOwner<ViewModelStoreOwner>()
        val hostSavedState = findOwner<SavedStateRegistryOwner>()

        // Set ViewTree owners on the container. We fall back to 'this' (ComponentDialog)
        // to ensure children can always find a valid owner.
        container.setViewTreeLifecycleOwner(hostLifecycle ?: this as LifecycleOwner)
        container.setViewTreeViewModelStoreOwner(hostViewModelStore ?: this as ViewModelStoreOwner)
        container.setViewTreeSavedStateRegistryOwner(hostSavedState ?: this as SavedStateRegistryOwner)

        setContentView(container)
    }

    private inline fun <reified T> findOwner(): T? {
        var curContext = context
        while (curContext is ContextWrapper) {
            if (curContext is T) return curContext
            curContext = curContext.baseContext
        }
        return null
    }

    override fun show() {
        super.show()
        window?.decorView?.post {
            sheet.show(animate = true)
            fadeDimmingIn()
        }
    }

    override fun dismiss() {
        sheet.hide(animate = true)
        fadeDimmingOut {
            super.dismiss()
        }
    }

    fun dismissImmediately() {
        sheet.hide(animate = false)
        dimmingView.alpha = 0f
        try {
            super.dismiss()
        } catch (_: Exception) {
        }
    }

    private fun handleDimmingTap() {
        if (dismissOnHide) {
            dismiss()
            return
        }
        if (isKeyboardVisible) {
            val focusedView = currentFocus
            if (focusedView != null) {
                context.getSystemService(Context.INPUT_METHOD_SERVICE)
                    ?.let { it as android.view.inputmethod.InputMethodManager }
                    ?.hideSoftInputFromWindow(focusedView.windowToken, 0)
            }
            isKeyboardVisible = false
        } else {
            dismiss()
        }
    }

    private fun fadeDimmingIn() {
        if (!sheet.config.isDimmingEnabled) return
        dimmingView.animate()
            .alpha(1f)
            .setDuration(300)
            .start()
    }

    private fun fadeDimmingOut(onEnd: () -> Unit) {
        if (!sheet.config.isDimmingEnabled) {
            onEnd()
            return
        }
        dimmingView.animate()
            .alpha(0f)
            .setDuration(200)
            .withEndAction(onEnd)
            .start()
    }
}
