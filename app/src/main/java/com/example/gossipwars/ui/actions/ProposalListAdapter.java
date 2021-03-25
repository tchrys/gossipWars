package com.example.gossipwars.ui.actions;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.gossipwars.R;
import com.example.gossipwars.logic.entities.Game;
import com.example.gossipwars.logic.entities.GameHelper;
import com.example.gossipwars.logic.entities.Region;
import com.example.gossipwars.logic.proposals.Proposal;
import com.example.gossipwars.logic.proposals.ProposalEnum;
import com.example.gossipwars.logic.proposals.StrategyProposal;
import com.google.android.material.chip.Chip;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.ArrayList;
import java.util.Arrays;

public class ProposalListAdapter extends
        RecyclerView.Adapter<ProposalListAdapter.ProposalViewHolder> {
    private VoteProposalsDialog context;
    private final ArrayList<Proposal> proposalsList;
    private final LayoutInflater mInflater;

    class ProposalViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final TextView propStatement;
        public final SwitchMaterial propVote;
        final ProposalListAdapter mAdapter;

        public ProposalViewHolder(View itemView, ProposalListAdapter adapter) {
            super(itemView);
            propStatement = itemView.findViewById(R.id.proposalStatement);
            propVote = itemView.findViewById(R.id.proposalSwitch);
            this.mAdapter = adapter;
            itemView.setOnClickListener(this);
            propVote.setOnCheckedChangeListener((compoundButton, isChecked) -> {
                context.sendVote(proposalsList.get(getLayoutPosition()), isChecked);
            });
        }

        @Override
        public void onClick(View view) {
            int mPosition = getLayoutPosition();
            Proposal element = proposalsList.get(mPosition);
            mAdapter.notifyDataSetChanged();
        }
    }

    public ProposalListAdapter(VoteProposalsDialog context, ArrayList<Proposal> proposalsList) {
        this.context = context;
        mInflater = LayoutInflater.from(context.getActivity());
        this.proposalsList = proposalsList;
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
        Proposal mCurrent = proposalsList.get(position);
        String regionName = "";
        if (Arrays.asList(ProposalEnum.DEFEND, ProposalEnum.ATTACK)
                .contains(mCurrent.getProposalEnum())) {
            regionName = GameHelper.INSTANCE.findRegionById(((StrategyProposal) mCurrent)
                                                    .getTargetRegion()).getName();
            regionName = "in region" + GameHelper.INSTANCE.camelCaseToSpaced(regionName);
        }
        holder.propStatement.setText(
                String.format("%s requests member's vote from alliance %s for %s %s %s",
                mCurrent.getInitiator().getUsername(),
                mCurrent.getAlliance().getName(),
                mCurrent.getProposalEnum().toString().toLowerCase() + "ing",
                mCurrent.getTarget().getUsername(),
                regionName
                ));
        if (mCurrent.getInitiator().getId().equals(Game.INSTANCE.getMyId())) {
            holder.propVote.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return proposalsList.size();
    }
}
