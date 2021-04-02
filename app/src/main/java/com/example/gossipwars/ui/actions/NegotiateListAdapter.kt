package com.example.gossipwars.ui.actions

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gossipwars.R
import com.example.gossipwars.logic.proposals.ArmyRequest
import com.example.gossipwars.ui.actions.NegotiateListAdapter.NegotiateViewHolder
import com.google.android.material.switchmaterial.SwitchMaterial
import java.util.*

class NegotiateListAdapter(
    private val context: VoteNegotiateDialog,
    private val requestsList: ArrayList<ArmyRequest>
) : RecyclerView.Adapter<NegotiateViewHolder>() {
    private val mInflater: LayoutInflater = LayoutInflater.from(context.activity)

    inner class NegotiateViewHolder(
        itemView: View,
        adapter: NegotiateListAdapter
    ) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val propStatement: TextView = itemView.findViewById(R.id.proposalStatement)
        private val propVote: SwitchMaterial = itemView.findViewById(R.id.proposalSwitch)
        val mAdapter: NegotiateListAdapter = adapter
        override fun onClick(view: View) {
            val mPosition = layoutPosition
            val (initiator, approver, armyOption, increase, id) = requestsList[mPosition]
            mAdapter.notifyDataSetChanged()
        }

        init {
            itemView.setOnClickListener(this)
            propVote.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
                context.sendVote(
                    requestsList[layoutPosition],
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
        val (initiator, _, armyOption, increase) = requestsList[position]
        holder.propStatement.text = String.format(
            "%s asks you if he can raise his army %s with %s",
            initiator.username,
            armyOption.toString().toLowerCase(Locale.ROOT),
            increase
        )
    }

    override fun getItemCount(): Int {
        return requestsList.size
    }

}