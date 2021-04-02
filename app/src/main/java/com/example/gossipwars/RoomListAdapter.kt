package com.example.gossipwars

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import com.example.gossipwars.communication.messages.gameInit.RoomInfoDTO
import com.google.android.material.chip.Chip
import devit951.github.magictip.tip.AutoCloseMagicTip
import java.util.*


class RoomListAdapter(
    private val context: MainActivity,
    private val roomInfoList: LinkedList<RoomInfoDTO>,
    private val username: String
) :
    RecyclerView.Adapter<RoomListAdapter.RoomViewHolder>() {
    private val mInflater: LayoutInflater = LayoutInflater.from(context)

    inner class RoomViewHolder(
        itemView: View,
        adapter: RoomListAdapter
    ) :
        RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val nameChip: Chip = itemView.findViewById(R.id.nameChip)
        val creatorChip: Chip = itemView.findViewById(R.id.creatorChip)
        val lengthChip: Chip = itemView.findViewById(R.id.lengthChip)
        val playersChip: Chip = itemView.findViewById(R.id.playersChip)
        val roomButton: Button = itemView.findViewById(R.id.roomButton)
        val mAdapter: RoomListAdapter = adapter

        override fun onClick(view: View) {
            // Get the position of the item that was clicked.
            val mPosition = layoutPosition
            // Use that to access the affected item in mWordList.
            val element = roomInfoList[mPosition]
            mAdapter.notifyDataSetChanged()
        }

        init {
            itemView.setOnClickListener(this)
            roomButton.setOnClickListener {
                context.joinGame(
                    roomInfoList[layoutPosition]
                )
            }
            nameChip.setOnClickListener {
                // has ellipsis
                if (nameChip.layout.getEllipsisCount(0) > 0) {
                    AutoCloseMagicTip(nameChip, 1000)
                        .settings {
                            this.text = nameChip.text
                        }.show()
                }
            }
            creatorChip.setOnClickListener {
                if (creatorChip.layout.getEllipsisCount(0) > 0) {
                    AutoCloseMagicTip(creatorChip, 1000)
                        .settings {
                            this.text = creatorChip.text
                        }.show()
                }
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RoomViewHolder {
        // Inflate an item view.
        val mItemView: View = mInflater.inflate(
            R.layout.rooms_items, parent, false
        )
        return RoomViewHolder(mItemView, this)
    }

    override fun onBindViewHolder(holder: RoomViewHolder, position: Int) {
        // Retrieve the data for that position.
        val mCurrent = roomInfoList[position]
        // Add the data to the view holder.
        holder.nameChip.text = mCurrent.roomName
        holder.creatorChip.text = mCurrent.username
        holder.lengthChip.text =
            context.getString(R.string.round_seconds, mCurrent.roundLength.toString())
        holder.playersChip.text = context.getString(
            R.string.room_players,
            mCurrent.crtPlayersNr.toString(),
            mCurrent.maxPlayers.toString()
        )
        if (mCurrent.username == username) {
            holder.roomButton.text = context.getString(R.string.room_start)
        } else {
            holder.roomButton.text = context.getString(R.string.room_join)
        }
    }

    override fun getItemCount(): Int {
        return roomInfoList.size
    }

}