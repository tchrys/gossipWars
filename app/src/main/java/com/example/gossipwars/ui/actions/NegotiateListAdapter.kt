package com.example.gossipwars.ui.actions

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.CompoundButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gossipwars.R
import com.example.gossipwars.logic.entities.GameHelper
import com.example.gossipwars.logic.entities.NegotiateVoteContent
import com.example.gossipwars.logic.entities.Snapshots
import com.example.gossipwars.ui.actions.NegotiateListAdapter.NegotiateViewHolder
import com.google.android.material.switchmaterial.SwitchMaterial
import com.richpath.RichPath
import com.richpath.RichPathView
import com.richpathanimator.RichPathAnimator
import java.util.*

class NegotiateListAdapter(
    private val context: VoteNegotiateDialog,
    private val requestsList: ArrayList<NegotiateVoteContent>
) : RecyclerView.Adapter<NegotiateViewHolder>() {
    private val mInflater: LayoutInflater = LayoutInflater.from(context.activity)

    inner class NegotiateViewHolder(
        itemView: View,
        adapter: NegotiateListAdapter
    ) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val proposalTitle: TextView = itemView.findViewById(R.id.proposalVoteTitle)
        val proposalMap: RichPathView = itemView.findViewById(R.id.proposalVoteMap)
        val proposalContent: TextView = itemView.findViewById(R.id.proposalVoteContent)
        private val propVote: SwitchMaterial = itemView.findViewById(R.id.proposalSwitch)
        val mAdapter: NegotiateListAdapter = adapter
        override fun onClick(view: View) {
            val mPosition = layoutPosition
            val content = requestsList[mPosition]
            mAdapter.notifyDataSetChanged()
        }

        init {
            itemView.setOnClickListener(this)
            propVote.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
                context.sendVote(
                    requestsList[layoutPosition].armyRequest,
                    isChecked
                )
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NegotiateViewHolder {
        // Inflate an item view.
        val mItemView = mInflater.inflate(R.layout.proposal_items, parent, false)
        return NegotiateViewHolder(mItemView, this)
    }

    override fun onBindViewHolder(holder: NegotiateViewHolder, position: Int) {
        val mCurrent = requestsList[position]
        holder.proposalTitle.text = mCurrent.title
        holder.proposalContent.text = mCurrent.content

        if (mCurrent.targetRegion == null) {
            holder.proposalMap.visibility = View.GONE
        } else {
            holder.proposalMap.setVectorDrawable(Snapshots.getDrawableForRegion(mCurrent.targetRegion))
            if (!mCurrent.membersRegions.isNullOrEmpty()) {
                for (regionId in mCurrent.membersRegions) {
                    val region: RichPath? =
                        holder.proposalMap.findRichPathByName(GameHelper.findRegionById(regionId)?.name)
                    region?.let {
                        RichPathAnimator.animate(it)
                            .interpolator(AccelerateDecelerateInterpolator())
                            .fillColor(it.fillColor, R.color.grey)
                            .start()
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return requestsList.size
    }

}