package com.sam.coachmark.coachMarks

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.os.Build
import android.os.Handler
import android.util.DisplayMetrics
import android.view.*
import android.view.animation.AlphaAnimation
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.sam.coachmark.R
import java.util.*
import kotlin.math.abs

class CoachMarkView private constructor(
    context : Context,
    private val coachMarks : List<CoachMark>,
    private val coachMarkListener : CoachMarkListener?
) : FrameLayout(context) {

    private var coachMarkActionsHeight: Float = 0f
    private val selfPaint = Paint()
    private val targetPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val X_FER_MODE_CLEAR = PorterDuffXfermode(PorterDuff.Mode.CLEAR)

    private var currentCoachMarkPosition : Int = -1

    private val selfRect = Rect()
    private var previous : Button
    private var next : Button
    private var skip : Button

    init {
        val rectangle = Rect()
        val window = (context as Activity).window
        window.decorView.getWindowVisibleDisplayFrame(rectangle)
        coachMarkActionsHeight = context.getResources().displayMetrics.density * COACH_MARK_ACTION_HEIGHT
        setWillNotDraw(false)
        setLayerType(View.LAYER_TYPE_HARDWARE, null)

        addActions(context)

        previous = findViewById(R.id.previous)
        previous.setOnClickListener { goPrev() }
        next = findViewById(R.id.next)
        next.setOnClickListener { goNext() }
        skip = findViewById(R.id.skip)
        skip.setOnClickListener { dismiss() }
        Handler().postDelayed({ this.goNext() }, 100)
    }

    private fun addActions(context : Context) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val child = inflater.inflate(R.layout.coach_marks_action, this, false)
        (child as LinearLayout).gravity = Gravity.BOTTOM
        child.setTag(ACTIONS_VIEW_TAG)
        addView(child)
        (child.getLayoutParams() as LayoutParams).bottomMargin = getNavBarHeight(context)
    }

    private fun goPrev() {
        if (currentCoachMarkPosition > 0) {
            currentCoachMarkPosition--
            addContent()
        }
    }

    private fun goNext() {
        if (currentCoachMarkPosition < coachMarks.size - 1) {
            currentCoachMarkPosition++
            addContent()
        } else {
            dismiss()
        }
    }

    private fun dismiss() {
        coachMarkListener?.onCoachMarkDismiss()
        ((context as Activity).window.decorView as ViewGroup).removeView(this@CoachMarkView)
    }

    private fun addContent() {
        val coachMark = coachMarks[currentCoachMarkPosition]
        removeView(findViewWithTag(COACH_MARK_TAG))
        val coachMarkMsgView = MessageView(context, coachMark, coachMark.target)
        coachMarkMsgView.tag = COACH_MARK_TAG
        addView(coachMarkMsgView, 0)
        setCoachMarkActionGravity(coachMark.target)
        setCoachMarkActionButtons()
    }

    private fun setCoachMarkActionGravity(view : View) {
        val locationTarget = IntArray(2)
        view.getLocationOnScreen(locationTarget)
        val child = findViewWithTag<View>(ACTIONS_VIEW_TAG)
        val params = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        if (abs(locationTarget[1] + view.height + getNavBarHeight(context) - height) < coachMarkActionsHeight) {
            val rectangle = Rect()
            val window = (context as Activity).window
            window.decorView.getWindowVisibleDisplayFrame(rectangle)
            params.topMargin = rectangle.top
            params.gravity = Gravity.TOP
        } else {
            params.topMargin = 0
            params.gravity = Gravity.BOTTOM
            params.bottomMargin = getNavBarHeight(context)
        }
        child.layoutParams = params
    }

    private fun setCoachMarkActionButtons() {
        previous.isEnabled = currentCoachMarkPosition >= 1
        if (currentCoachMarkPosition == coachMarks.size - 1) {
            next.text = context.getString(R.string.done)
        } else {
            next.text = context.getString(R.string.next)
        }
    }

    override fun onDraw(canvas : Canvas) {
        super.onDraw(canvas)
        selfPaint.color = BACKGROUND_COLOR
        selfPaint.style = Paint.Style.FILL
        selfPaint.isAntiAlias = true
        canvas.drawRect(selfRect, selfPaint)

        targetPaint.xfermode = X_FER_MODE_CLEAR
        targetPaint.isAntiAlias = true
    }

    fun show() {
        if (coachMarks.isEmpty()) {
            return
        }
        this.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        this.isClickable = true

        ((context as Activity).window.decorView as ViewGroup).addView(this)
        val startAnimation = AlphaAnimation(0.0f, 1.0f)
        startAnimation.duration = APPEARING_ANIMATION_DURATION.toLong()
        startAnimation.fillAfter = true
        this.startAnimation(startAnimation)

        coachMarkListener?.onCoachMarkShow()
    }

    fun restart() {
        currentCoachMarkPosition = -1
        goNext()
        show()
    }

    class Builder(private val context : Context) {
        private var coachMarks: List<CoachMark> = ArrayList()
        private var coachMarkListener : CoachMarkListener? = null

        fun setCoachMarks(vararg coachMarks: CoachMark): Builder {
            this.coachMarks = removeUnavailableViews(coachMarks.toList())
            return this
        }

        fun setCoachMarks(coachMarks: List<CoachMark>): Builder {
            this.coachMarks = removeUnavailableViews(coachMarks)
            return this
        }

        private fun setAvailableTarget(coachMark: CoachMark) : Boolean {
            if (coachMark.view != null) {
                coachMark.target = coachMark.view!!
                return true
            }
            if (coachMark.viewId == null) return false
//            val id = context.resources.getIdentifier(coachMark.viewId, "id", context.packageName)
            coachMark.target = (context as Activity).findViewById(coachMark.viewId) ?: return false
            return true
        }

        private fun removeUnavailableViews(coachMarks : List<CoachMark>) : List<CoachMark> {
            return coachMarks.filter(::setAvailableTarget)
        }

        fun setCoachMarkListener(coachMarkListener : CoachMarkListener) : Builder {
            this.coachMarkListener = coachMarkListener
            return this
        }

        fun build() : CoachMarkView {
            return CoachMarkView(context, coachMarks, coachMarkListener)
        }
    }

    companion object {

        const val BACKGROUND_COLOR = -0x27000000
        private const val APPEARING_ANIMATION_DURATION = 400
        const val COACH_MARK_ACTION_HEIGHT = 80
        private const val COACH_MARK_TAG = "CoachMark"
        private const val ACTIONS_VIEW_TAG = "ActionsView"

        fun getNavBarHeight(c : Context) : Int {
            val result = 0
            if (hasSoftKeys(c)) {
                val resources = c.resources
                val orientation = resources.configuration.orientation
                val resourceId : Int = if (isTablet(c)) {
                    resources.getIdentifier(
                        if (orientation == Configuration.ORIENTATION_PORTRAIT) "navigation_bar_height" else "navigation_bar_height_landscape",
                        "dimen",
                        "android"
                    )
                } else {
                    resources.getIdentifier(
                        if (orientation == Configuration.ORIENTATION_PORTRAIT) "navigation_bar_height" else "navigation_bar_width",
                        "dimen",
                        "android"
                    )
                }
                if (resourceId > 0) {
                    return resources.getDimensionPixelSize(resourceId)
                }
            }
            return result
        }

        private fun hasSoftKeys(c : Context) : Boolean {
            val hasSoftwareKeys : Boolean

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                val d = (c as Activity).windowManager.defaultDisplay

                val realDisplayMetrics = DisplayMetrics()
                d.getRealMetrics(realDisplayMetrics)

                val realHeight = realDisplayMetrics.heightPixels
                val realWidth = realDisplayMetrics.widthPixels

                val displayMetrics = DisplayMetrics()
                d.getMetrics(displayMetrics)

                val displayHeight = displayMetrics.heightPixels
                val displayWidth = displayMetrics.widthPixels

                hasSoftwareKeys = realWidth - displayWidth > 0 || realHeight - displayHeight > 0
            } else {
                val hasMenuKey = ViewConfiguration.get(c).hasPermanentMenuKey()
                val hasBackKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK)
                hasSoftwareKeys = !hasMenuKey && !hasBackKey
            }
            return hasSoftwareKeys
        }

        private fun isTablet(c : Context) : Boolean {
            return c.resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK >= Configuration.SCREENLAYOUT_SIZE_LARGE
        }
    }

}