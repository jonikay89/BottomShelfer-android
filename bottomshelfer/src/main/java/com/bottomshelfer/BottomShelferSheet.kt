package com.bottomshelfer

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Outline
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import androidx.core.view.NestedScrollingParent3
import androidx.core.view.ViewCompat
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import com.bottomshelfer.internal.GrabberView
import kotlin.math.abs

@SuppressLint("ClickableViewAccessibility")
class BottomShelferSheet @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), NestedScrollingParent3 {

    var autoFocus: Boolean = false

    var config: BottomShelferLayoutConfig = BottomShelferLayoutConfig.DEFAULT
        set(value) {
            field = value
            applyConfig()
        }

    var cornerRadiusDp: Float
        get() = config.cornerRadiusDp
        set(value) { config = config.copy(cornerRadiusDp = value) }

    var isDraggingEnabled: Boolean
        get() = config.isDraggingEnabled
        set(value) { config = config.copy(isDraggingEnabled = value) }

    var allowGrabbingNonScrollViews: Boolean
        get() = config.allowGrabbingNonScrollViews
        set(value) { config = config.copy(allowGrabbingNonScrollViews = value) }

    var callback: BottomShelferCallback? = null
    var parentDialog: BottomShelferDialog? = null

    val contentLayout: FrameLayout = FrameLayout(context)

    private val grabberHitArea: View = View(context)
    private var grabberPill: GrabberView? = null
    private var keyboardOffsetY = 0
    private var snapYPositions = mutableListOf<Float>()
    private var snapHeights = mutableListOf<Float>()
    private var detents: List<BottomShelferDetent> = emptyList()
    private var selectedDetentIndex: Int = 0
    private var touchDownY = 0f
    private var dragStartTranslationY = 0f
    private var isUserDragging = false
    private var maxSheetHeight = 0
    private var containerHeight = 0
    private var velocityTracker: VelocityTracker? = null
    private var trackedScrollView: View? = null
    private var nestedScrollActive = false
    private var nestedScrollTranslationY = 0f
    private var touchActive = false

    private var _isVisible = false
    val isVisible: Boolean get() = _isVisible

    init {
        if (attrs != null) {
            val a = context.obtainStyledAttributes(attrs, R.styleable.BottomShelferSheet)
            val radius = a.getDimension(R.styleable.BottomShelferSheet_bottomshelfer_cornerRadius, -1f)
            val maxWidth = a.getDimension(R.styleable.BottomShelferSheet_bottomshelfer_maxSheetWidth, -1f)
            val maxFrac = a.getFloat(R.styleable.BottomShelferSheet_bottomshelfer_maxHeightFraction, -1f)
            val grabberArea = a.getDimension(R.styleable.BottomShelferSheet_bottomshelfer_grabberHitAreaHeight, -1f)
            val pillW = a.getDimension(R.styleable.BottomShelferSheet_bottomshelfer_grabberPillWidth, -1f)
            val pillH = a.getDimension(R.styleable.BottomShelferSheet_bottomshelfer_grabberPillHeight, -1f)
            val pillOff = a.getDimension(R.styleable.BottomShelferSheet_bottomshelfer_grabberPillBottomOffset, -1f)
            val pillRad = a.getDimension(R.styleable.BottomShelferSheet_bottomshelfer_grabberPillCornerRadius, -1f)
            val dim = a.getBoolean(R.styleable.BottomShelferSheet_bottomshelfer_isDimmingEnabled, true)
            val drag = a.getBoolean(R.styleable.BottomShelferSheet_bottomshelfer_isDraggingEnabled, true)
            val dimColor = a.getColor(R.styleable.BottomShelferSheet_bottomshelfer_dimingColor, 0x4D000000.toInt())
            a.recycle()

            config = BottomShelferLayoutConfig(
                cornerRadiusDp = if (radius > 0) pxToDp(radius) else 20f,
                maxSheetWidthDp = if (maxWidth > 0) pxToDp(maxWidth).toInt() else 430,
                maxHeightFraction = if (maxFrac > 0) maxFrac else 0.9f,
                grabberHitAreaHeightDp = if (grabberArea > 0) pxToDp(grabberArea).toInt() else 44,
                grabberPillWidthDp = if (pillW > 0) pxToDp(pillW).toInt() else 36,
                grabberPillHeightDp = if (pillH > 0) pxToDp(pillH).toInt() else 5,
                grabberPillBottomOffsetDp = if (pillOff > 0) pxToDp(pillOff).toInt() else 12,
                grabberPillCornerRadiusDp = if (pillRad > 0) pxToDp(pillRad) else 2.5f,
                isDimmingEnabled = dim,
                isDraggingEnabled = drag,
                dimmingColor = dimColor,
            )
        }

        clipChildren = false
        clipToPadding = false
        visibility = View.GONE
        setBackgroundColor(Color.WHITE)

        grabberHitArea.setBackgroundColor(Color.TRANSPARENT)
        addView(grabberHitArea)

        contentLayout.id = ViewCompat.generateViewId()
        addView(contentLayout)

        applyConfig()
    }

    private fun applyConfig() {
        grabberPill?.let { removeView(it) }
        grabberPill = null

        if (config.grabberPillWidthDp > 0 && config.grabberPillHeightDp > 0) {
            val pillW = dpToPx(config.grabberPillWidthDp.toFloat())
            val pillH = dpToPx(config.grabberPillHeightDp.toFloat())
            val pillR = dpToPx(config.grabberPillCornerRadiusDp)
            grabberPill = GrabberView(context, pillW, pillH, pillR, config.grabberPillColor)
            grabberPill?.let { addView(it) }
        }

        applyCornerMask()
    }

    private fun applyCornerMask() {
        val radius = dpToPx(config.cornerRadiusDp)
        clipToOutline = true
        outlineProvider = object : android.view.ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                outline.setRoundRect(0, 0, view.width, view.height + radius.toInt(), radius)
            }
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val parent = parent as? View
        containerHeight = parent?.height ?: context.resources.displayMetrics.heightPixels
        val maxH = (containerHeight * config.maxHeightFraction).toInt()
        maxSheetHeight = maxH
        rebuildSnapPoints()
        if (pendingShow) {
            pendingShow = false
            translationY = maxSheetHeight.toFloat()
            visibility = View.VISIBLE
            snapToCurrentDetent(animate = true)
        } else if (!isUserDragging && !touchActive) {
            snapToCurrentDetent(animate = false)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val maxWidthPx = dpToPx(config.maxSheetWidthDp.toFloat()).toInt()
        val sheetWidth = minOf(widthSize, maxWidthPx)

        val parentView = parent as? View
        val heightFromParent = if (heightSize > 0) heightSize else (parentView?.height ?: 0)
        containerHeight = if (heightFromParent > 0) heightFromParent else resources.displayMetrics.heightPixels
        maxSheetHeight = (containerHeight * config.maxHeightFraction).toInt()

        val widthSpec = MeasureSpec.makeMeasureSpec(sheetWidth, MeasureSpec.EXACTLY)
        val heightSpec = MeasureSpec.makeMeasureSpec(maxSheetHeight, MeasureSpec.EXACTLY)
        val visibleSheetH = ((maxSheetHeight - translationY).coerceIn(0f, maxSheetHeight.toFloat())).toInt()
        val contentHeightSpec = MeasureSpec.makeMeasureSpec(visibleSheetH, MeasureSpec.EXACTLY)

        for (i in 0 until childCount) {
            val child = getChildAt(i)
            val spec = if (child === contentLayout) contentHeightSpec else heightSpec
            child.measure(widthSpec, spec)
        }
        setMeasuredDimension(sheetWidth, maxSheetHeight)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        val grabberAreaH = dpToPx(config.grabberHitAreaHeightDp.toFloat()).toInt()
        grabberHitArea.layout(0, 0, right - left, grabberAreaH)

        grabberPill?.let { pill ->
            val pillW = pill.measuredWidth
            val pillH = pill.measuredHeight
            val pillX = ((right - left) - pillW) / 2
            val pillOffset = dpToPx(config.grabberPillBottomOffsetDp.toFloat()).toInt()
            val pillY = grabberAreaH - pillH - pillOffset
            pill.layout(pillX, maxOf(pillY, 0), pillX + pillW, pillY + pillH)
        }

        val visibleSheetH = ((maxSheetHeight - translationY).coerceIn(0f, maxSheetHeight.toFloat())).toInt()
        contentLayout.layout(0, 0, right - left, visibleSheetH)
    }

    fun setDetents(detents: List<BottomShelferDetent>) {
        this.detents = detents.sortedBy { it.height }
        rebuildSnapPoints()
    }

    fun setSelectedDetentIndex(index: Int) {
        selectedDetentIndex = minOf(index, maxOf(detents.size - 1, 0))
        snapToCurrentDetent(animate = false)
    }

    fun getSelectedDetentIndex(): Int = selectedDetentIndex

    fun snapToHeight(targetHeight: Int) {
        val maxH = (containerHeight * config.maxHeightFraction).toInt()
        val clamped = minOf(targetHeight, maxH)
        selectedDetentIndex = detentIndexForHeight(clamped)
        val targetOffset = (maxSheetHeight - clamped - keyboardOffsetY).toFloat()
        animateToTranslationY(targetOffset)
    }

    private var pendingShow = false

    fun show(animate: Boolean = true) {
        val wasVisible = _isVisible
        _isVisible = true
        rebuildSnapPoints()
        if (!wasVisible && animate) {
            if (maxSheetHeight > 0) {
                translationY = maxSheetHeight.toFloat()
                visibility = View.VISIBLE
                snapToCurrentDetent(animate = true)
            } else {
                pendingShow = true
                visibility = View.INVISIBLE
            }
        } else if (!animate) {
            visibility = View.VISIBLE
            snapToCurrentDetent(animate = false)
        } else {
            visibility = View.VISIBLE
        }
        if (autoFocus && _isVisible) {
            postDelayed({
                focusFirstEditText(contentLayout)
            }, 300)
        }
    }

    private fun focusFirstEditText(parent: ViewGroup) {
        for (i in 0 until parent.childCount) {
            val child = parent.getChildAt(i)
            if (child is android.widget.EditText) {
                child.requestFocus()
                child.post {
                    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                    imm?.showSoftInput(child, InputMethodManager.SHOW_FORCED)
                }
                return
            }
            if (child is ViewGroup) {
                focusFirstEditText(child)
            }
        }
    }

    fun hide(animate: Boolean = true) {
        if (!_isVisible) return
        _isVisible = false
        callback?.onDismiss()
        if (animate) {
            animateToTranslationY(maxSheetHeight.toFloat()) {
                visibility = View.GONE
            }
        } else {
            translationY = maxSheetHeight.toFloat()
            visibility = View.GONE
        }
    }

    fun addContentView(view: View) {
        contentLayout.addView(view)
    }

    fun addContentView(view: View, params: ViewGroup.LayoutParams) {
        contentLayout.addView(view, params)
    }

    override fun onStartNestedScroll(child: View, target: View, axes: Int, type: Int): Boolean {
        return axes and ViewCompat.SCROLL_AXIS_VERTICAL != 0
    }

    override fun onStartNestedScroll(child: View, target: View, axes: Int): Boolean {
        return onStartNestedScroll(child, target, axes, ViewCompat.TYPE_TOUCH)
    }

    override fun onNestedScrollAccepted(child: View, target: View, axes: Int, type: Int) {
        nestedScrollActive = true
        touchActive = true
        nestedScrollTranslationY = translationY
    }

    override fun onNestedScrollAccepted(child: View, target: View, axes: Int) {
        onNestedScrollAccepted(child, target, axes, ViewCompat.TYPE_TOUCH)
    }

    override fun onStopNestedScroll(target: View, type: Int) {
        nestedScrollActive = false
        touchActive = false
        isUserDragging = false
    }

    override fun onStopNestedScroll(target: View) {
        onStopNestedScroll(target, ViewCompat.TYPE_TOUCH)
    }

    override fun onNestedScroll(
        target: View, dxConsumed: Int, dyConsumed: Int,
        dxUnconsumed: Int, dyUnconsumed: Int, type: Int, consumed: IntArray
    ) {
        if (dyUnconsumed != 0) {
            consumed[1] = dyUnconsumed
        }
    }

    override fun onNestedScroll(
        target: View, dxConsumed: Int, dyConsumed: Int,
        dxUnconsumed: Int, dyUnconsumed: Int, type: Int
    ) {
        onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type, IntArray(2))
    }

    override fun onNestedScroll(
        target: View, dxConsumed: Int, dyConsumed: Int,
        dxUnconsumed: Int, dyUnconsumed: Int
    ) {
        onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, ViewCompat.TYPE_TOUCH)
    }

    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray, type: Int) {
    }

    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray) {
    }

    override fun onNestedFling(
        target: View, velocityX: Float, velocityY: Float, consumed: Boolean
    ): Boolean = false

    override fun onNestedPreFling(target: View, velocityX: Float, velocityY: Float): Boolean = false

    override fun getNestedScrollAxes(): Int {
        return if (nestedScrollActive) ViewCompat.SCROLL_AXIS_VERTICAL else ViewCompat.SCROLL_AXIS_NONE
    }

    private fun snapToNearestDetent() {
        if (snapYPositions.isEmpty()) return
        val currentOffset = translationY + keyboardOffsetY
        var bestIndex = 0
        var bestDist = Float.MAX_VALUE
        for (i in snapYPositions.indices) {
            val dist = abs(currentOffset - snapYPositions[i])
            if (dist < bestDist) {
                bestDist = dist
                bestIndex = i
            }
        }
        selectedDetentIndex = bestIndex
        val target = snapYPositions[bestIndex] - keyboardOffsetY
        animateToTranslationY(target)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (!config.isDraggingEnabled) return false
        if (!_isVisible) return false

        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                touchDownY = ev.rawY
                dragStartTranslationY = translationY
                isUserDragging = true
                touchActive = true
                val localY = ev.y
                val grabberAreaH = dpToPx(config.grabberHitAreaHeightDp.toFloat())
                if (localY < grabberAreaH) {
                    return true
                }
                trackedScrollView = findScrollViewAt(ev.rawX, ev.rawY)
                if (trackedScrollView != null) {
                    return false
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                touchActive = false
            }
            MotionEvent.ACTION_MOVE -> {
                if (trackedScrollView != null) return false
                val dy = touchDownY - ev.rawY
                if (abs(dy) > 10f) {
                    return true
                }
            }
        }
        return super.onInterceptTouchEvent(ev)
    }

    @SuppressLint("Recycle")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!config.isDraggingEnabled) return false
        if (!_isVisible) return false

        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain()
        }
        velocityTracker?.addMovement(event)

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                touchDownY = event.rawY
                dragStartTranslationY = translationY
                isUserDragging = true
                val localY = event.y
                val grabberAreaH = dpToPx(config.grabberHitAreaHeightDp.toFloat())
                if (localY < grabberAreaH) {
                    callback?.onGrabberDragBegan()
                    animateGrabberPill(active = true)
                } else {
                    callback?.onContentDragBegan()
                }
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val dy = event.rawY - touchDownY
                var newTranslationY = (dragStartTranslationY + dy)
                newTranslationY = newTranslationY.coerceIn(-keyboardOffsetY.toFloat(), maxSheetHeight.toFloat())
                translationY = newTranslationY
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isUserDragging = false
                val localY = event.y
                val grabberAreaH = dpToPx(config.grabberHitAreaHeightDp.toFloat())

                velocityTracker?.let { vt ->
                    vt.computeCurrentVelocity(1000)
                    val velocityY = vt.yVelocity

                    if (localY < grabberAreaH) {
                        callback?.onGrabberDragEnded()
                        animateGrabberPill(active = false)
                    } else {
                        callback?.onContentDragEnded()
                    }

                    resolveAndSnap(velocityY)
                }
                velocityTracker?.recycle()
                velocityTracker = null
                trackedScrollView = null
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        containerHeight = context.resources.displayMetrics.heightPixels
        maxSheetHeight = (containerHeight * config.maxHeightFraction).toInt()
        translationY = translationY.coerceIn(-keyboardOffsetY.toFloat(), maxSheetHeight.toFloat())
        rebuildSnapPoints()
        if (!isUserDragging && _isVisible) {
            snapToCurrentDetent(animate = false)
        }
    }

    private fun rebuildSnapPoints() {
        if (containerHeight == 0) return
        val maxH = (containerHeight * config.maxHeightFraction).toInt()
        val sorted = detents.sortedBy { it.height }
        snapHeights.clear()
        snapYPositions.clear()
        for (d in sorted) {
            val h = minOf(d.height, maxH)
            snapHeights.add(h.toFloat())
            snapYPositions.add(maxSheetHeight - h.toFloat())
        }
    }

    private fun detentIndexForHeight(height: Int): Int {
        if (detents.isEmpty()) return 0
        var best = 0
        var bestDist = abs(detents[0].height - height)
        for (i in 1 until detents.size) {
            val d = abs(detents[i].height - height)
            if (d < bestDist) { bestDist = d; best = i }
        }
        return best
    }

    private fun snapIndexClosest(toTranslationY: Float): Int {
        if (snapYPositions.isEmpty()) return 0
        var best = 0
        var bestDist = abs(snapYPositions[0] - toTranslationY)
        for (i in 1 until snapYPositions.size) {
            val d = abs(snapYPositions[i] - toTranslationY)
            if (d < bestDist) { bestDist = d; best = i }
        }
        return best
    }

    private fun resolveAndSnap(velocityY: Float) {
        if (snapYPositions.isEmpty()) {
            hide(animate = true)
            return
        }

        val currentTranslationY = translationY
        val dismissThresholdPercent = 0.85f
        val currentProgress = if (maxSheetHeight > 0) (currentTranslationY + keyboardOffsetY) / maxSheetHeight else 0f

        if (currentProgress >= dismissThresholdPercent) {
            parentDialog?.dismissImmediately()
            return
        }

        val currentSnapIndex = snapIndexClosest(toTranslationY = currentTranslationY + keyboardOffsetY)
        val isDraggingDown = velocityY > 0
        val velocityInPercent = if (maxSheetHeight > 0) abs(velocityY) / maxSheetHeight else 0f
        val isFastSwipe = velocityInPercent > 1.5f

        if (isFastSwipe) {
            if (isDraggingDown) {
                val nextIndex = currentSnapIndex - 1
                if (nextIndex < 0) {
                    parentDialog?.dismissImmediately()
                    return
                }
                snapToIndex(nextIndex, velocityY)
            } else {
                val prevIndex = minOf(currentSnapIndex + 1, snapYPositions.lastIndex)
                snapToIndex(prevIndex, velocityY)
            }
            return
        }

        snapToIndex(currentSnapIndex, velocityY)
    }

    private fun snapToIndex(index: Int, velocity: Float = 0f) {
        if (snapYPositions.isEmpty()) return
        val idx = index.coerceIn(0, snapYPositions.lastIndex)
        val targetTranslationY = snapYPositions[idx] - keyboardOffsetY
        val targetHeight = snapHeights[idx]
        selectedDetentIndex = detentIndexForHeight(targetHeight.toInt())

        val spring = SpringAnimation(this, SpringAnimation.TRANSLATION_Y, targetTranslationY)
        spring.spring.stiffness = SpringForce.STIFFNESS_MEDIUM
        spring.spring.dampingRatio = 0.85f
        if (abs(velocity) > 0) {
            spring.setStartVelocity(velocity)
        }
        spring.addUpdateListener { _, _, _ ->
            requestLayout()
        }
        spring.addEndListener { _, _, _, _ ->
            finalizeSnap(targetHeight.toInt())
        }
        spring.start()
    }

    private fun snapToCurrentDetent(animate: Boolean) {
        if (snapHeights.isEmpty()) {
            translationY = maxSheetHeight.toFloat()
            return
        }
        selectedDetentIndex = minOf(selectedDetentIndex, maxOf(snapHeights.size - 1, 0))
        val targetHeight = snapHeights[selectedDetentIndex]
        val targetTranslationY = maxSheetHeight - targetHeight - keyboardOffsetY

        if (animate) {
            val anim = ValueAnimator.ofFloat(translationY, targetTranslationY)
            anim.duration = 400
            anim.interpolator = DecelerateInterpolator()
            anim.addUpdateListener {
                translationY = it.animatedValue as Float
                requestLayout()
            }
            anim.addListener(object : android.animation.Animator.AnimatorListener {
                override fun onAnimationStart(a: android.animation.Animator) {}
                override fun onAnimationEnd(a: android.animation.Animator) {
                    finalizeSnap(targetHeight.toInt())
                }
                override fun onAnimationCancel(a: android.animation.Animator) {}
                override fun onAnimationRepeat(a: android.animation.Animator) {}
            })
            anim.start()
        } else {
            translationY = targetTranslationY
            finalizeSnap(targetHeight.toInt())
        }
    }

    private fun animateToTranslationY(target: Float, onEnd: (() -> Unit)? = null) {
        val anim = ValueAnimator.ofFloat(translationY, target)
        anim.duration = 300
        anim.interpolator = DecelerateInterpolator()
        anim.addUpdateListener {
            translationY = it.animatedValue as Float
            requestLayout()
        }
        anim.addListener(object : android.animation.Animator.AnimatorListener {
            override fun onAnimationStart(a: android.animation.Animator) {}
            override fun onAnimationEnd(a: android.animation.Animator) {
                onEnd?.invoke()
            }
            override fun onAnimationCancel(a: android.animation.Animator) {}
            override fun onAnimationRepeat(a: android.animation.Animator) {}
        })
        anim.start()
    }

    private fun finalizeSnap(height: Int) {
        isUserDragging = false
        val idx = minOf(selectedDetentIndex, maxOf(detents.size - 1, 0))
        val h = if (detents.isEmpty()) (containerHeight * config.maxHeightFraction).toInt() else detents[idx].height
        callback?.onDetentChanged(idx, h)
        requestLayout()
    }

    private fun animateGrabberPill(active: Boolean) {
        grabberPill?.let { pill ->
            pill.animate()
                .scaleX(if (active) 1.3f else 1.0f)
                .alpha(if (active) 0.6f else 1.0f)
                .setDuration(200)
                .setInterpolator(DecelerateInterpolator())
                .start()
        }
    }

    private fun findScrollViewAt(x: Float, y: Float): View? {
        return findScrollViewRecursive(contentLayout, x, y)
    }

    private fun findScrollViewRecursive(parent: View, x: Float, y: Float): View? {
        val loc = IntArray(2)
        parent.getLocationOnScreen(loc)
        val px = loc[0]
        val py = loc[1]
        if (x < px || x > px + parent.width || y < py || y > py + parent.height) return null
        if (parent is ViewGroup) {
            for (i in parent.childCount - 1 downTo 0) {
                val child = parent.getChildAt(i)
                val found = findScrollViewRecursive(child, x, y)
                if (found != null) return found
            }
        }
        if (isScrollableContainer(parent)) return parent
        return null
    }

    private fun isScrollableContainer(view: View): Boolean {
        val name = view.javaClass.name
        return name.contains("ScrollView") || name.contains("RecyclerView") ||
                name.contains("ListView") || name.contains("GridView") ||
                view.canScrollVertically(-1)
    }

    fun setKeyboardOffset(offset: Int) {
        val newOffset = offset.coerceAtMost(maxSheetHeight)
        val keyboardDelta = newOffset - keyboardOffsetY
        keyboardOffsetY = newOffset
        translationY = (translationY - keyboardDelta).coerceIn(-keyboardOffsetY.toFloat(), maxSheetHeight.toFloat())
        requestLayout()
    }

    private fun dpToPx(dp: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics
        )
    }

    private fun pxToDp(px: Float): Float {
        return px / resources.displayMetrics.density
    }
}

