package com.example.gossipwars.ui.actions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gossipwars.InGameActivity
import com.example.gossipwars.R
import com.example.gossipwars.logic.entities.ChatMessage
import com.example.gossipwars.logic.entities.Game
import com.example.gossipwars.logic.proposals.Proposal
import com.google.android.material.chip.Chip


class ActionsFragment : Fragment() {

    private lateinit var actionsViewModel: ActionsViewModel

    private var mRecyclerView: RecyclerView? = null
    private var mAdapter: ProposalListAdapter? = null
    private var username : String = "dsf"
//    private var props : ArrayList<Proposal> = ArrayList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        actionsViewModel = ViewModelProviders.of(this).get(ActionsViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_actions, container, false)


//        actionsViewModel.proposals.observe(viewLifecycleOwner, Observer {
//            props.clear()
//            it.forEach { proposal: Proposal -> props.add(proposal) }
//            mRecyclerView?.adapter?.notifyDataSetChanged()
//        })


//        mRecyclerView = root.findViewById(R.id.actions_recyclerview)
//        mAdapter = ProposalListAdapter(this, props, username)
//        mRecyclerView?.adapter = mAdapter
//        mRecyclerView?.layoutManager = LinearLayoutManager(this.activity)


//        val button : Button = root.findViewById(R.id.exp_button)
//        button.setOnClickListener {
//            actionsViewModel.addProposal()
//        }

        val kickChip: Chip = root.findViewById(R.id.kickChip)
        kickChip.setOnClickListener {
            if (noAllianceForMe()) {
                showSnackbarForError("You must belong to an alliance to do this action")
            } else {
                fragmentManager?.let { KickDialogFragment().show(it, "kickDialogTag") }
            }
        }

        val joinChip: Chip = root.findViewById(R.id.joinChip)
        joinChip.setOnClickListener {
            if (noAllianceForMe()) {
                showSnackbarForError("You must belong to an alliance to do this action")
            } else {
                fragmentManager?.let { JoinDialogFragment().show(it, "joinDialogTag") }
            }
        }

        val negotiateChip: Chip = root.findViewById(R.id.negotiateChip)
        negotiateChip.setOnClickListener {
            fragmentManager?.let { NegotiateDialogFragment().show(it, "negotiateDialogTag") }
        }

        val bonusChip: Chip = root.findViewById(R.id.roundBonus)
        bonusChip.setOnClickListener {
            if (Game.myBonusTaken.value!!) {
                showSnackbarForError("You've already taken the bonus for this round")
            } else {
                fragmentManager?.let { BonusDialogFragment().show(it, "bonusDialogTag") }
            }
        }

        val attackChip: Chip = root.findViewById(R.id.attackChip)
        attackChip.setOnClickListener {
            if (noAllianceForMe()) {
                showSnackbarForError("You must belong to an alliance to do this action")
            } else {
                fragmentManager?.let { AttackDialogFragment().show(it, "attackDialogTag") }
            }
        }

        val defendChip: Chip = root.findViewById(R.id.defendChip)
        defendChip.setOnClickListener {
            if (noAllianceForMe()) {
                showSnackbarForError("You must belong to an alliance to do this action")
            } else {
                fragmentManager?.let { DefendDialogFragment().show(it, "defendDialogTag") }
            }
        }

        Game.myBonusTaken.observe(viewLifecycleOwner, Observer {
            if (it) {
                context?.let { it1 -> ContextCompat.getColor(it1, R.color.disabledChip) }?.let { it2 ->
                    bonusChip.setTextColor(
                        it2
                    )
                }
            }
        })

        if (noAllianceForMe()) {
            context?.let { ContextCompat.getColor(it, R.color.disabledChip) }?.let {
                kickChip.setTextColor(it)
                joinChip.setTextColor(it)
                attackChip.setTextColor(it)
                defendChip.setTextColor(it)
            }
        }

        return root
    }

    fun sendVote(proposal: Proposal, boolean: Boolean) {

    }

    fun noAllianceForMe(): Boolean = Game.findAlliancesForPlayer(Game.myId)?.size == 0

    fun showSnackbarForError(message: String) {
        (activity as InGameActivity).showSnackBarOnError(R.id.fragment_actions_layout, message)
    }
}