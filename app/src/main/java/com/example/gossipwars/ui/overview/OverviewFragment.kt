package com.example.gossipwars.ui.overview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gossipwars.InGameActivity
import com.example.gossipwars.R
import com.example.gossipwars.logic.entities.GameHelper
import com.example.gossipwars.logic.entities.NewsfeedInfo
import com.example.gossipwars.logic.entities.Notifications
import com.example.gossipwars.logic.entities.Snapshots
import com.richpath.RichPath
import com.richpath.RichPathView
import com.richpathanimator.RichPathAnimator

class OverviewFragment : Fragment() {

    private lateinit var overviewViewModel: OverviewViewModel
    private var mRecyclerView: RecyclerView? = null
    private var mAdapter: NewsFeedListAdapter? = null
    private var newsFeedList: ArrayList<NewsfeedInfo> = ArrayList()
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

        mRecyclerView = root.findViewById(R.id.overviewNewsFeedRecyclerView)
        mAdapter = NewsFeedListAdapter(this, newsFeedList)
        mRecyclerView?.adapter = mAdapter
        mRecyclerView?.layoutManager = LinearLayoutManager(this.activity)

        Notifications.roundOngoing.observe(viewLifecycleOwner, Observer {
            if (it) {
                newsFeedList.clear()
                Snapshots.generateNewsFeed().forEach { newsfeedInfo: NewsfeedInfo ->
                    newsFeedList.add(newsfeedInfo)
                }
                mRecyclerView?.adapter?.notifyDataSetChanged()
            }
        })


        return root
    }

    private fun subscribeToTimer() {
        Notifications.roundTimer.observe(viewLifecycleOwner, Observer {
            if (it > 5) {
                (context as InGameActivity).supportActionBar?.title =
                    getString(
                        R.string.bar_title,
                        fragmentBarTitle,
                        GameHelper.roundTimeToString(it)
                    )
            } else {
                (context as InGameActivity).supportActionBar?.title =
                    HtmlCompat.fromHtml(
                        getString(
                            R.string.bar_title_alert, fragmentBarTitle,
                            GameHelper.roundTimeToString(it)
                        ), HtmlCompat.FROM_HTML_MODE_LEGACY
                    )
            }
        })
    }


}