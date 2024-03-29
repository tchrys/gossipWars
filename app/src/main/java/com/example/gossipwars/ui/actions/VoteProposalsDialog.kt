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
import com.example.gossipwars.logic.entities.ProposalVoteContent
import com.example.gossipwars.logic.proposals.Proposal

class VoteProposalsDialog(val title: String, private val props: ArrayList<ProposalVoteContent>) :
    DialogFragment() {
    internal lateinit var listener: VoteDialogListener
    private var mRecyclerView: RecyclerView? = null
    private var mAdapter: ProposalListAdapter? = null
    private var proposalsVotes: MutableMap<Proposal, Boolean> = mutableMapOf()

    interface VoteDialogListener {
        fun onDialogPositiveClick(dialog: VoteProposalsResult)
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
        val inflater = requireActivity().layoutInflater
        val builder = AlertDialog.Builder(activity)
        val voteProposalView: View = inflater.inflate(R.layout.vote_proposal_form, null)

        props.forEach { content: ProposalVoteContent -> proposalsVotes[content.proposal] = false }
        mRecyclerView = voteProposalView.findViewById(R.id.proposalsRecyclerView)
        mAdapter = ProposalListAdapter(this, props)
        mRecyclerView?.adapter = mAdapter
        mRecyclerView?.layoutManager = LinearLayoutManager(context)

        builder.setView(voteProposalView)
        builder.setTitle(title)
        if (title == "Your requests") {
            builder.setNegativeButton("Close") { _, _ -> }
        } else {
            builder.setPositiveButton("Done") { _, _ ->
                listener.onDialogPositiveClick(createResponse())
            }
                .setNegativeButton("Cancel") { _, _ -> }
        }
        return builder.create()
    }

    fun sendVote(proposal: Proposal, vote: Boolean) {
        proposalsVotes[proposal] = vote
    }

    private fun createResponse(): VoteProposalsResult {
        val responses = VoteProposalsResult()
        proposalsVotes.forEach { entry ->
            val prop = entry.key
            responses.responseList.add(
                ProposalResponse(
                    prop.alliance.id, prop.proposalId,
                    entry.value, Game.myId
                )
            )
        }
        return responses
    }

}