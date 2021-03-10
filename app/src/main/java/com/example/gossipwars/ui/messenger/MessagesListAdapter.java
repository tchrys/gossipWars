package com.example.gossipwars.ui.messenger;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gossipwars.R;
import com.example.gossipwars.logic.entities.ChatMessage;

import java.util.ArrayList;

public class MessagesListAdapter extends RecyclerView.Adapter<MessagesListAdapter.MessageViewHolder> {
    private MessengerActivity context;
    private final ArrayList<ChatMessage> messagesList;
    private final LayoutInflater mInflater;

    class MessageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final TextView messageAuthor;
        public final TextView messageContent;
        public final TextView messageDate;
        final MessagesListAdapter mAdapter;

        public MessageViewHolder(View itemView, MessagesListAdapter adapter) {
            super(itemView);
            messageAuthor = itemView.findViewById(R.id.messageAuthor);
            messageContent = itemView.findViewById(R.id.messageContent);
            messageDate = itemView.findViewById(R.id.messageDate);
            this.mAdapter = adapter;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            // get the position of the view that was clicked
            int mPosition = getLayoutPosition();
            ChatMessage elem = messagesList.get(mPosition);
            mAdapter.notifyDataSetChanged();
        }
    }

    public MessagesListAdapter(MessengerActivity context, ArrayList<ChatMessage> messagesList) {
        this.context = context;
        mInflater = LayoutInflater.from(context);
        this.messagesList = messagesList;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // inflate an item view
        View mItemView = mInflater.inflate(R.layout.messages_items, parent, false);
        return new MessageViewHolder(mItemView, this);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        // retrieve the data for that position
        ChatMessage chatMessage = messagesList.get(position);
        holder.messageAuthor.setText(chatMessage.getSender().getUsername());
        holder.messageContent.setText(chatMessage.getContent());
    }

    @Override
    public int getItemCount() {
        return messagesList.size();
    }

}
