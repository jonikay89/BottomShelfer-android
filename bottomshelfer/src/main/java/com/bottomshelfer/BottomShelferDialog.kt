package com.bottomshelfer

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout

class BottomShelferDialog(
    context: Context,
    private val sheet: BottomShelferSheet
) : Dialog(context, android.R.style.Theme_Translucent_NoTitleBar) {

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
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            @Suppress("DEPRECATION")
            setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        }

        val container = FrameLayout(context)
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

        if (sheet.config.isDimmingEnabled) {
            dimmingView.setOnClickListener {
                handleDimmingTap()
            }
        } else {
            dimmingView.isClickable = false
            dimmingView.alpha = 0f
        }

        setContentView(container)

        window?.decorView?.let { decor ->
            val id = context.resources.getIdentifier(
                "view_tree_lifecycle_owner", "id", "androidx.lifecycle"
            )
            if (id != 0) {
                decor.setTag(id, context)
            }
        }
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
        super.dismiss()
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
