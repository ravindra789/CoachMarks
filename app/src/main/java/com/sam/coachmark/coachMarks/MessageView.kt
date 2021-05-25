package com.sam.coachmark.coachMarks

import android.app.Activity
import android.content.Context
import android.graphics.*
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.TextView
import com.sam.coachmark.R


class MessageView internal constructor(context: Context, coachMark: CoachMark, private val target: View?) : FrameLayout(context) {

    companion object {
        private const val RADIUS_SIZE_TARGET_RECT = 15
        private const val MARGIN_INDICATOR = 15
    }

    private val selfPaint = Paint()
    private val targetPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val X_FER_MODE_CLEAR = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    private var targetRect: RectF? = null
    private val selfRect = Rect()

    private var isTop: Boolean = false

    private var density: Float = 0f
    private var marginGuide: Float = 0f
    private var coachMarkActionsHeight: Float = 0f

    private var mMessageView: View? = null

    private var layoutListener: ViewTreeObserver.OnGlobalLayoutListener = object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN)
                viewTreeObserver.removeOnGlobalLayoutListener(this)
            else
                viewTreeObserver.removeGlobalOnLayoutListener(this)

            setMessageLocation(resolveMessageViewLocation())
            val locationTarget = IntArray(2)
            target!!.getLocationOnScreen(locationTarget)

            targetRect = RectF(locationTarget[0].toFloat(),
                    locationTarget[1].toFloat(),
                    (locationTarget[0] + target.width).toFloat(),
                    (locationTarget[1] + target.height).toFloat())

            selfRect.set(paddingLeft,
                    paddingTop,
                    width - paddingRight,
                    height - paddingBottom - CoachMarkView.getNavBarHeight(getContext()))

            setMessageLocation(resolveMessageViewLocation())
        }
    }

    init {
        init(context)
        val locationTarget = IntArray(2)
        target!!.getLocationOnScreen(locationTarget)
        targetRect = RectF(locationTarget[0].toFloat(),
                locationTarget[1].toFloat(),
                (locationTarget[0] + target.width).toFloat(),
                (locationTarget[1] + target.height).toFloat())
        addMessageView(context, coachMark)
        setMessageLocation(resolveMessageViewLocation())
        viewTreeObserver.addOnGlobalLayoutListener(layoutListener)
    }

    private fun init(context: Context) {
        val rectangle = Rect()
        val window = (context as Activity).window
        window.decorView.getWindowVisibleDisplayFrame(rectangle)
        setWillNotDraw(false)
        setLayerType(View.LAYER_TYPE_HARDWARE, null)
        density = context.getResources().displayMetrics.density
        marginGuide = MARGIN_INDICATOR * density
        coachMarkActionsHeight = CoachMarkView.COACH_MARK_ACTION_HEIGHT * density
    }

    private fun addMessageView(context:Context, coachMark: CoachMark) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val messageView = inflater.inflate(R.layout.coach_marks_message, this, false)
        messageView.findViewById<TextView>(R.id.textView).text = coachMark.message
        mMessageView = messageView
        addView(mMessageView, LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        animateMessageView(messageView)
    }

    private fun animateMessageView(view: View) {
        val anim = AnimationUtils.loadAnimation(context, R.anim.slide_in_bottom_top)
        view.startAnimation(anim)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (target != null) {
            selfPaint.color = CoachMarkView.BACKGROUND_COLOR
            selfPaint.style = Paint.Style.FILL
            selfPaint.isAntiAlias = true
            canvas.drawRect(selfRect, selfPaint)

            targetPaint.xfermode = X_FER_MODE_CLEAR
            targetPaint.isAntiAlias = true

            canvas.drawRoundRect(targetRect!!, RADIUS_SIZE_TARGET_RECT.toFloat(), RADIUS_SIZE_TARGET_RECT.toFloat(), targetPaint)
        }
    }

    private fun setMessageLocation(p: Point) {
        mMessageView!!.x = p.x.toFloat()
        mMessageView!!.y = p.y.toFloat()
        postInvalidate()
    }

    private fun resolveMessageViewLocation(): Point {
        var xMessageView = 0

        var yMessageView: Int
        if (targetRect!!.top + marginGuide + coachMarkActionsHeight > height / 2.0f) {
            isTop = false
            yMessageView = (targetRect!!.top - mMessageView!!.height.toFloat()).toInt()
        } else {
            isTop = true
            yMessageView = (targetRect!!.top + target!!.height.toFloat()).toInt()
        }

        if (yMessageView < 0)
            yMessageView = 0

        return Point(xMessageView, yMessageView)
    }

}