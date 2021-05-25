package com.sam.coachmark.coachMarks

import android.view.View
import androidx.annotation.IdRes

data class CoachMark(
        val message : String?,
        @IdRes val viewId : Int? = null,
        var view: View? = null
) {
        lateinit var target : View
}