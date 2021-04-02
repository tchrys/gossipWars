package com.example.gossipwars.ui.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gossipwars.R
import com.example.gossipwars.logic.entities.Alliance
import com.example.gossipwars.ui.chat.AlliancesListAdapter.AllianceViewHolder
import java.util.*

class AlliancesListAdapter(
    private val context: ChatFragment,
    private val alliancesList: ArrayList<Alliance>
) : RecyclerView.Adapter<AllianceViewHolder>() {
    private val mInflater: LayoutInflater = LayoutInflater.from(context.activity)

    inner class AllianceViewHolder(
        itemView: View,
        val mAdapter: AlliancesListAdapter
    ) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val allianceNameTextView: TextView = itemView.findViewById(R.id.allianceNameTextView)
        val allianceMembersTextView: TextView = itemView.findViewById(R.id.allianceMembersTextView)
        override fun onClick(view: View) {
            // get the position of the view that was clicked
            val mPosition = layoutPosition
            val (id) = alliancesList[mPosition]
            mAdapter.notifyDataSetChanged()
        }

        init {
            itemView.setOnClickListener(this)
            itemView.setOnClickListener {
                context.enterAllianceChat(
                    alliancesList[layoutPosition]
                )
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllianceViewHolder {
        // inflate an item view
        val mItemView = mInflater.inflate(R.layout.alliance_items, parent, false)
        return AllianceViewHolder(mItemView, this)
    }

    override fun onBindViewHolder(holder: AllianceViewHolder, position: Int) {
        // retrieve the data for that position
        val mCurrent = alliancesList[position]
        holder.allianceNameTextView.text = mCurrent.name
        var allianceMembersString = ""
        for (i in mCurrent.playersInvolved.indices) {
            allianceMembersString += mCurrent.playersInvolved[i].username
            if (i != mCurrent.playersInvolved.size - 1) {
                allianceMembersString += ","
            }
        }
        holder.allianceMembersTextView.text = allianceMembersString
    }

    override fun getItemCount(): Int {
        return alliancesList.size
    }

}