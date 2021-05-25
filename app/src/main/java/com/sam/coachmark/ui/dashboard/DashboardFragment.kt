package com.sam.coachmark.ui.dashboard

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import com.sam.coachmark.R
import com.sam.coachmark.coachMarks.CoachMark
import com.sam.coachmark.coachMarks.CoachMarkListener
import com.sam.coachmark.coachMarks.CoachMarkView

class DashboardFragment : Fragment(R.layout.fragment_dashboard), CoachMarkListener {

    override fun onViewCreated(view : View, savedInstanceState : Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val ok = view.findViewById<Button>(R.id.ok)
        val done = view.findViewById<Button>(R.id.done)
        val c21 = CoachMark("text1 header", R.id.text1)
        val c22 = CoachMark("ok header", view = ok)
        val c23 = CoachMark("text2 header", R.id.text2)
        val c24 = CoachMark("done header", view = done)
        val c25 = CoachMark("custom header", R.id.navigation_dashboard)
        val c26 = CoachMark("about header", R.id.help)

        val coachMarkView = CoachMarkView.Builder(requireContext())
            .setCoachMarks(c21, c22, c23, c24, c25, c26)
            .setCoachMarkListener(this)
            .build()

        done.setOnClickListener {
            coachMarkView.show()
        }

        ok.setOnClickListener {
            coachMarkView.restart()
        }
    }

    override fun onCoachMarkShow() {
    }

    override fun onCoachMarkDismiss() {
    }
}