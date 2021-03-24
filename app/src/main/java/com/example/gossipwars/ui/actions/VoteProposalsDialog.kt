package com.example.gossipwars.ui.actions

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gossipwars.R
import com.example.gossipwars.communication.messages.allianceCommunication.ProposalResponse
import com.example.gossipwars.logic.entities.Game
import com.example.gossipwars.logic.proposals.Proposal

class VoteProposalsDialog(val title: String, val props: ArrayList<Proposal>,
                            val username: String): DialogFragment() {
    internal lateinit var listener: VoteDialogListener
    private var mRecyclerView: RecyclerView? = null
    private var mAdapter: ProposalListAdapter? = null
    var responses: VoteProposalsResult = VoteProposalsResult()

    interface VoteDialogListener {
        fun onDialogPositiveClick(dialog: VoteProposalsResult?)
        fun onDialogNegativeClick(dialog: VoteProposalsResult?)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            listener = context as VoteDialogListener
        } catch (e: ClassCastException) {
            throw ClassCastException(context.toString() + " must implement voteListener")
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        if (activity == null)
            throw IllegalStateException("activity can not be null")
        var inflater = requireActivity().layoutInflater
        var builder = AlertDialog.Builder(activity)
        val voteProposalView: View = inflater.inflate(R.layout.vote_proposal_form, null)

        mRecyclerView = voteProposalView.findViewById(R.id.proposalsRecyclerView)
        mAdapter = ProposalListAdapter(this, props, username)
        mRecyclerView?.adapter = mAdapter
        mRecyclerView?.layoutManager = LinearLayoutManager(context)

        builder.setView(voteProposalView)
        builder.setTitle(title)
            .setPositiveButton("Done") { _, _ ->
                listener.onDialogPositiveClick(responses)
            }
            .setNegativeButton("Cancel") { _, _ ->
                listener.onDialogNegativeClick(null)
            }
        return builder.create()
    }

    fun sendVote(proposal: Proposal, vote: Boolean) {
        props.remove(proposal)
        mRecyclerView?.adapter?.notifyDataSetChanged()
        responses.responseList.add(ProposalResponse(proposal.alliance.id, proposal.proposalId,
                                        vote, Game.myId))
    }


}