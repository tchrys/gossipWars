package com.example.gossipwars.ui.actions;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.gossipwars.R;
import com.example.gossipwars.logic.proposals.ArmyRequest;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.ArrayList;

public class NegotiateListAdapter extends
        RecyclerView.Adapter<NegotiateListAdapter.NegotiateViewHolder> {
    private VoteNegotiateDialog context;
    private final ArrayList<ArmyRequest> requestsList;
    private final LayoutInflater mInflater;

    class NegotiateViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final TextView propStatement;
        public final SwitchMaterial propVote;
        final NegotiateListAdapter mAdapter;

        public NegotiateViewHolder(View itemView, NegotiateListAdapter adapter) {
            super(itemView);
            propStatement = itemView.findViewById(R.id.proposalStatement);
            propVote = itemView.findViewById(R.id.proposalSwitch);
            this.mAdapter = adapter;
            itemView.setOnClickListener(this);
            propVote.setOnCheckedChangeListener((compoundButton, isChecked) -> {
                context.sendVote(requestsList.get(getLayoutPosition()), isChecked);
            });
        }

        @Override
        public void onClick(View view) {
            int mPosition = getLayoutPosition();
            ArmyRequest element = requestsList.get(mPosition);
            mAdapter.notifyDataSetChanged();
        }
    }

    public NegotiateListAdapter(VoteNegotiateDialog context, ArrayList<ArmyRequest> requestsList) {
        this.context = context;
        mInflater = LayoutInflater.from(context.getActivity());
        this.requestsList = requestsList;
    }

    @Override
    public NegotiateViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate an item view.
        View mItemView = mInflater.inflate(R.layout.proposal_items, parent, false);
        return new NegotiateViewHolder(mItemView, this);
    }

    @Override
    public void onBindViewHolder(NegotiateViewHolder holder, int position) {
        ArmyRequest mCurrent = requestsList.get(position);
        holder.propStatement.setText(
                String.format("%s asks you if he can raise his army %s with %s",
                        mCurrent.getInitiator().getUsername(),
                        mCurrent.getArmyOption().toString().toLowerCase(),
                        mCurrent.getIncrease()
                ));
    }

    @Override
    public int getItemCount() {
        return requestsList.size();
    }
}

