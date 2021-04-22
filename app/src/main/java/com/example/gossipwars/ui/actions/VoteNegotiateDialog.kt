package com.example.gossipwars.ui.actions

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gossipwars.R
import com.example.gossipwars.logic.entities.NegotiateVoteContent
import com.example.gossipwars.logic.proposals.ArmyRequest

class VoteNegotiateDialog(val title: String, private val requests: ArrayList<NegotiateVoteContent>): DialogFragment() {
    internal lateinit var listener: NegotiateDialogListener
    private var mRecyclerView: RecyclerView? = null
    private var mAdapter: NegotiateListAdapter? = null
    var requestsVotes: MutableMap<ArmyRequest, Boolean> = mutableMapOf()

    interface NegotiateDialogListener {
        fun onDialogPositiveClick(dialog: VoteNegotiateResult)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            listener = context as NegotiateDialogListener
        } catch (e: ClassCastException) {
            throw ClassCastException(context.toString() + " must implement negotiateListener")
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        if (activity == null)
            throw IllegalStateException("activity can not be null")
        val inflater = requireActivity().layoutInflater
        val builder = AlertDialog.Builder(activity)
        val negotiateProposalView: View = inflater.inflate(R.layout.vote_proposal_form, null)

        requests.forEach { content: NegotiateVoteContent -> requestsVotes[content.armyRequest] = false }
        mRecyclerView = negotiateProposalView.findViewById(R.id.proposalsRecyclerView)
        mAdapter = NegotiateListAdapter(this, requests)
        mRecyclerView?.adapter = mAdapter
        mRecyclerView?.layoutManager = LinearLayoutManager(context)

        builder.setView(negotiateProposalView)
        builder.setTitle(title)
            .setPositiveButton("Done") { _, _ ->
                listener.onDialogPositiveClick(createResponse())
            }
            .setNegativeButton("Cancel") { _, _ -> }
        return builder.create()
    }

    fun sendVote(request: ArmyRequest, vote: Boolean) {
        requestsVotes[request] = vote
    }

    private fun createResponse(): VoteNegotiateResult {
        val responses = VoteNegotiateResult()
        requestsVotes.forEach { entry ->
            if (entry.value) {
                responses.yesList.add(ArmyRequest(entry.key.initiator, entry.key.approver,
                                        entry.key.armyOption, entry.key.increase, entry.key.id))
            } else {
                responses.noList.add(ArmyRequest(entry.key.initiator, entry.key.approver,
                    entry.key.armyOption, entry.key.increase, entry.key.id))
            }
        }
        return responses
    }

}