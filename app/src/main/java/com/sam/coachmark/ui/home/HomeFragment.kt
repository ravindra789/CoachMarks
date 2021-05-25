package com.sam.coachmark.ui.home

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import com.sam.coachmark.R
import com.sam.coachmark.coachMarks.CoachMark
import com.sam.coachmark.coachMarks.CoachMarkView

class HomeFragment : Fragment(R.layout.fragment_home) {

    override fun onViewCreated(view : View, savedInstanceState : Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val coachMark = CoachMark("This is a Simple CoachMark", view = view.findViewById(R.id.home))
        val coachMarkView = CoachMarkView.Builder(requireContext())
            .setCoachMarks(coachMark)
            .build()
        view.findViewById<Button>(R.id.start).setOnClickListener {
            coachMarkView.show()
        }
    }

}