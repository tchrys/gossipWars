package com.example.gossipwars.ui.actions;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.recyclerview.widget.RecyclerView;

import com.example.gossipwars.R;
import com.example.gossipwars.logic.proposals.Proposal;
import com.example.gossipwars.logic.proposals.ProposalEnum;
import com.example.gossipwars.logic.proposals.StrategyProposal;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;

public class ProposalListAdapter extends
        RecyclerView.Adapter<ProposalListAdapter.ProposalViewHolder> {
    private VoteProposalsDialog context;
    private final ArrayList<Proposal> proposalsList;
    private final LayoutInflater mInflater;
    private final String username;

    class ProposalViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final Chip actionChip;
        public final Chip initiatorChip;
        public final Button yesButton;
        public final Button noButton;
        final ProposalListAdapter mAdapter;

        public ProposalViewHolder(View itemView, ProposalListAdapter adapter) {
            super(itemView);
            actionChip = itemView.findViewById(R.id.actionChip);
            initiatorChip = itemView.findViewById(R.id.initiatorChip);
            yesButton = itemView.findViewById(R.id.voteYesButton);
            noButton = itemView.findViewById(R.id.voteNoButton);
            this.mAdapter = adapter;
            itemView.setOnClickListener(this);
            yesButton.setOnClickListener(view -> {
                context.sendVote(proposalsList.get(getLayoutPosition()), true);
            });
            noButton.setOnClickListener(view -> {
                context.sendVote(proposalsList.get(getLayoutPosition()), false);
            });
        }

        @Override
        public void onClick(View view) {
            // Get the position of the item that was clicked.
            int mPosition = getLayoutPosition();
            // Use that to access the affected item in mWordList.
            Proposal element = proposalsList.get(mPosition);
            mAdapter.notifyDataSetChanged();
        }
    }

    public ProposalListAdapter(VoteProposalsDialog context, ArrayList<Proposal> proposalsList, String username) {
        this.context = context;
        mInflater = LayoutInflater.from(context.getActivity());
        this.proposalsList = proposalsList;
        this.username = username;
    }

    @Override
    public ProposalViewHolder onCreateViewHolder(ViewGroup parent,
                                             int viewType) {
        // Inflate an item view.
        View mItemView = mInflater.inflate(
                R.layout.proposal_items, parent, false);
        return new ProposalViewHolder(mItemView, this);
    }

    @Override
    public void onBindViewHolder(ProposalViewHolder holder, int position) {
        // Retrieve the data for that position.
        Proposal mCurrent = proposalsList.get(position);
        // Add the data to the view holder.
        holder.initiatorChip.setText("From " + mCurrent.getInitiator().getUsername()
            + ", alliance " + mCurrent.getAlliance().getName());
        if (mCurrent.getProposalEnum().equals(ProposalEnum.JOIN)) {
            holder.actionChip.setText("Add " + mCurrent.getTarget().getUsername());
        } else if (mCurrent.getProposalEnum().equals(ProposalEnum.KICK)) {
            holder.actionChip.setText("Kick " + mCurrent.getTarget().getUsername());
        } else if (mCurrent instanceof StrategyProposal) {
            String action = mCurrent.getProposalEnum().equals(ProposalEnum.ATTACK) ? "Attack " : "Help ";
            holder.actionChip.setText(action + mCurrent.getTarget().getUsername()
            + " in region " + ((StrategyProposal) mCurrent).getTargetRegion());
        }
        if (username.equals(mCurrent.getInitiator().getUsername())) {
            holder.yesButton.setVisibility(View.INVISIBLE);
            holder.noButton.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return proposalsList.size();
    }
}
