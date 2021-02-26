package com.example.gossipwars;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.recyclerview.widget.RecyclerView;

import com.example.gossipwars.communication.messages.gameInit.RoomInfoDTO;
import com.google.android.material.chip.Chip;

import java.util.LinkedList;

public class RoomListAdapter extends
        RecyclerView.Adapter<RoomListAdapter.RoomViewHolder> {
    private MainActivity context;
    private final LinkedList<RoomInfoDTO> roomInfoList;
    private final LayoutInflater mInflater;
    private final String username;
    private final int MAX_LEN = 12;

    class RoomViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final Chip nameChip;
        public final Chip creatorChip;
        public final Chip lengthChip;
        public final Chip playersChip;
        public final Button roomButton;
        final RoomListAdapter mAdapter;

        public RoomViewHolder(View itemView, RoomListAdapter adapter) {
            super(itemView);
            nameChip = itemView.findViewById(R.id.nameChip);
            creatorChip = itemView.findViewById(R.id.creatorChip);
            lengthChip = itemView.findViewById(R.id.lengthChip);
            playersChip = itemView.findViewById(R.id.playersChip);
            roomButton = itemView.findViewById(R.id.roomButton);
            this.mAdapter = adapter;
            itemView.setOnClickListener(this);
            roomButton.setOnClickListener(view -> {
                context.joinGame(roomInfoList.get(getLayoutPosition()));
            });
        }

        @Override
        public void onClick(View view) {
            // Get the position of the item that was clicked.
            int mPosition = getLayoutPosition();
            // Use that to access the affected item in mWordList.
            RoomInfoDTO element = roomInfoList.get(mPosition);
            mAdapter.notifyDataSetChanged();
        }
    }

    public RoomListAdapter(MainActivity context, LinkedList<RoomInfoDTO> roomInfoList, String username) {
        this.context = context;
        mInflater = LayoutInflater.from(context);
        this.roomInfoList = roomInfoList;
        this.username = username;
    }

    @Override
    public RoomViewHolder onCreateViewHolder(ViewGroup parent,
                                             int viewType) {
        // Inflate an item view.
        View mItemView = mInflater.inflate(
                R.layout.rooms_items, parent, false);
        return new RoomViewHolder(mItemView, this);
    }

    @Override
    public void onBindViewHolder(RoomViewHolder holder, int position) {
        // Retrieve the data for that position.
        RoomInfoDTO mCurrent = roomInfoList.get(position);
        // Add the data to the view holder.
        if (mCurrent.getRoomName().length() <= MAX_LEN) {
            holder.nameChip.setText(mCurrent.getRoomName());
        } else {
            holder.nameChip.setText(mCurrent.getRoomName().subSequence(0, MAX_LEN) + "...");
        }
        if (mCurrent.getUsername().length() <= MAX_LEN) {
            holder.creatorChip.setText(mCurrent.getUsername());
        } else {
            holder.creatorChip.setText(mCurrent.getUsername().subSequence(0, MAX_LEN) + "...");
        }
        holder.lengthChip.setText(mCurrent.getRoundLength() + "s");
        holder.playersChip.setText(mCurrent.getCrtPlayersNr() +
                        " / " + mCurrent.getMaxPlayers());
        if (mCurrent.getUsername().equals(username)) {
            holder.roomButton.setText("Start");
        } else {
            holder.roomButton.setText("Join");
        }
    }

    @Override
    public int getItemCount() {
        return roomInfoList.size();
    }
}
