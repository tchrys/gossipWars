package com.example.gossipwars.ui.actions

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gossipwars.R
import com.example.gossipwars.logic.entities.Game.myId
import com.example.gossipwars.logic.entities.GameHelper.camelCaseToSpaced
import com.example.gossipwars.logic.entities.GameHelper.findRegionById
import com.example.gossipwars.logic.proposals.Proposal
import com.example.gossipwars.logic.proposals.ProposalEnum
import com.example.gossipwars.logic.proposals.StrategyProposal
import com.example.gossipwars.ui.actions.ProposalListAdapter.ProposalViewHolder
import com.google.android.material.switchmaterial.SwitchMaterial
import java.util.*

class ProposalListAdapter(
    private val context: VoteProposalsDialog,
    private val proposalsList: ArrayList<Proposal>
) : RecyclerView.Adapter<ProposalViewHolder>() {
    private val mInflater: LayoutInflater = LayoutInflater.from(context.activity)

    inner class ProposalViewHolder(
        itemView: View,
        adapter: ProposalListAdapter
    ) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val propStatement: TextView = itemView.findViewById(R.id.proposalStatement)
        val propVote: SwitchMaterial = itemView.findViewById(R.id.proposalSwitch)
        val mAdapter: ProposalListAdapter = adapter
        override fun onClick(view: View) {
            val mPosition = layoutPosition
            val element = proposalsList[mPosition]
            mAdapter.notifyDataSetChanged()
        }

        init {
            itemView.setOnClickListener(this)
            propVote.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
                context.sendVote(
                    proposalsList[layoutPosition],
                    isChecked
                )
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ProposalViewHolder {
        // Inflate an item view.
        val mItemView = mInflater.inflate(
            R.layout.proposal_items, parent, false
        )
        return ProposalViewHolder(mItemView, this)
    }

    override fun onBindViewHolder(holder: ProposalViewHolder, position: Int) {
        val mCurrent = proposalsList[position]
        var regionName = ""
        if (listOf(ProposalEnum.DEFEND, ProposalEnum.ATTACK)
                .contains(mCurrent.proposalEnum)
        ) {
            regionName = findRegionById(
                (mCurrent as StrategyProposal)
                    .targetRegion
            )!!.name
            regionName = "in region" + camelCaseToSpaced(regionName)
        }
        holder.propStatement.text = String.format(
            "%s requests member's vote from alliance %s for %s %s %s",
            mCurrent.initiator.username,
            mCurrent.alliance.name,
            mCurrent.proposalEnum.toString().toLowerCase(Locale.ROOT) + "ing",
            mCurrent.target.username,
            regionName
        )
        if (mCurrent.initiator.id == myId) {
            holder.propVote.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int {
        return proposalsList.size
    }

}