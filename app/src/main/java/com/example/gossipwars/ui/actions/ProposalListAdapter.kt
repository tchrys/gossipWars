package com.example.gossipwars.ui.actions

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.CompoundButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gossipwars.R
import com.example.gossipwars.logic.entities.Game
import com.example.gossipwars.logic.entities.Game.myId
import com.example.gossipwars.logic.entities.GameHelper
import com.example.gossipwars.logic.entities.GameHelper.camelCaseToSpaced
import com.example.gossipwars.logic.entities.GameHelper.findRegionById
import com.example.gossipwars.logic.entities.ProposalVoteContent
import com.example.gossipwars.logic.entities.Snapshots
import com.example.gossipwars.logic.proposals.ProposalEnum
import com.example.gossipwars.logic.proposals.StrategyProposal
import com.example.gossipwars.ui.actions.ProposalListAdapter.ProposalViewHolder
import com.google.android.material.switchmaterial.SwitchMaterial
import com.richpath.RichPath
import com.richpath.RichPathView
import com.richpathanimator.RichPathAnimator
import java.util.*

class ProposalListAdapter(
    private val context: VoteProposalsDialog,
    private val proposalsList: ArrayList<ProposalVoteContent>
) : RecyclerView.Adapter<ProposalViewHolder>() {
    private val mInflater: LayoutInflater = LayoutInflater.from(context.activity)

    inner class ProposalViewHolder(
        itemView: View,
        adapter: ProposalListAdapter
    ) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val proposalTitle: TextView = itemView.findViewById(R.id.proposalVoteTitle)
        val proposalMap: RichPathView = itemView.findViewById(R.id.proposalVoteMap)
        val proposalContent: TextView = itemView.findViewById(R.id.proposalVoteContent)
        val propVote: SwitchMaterial = itemView.findViewById(R.id.proposalSwitch)
        val mAdapter: ProposalListAdapter = adapter
        override fun onClick(view: View) {
            val mPosition = layoutPosition
            proposalsList[mPosition]
            mAdapter.notifyDataSetChanged()
        }

        init {
            itemView.setOnClickListener(this)
            propVote.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
                context.sendVote(
                    proposalsList[layoutPosition].proposal,
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
        val mCurrent: ProposalVoteContent = proposalsList[position]
        holder.proposalTitle.text = mCurrent.title
        holder.proposalContent.text = mCurrent.content

        holder.proposalMap.setVectorDrawable(Snapshots.getDrawableForRegion(-1))
        if (mCurrent.targetRegion != null) {
            val region: RichPath? =
                holder.proposalMap.findRichPathByName(GameHelper.findRegionById(mCurrent.targetRegion)?.name)
            region?.let {
                RichPathAnimator.animate(it)
                    .interpolator(AccelerateDecelerateInterpolator())
                    .fillColor(it.fillColor, R.color.colorPrimaryDark)
                    .start()
            }
        }
        if (!mCurrent.membersRegions.isNullOrEmpty()) {
            for (idx in 0 until mCurrent.membersRegions.size) {
                val region: RichPath? =
                    holder.proposalMap.findRichPathByName(GameHelper.findRegionById(mCurrent.membersRegions[idx])?.name)
                region?.let {
                    RichPathAnimator.animate(it)
                        .interpolator(AccelerateDecelerateInterpolator())
                        .fillColor(it.fillColor, GameHelper.getColorByPlayerIdx(idx))
                        .start()
                }
            }
        }

        if (mCurrent.proposal.initiator.id == myId) {
            holder.propVote.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int {
        return proposalsList.size
    }

}