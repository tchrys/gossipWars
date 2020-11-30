package com.example.gossipwars.ui.actions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gossipwars.InGameActivity
import com.example.gossipwars.R
import com.example.gossipwars.logic.entities.Game
import com.example.gossipwars.logic.entities.Proposal


class ActionsFragment : Fragment() {

    private lateinit var actionsViewModel: ActionsViewModel

    private var mRecyclerView: RecyclerView? = null
    private var mAdapter: ProposalListAdapter? = null
    private var username : String = "dsf"
    private var props : ArrayList<Proposal> = ArrayList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        actionsViewModel = ViewModelProviders.of(this).get(ActionsViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_actions, container, false)
        val textView: TextView = root.findViewById(R.id.text_actions)
        actionsViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })
        actionsViewModel.proposals.observe(viewLifecycleOwner, Observer {
            props.clear()
            it.forEach { proposal: Proposal -> props.add(proposal) }
        })


        mRecyclerView = root.findViewById(R.id.actions_recyclerview)
        mAdapter = ProposalListAdapter(this, props, username)
        mRecyclerView?.setAdapter(mAdapter)
        mRecyclerView?.setLayoutManager(LinearLayoutManager(this.activity))


        val button : Button = root.findViewById(R.id.exp_button)
        button.setOnClickListener { view ->
            actionsViewModel.addProposal()
            mRecyclerView?.adapter?.notifyDataSetChanged()
        }

        return root
    }

    fun sendVote(proposal: Proposal, boolean: Boolean) {

    }
}