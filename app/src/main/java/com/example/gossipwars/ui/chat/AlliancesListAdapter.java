package com.example.gossipwars.ui.chat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gossipwars.R;
import com.example.gossipwars.logic.entities.Alliance;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AlliancesListAdapter extends
        RecyclerView.Adapter<AlliancesListAdapter.AllianceViewHolder> {
    private ChatFragment context;
    private final ArrayList<Alliance> alliancesList;
    private final LayoutInflater mInflater;

    class AllianceViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final TextView allianceNameTextView;
        public final TextView allianceMembersTextView;
        final AlliancesListAdapter mAdapter;

        public AllianceViewHolder(View itemView, AlliancesListAdapter adapter) {
            super(itemView);
            allianceNameTextView = itemView.findViewById(R.id.allianceNameTextView);
            allianceMembersTextView = itemView.findViewById(R.id.allianceMembersTextView);
            this.mAdapter = adapter;
            itemView.setOnClickListener(this);
            itemView.setOnClickListener(view ->
                    context.enterAllianceChat(alliancesList.get(getLayoutPosition())));
        }

        @Override
        public void onClick(View view) {
            // get the position of the view that was clicked
            int mPosition = getLayoutPosition();
            Alliance element = alliancesList.get(mPosition);
            mAdapter.notifyDataSetChanged();
        }
    }

    public AlliancesListAdapter(ChatFragment context, ArrayList<Alliance> alliancesList) {
        this.context = context;
        mInflater = LayoutInflater.from(context.getActivity());
        this.alliancesList = alliancesList;
    }

    @NonNull
    @Override
    public AllianceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // inflate an item view
        View mItemView = mInflater.inflate(R.layout.alliance_items, parent, false);
        return new AllianceViewHolder(mItemView, this);
    }

    @Override
    public void onBindViewHolder(@NonNull AllianceViewHolder holder, int position) {
        // retrieve the data for that position
        Alliance mCurrent = alliancesList.get(position);
        holder.allianceNameTextView.setText(mCurrent.name);
        String allianceMembersString = "";
        for (int i = 0; i < mCurrent.getPlayersInvolved().size(); ++i) {
            allianceMembersString += mCurrent.getPlayersInvolved().get(i).getUsername();
            if (i != mCurrent.getPlayersInvolved().size() - 1) {
                allianceMembersString += ",";
            }
        }
        holder.allianceMembersTextView.setText(allianceMembersString);
    }

    @Override
    public int getItemCount() {
        return alliancesList.size();
    }
}
