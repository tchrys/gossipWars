package com.example.gossipwars.ui.messenger;

import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gossipwars.R;
import com.example.gossipwars.logic.entities.ChatMessage;
import com.example.gossipwars.logic.entities.Game;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.UUID;

public class MessagesListAdapter extends RecyclerView.Adapter<MessagesListAdapter.MessageViewHolder> {
    private MessengerActivity context;
    private final ArrayList<ChatMessage> messagesList;
    private final LayoutInflater mInflater;
    private final String myUsername;

    class MessageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final TextView messageAuthor;
        public final TextView messageContent;
        public final TextView messageDate;
        public final CardView cardView;
        public final LinearLayout messageLinearLayout;
        final MessagesListAdapter mAdapter;

        public MessageViewHolder(View itemView, MessagesListAdapter adapter) {
            super(itemView);
            messageAuthor = itemView.findViewById(R.id.messageAuthor);
            messageContent = itemView.findViewById(R.id.messageContent);
            messageDate = itemView.findViewById(R.id.messageDate);
            cardView = itemView.findViewById(R.id.messageCardView);
            messageLinearLayout = itemView.findViewById(R.id.messageLinearLayout);
            this.mAdapter = adapter;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            // get the position of the view that was clicked
        }
    }

    public MessagesListAdapter(MessengerActivity context, ArrayList<ChatMessage> messagesList,
                                        String myUsername) {
        this.context = context;
        mInflater = LayoutInflater.from(context);
        this.messagesList = messagesList;
        this.myUsername = myUsername;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // inflate an item view
        Log.d("DBG", "on create view holder");
        View mItemView = mInflater.inflate(R.layout.messages_items, parent, false);
        return new MessageViewHolder(mItemView, this);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        ChatMessage chatMessage = messagesList.get(position);
        UUID senderId = chatMessage.getSender().getId();
        holder.messageAuthor.setText(chatMessage.getSender().getUsername());
        holder.messageContent.setText(chatMessage.getContent());
        String msgDate = String.valueOf(chatMessage.getMessageDate().get(Calendar.HOUR_OF_DAY));
        msgDate += ":";
        String messageMinutes = String.valueOf(chatMessage.getMessageDate().get(Calendar.MINUTE));
        msgDate += messageMinutes.length() == 1 ? "0" + messageMinutes : messageMinutes;
        holder.messageDate.setText(msgDate);

        int displayWidth = context.getResources().getDisplayMetrics().widthPixels;
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) holder.cardView.getLayoutParams();
        holder.cardView.setMinimumWidth(displayWidth / 6);

//        if (chatMessage.getMessageDate().get(Calendar.MILLISECOND) % 2 == 1) {
//            Log.d("DBG", chatMessage.getContent() + " e pe random");
//            senderId = UUID.randomUUID();
//        }

        if (senderId.equals(Game.myId)) {
            Log.d("DBG", chatMessage.getContent() + " e verde");
            holder.cardView.setCardBackgroundColor(context.getResources().getColor(R.color.light_green));
            holder.messageLinearLayout.setGravity(Gravity.END);
            layoutParams.setMarginStart(displayWidth / 4);
            layoutParams.setMarginEnd(0);
            holder.cardView.requestLayout();
            holder.messageAuthor.setVisibility(View.GONE);
        } else {
            holder.cardView.setCardBackgroundColor(context.getResources().getColor(R.color.light_blue));
            holder.messageLinearLayout.setGravity(Gravity.START);
            layoutParams.setMarginStart(0);
            layoutParams.setMarginEnd(displayWidth / 4);
            if (position > 0 && messagesList.get(position - 1).getSender().getId().equals(senderId)) {
                holder.messageAuthor.setVisibility(View.GONE);
                layoutParams.setMarginStart(50);
            }
            holder.cardView.requestLayout();
        }
    }

    @Override
    public int getItemCount() {
        return messagesList.size();
    }

}
