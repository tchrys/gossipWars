package com.example.gossipwars.ui.overview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.gossipwars.InGameActivity
import com.example.gossipwars.R
import com.example.gossipwars.logic.entities.GameHelper
import com.example.gossipwars.logic.entities.Notifications

class OverviewFragment : Fragment() {

    private lateinit var overviewViewModel: OverviewViewModel
    private lateinit var fragmentBarTitle: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        overviewViewModel =
            ViewModelProviders.of(this).get(OverviewViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_overview, container, false)
        fragmentBarTitle = (context as InGameActivity).supportActionBar?.title.toString()
        subscribeToTimer()

        return root
    }

    private fun subscribeToTimer() {
        Notifications.roundTimer.observe(viewLifecycleOwner, Observer {
            if (it > 5) {
                (context as InGameActivity).supportActionBar?.title =
                    getString(R.string.bar_title, fragmentBarTitle, GameHelper.roundTimeToString(it))
            } else {
                (context as InGameActivity).supportActionBar?.title =
                    HtmlCompat.fromHtml(getString(R.string.bar_title_alert, fragmentBarTitle,
                        GameHelper.roundTimeToString(it)), HtmlCompat.FROM_HTML_MODE_LEGACY)
            }
        })
    }


}